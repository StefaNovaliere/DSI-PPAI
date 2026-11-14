package ppai.persistencia;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import ppai.entidades.Bolsin;
import ppai.entidades.ComisionMedica;
import ppai.entidades.Empleado;
import ppai.entidades.Sesion;
import ppai.entidades.TipoDocumento;
import ppai.entidades.Usuario;

/**
 * Esquema de persistencia sobre una base de datos SQLite, usando JPA
 * (implementación Hibernate) como framework de persistencia.
 *
 * <p>Hibernate hace el mapeo objeto-relacional definido con las anotaciones de
 * las entidades: materializa filas en objetos, desmaterializa objetos en filas
 * y administra las transacciones (commit/rollback).
 */
public class RepositorioJPA implements Repositorio, AutoCloseable {

    public static final String ARCHIVO_BD_POR_DEFECTO = "bolsines.db";
    /**
     * Versión del esquema de la base. Si el archivo es de una versión anterior
     * del programa (otras tablas), se vuelve a crear con los datos de prueba.
     */
    static final int VERSION_ESQUEMA = 2;
    private static final Logger LOG_HIBERNATE = Logger.getLogger("org.hibernate");

    private final EntityManagerFactory fabrica;
    private final EntityManager em;

    /**
     * @param archivoBD archivo de la base SQLite (se crea si no existe)
     * @param reiniciarDatos si es true, borra las tablas y vuelve a cargar los datos de prueba
     */
    public RepositorioJPA(String archivoBD, boolean reiniciarDatos) {
        LOG_HIBERNATE.setLevel(Level.SEVERE);
        boolean reiniciar = reiniciarDatos || esDeOtraVersion(archivoBD);
        Map<String, Object> propiedades = new HashMap<>();
        propiedades.put("jakarta.persistence.jdbc.url", "jdbc:sqlite:" + archivoBD);
        propiedades.put("hibernate.hbm2ddl.auto", reiniciar ? "create" : "update");
        fabrica = Persistence.createEntityManagerFactory("ppai-bolsines", propiedades);
        em = fabrica.createEntityManager();
        if (estaVacia()) {
            cargarDatosDePrueba();
        }
        guardarVersionEsquema();
    }

    private static boolean esDeOtraVersion(String archivoBD) {
        if (!Files.exists(Path.of(archivoBD))) {
            return false;
        }
        int version;
        try (Connection conexion = DriverManager.getConnection("jdbc:sqlite:" + archivoBD);
             Statement consulta = conexion.createStatement();
             ResultSet resultado = consulta.executeQuery("PRAGMA user_version")) {
            version = resultado.next() ? resultado.getInt(1) : 0;
        } catch (SQLException e) {
            version = 0;
        }
        if (version == VERSION_ESQUEMA) {
            return false;
        }
        System.out.println("La base " + archivoBD + " es de una version anterior del programa: "
                + "se vuelve a crear con los datos de prueba.");
        return true;
    }

    private void guardarVersionEsquema() {
        iniciarTransaccion();
        em.createNativeQuery("PRAGMA user_version = " + VERSION_ESQUEMA).executeUpdate();
        confirmarTransaccion();
    }

    private boolean estaVacia() {
        return em.createQuery("select count(b) from Bolsin b", Long.class).getSingleResult() == 0;
    }

    private void cargarDatosDePrueba() {
        DatosDePrueba datos = new DatosDePrueba();
        iniciarTransaccion();
        for (ComisionMedica cm : datos.getComisiones()) {
            em.persist(cm);
        }
        for (TipoDocumento tipo : datos.getTiposDocumento()) {
            em.persist(tipo);
        }
        for (Usuario usuario : datos.getUsuarios()) {
            em.persist(usuario);
        }
        for (Empleado empleado : datos.getEmpleados()) {
            em.persist(empleado);
        }
        em.persist(datos.getSesionActual());
        for (Bolsin bolsin : datos.getBolsines()) {
            em.persist(bolsin); // en cascada: remitos, detalles, documentación y cambios de estado
        }
        confirmarTransaccion();
        em.clear();
    }

    @Override
    public Sesion getSesionActual() {
        return em.createQuery("select s from Sesion s where s.fechaHoraFin is null order by s.id desc", Sesion.class)
                .setMaxResults(1)
                .getSingleResult();
    }

    @Override
    public List<Empleado> getEmpleados() {
        return em.createQuery("select e from Empleado e", Empleado.class).getResultList();
    }

    @Override
    public List<Bolsin> getBolsines() {
        return em.createQuery("select b from Bolsin b order by b.numeroBolsin", Bolsin.class).getResultList();
    }

    @Override
    public void iniciarTransaccion() {
        em.getTransaction().begin();
    }

    @Override
    public void actualizar(Bolsin bolsin) {
        em.merge(bolsin);
    }

    @Override
    public void confirmarTransaccion() {
        em.getTransaction().commit();
    }

    @Override
    public void deshacerTransaccion() {
        EntityTransaction transaccion = em.getTransaction();
        if (transaccion.isActive()) {
            transaccion.rollback();
        }
        // Se descartan los objetos modificados en memoria: se vuelven a materializar desde la base
        em.clear();
    }

    @Override
    public void close() {
        em.close();
        fabrica.close();
    }
}
