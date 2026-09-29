package ppai.persistencia;

import java.util.List;

import ppai.entidades.Bolsin;
import ppai.entidades.Empleado;
import ppai.entidades.Estado;
import ppai.entidades.Sesion;

/**
 * Esquema de persistencia en memoria, sin base de datos. Se usa en las pruebas
 * unitarias para probar el CU sin depender de la base.
 */
public class RepositorioEnMemoria implements Repositorio {

    private final DatosDePrueba datos = new DatosDePrueba();

    @Override
    public Sesion getSesionActual() {
        return datos.getSesionActual();
    }

    @Override
    public List<Empleado> getEmpleados() {
        return datos.getEmpleados();
    }

    @Override
    public List<Bolsin> getBolsines() {
        return datos.getBolsines();
    }

    @Override
    public List<Estado> getEstados() {
        return datos.getEstados();
    }

    @Override
    public void iniciarTransaccion() {
        // En memoria no hay transacciones
    }

    @Override
    public void actualizar(Bolsin bolsin) {
        // Los objetos ya están en memoria
    }

    @Override
    public void confirmarTransaccion() {
        // En memoria no hay transacciones
    }

    @Override
    public void deshacerTransaccion() {
        // En memoria no hay transacciones
    }
}
