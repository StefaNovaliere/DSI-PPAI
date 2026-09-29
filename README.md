# PPAI 2026 – 3K2 – Grupo 10 – Implementación del CU 28 "Registrar Recepción de Bolsín"

Implementación en **Java 17**, bajo el **Paradigma Orientado a Objetos**, de la realización
de caso de uso de análisis de la entrega `PPAI2026_3K2_G10_E1_Analisis`, aplicando el
**patrón de diseño State** a la clase `Documentacion`.

## Cómo ejecutarlo

Requisitos: JDK 17 o superior y Maven.

```bash
mvn test                                  # corre las pruebas (JUnit 5)
mvn package                               # genera target/ppai-bolsines-g10-1.0.0.jar
java -jar target/ppai-bolsines-g10-1.0.0.jar
```

También se puede abrir la carpeta como proyecto Maven en IntelliJ / NetBeans / Eclipse y
ejecutar `ppai.App`.

> En la consola de Windows, si los acentos se ven mal, ejecutar antes `chcp 65001`.

El usuario logueado es `aperez` (CM Córdoba). Hay datos de prueba con dos bolsines
enviados a CM Córdoba (101 y 102), uno enviado a otra CM (103) y uno ya recibido (104);
estos dos últimos **no** deben aparecer en la lista.

## Arquitectura (capas)

| Capa | Paquete | Clases |
|------|---------|--------|
| Interfaz (boundary) | `ppai.boundary` | `PantallaRecepcionBolsin` |
| Control | `ppai.control` | `GestorRecepcionBolsin`, `GestorNotificacionCU29` |
| Dominio (entity) | `ppai.entidades` | `Bolsin`, `Remito`, `DetalleRemito`, `Documentacion`, `CambioEstadoBolsin`, `CambioEstadoDocumentacion`, `Estado`, `Empleado`, `Sesion`, `ComisionMedica`, `TipoDocumento` |
| Patrón State | `ppai.entidades.estadodocumentacion` | `EstadoDocumentacion` + 9 estados concretos |
| Persistencia | `ppai.persistencia` | `Repositorio` (esquema de persistencia), `RepositorioEnMemoria` |

Los nombres de clases y métodos respetan el diagrama de clases y el de secuencia del
análisis (`registrarNuevoRecBolsin`, `buscarCMUsuarioLogged`, `buscarBolsinesEnviadosCM`,
`buscarCMOrigenBolsines`, `tomarSeleccionBolsin`, `buscarInformacionRemito`,
`tomarSeleccionPrimerOpcion`, `tomarConfirmacion`, `getFechaYHoraActual`,
`buscarEstadoRecibidoEnCMDestino`, `recibirBolsin`, `recibirYAceptarRemito`,
`recibirYAceptar`, `actualizarEstadoDoc`, `recibir`, `buscarInformacionDocumentacion`,
`buscarCorreoCM`, `llamarCU29`, `finCU`, etc.).

El gestor accede a los objetos persistentes sólo a través de la interfaz `Repositorio`
(esquema de persistencia). `RepositorioEnMemoria` simula la base de datos; para usar
una base relacional alcanza con otra implementación de la interfaz, sin tocar el gestor
ni el dominio.

## Patrón State

Participantes (según la plantilla de la cátedra):

- **Contexto: `Documentacion`.** Conoce su `estadoActual` y **delega** cada evento de su
  máquina de estados (`remitar`, `cancelarRemito`, `agregarAlBolsin`, `enviar`, `recibir`,
  `registrar`, `darDeBaja`).
- **Estado abstracto: `EstadoDocumentacion`.** Declara un método por evento. Por defecto
  lanza `IllegalStateException` (transición inválida). Además tiene los pasos comunes de
  toda transición: cerrar el `CambioEstadoDocumentacion` actual, crear el nuevo y hacer
  `setEstado` en el contexto.
- **Estados concretos:** `Registrada`, `EnRemito`, `EnBolsinSaliente`, `EnBolsinEnviado`,
  `ParaRedirigir`, `NoRecibida`, `RecibidaYAceptada`, `RecibidaYRechazada`, `DeBaja`.
  Cada uno redefine sólo las transiciones que salen de él en la máquina de estados.

```mermaid
classDiagram
    class Documentacion {
        -estadoActual: EstadoDocumentacion
        +recibir(fechaHora, responsable)
        +remitar(...) / enviar(...) / ...
        +setEstado(estado)
        +agregarCambioEstado(ce)
    }
    class EstadoDocumentacion {
        <<abstract>>
        +recibir(doc, fechaHora, responsable)
        +remitar(...) / enviar(...) / ...
        #cambiarEstado(doc, proximo, fechaHora, responsable)
    }
    class CambioEstadoDocumentacion {
        -fechaHoraInicio
        -fechaHoraFin
        +sosActual()
        +setFechaHoraFin()
    }
    Documentacion --> EstadoDocumentacion : estadoActual
    Documentacion --> "1..*" CambioEstadoDocumentacion : cambioEstado
    CambioEstadoDocumentacion --> EstadoDocumentacion : estado
    EstadoDocumentacion <|-- Registrada
    EstadoDocumentacion <|-- EnRemito
    EstadoDocumentacion <|-- EnBolsinSaliente
    EstadoDocumentacion <|-- EnBolsinEnviado
    EstadoDocumentacion <|-- ParaRedirigir
    EstadoDocumentacion <|-- NoRecibida
    EstadoDocumentacion <|-- RecibidaYAceptada
    EstadoDocumentacion <|-- RecibidaYRechazada
    EstadoDocumentacion <|-- DeBaja
```

### Dinámica en el CU 28

```
GestorRecepcionBolsin.tomarConfirmacion()
 └─ Bolsin.recibirYAceptarRemito()
     └─ Remito.recibirYAceptar()
         └─ DetalleRemito.actualizarEstadoDoc()
             └─ Documentacion.recibir()
                 └─ estadoActual.recibir(this, ...)      ← polimorfismo
                     EnBolsinEnviado.recibir(): cierra CE actual, new RecibidaYAceptada(),
                                                new CambioEstadoDocumentacion, doc.setEstado()
                     ParaRedirigir.recibir():   ídem → RecibidaYAceptada
```

### Cambios respecto del análisis

- Se eliminó `buscarEstadoRecibidaYAceptada()` del gestor y el loop sobre `Estado`
  preguntando `esAmbitoDocumentacion()` / `esRecibidaYAceptada()`: ahora el estado siguiente
  lo decide el estado concreto de cada documentación.
- `Estado` ya no tiene ámbito Documentación. `CambioEstadoDocumentacion` referencia a un
  `EstadoDocumentacion`.
- Una documentación en `ParaRedirigir` también se recibe correctamente, sin agregar
  ningún `if` (las dos transiciones del CU 28 de la máquina de estados).
- Un evento inválido para el estado actual, por ejemplo recibir una documentación
  `Registrada`, se rechaza con una excepción en lugar de dejar un estado inconsistente.

### Alcance

- `Bolsin` y `Remito` mantienen la entidad `Estado` con ámbito, tal como en el análisis,
  porque la entrega sólo modela la máquina de estados de Documentación. Si se modelan sus
  máquinas de estados, se les puede aplicar State de la misma forma.
- Las transiciones del CU 31 (Registrar Revisión de Documentación) no se implementan. La
  opción "Hay diferencias con lo registrado" informa que corresponde el CU 31.
- El CU 29 (notificación por correo) se simula imprimiendo el mail por consola.
