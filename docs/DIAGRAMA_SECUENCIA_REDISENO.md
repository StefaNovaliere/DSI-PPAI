# Rediseño del diagrama de secuencia (patrón State) vs. código

Se comparó, mensaje por mensaje, el **rediseño del diagrama de secuencia del CU 28 con el
patrón State** que hizo el grupo con el código. El código se adaptó al diagrama: los mismos
objetos, los mismos mensajes y en el mismo orden. Una ejecución real del flujo principal está
en [`traza_ejecucion_cu28.txt`](traza_ejecucion_cu28.txt).

Para que diagrama y código queden **iguales**, al diagrama le faltan algunos mensajes y
parámetros que el código necesita (columna "Corregir en el diagrama" y lista al final).

**Diagrama corregido:** [`diagrama-secuencia/cu28_state_corregido.pdf`](diagrama-secuencia/cu28_state_corregido.pdf)
(fuente editable en PlantUML: [`cu28_state_corregido.puml`](diagrama-secuencia/cu28_state_corregido.puml)).

Referencias: ✅ igual en el diagrama y en el código · ✏️ corregir en el diagrama.

## Mensaje por mensaje

| # | Emisor → receptor | Mensaje en el diagrama | Código | |
|---|---|---|---|---|
| 1 | EB → Pantalla | `tomarConfirmacion()` | [`PantallaGraficaRecepcionBolsin.tomarConfirmacion()`](../src/main/java/ppai/boundary/PantallaGraficaRecepcionBolsin.java#L428) (diálogo de confirmación) | ✅ |
| 2 | Pantalla → Gestor | `tomarConfirmacion()` | [`GestorRecepcionBolsin.tomarConfirmacion()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L248), con `confirma: boolean` (si es falso, flujo A6) | ✏️ parámetro |
| — | Gestor → Gestor | *(falta)* | [`GestorRecepcionBolsin.getFechaYHoraActual()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L311) | ✏️ agregar |
| 3 | Gestor → seleccionado:Bolsin | `recibir()` | [`Bolsin.recibir(fechaHora, responsable)`](../src/main/java/ppai/entidades/Bolsin.java#L160) | ✏️ parámetros |
| 4 | Bolsin → Enviado | `recibir(bolsin)` | [`Enviado.recibir(bolsin, fechaHora, responsable)`](../src/main/java/ppai/entidades/estadobolsin/Enviado.java#L26) | ✏️ parámetros |
| — | Enviado → Enviado, y loop → :CambioEstadoBolsin | *(falta)* | [`EstadoBolsin.buscarCambioEstadoActual(bolsin)`](../src/main/java/ppai/entidades/estadobolsin/EstadoBolsin.java#L55), que recorre el historial con [`*sosActual()`](../src/main/java/ppai/entidades/CambioEstadoBolsin.java#L48) | ✏️ agregar |
| 5 | Enviado → actual:CambioEstadoBolsin | `setFechaHoraFin(fechaHora)` | [`CambioEstadoBolsin.setFechaHoraFin()`](../src/main/java/ppai/entidades/CambioEstadoBolsin.java#L56) | ✅ |
| 6 | Enviado → Enviado | `crearEstado(): Recibido en CM destino` | [`Enviado.crearEstado(): EstadoBolsin`](../src/main/java/ppai/entidades/estadobolsin/Enviado.java#L35) | ✏️ tipo de retorno |
| 7 | Enviado → Recibido en CM destino | `new()` | [`new RecibidoEnCMDestino()`](../src/main/java/ppai/entidades/estadobolsin/RecibidoEnCMDestino.java#L10) | ✅ (nombre de la clase: `RecibidoEnCMDestino`) |
| 8 | Enviado → Enviado | `crearCambioEstado(): void` | [`EstadoBolsin.crearCambioEstado(estado, fechaHora, responsable): CambioEstadoBolsin`](../src/main/java/ppai/entidades/estadobolsin/EstadoBolsin.java#L65) | ✏️ parámetros y retorno |
| 9 | Enviado → nuevo:CambioEstadoBolsin | `new(fechaHoraInicio, estado, responsable)` | [`new CambioEstadoBolsin(fechaHoraInicio, estado, responsableCE)`](../src/main/java/ppai/entidades/CambioEstadoBolsin.java#L41) | ✅ |
| 10 | Enviado → seleccionado:Bolsin | `setEstado(estado: EstadoBolsin)` | [`Bolsin.setEstado()`](../src/main/java/ppai/entidades/Bolsin.java#L96) | ✅ |
| 11 | Enviado → seleccionado:Bolsin | `agregarCambioEstado(cambioEstado: CambioEstado)` | [`Bolsin.agregarCambioEstado(cambioEstado: CambioEstadoBolsin)`](../src/main/java/ppai/entidades/Bolsin.java#L91) | ✏️ tipo |
| 12 | Gestor → seleccionado:Bolsin | `recibirYAceptarRemito(fechaHora)` | [`Bolsin.recibirYAceptarRemito(fechaHora, responsable, opcion, documentacionIndicada)`](../src/main/java/ppai/entidades/Bolsin.java#L168) | ✏️ parámetros |
| 13 | Bolsin → :Remito (loop Remitos) | `recibirYAceptar(fechaHora)` | [`Remito.recibirYAceptar(fechaHora, responsable, opcion, documentacionIndicada)`](../src/main/java/ppai/entidades/Remito.java#L103) | ✏️ parámetros |
| 14 | Remito → EnBolsinEnviado | `recibirYAceptar(remito)` | [`EnBolsinEnviado.recibirYAceptar(remito, fechaHora, responsable, opcion, documentacionIndicada)`](../src/main/java/ppai/entidades/estadoremito/EnBolsinEnviado.java#L23) | ✏️ parámetros |
| 15 | EnBolsinEnviado → EnBolsinEnviado | `crearEstado(): EstadoRemito` | [`EnBolsinEnviado.crearEstado(remito, opcion, documentacionIndicada): EstadoRemito`](../src/main/java/ppai/entidades/estadoremito/EnBolsinEnviado.java#L37) | ✏️ parámetros (la guarda: parcial o no) |
| 16 | EnBolsinEnviado → RemitoRecibidoYAceptado | `new()` | [`new RemitoRecibidoYAceptado()`](../src/main/java/ppai/entidades/estadoremito/RemitoRecibidoYAceptado.java#L6) (o `RemitoRecibidoYAceptadoParcial` en las opciones 2 y 3) | ✅ |
| 17 | EnBolsinEnviado → :Remito | `setEstado(estado: EstadoRemito)` | [`Remito.setEstado()`](../src/main/java/ppai/entidades/Remito.java#L119) | ✅ |
| — | EnBolsinEnviado → :Remito | *(falta)* | [`Remito.getDetallesRemito()`](../src/main/java/ppai/entidades/Remito.java#L139): de ahí salen los detalles que recorre el estado | ✏️ agregar |
| 18 | EnBolsinEnviado → EnBolsinEnviado | `actualizarEstadoDocumentacion(detalleRemito[])` | [`EnBolsinEnviado.actualizarEstadoDocumentacion(detallesRemito, fechaHora, responsable, opcion, documentacionIndicada)`](../src/main/java/ppai/entidades/estadoremito/EnBolsinEnviado.java#L44) | ✏️ parámetros |
| 19 | EnBolsinEnviado → :DetalleRemito (loop Detalle Remito) | `recibirYAceptarDoc()` | [`DetalleRemito.recibirYAceptarDoc(fechaHora, responsable, opcion, documentacionIndicada)`](../src/main/java/ppai/entidades/DetalleRemito.java#L63) | ✏️ parámetros y línea de vida |
| 20 | DetalleRemito → :Documentación | `recibir()` | [`Documentacion.recibir(fechaHora, responsable, situacion)`](../src/main/java/ppai/entidades/Documentacion.java#L95) | ✏️ parámetros |
| 21 | Documentación → En bolsín enviado | `recibir(this)` | [`EnBolsinEnviado.recibir(documentacion, fechaHora, responsable, situacion)`](../src/main/java/ppai/entidades/estadodocumentacion/EnBolsinEnviado.java#L24) | ✏️ parámetros |
| — | En bolsín enviado → En bolsín enviado, y loop → :CambioEstadoDocumentacion | *(falta)* | [`EstadoDocumentacion.buscarCambioEstadoActual(documentacion)`](../src/main/java/ppai/entidades/estadodocumentacion/EstadoDocumentacion.java#L56), que recorre el historial con [`*sosActual()`](../src/main/java/ppai/entidades/CambioEstadoDocumentacion.java#L49) | ✏️ agregar |
| 22 | En bolsín enviado → actual:CambioEstadoDocumentacion | `setFechaHoraFin(fechaHora)` | [`CambioEstadoDocumentacion.setFechaHoraFin()`](../src/main/java/ppai/entidades/CambioEstadoDocumentacion.java#L53) | ✅ |
| 23 | En bolsín enviado → En bolsín enviado | `crearEstado(): RecibidaYAceptada` | [`EnBolsinEnviado.crearEstado(situacion): EstadoDocumentacion`](../src/main/java/ppai/entidades/estadodocumentacion/EnBolsinEnviado.java#L35) | ✏️ parámetro (la guarda) y tipo de retorno |
| 24 | En bolsín enviado → RecibidaYAceptada | `new()` | [`new RecibidaYAceptada()`](../src/main/java/ppai/entidades/estadodocumentacion/RecibidaYAceptada.java#L9) | ✅ |
| 25 | En bolsín enviado → En bolsín enviado | `crearCambioEstado(): void` | [`EstadoDocumentacion.crearCambioEstado(estado, fechaHora, responsable): CambioEstadoDocumentacion`](../src/main/java/ppai/entidades/estadodocumentacion/EstadoDocumentacion.java#L66) | ✏️ parámetros y retorno |
| 26 | En bolsín enviado → nuevo:CambioEstadoDocumentacion | `new(fechaHoraInicio, estado, responsableCE)` | [`new CambioEstadoDocumentacion(fechaHoraInicio, estado, responsableCE)`](../src/main/java/ppai/entidades/CambioEstadoDocumentacion.java#L41) | ✅ |
| 27 | En bolsín enviado → :Documentación | `setEstado(estado: EstadoDoc)` | [`Documentacion.setEstado(estado: EstadoDocumentacion)`](../src/main/java/ppai/entidades/Documentacion.java#L101) | ✏️ tipo |
| 28 | En bolsín enviado → :Documentación | `agregarCambioEstado(cambioEstado)` | [`Documentacion.agregarCambioEstado()`](../src/main/java/ppai/entidades/Documentacion.java#L105) | ✅ |

## Correcciones al diagrama

Ordenadas por importancia. Las primeras tres son errores: tal como está dibujado, el diagrama
no se puede implementar.

1. **Faltan `fechaHora` y `responsable`.** `Bolsin.recibir()` y `recibir(bolsin)` no reciben
   la fecha y hora, pero el estado las usa en `setFechaHoraFin(fechaHora)` y en el
   `new(fechaHoraInicio, …)`. El responsable (`Empleado`) aparece en el `new` del cambio de
   estado sin que nadie lo pase. Lo mismo en Remito y Documentación. Agregar:
   - el mensaje **`getFechaYHoraActual()`** del gestor, antes de `recibir()` (está en el
     análisis entre `tomarConfirmacion()` y la recepción);
   - los parámetros `fechaHora` y `responsable` en todos los mensajes del camino, como en la
     tabla.
2. **Falta buscar el cambio de estado actual.** El estado le manda `setFechaHoraFin()` a
   `actual:CambioEstadoBolsin` sin haberlo buscado. Agregar `buscarCambioEstadoActual()` y el
   loop `*sosActual()` sobre `:CambioEstadoBolsin` (que hay que agregar como línea de vida).
   En Documentación la línea de vida `:CambioEstadoDocumentacion` ya está dibujada, pero no
   recibe ningún mensaje: le falta el mismo loop.
3. **Las líneas de vida del loop de detalles.** Falta `:DetalleRemito`: los mensajes
   `recibirYAceptarDoc()` y `recibir()` hoy llegan y salen de la línea de vida de
   `RemitoRecibidoYAceptado`. Además hay una línea de vida `:Bolsin` (entre `:Remito` y
   `EnBolsinEnviado`) que no recibe ningún mensaje: seguramente era `:DetalleRemito`.
   También falta de dónde saca el estado los detalles: `getDetallesRemito()` al remito.
4. **Los parámetros de los flujos alternativos.** Las opciones 2 a 4 (A3 a A5) hacen que la
   documentación marcada pase a *No recibida*, *Recibida y rechazada* o *Para redirigir*, y
   que el remito quede parcial. Por eso `recibirYAceptarRemito`, `recibirYAceptar`,
   `actualizarEstadoDocumentacion` y `recibirYAceptarDoc` llevan `opcion` y
   `documentacionIndicada`, y `Documentacion.recibir` lleva la `situacion` (la guarda de la
   máquina de estados). En el flujo principal valen `opcion = CONTENIDO_IGUAL`, una lista
   vacía y `situacion = IGUAL_A_LA_REGISTRADA`.
5. **Tipos:** `crearEstado()` devuelve `EstadoBolsin`, `EstadoRemito` o
   `EstadoDocumentacion` (no "Recibido en CM destino"); `crearCambioEstado()` devuelve el
   cambio de estado que después se agrega, no `void`; `agregarCambioEstado(cambioEstado:
   CambioEstadoBolsin)`; `setEstado(estado: EstadoDocumentacion)`.
6. **Nombres de clase:** "Recibido en CM destino" es la clase `RecibidoEnCMDestino` y "En
   bolsín enviado" (la de Documentación) es `EnBolsinEnviado`. Hay dos `EnBolsinEnviado`: el
   del remito (paquete `estadoremito`) y el de la documentación (paquete
   `estadodocumentacion`). Conviene distinguirlos en el diagrama, por ejemplo
   `estado:EnBolsinEnviado` (Remito) y `estadoActual:EnBolsinEnviado` (Documentación).
7. **Notas "Los métodos anteriores/posteriores se mantienen como en el análisis".** Antes de
   `tomarConfirmacion()` se agregaron el filtro (A1, A2) y las opciones 2 a 4 (A3 a A5); y
   después cambian dos mensajes por la ERS: `buscarCorreoCM()` le pide el correo a la **CM
   origen** del bolsín (`obtenerCorreoCMOrigen()` → `ComisionMedica.getEmail()`) y
   `buscarInformacionDocumentacion()` usa `obtenerInformacionRemito()` para mostrar también el
   estado de cada remito. Ver la tabla "Flujos alternativos" de
   [`VERIFICACION_DIAGRAMA_SECUENCIA.md`](VERIFICACION_DIAGRAMA_SECUENCIA.md#lo-que-el-código-agrega-respecto-del-diagrama).
8. **Opcional:** la transacción (`iniciarTransaccion()`, `actualizar(bolsin)`,
   `confirmarTransaccion()`) envuelve los mensajes 3 a 28; se puede dibujar o mencionar en una
   nota.

## Cómo queda la secuencia (para redibujar)

```
EB → Pantalla: tomarConfirmacion()
Pantalla → Gestor: tomarConfirmacion(confirma)
Gestor → Gestor: getFechaYHoraActual(): DateTime
Gestor → seleccionado:Bolsin: recibir(fechaHora, responsable)
  Bolsin → estadoActual:Enviado: recibir(bolsin, fechaHora, responsable)
    Enviado → Enviado: buscarCambioEstadoActual(bolsin): CambioEstadoBolsin
      loop: Enviado → :CambioEstadoBolsin: *sosActual(): boolean
    Enviado → actual:CambioEstadoBolsin: setFechaHoraFin(fechaHora)
    Enviado → Enviado: crearEstado(): EstadoBolsin
      Enviado → :RecibidoEnCMDestino: new()
    Enviado → Enviado: crearCambioEstado(estado, fechaHora, responsable): CambioEstadoBolsin
      Enviado → nuevo:CambioEstadoBolsin: new(fechaHoraInicio, estado, responsable)
    Enviado → seleccionado:Bolsin: setEstado(estado)
    Enviado → seleccionado:Bolsin: agregarCambioEstado(cambioEstado)
Gestor → seleccionado:Bolsin: recibirYAceptarRemito(fechaHora, responsable, opcion, documentacionIndicada)
  loop Remitos: Bolsin → :Remito: recibirYAceptar(fechaHora, responsable, opcion, documentacionIndicada)
    Remito → estado:EnBolsinEnviado: recibirYAceptar(remito, fechaHora, responsable, opcion, documentacionIndicada)
      EnBolsinEnviado → EnBolsinEnviado: crearEstado(remito, opcion, documentacionIndicada): EstadoRemito
        EnBolsinEnviado → :RemitoRecibidoYAceptado: new()
      EnBolsinEnviado → :Remito: setEstado(estado)
      EnBolsinEnviado → :Remito: getDetallesRemito(): DetalleRemito[]
      EnBolsinEnviado → EnBolsinEnviado: actualizarEstadoDocumentacion(detallesRemito, fechaHora, responsable, opcion, documentacionIndicada)
        loop Detalles: EnBolsinEnviado → :DetalleRemito: recibirYAceptarDoc(fechaHora, responsable, opcion, documentacionIndicada)
          DetalleRemito → :Documentacion: recibir(fechaHora, responsable, situacion)
            Documentacion → estadoActual:EnBolsinEnviado: recibir(documentacion, fechaHora, responsable, situacion)
              EnBolsinEnviado → EnBolsinEnviado: buscarCambioEstadoActual(documentacion): CambioEstadoDocumentacion
                loop: EnBolsinEnviado → :CambioEstadoDocumentacion: *sosActual(): boolean
              EnBolsinEnviado → actual:CambioEstadoDocumentacion: setFechaHoraFin(fechaHora)
              EnBolsinEnviado → EnBolsinEnviado: crearEstado(situacion): EstadoDocumentacion
                EnBolsinEnviado → :RecibidaYAceptada: new()
              EnBolsinEnviado → EnBolsinEnviado: crearCambioEstado(estado, fechaHora, responsable): CambioEstadoDocumentacion
                EnBolsinEnviado → nuevo:CambioEstadoDocumentacion: new(fechaHoraInicio, estado, responsableCE)
              EnBolsinEnviado → :Documentacion: setEstado(estado)
              EnBolsinEnviado → :Documentacion: agregarCambioEstado(cambioEstado)
```

(`DetalleRemito.recibirYAceptarDoc()` calcula la `situacion` con `esDocumentacionIndicada()`:
si el usuario marcó esa documentación, es la de la opción elegida; si no,
`IGUAL_A_LA_REGISTRADA`. Se puede mostrar como un mensaje a sí mismo.)
