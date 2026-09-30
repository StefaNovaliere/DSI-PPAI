# Verificación: diagrama de secuencia del CU 28 vs. implementación

Se comparó, mensaje por mensaje, la **Vista de Interacción del CU 28 Registrar Recepción de
Bolsín** (entrega 1, análisis) con el código, incluidas las correcciones de la entrega 1.

La verificación tiene dos partes:

1. **Estática:** para cada mensaje del diagrama se ubicó el método que lo implementa (tabla).
2. **Dinámica:** se ejecutó el flujo principal (bolsín 101, "Confirmar recepción",
   confirmar) con el depurador de Java y se registró cada método de nuestras clases que se
   invocó y desde dónde. El resultado está en [`traza_ejecucion_cu28.txt`](traza_ejecucion_cu28.txt).
   Se excluyeron las llamadas internas de Hibernate al leer la base.

Referencias: ✅ implementado igual que en el diagrama · ✅ ajustado: se corrigió el código en
esta revisión para que siga al diagrama · 🔄 cambia por el rediseño con el patrón State ·
(n) ver notas.

## Mensaje por mensaje

| # | Emisor → receptor | Mensaje del diagrama | Implementación | Resultado |
|---|-------------------|----------------------|----------------|-----------|
| 1 | EB → Pantalla | `opcRegistrarRecBolsin()` | [`PantallaGraficaRecepcionBolsin.opcRegistrarRecBolsin()`](../src/main/java/ppai/boundary/PantallaGraficaRecepcionBolsin.java#L226) (opción del menú) | ✅ |
| 2 | Pantalla → Pantalla | `habilitarPantalla()` | [`PantallaGraficaRecepcionBolsin.habilitarPantalla()`](../src/main/java/ppai/boundary/PantallaGraficaRecepcionBolsin.java#L232) | ✅ |
| 3 | Pantalla → Gestor | `registrarNuevoRecBolsin()` | [`GestorRecepcionBolsin.registrarNuevoRecBolsin()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L47) | ✅ |
| 4 | Gestor → Gestor | `buscarCMUsuarioLogged()` | [`GestorRecepcionBolsin.buscarCMUsuarioLogged()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L68) | ✅ |
| 5 | Gestor → actual:Sesion | `getUsuario()` | [`Sesion.getUsuario()`](../src/main/java/ppai/entidades/Sesion.java#L39) (devuelve el `Usuario`) | ✅ corrección E1 |
| 6 | Gestor → :Empleado | `*esTuUsuario()` | [`Empleado.esTuUsuario()`](../src/main/java/ppai/entidades/Empleado.java#L47) | ✅ corrección E1 |
| 7 | Gestor → Logueado:Empleado | `getCM()` | [`Empleado.getCM()`](../src/main/java/ppai/entidades/Empleado.java#L52) | ✅ ajustado |
| 8 | Empleado → :ComisionMedica | `getNombre()` | [`ComisionMedica.getNombre()`](../src/main/java/ppai/entidades/ComisionMedica.java#L38) | ✅ |
| 9 | Gestor → Pantalla | `mostrarCM()` | [`PantallaGraficaRecepcionBolsin.mostrarCM()`](../src/main/java/ppai/boundary/PantallaGraficaRecepcionBolsin.java#L287) | ✅ |
| 10 | Gestor → Gestor | `buscarBolsinesEnviadosCM()` | [`GestorRecepcionBolsin.buscarBolsinesEnviadosCM()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L83) | ✅ |
| 11 | Gestor → :Bolsin (loop) | `sosEnviado()` | [`Bolsin.sosEnviado()`](../src/main/java/ppai/entidades/Bolsin.java#L76) | ✅ |
| 12 | Bolsin → :CambioEstadoBolsin | `*sosActual()` | [`CambioEstadoBolsin.sosActual()`](../src/main/java/ppai/entidades/CambioEstadoBolsin.java#L44) (desde [`Bolsin.buscarCambioEstadoActual()`](../src/main/java/ppai/entidades/Bolsin.java#L142)) | ✅ |
| 13 | Bolsin → actual:CambioEstadoBolsin | `sosEnviado()` | [`CambioEstadoBolsin.sosEnviado()`](../src/main/java/ppai/entidades/CambioEstadoBolsin.java#L48) | ✅ |
| 14 | Gestor → enviado:Bolsin | `obtenerCMDestino()` | [`Bolsin.obtenerCMDestino()`](../src/main/java/ppai/entidades/Bolsin.java#L82) | ✅ ajustado |
| 15 | Bolsin → :ComisionMedica | `getNombre()` | [`ComisionMedica.getNombre()`](../src/main/java/ppai/entidades/ComisionMedica.java#L38) | ✅ |
| 16 | Gestor → Logueado:Empleado | `esTuCM()` | [`Empleado.esTuCM()`](../src/main/java/ppai/entidades/Empleado.java#L57) | ✅ ajustado |
| 17 | Gestor → Gestor | `buscarCMOrigenbolsines()` | [`GestorRecepcionBolsin.buscarCMOrigenBolsines()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L92) | ✅ (1) |
| 18 | Gestor → enviadoUSLog:Bolsin (loop) | `obtenerCMOrigen()` | [`Bolsin.obtenerCMOrigen()`](../src/main/java/ppai/entidades/Bolsin.java#L87) | ✅ |
| 19 | Bolsin → :ComisionMedica | `getNombre()` | [`ComisionMedica.getNombre()`](../src/main/java/ppai/entidades/ComisionMedica.java#L38) | ✅ |
| 20 | Gestor → enviadoUSLog:Bolsin | `getNumeroPrecinto()` | [`Bolsin.getNumeroPrecinto()`](../src/main/java/ppai/entidades/Bolsin.java#L95) | ✅ |
| 21 | Gestor → Pantalla | `solicitarSelBolsin()` | [`PantallaGraficaRecepcionBolsin.solicitarSelBolsin()`](../src/main/java/ppai/boundary/PantallaGraficaRecepcionBolsin.java#L293) | ✅ |
| 22 | EB → Pantalla | `tomarSeleccionBolsin()` | [`PantallaGraficaRecepcionBolsin.tomarSeleccionBolsin()`](../src/main/java/ppai/boundary/PantallaGraficaRecepcionBolsin.java#L255) (clic en la lista) | ✅ |
| 23 | Pantalla → Gestor | `tomarSeleccionBolsin()` | [`GestorRecepcionBolsin.tomarSeleccionBolsin()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L101) | ✅ |
| 24 | Gestor → Gestor | `buscarInformacionRemito()` | [`GestorRecepcionBolsin.buscarInformacionRemito()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L124) | ✅ |
| 25 | Gestor → seleccionado:Bolsin | `obtenerInformacionRemito()` | [`Bolsin.obtenerInformacionRemito()`](../src/main/java/ppai/entidades/Bolsin.java#L100) | ✅ |
| 26 | Bolsin → :Remito (loop) | `obtenerNumero()` | [`Remito.obtenerNumero()`](../src/main/java/ppai/entidades/Remito.java#L65) | ✅ ajustado |
| 27 | Remito → :DetalleRemito (loop) | `obtenerDocumentacion()` | [`DetalleRemito.obtenerDocumentacion()`](../src/main/java/ppai/entidades/DetalleRemito.java#L42) (desde [`Remito.obtenerDatosRemito()`](../src/main/java/ppai/entidades/Remito.java#L70)) | ✅ (2) |
| 28 | DetalleRemito → :Documentación | `getAsunto()` | [`Documentacion.getAsunto()`](../src/main/java/ppai/entidades/Documentacion.java#L111) | ✅ (2) |
| 29 | DetalleRemito → :Documentación | `mostrarTipoDocumentacion()` | [`Documentacion.mostrarTipoDocumentacion()`](../src/main/java/ppai/entidades/Documentacion.java#L123) | ✅ (2) |
| 30 | Documentación → :TipoDocumentación | `getNombre()` | [`TipoDocumento.getNombre()`](../src/main/java/ppai/entidades/TipoDocumento.java#L32) | ✅ |
| 31 | Gestor → Pantalla | `mostrarNroRemito()` | [`PantallaGraficaRecepcionBolsin.mostrarNroRemito()`](../src/main/java/ppai/boundary/PantallaGraficaRecepcionBolsin.java#L318) | ✅ ajustado |
| 32 | Gestor → Pantalla | `mostrarDatosDocumentacion()` | [`PantallaGraficaRecepcionBolsin.mostrarDatosDocumentacion()`](../src/main/java/ppai/boundary/PantallaGraficaRecepcionBolsin.java#L323) | ✅ ajustado |
| 33 | Gestor → Pantalla | `solicitarSelOpcionesRecBolsin()` | [`PantallaGraficaRecepcionBolsin.solicitarSelOpcionesRecBolsin()`](../src/main/java/ppai/boundary/PantallaGraficaRecepcionBolsin.java#L334) | ✅ |
| 34 | EB → Pantalla | `tomarSeleccionPrimeraOpcion()` | [`PantallaGraficaRecepcionBolsin.tomarSeleccionPrimeraOpcion()`](../src/main/java/ppai/boundary/PantallaGraficaRecepcionBolsin.java#L268) (botón "Confirmar recepción") | ✅ renombrado (3) |
| 35 | Pantalla → Gestor | `tomarSeleccionPrimeraOpcion()` | [`GestorRecepcionBolsin.tomarSeleccionPrimeraOpcion()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L129) | ✅ renombrado (3) |
| 36 | Gestor → Pantalla | `solicitarConfirmacion()` | [`PantallaGraficaRecepcionBolsin.solicitarConfirmacion()`](../src/main/java/ppai/boundary/PantallaGraficaRecepcionBolsin.java#L341) | ✅ |
| 37 | EB → Pantalla | `tomarConfirmacion()` | [`PantallaGraficaRecepcionBolsin.tomarConfirmacion()`](../src/main/java/ppai/boundary/PantallaGraficaRecepcionBolsin.java#L278) (diálogo de confirmación) | ✅ |
| 38 | Pantalla → Gestor | `tomarConfirmacion()` | [`GestorRecepcionBolsin.tomarConfirmacion()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L144) | ✅ |
| 39 | Gestor → Gestor | `getFechaYHoraActual()` | [`GestorRecepcionBolsin.getFechaYHoraActual()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L183) | ✅ |
| 40 | Gestor → Gestor | `buscarEstadoRecibidoEnCMDestino()` | [`GestorRecepcionBolsin.buscarEstadoRecibidoEnCMDestino()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L187) | ✅ |
| 41 | Gestor → :Estado (loop) | `esAmbitoBolsin()` | [`Estado.esAmbitoBolsin()`](../src/main/java/ppai/entidades/Estado.java#L41) | ✅ |
| 42 | Gestor → :Estado (loop) | `esRecibidoEnCMDestino()` | [`Estado.esRecibidoEnCMDestino()`](../src/main/java/ppai/entidades/Estado.java#L53) | ✅ |
| 43 | Gestor → seleccionado:Bolsin | `recibirBolsin()` | [`Bolsin.recibirBolsin()`](../src/main/java/ppai/entidades/Bolsin.java#L109) | ✅ |
| 44 | Bolsin → actual:CambioEstadoBolsin | `setFechaHoraFin()` | [`CambioEstadoBolsin.setFechaHoraFin()`](../src/main/java/ppai/entidades/CambioEstadoBolsin.java#L52) | ✅ |
| 45 | Bolsin → Bolsin | `crearCEBolsin()` | [`Bolsin.crearCEBolsin()`](../src/main/java/ppai/entidades/Bolsin.java#L117) | ✅ |
| 46 | Bolsin → nuevo:CambioEstadoBolsin | `new()` | [`new CambioEstadoBolsin()`](../src/main/java/ppai/entidades/CambioEstadoBolsin.java#L37) | ✅ |
| 47 | Gestor → Gestor | `buscarEstadoRecibidoYAceptado()` | [`GestorRecepcionBolsin.buscarEstadoRecibidoYAceptado()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L196) | ✅ |
| 48 | Gestor → :Estado (loop) | `esAmbitoRemito()` | [`Estado.esAmbitoRemito()`](../src/main/java/ppai/entidades/Estado.java#L45) | ✅ |
| 49 | Gestor → :Estado (loop) | `esRecibidoYAceptado()` | [`Estado.esRecibidoYAceptado()`](../src/main/java/ppai/entidades/Estado.java#L57) | ✅ |
| 50 | Gestor → Gestor | `buscarEstadoRecibidaYAceptada()` | — (se eliminó) | 🔄 patrón State |
| 51 | Gestor → :Estado (loop) | `esAmbitoDocumentación()` | — (se eliminó) | 🔄 patrón State |
| 52 | Gestor → :Estado (loop) | `esRecibidaYAceptada()` | — (se eliminó) | 🔄 patrón State |
| 53 | Gestor → seleccionado:Bolsin | `recibirYAceptarRemito()` | [`Bolsin.recibirYAceptarRemito()`](../src/main/java/ppai/entidades/Bolsin.java#L122) | ✅ |
| 54 | Bolsin → :Remito (loop) | `recibirYAceptar()` | [`Remito.recibirYAceptar()`](../src/main/java/ppai/entidades/Remito.java#L82) | ✅ |
| 55 | Remito → :DetalleRemito (loop) | `actualizarEstadoDoc()` | [`DetalleRemito.actualizarEstadoDoc()`](../src/main/java/ppai/entidades/DetalleRemito.java#L51) | ✅ |
| 56 | DetalleRemito → :Documentación | `recibir()` | [`Documentacion.recibir()`](../src/main/java/ppai/entidades/Documentacion.java#L91) → delega en [`EnBolsinEnviado.recibir()`](../src/main/java/ppai/entidades/estadodocumentacion/EnBolsinEnviado.java#L21) / [`ParaRedirigir.recibir()`](../src/main/java/ppai/entidades/estadodocumentacion/ParaRedirigir.java#L20) | 🔄 patrón State |
| 57 | Documentación → :CambioEstadoDocumentacion | `*sosActual()` | [`CambioEstadoDocumentacion.sosActual()`](../src/main/java/ppai/entidades/CambioEstadoDocumentacion.java#L49) (desde [`EstadoDocumentacion.buscarCambioEstadoActual()`](../src/main/java/ppai/entidades/estadodocumentacion/EstadoDocumentacion.java#L58)) | 🔄 lo hace el estado |
| 58 | Documentación → actual:CambioEstadoDocumentacion | `setFechaHoraFin()` | [`CambioEstadoDocumentacion.setFechaHoraFin()`](../src/main/java/ppai/entidades/CambioEstadoDocumentacion.java#L53) (desde [`EstadoDocumentacion.cambiarEstado()`](../src/main/java/ppai/entidades/estadodocumentacion/EstadoDocumentacion.java#L48)) | 🔄 lo hace el estado |
| 59 | Documentación → RecibidaYAceptada:CambioEstadoDocumentacion | `new()` | [`new CambioEstadoDocumentacion()`](../src/main/java/ppai/entidades/CambioEstadoDocumentacion.java#L41) (desde [`EstadoDocumentacion.crearCambioEstado()`](../src/main/java/ppai/entidades/estadodocumentacion/EstadoDocumentacion.java#L67)) | 🔄 lo hace el estado |
| 60 | Gestor → Gestor | `buscarInformacionDocumentacion()` | [`GestorRecepcionBolsin.buscarInformacionDocumentacion()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L205) | ✅ |
| 61 | Gestor → seleccionado:Bolsin | `obtenerInformacionDocumentacion()` | [`Bolsin.obtenerInformacionDocumentacion()`](../src/main/java/ppai/entidades/Bolsin.java#L129) | ✅ |
| 62 | Bolsin → :Remito (loop) | `obtenerDatosRemito()` | [`Remito.obtenerDatosRemito()`](../src/main/java/ppai/entidades/Remito.java#L70) | ✅ ajustado |
| 63 | Remito → :DetalleRemito (loop) | `obtenerDocumentacion()` | [`DetalleRemito.obtenerDocumentacion()`](../src/main/java/ppai/entidades/DetalleRemito.java#L42) | ✅ |
| 64 | DetalleRemito → :Documentación | `getDatosDocumentacion()` | [`Documentacion.getDatosDocumentacion()`](../src/main/java/ppai/entidades/Documentacion.java#L131) | ✅ |
| 65 | Gestor → Gestor | `buscarCorreoCM()` | [`GestorRecepcionBolsin.buscarCorreoCM()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L209) | ✅ |
| 66 | Gestor → Logueado:Empleado | `getEmail()` | [`Empleado.getEmail()`](../src/main/java/ppai/entidades/Empleado.java#L61) | ✅ |
| 67 | Gestor → Gestor | `llamarCU29()` | [`GestorRecepcionBolsin.llamarCU29()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L214) | ✅ |
| 68 | Gestor → CU 29 | `«include»` | [`GestorNotificacionCU29.notificarRecepcion()`](../src/main/java/ppai/control/GestorNotificacionCU29.java#L22) | ✅ corrección E1 |
| 69 | Gestor → Gestor | `finCU()` | [`GestorRecepcionBolsin.finCU()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L218) | ✅ |

**Resultado:** los 69 mensajes del diagrama tienen soporte en el código, salvo los 3 que el
rediseño con el patrón State elimina a propósito (50 a 52).

## Correcciones de la entrega 1

- **Faltaba la clase `Usuario`** (mensajes 5 y 6): `Sesion.getUsuario()` devuelve el `Usuario`
  logueado y el gestor recorre los empleados con `esTuUsuario(usuario)`.
- **La llamada al otro CU** (mensajes 67 y 68): `llamarCU29()` le pasa el control al gestor
  del CU 29 (`GestorNotificacionCU29.notificarRecepcion(correo, nroBolsin, documentación)`).
  En el diagrama rediseñado conviene dibujar ese mensaje hacia el CU 29 con sus parámetros.

## Ajustes hechos en el código en esta revisión

| Mensajes | Antes | Ahora (como el diagrama) |
|----------|-------|--------------------------|
| 7, 8, 14–16 | `getCM()` y `obtenerCMDestino()` devolvían el objeto `ComisionMedica`; `esTuCM()` comparaba objetos | Llaman a `getNombre()` de la CM y devuelven el nombre; `esTuCM(nombreCM)` compara con ese nombre |
| 26, 62 | El bolsín pasaba por un método intermedio | `Bolsin` le pide a cada `Remito` `obtenerNumero()` y `obtenerDatosRemito()` |
| 31, 32 | `mostrarNroRemito()` y `mostrarDatosDocumentacion()` se llamaban una vez por remito | Se llaman una sola vez, después del loop, con todos los remitos |
| 34, 35 | `tomarSeleccionPrimerOpcion()` | `tomarSeleccionPrimeraOpcion()` |

## Cambios por el patrón State (a reflejar en el diagrama rediseñado)

- **Se eliminan los mensajes 50 a 52** (`buscarEstadoRecibidaYAceptada()` y su loop sobre
  `Estado`): el gestor ya no busca el estado siguiente de la documentación. `Estado` ya no
  tiene ámbito Documentación.
- **`Documentacion.recibir()` delega en su estado actual** (mensajes 56 a 59). La secuencia
  real, tomada de la traza de ejecución, es:

```
DetalleRemito.actualizarEstadoDoc()
  Documentacion.recibir()
    EnBolsinEnviado.recibir()          ← o ParaRedirigir.recibir(), según el estado actual
      new RecibidaYAceptada()
      EstadoDocumentacion.cambiarEstado()
        EstadoDocumentacion.buscarCambioEstadoActual()
          Documentacion.getCambiosEstado()
          CambioEstadoDocumentacion.sosActual()   (loop)
        CambioEstadoDocumentacion.setFechaHoraFin()
        EstadoDocumentacion.crearCambioEstado()
          new CambioEstadoDocumentacion()
        Documentacion.agregarCambioEstado()
        Documentacion.setEstado()
```

## Lo que el código agrega respecto del diagrama

Estos mensajes no están en el diagrama del análisis. Conviene incluirlos en el diagrama
rediseñado de la entrega 2 o, al menos, poder explicarlos en la defensa:

- **Persistencia:** el gestor obtiene los objetos con `Repositorio.getSesionActual()`,
  `getEmpleados()`, `getBolsines()` y `getEstados()`, y registra la recepción entre
  `iniciarTransaccion()`, `actualizar(bolsin)` y `confirmarTransaccion()` (o
  `deshacerTransaccion()` si falla).
- **`Bolsin.getNumeroBolsin()`**, para mostrar el número de cada bolsín y ubicar el
  seleccionado.
- **`Remito.setEstado()`** dentro de `recibirYAceptar()`: el remito pasa a RecibidoYAceptado.
  Está en el diagrama de clases, pero no en el de secuencia.
- **`getDatosDocumentacion()` también informa el estado actual** (`EstadoDocumentacion.getNombre()`
  y `getDescripcion()`), que la pantalla muestra en la columna Estado.
- **Pantalla:** `mostrarRecepcionRegistrada()` (resultado de la recepción),
  `mostrarMensaje()` (flujos alternativos) y `finCU()`.
- **Flujo alternativo A3:** `tomarSeleccionSegundaOpcion()` ("Informar diferencias").

## Notas sobre el diagrama del análisis

1. **`buscarCMOrigenbolsines()`:** en el diagrama de secuencia está con "b" minúscula y en el
   de clases, `buscarCMOrigenBolsines()`. Se usó el del diagrama de clases.
2. **`obtenerDocumentacion()`** aparece dos veces con distinto contenido. En el primer loop
   (mensajes 27 a 29), el `DetalleRemito` le pide `getAsunto()` y `mostrarTipoDocumentacion()`
   a la documentación. En el segundo (mensajes 63 y 64), le pide `getDatosDocumentacion()`. En
   el código, `obtenerDocumentacion()` siempre llama a `getDatosDocumentacion()`, que a su vez
   invoca `getAsunto()` y `mostrarTipoDocumentacion()`. Sugerencia para el diagrama
   rediseñado: usar `getDatosDocumentacion()` en los dos loops. Además, en el primer loop el
   loop de detalles cuelga de la activación de `obtenerNumero()`; conviene que cuelgue de
   `obtenerDatosRemito()`.
3. **`tomarSeleccionPrimeraOpcion()`:** en el diagrama de secuencia figura así y en el de
   clases como `tomarSeleccionPrimerOpcion()`. El código usa el del diagrama de secuencia; hay
   que unificar el diagrama de clases.
