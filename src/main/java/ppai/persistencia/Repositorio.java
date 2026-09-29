package ppai.persistencia;

import java.util.List;

import ppai.entidades.Bolsin;
import ppai.entidades.Empleado;
import ppai.entidades.Estado;
import ppai.entidades.Sesion;

/**
 * Esquema de persistencia: única puerta de acceso del gestor a los objetos
 * persistentes. El gestor no sabe si los objetos se materializan desde una
 * base de datos relacional o desde memoria; alcanza con cambiar la
 * implementación ({@link RepositorioJPA} o {@link RepositorioEnMemoria}).
 */
public interface Repositorio {

    Sesion getSesionActual();

    List<Empleado> getEmpleados();

    List<Bolsin> getBolsines();

    List<Estado> getEstados();

    void iniciarTransaccion();

    /** Desmaterializa el bolsín y todo lo que contiene (remitos, documentación, cambios de estado). */
    void actualizar(Bolsin bolsin);

    /** Confirma (commit) los cambios de la transacción. */
    void confirmarTransaccion();

    /** Deshace (rollback) los cambios de la transacción. */
    void deshacerTransaccion();
}
