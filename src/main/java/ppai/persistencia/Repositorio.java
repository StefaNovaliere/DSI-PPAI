package ppai.persistencia;

import java.util.List;

import ppai.entidades.Bolsin;
import ppai.entidades.Empleado;
import ppai.entidades.Estado;
import ppai.entidades.Sesion;

/**
 * Esquema de persistencia: única puerta de acceso del gestor a los objetos
 * persistentes. El gestor no sabe si los objetos se materializan desde una
 * base relacional, un archivo o memoria; basta con cambiar la implementación.
 */
public interface Repositorio {

    Sesion getSesionActual();

    List<Empleado> getEmpleados();

    List<Bolsin> getBolsines();

    List<Estado> getEstados();
}
