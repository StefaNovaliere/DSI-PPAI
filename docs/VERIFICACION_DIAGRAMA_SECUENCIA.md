# Verificación: diagrama de secuencia del CU 28 vs. implementación

Se comparó, mensaje por mensaje, la **Vista de Interacción del CU 28 Registrar Recepción de
Bolsín** (entrega 1, análisis) con el código, incluidas las correcciones de la entrega 1.

La verificación tiene dos partes:

1. **Estática:** para cada mensaje del diagrama se ubicó el método que lo implementa (tabla).
2. **Dinámica:** se ejecutó el flujo principal (bolsín 101, opción 1 "El contenido del bolsín
   es igual al registrado", confirmar) con el depurador de Java y se registró cada método de
   nuestras clases que se invocó y desde dónde. El resultado está en [`traza_ejecucion_cu28.txt`](traza_ejecucion_cu28.txt).
   Se excluyeron las llamadas internas de Hibernate al leer la base.

Referencias: ✅ implementado igual que en el diagrama · ✅ ajustado: se corrigió el código en
esta revisión para que siga al diagrama · 🔄 cambia por el rediseño con el patrón State ·
📄 cambia para cumplir la ERS · (n) ver notas.

## Mensaje por mensaje

| # | Emisor → receptor | Mensaje del diagrama | Implementación | Resultado |
|---|-------------------|----------------------|----------------|-----------|
| 1 | EB → Pantalla | `opcRegistrarRecBolsin()` | [`PantallaGraficaRecepcionBolsin.opcRegistrarRecBolsin()`](../src/main/java/ppai/boundary/PantallaGraficaRecepcionBolsin.java#L353) (opción del menú) | ✅ |
| 2 | Pantalla → Pantalla | `habilitarPantalla()` | [`PantallaGraficaRecepcionBolsin.habilitarPantalla()`](../src/main/java/ppai/boundary/PantallaGraficaRecepcionBolsin.java#L359) | ✅ |
| 3 | Pantalla → Gestor | `registrarNuevoRecBolsin()` | [`GestorRecepcionBolsin.registrarNuevoRecBolsin()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L49) | ✅ |
| 4 | Gestor → Gestor | `buscarCMUsuarioLogged()` | [`GestorRecepcionBolsin.buscarCMUsuarioLogged()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L71) | ✅ |
| 5 | Gestor → actual:Sesion | `getUsuario()` | [`Sesion.getUsuario()`](../src/main/java/ppai/entidades/Sesion.java#L39) (devuelve el `Usuario`) | ✅ corrección E1 |
| 6 | Gestor → :Empleado | `*esTuUsuario()` | [`Empleado.esTuUsuario()`](../src/main/java/ppai/entidades/Empleado.java#L47) | ✅ corrección E1 |
| 7 | Gestor → Logueado:Empleado | `getCM()` | [`Empleado.getCM()`](../src/main/java/ppai/entidades/Empleado.java#L52) | ✅ ajustado |
| 8 | Empleado → :ComisionMedica | `getNombre()` | [`ComisionMedica.getNombre()`](../src/main/java/ppai/entidades/ComisionMedica.java#L60) | ✅ |
| 9 | Gestor → Pantalla | `mostrarCM()` | [`PantallaGraficaRecepcionBolsin.mostrarCM()`](../src/main/java/ppai/boundary/PantallaGraficaRecepcionBolsin.java#L437) | ✅ |
| 10 | Gestor → Gestor | `buscarBolsinesEnviadosCM()` | [`GestorRecepcionBolsin.buscarBolsinesEnviadosCM()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L86) | ✅ |
| 11 | Gestor → :Bolsin (loop) | `sosEnviado()` | [`Bolsin.sosEnviado()`](../src/main/java/ppai/entidades/Bolsin.java#L103) | ✅ |
| 12 | Bolsin → :CambioEstadoBolsin | `*sosActual()` | [`CambioEstadoBolsin.sosActual()`](../src/main/java/ppai/entidades/CambioEstadoBolsin.java#L48) (desde [`Bolsin.buscarCambioEstadoActual()`](../src/main/java/ppai/entidades/Bolsin.java#L186)) | ✅ |
| 13 | Bolsin → actual:CambioEstadoBolsin | `sosEnviado()` | [`CambioEstadoBolsin.sosEnviado()`](../src/main/java/ppai/entidades/CambioEstadoBolsin.java#L52) | ✅ pregunta a su estado: `EstadoBolsin.esEnviado()` |
| 14 | Gestor → enviado:Bolsin | `obtenerCMDestino()` | [`Bolsin.obtenerCMDestino()`](../src/main/java/ppai/entidades/Bolsin.java#L109) | ✅ ajustado |
| 15 | Bolsin → :ComisionMedica | `getNombre()` | [`ComisionMedica.getNombre()`](../src/main/java/ppai/entidades/ComisionMedica.java#L60) | ✅ |
| 16 | Gestor → Logueado:Empleado | `esTuCM()` | [`Empleado.esTuCM()`](../src/main/java/ppai/entidades/Empleado.java#L57) | ✅ ajustado |
| 17 | Gestor → Gestor | `buscarCMOrigenbolsines()` | [`GestorRecepcionBolsin.buscarCMOrigenBolsines()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L96) | ✅ (1) |
| 18 | Gestor → enviadoUSLog:Bolsin (loop) | `obtenerCMOrigen()` | [`Bolsin.obtenerCMOrigen()`](../src/main/java/ppai/entidades/Bolsin.java#L114) | ✅ |
| 19 | Bolsin → :ComisionMedica | `getNombre()` | [`ComisionMedica.getNombre()`](../src/main/java/ppai/entidades/ComisionMedica.java#L60) | ✅ |
| 20 | Gestor → enviadoUSLog:Bolsin | `getNumeroPrecinto()` | [`Bolsin.getNumeroPrecinto()`](../src/main/java/ppai/entidades/Bolsin.java#L122) | ✅ |
| 21 | Gestor → Pantalla | `solicitarSelBolsin()` | [`PantallaGraficaRecepcionBolsin.solicitarSelBolsin()`](../src/main/java/ppai/boundary/PantallaGraficaRecepcionBolsin.java#L443) | ✅ |
| 22 | EB → Pantalla | `tomarSeleccionBolsin()` | [`PantallaGraficaRecepcionBolsin.tomarSeleccionBolsin()`](../src/main/java/ppai/boundary/PantallaGraficaRecepcionBolsin.java#L399) (clic en la lista) | ✅ |
| 23 | Pantalla → Gestor | `tomarSeleccionBolsin()` | [`GestorRecepcionBolsin.tomarSeleccionBolsin()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L145) | ✅ |
| 24 | Gestor → Gestor | `buscarInformacionRemito()` | [`GestorRecepcionBolsin.buscarInformacionRemito()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L169) | ✅ |
| 25 | Gestor → seleccionado:Bolsin | `obtenerInformacionRemito()` | [`Bolsin.obtenerInformacionRemito()`](../src/main/java/ppai/entidades/Bolsin.java#L152) | ✅ |
| 26 | Bolsin → :Remito (loop) | `obtenerNumero()` | [`Remito.obtenerNumero()`](../src/main/java/ppai/entidades/Remito.java#L71) | ✅ ajustado |
| 27 | Remito → :DetalleRemito (loop) | `obtenerDocumentacion()` | [`DetalleRemito.obtenerDocumentacion()`](../src/main/java/ppai/entidades/DetalleRemito.java#L43) (desde [`Remito.obtenerDatosRemito()`](../src/main/java/ppai/entidades/Remito.java#L76)) | ✅ (2) |
| 28 | DetalleRemito → :Documentación | `getAsunto()` | [`Documentacion.getAsunto()`](../src/main/java/ppai/entidades/Documentacion.java#L115) | ✅ (2) |
| 29 | DetalleRemito → :Documentación | `mostrarTipoDocumentacion()` | [`Documentacion.mostrarTipoDocumentacion()`](../src/main/java/ppai/entidades/Documentacion.java#L127) | ✅ (2) |
| 30 | Documentación → :TipoDocumentación | `getNombre()` | [`TipoDocumento.getNombre()`](../src/main/java/ppai/entidades/TipoDocumento.java#L32) | ✅ |
| 31 | Gestor → Pantalla | `mostrarNroRemito()` | [`PantallaGraficaRecepcionBolsin.mostrarNroRemito()`](../src/main/java/ppai/boundary/PantallaGraficaRecepcionBolsin.java#L468) | ✅ ajustado |
| 32 | Gestor → Pantalla | `mostrarDatosDocumentacion()` | [`PantallaGraficaRecepcionBolsin.mostrarDatosDocumentacion()`](../src/main/java/ppai/boundary/PantallaGraficaRecepcionBolsin.java#L473) | ✅ ajustado |
| 33 | Gestor → Pantalla | `solicitarSelOpcionesRecBolsin()` | [`PantallaGraficaRecepcionBolsin.solicitarSelOpcionesRecBolsin()`](../src/main/java/ppai/boundary/PantallaGraficaRecepcionBolsin.java#L485) | ✅ |
| 34 | EB → Pantalla | `tomarSeleccionPrimeraOpcion()` | [`PantallaGraficaRecepcionBolsin.tomarSeleccionOpcion()`](../src/main/java/ppai/boundary/PantallaGraficaRecepcionBolsin.java#L413) (opción 1 y botón "Registrar recepción") | ✅ renombrado (3) |
| 35 | Pantalla → Gestor | `tomarSeleccionPrimeraOpcion()` | [`GestorRecepcionBolsin.tomarSeleccionPrimeraOpcion()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L174) | ✅ renombrado (3) |
| 36 | Gestor → Pantalla | `solicitarConfirmacion()` | [`PantallaGraficaRecepcionBolsin.solicitarConfirmacion()`](../src/main/java/ppai/boundary/PantallaGraficaRecepcionBolsin.java#L498) | ✅ |
| 37 | EB → Pantalla | `tomarConfirmacion()` | [`PantallaGraficaRecepcionBolsin.tomarConfirmacion()`](../src/main/java/ppai/boundary/PantallaGraficaRecepcionBolsin.java#L428) (diálogo de confirmación) | ✅ |
| 38 | Pantalla → Gestor | `tomarConfirmacion()` | [`GestorRecepcionBolsin.tomarConfirmacion()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L248) | ✅ |
| 39 | Gestor → Gestor | `getFechaYHoraActual()` | [`GestorRecepcionBolsin.getFechaYHoraActual()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L311) | ✅ |
| 40 | Gestor → Gestor | `buscarEstadoRecibidoEnCMDestino()` | — (se eliminó) | 🔄 patrón State |
| 41 | Gestor → :Estado (loop) | `esAmbitoBolsin()` | — (se eliminó) | 🔄 patrón State |
| 42 | Gestor → :Estado (loop) | `esRecibidoEnCMDestino()` | — (se eliminó) | 🔄 patrón State |
| 43 | Gestor → seleccionado:Bolsin | `recibirBolsin()` | [`Bolsin.recibirBolsin()`](../src/main/java/ppai/entidades/Bolsin.java#L162) → delega en [`Enviado.recibir()`](../src/main/java/ppai/entidades/estadobolsin/Enviado.java#L25) | 🔄 patrón State |
| 44 | Bolsin → actual:CambioEstadoBolsin | `setFechaHoraFin()` | [`CambioEstadoBolsin.setFechaHoraFin()`](../src/main/java/ppai/entidades/CambioEstadoBolsin.java#L56) (desde [`EstadoBolsin.cambiarEstado()`](../src/main/java/ppai/entidades/estadobolsin/EstadoBolsin.java#L56)) | 🔄 lo hace el estado |
| 45 | Bolsin → Bolsin | `crearCEBolsin()` | [`EstadoBolsin.crearCambioEstado()`](../src/main/java/ppai/entidades/estadobolsin/EstadoBolsin.java#L75) | 🔄 lo hace el estado |
| 46 | Bolsin → nuevo:CambioEstadoBolsin | `new()` | [`new CambioEstadoBolsin()`](../src/main/java/ppai/entidades/CambioEstadoBolsin.java#L41) (desde [`EstadoBolsin.crearCambioEstado()`](../src/main/java/ppai/entidades/estadobolsin/EstadoBolsin.java#L75)) | 🔄 lo hace el estado |
| 47 | Gestor → Gestor | `buscarEstadoRecibidoYAceptado()` | — (se eliminó) | 🔄 patrón State |
| 48 | Gestor → :Estado (loop) | `esAmbitoRemito()` | — (se eliminó) | 🔄 patrón State |
| 49 | Gestor → :Estado (loop) | `esRecibidoYAceptado()` | — (se eliminó) | 🔄 patrón State |
| 50 | Gestor → Gestor | `buscarEstadoRecibidaYAceptada()` | — (se eliminó) | 🔄 patrón State |
| 51 | Gestor → :Estado (loop) | `esAmbitoDocumentación()` | — (se eliminó) | 🔄 patrón State |
| 52 | Gestor → :Estado (loop) | `esRecibidaYAceptada()` | — (se eliminó) | 🔄 patrón State |
| 53 | Gestor → seleccionado:Bolsin | `recibirYAceptarRemito()` | [`Bolsin.recibirYAceptarRemito()`](../src/main/java/ppai/entidades/Bolsin.java#L170) (recibe la opción elegida y la documentación indicada) | ✅ |
| 54 | Bolsin → :Remito (loop) | `recibirYAceptar()` | [`Remito.recibirYAceptar()`](../src/main/java/ppai/entidades/Remito.java#L105) → delega en [`EnBolsinEnviado.recibirYAceptar()`](../src/main/java/ppai/entidades/estadoremito/EnBolsinEnviado.java#L14) (o `recibirYAceptarParcial()`) | 🔄 patrón State |
| 55 | Remito → :DetalleRemito (loop) | `actualizarEstadoDoc()` | [`DetalleRemito.actualizarEstadoDoc()`](../src/main/java/ppai/entidades/DetalleRemito.java#L63) | ✅ |
| 56 | DetalleRemito → :Documentación | `recibir()` | [`Documentacion.recibir()`](../src/main/java/ppai/entidades/Documentacion.java#L95) → delega en [`EnBolsinEnviado.recibir()`](../src/main/java/ppai/entidades/estadodocumentacion/EnBolsinEnviado.java#L23) / [`ParaRedirigir.recibir()`](../src/main/java/ppai/entidades/estadodocumentacion/ParaRedirigir.java#L23) | 🔄 patrón State |
| 57 | Documentación → :CambioEstadoDocumentacion | `*sosActual()` | [`CambioEstadoDocumentacion.sosActual()`](../src/main/java/ppai/entidades/CambioEstadoDocumentacion.java#L49) (desde [`EstadoDocumentacion.buscarCambioEstadoActual()`](../src/main/java/ppai/entidades/estadodocumentacion/EstadoDocumentacion.java#L67)) | 🔄 lo hace el estado |
| 58 | Documentación → actual:CambioEstadoDocumentacion | `setFechaHoraFin()` | [`CambioEstadoDocumentacion.setFechaHoraFin()`](../src/main/java/ppai/entidades/CambioEstadoDocumentacion.java#L53) (desde [`EstadoDocumentacion.cambiarEstado()`](../src/main/java/ppai/entidades/estadodocumentacion/EstadoDocumentacion.java#L57)) | 🔄 lo hace el estado |
| 59 | Documentación → RecibidaYAceptada:CambioEstadoDocumentacion | `new()` | [`new CambioEstadoDocumentacion()`](../src/main/java/ppai/entidades/CambioEstadoDocumentacion.java#L41) (desde [`EstadoDocumentacion.crearCambioEstado()`](../src/main/java/ppai/entidades/estadodocumentacion/EstadoDocumentacion.java#L76)) | 🔄 lo hace el estado |
| 60 | Gestor → Gestor | `buscarInformacionDocumentacion()` | [`GestorRecepcionBolsin.buscarInformacionDocumentacion()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L316) | ✅ |
| 61 | Gestor → seleccionado:Bolsin | `obtenerInformacionDocumentacion()` | [`Bolsin.obtenerInformacionRemito()`](../src/main/java/ppai/entidades/Bolsin.java#L152) (el mismo del mensaje 25: devuelve también el estado de cada remito) | ✅ ajustado |
| 62 | Bolsin → :Remito (loop) | `obtenerDatosRemito()` | [`Remito.obtenerDatosRemito()`](../src/main/java/ppai/entidades/Remito.java#L76) | ✅ ajustado |
| 63 | Remito → :DetalleRemito (loop) | `obtenerDocumentacion()` | [`DetalleRemito.obtenerDocumentacion()`](../src/main/java/ppai/entidades/DetalleRemito.java#L43) | ✅ |
| 64 | DetalleRemito → :Documentación | `getDatosDocumentacion()` | [`Documentacion.getDatosDocumentacion()`](../src/main/java/ppai/entidades/Documentacion.java#L135) | ✅ |
| 65 | Gestor → Gestor | `buscarCorreoCM()` | [`GestorRecepcionBolsin.buscarCorreoCM()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L321) | ✅ |
| 66 | Gestor → Logueado:Empleado | `getEmail()` | [`Bolsin.obtenerCorreoCMOrigen()`](../src/main/java/ppai/entidades/Bolsin.java#L178) → [`ComisionMedica.getEmail()`](../src/main/java/ppai/entidades/ComisionMedica.java#L68): el correo es el de la **CM origen** | 📄 ERS |
| 67 | Gestor → Gestor | `llamarCU29()` | [`GestorRecepcionBolsin.llamarCU29()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L330) | ✅ |
| 68 | Gestor → CU 29 | `«include»` | [`GestorNotificacionCU29.notificarRecepcion()`](../src/main/java/ppai/control/GestorNotificacionCU29.java#L48) | ✅ corrección E1 |
| 69 | Gestor → Gestor | `finCU()` | [`GestorRecepcionBolsin.finCU()`](../src/main/java/ppai/control/GestorRecepcionBolsin.java#L338) | ✅ |

**Resultado:** los 69 mensajes del diagrama tienen soporte en el código, salvo los 9 que el
rediseño con el patrón State elimina a propósito (40 a 42 y 47 a 52: la búsqueda del estado
siguiente). El mensaje 66 cambia para cumplir la ERS.

## Correcciones de la entrega 1

- **Faltaba la clase `Usuario`** (mensajes 5 y 6): `Sesion.getUsuario()` devuelve el `Usuario`
  logueado y el gestor recorre los empleados con `esTuUsuario(usuario)`.
- **La llamada al otro CU** (mensajes 67 y 68): `llamarCU29()` le pasa el control al gestor
  del CU 29 (`GestorNotificacionCU29.notificarRecepcion(correoCMOrigen, datosRecepcion)`).
  En el diagrama rediseñado conviene dibujar ese mensaje hacia el CU 29 con sus parámetros.
- **El destinatario del correo** (mensaje 66): según la ERS, el CU 29 notifica "a la comisión
  médica origen". El gestor le pide el correo al bolsín seleccionado
  (`obtenerCorreoCMOrigen()`), que se lo pide a su CM origen, en lugar de pedírselo al
  empleado logueado.

## Ajustes hechos en el código en esta revisión

| Mensajes | Antes | Ahora (como el diagrama) |
|----------|-------|--------------------------|
| 7, 8, 14–16 | `getCM()` y `obtenerCMDestino()` devolvían el objeto `ComisionMedica`; `esTuCM()` comparaba objetos | Llaman a `getNombre()` de la CM y devuelven el nombre; `esTuCM(nombreCM)` compara con ese nombre |
| 26, 62 | El bolsín pasaba por un método intermedio | `Bolsin` le pide a cada `Remito` `obtenerNumero()` y `obtenerDatosRemito()` |
| 31, 32 | `mostrarNroRemito()` y `mostrarDatosDocumentacion()` se llamaban una vez por remito | Se llaman una sola vez, después del loop, con todos los remitos |
| 34, 35 | `tomarSeleccionPrimerOpcion()` | `tomarSeleccionPrimeraOpcion()` |

## Cambios por el patrón State (a reflejar en el diagrama rediseñado)

- **Se eliminan los mensajes 40 a 42 y 47 a 52** (`buscarEstadoRecibidoEnCMDestino()`,
  `buscarEstadoRecibidoYAceptado()`, `buscarEstadoRecibidaYAceptada()` y sus loops sobre
  `Estado`): el gestor ya no busca el estado siguiente de ningún objeto. La entidad `Estado`
  ya no existe; cada clase tiene su estado abstracto y sus estados concretos.
- **`Bolsin.recibirBolsin()` delega en su estado actual** (mensajes 43 a 46): `Enviado`
  cierra el cambio de estado actual y crea el nuevo, con `RecibidoEnCMDestino`.
- **`Remito.recibirYAceptar()` delega en su estado** (mensaje 54): `EnBolsinEnviado` lo pasa a
  `RecibidoYAceptado`, o a `RecibidoYAceptadoParcial` si el usuario marcó documentación de
  ese remito con la opción 2 o 3.
- **`Documentacion.recibir()` delega en su estado actual** (mensajes 56 a 59), con la
  situación de la documentación como guarda. La secuencia real, tomada de la traza de
  ejecución, es:

```
DetalleRemito.actualizarEstadoDoc()
  Documentacion.recibir()
    EnBolsinEnviado.recibir()          ← o ParaRedirigir.recibir(), según el estado actual
      new RecibidaYAceptada()           ← según la guarda: NoRecibida, RecibidaYRechazada, ParaRedirigir
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
  `getEmpleados()` y `getBolsines()`, y registra la recepción entre `iniciarTransaccion()`,
  `actualizar(bolsin)` y `confirmarTransaccion()` (o `deshacerTransaccion()` si un estado
  rechaza la transición).
- **`Bolsin.getNumeroBolsin()`**, para mostrar el número de cada bolsín y ubicar el
  seleccionado.
- **`getDatosDocumentacion()` también informa el estado actual** (`getNombre()` y
  `getDescripcion()` del objeto estado), que la pantalla muestra en la columna Estado; y
  `obtenerInformacionRemito()` informa el estado de cada remito.
- **CU 29 con envío real del correo:** `llamarCU29()` arma un `DatosRecepcion` con
  `getNumeroBolsin()`, `getNumeroPrecinto()`, `obtenerCMOrigen()`, la CM del usuario, la fecha y
  hora, `getNombreCompleto()` del empleado, la opción elegida y cada remito con su
  documentación, y se lo pasa a `GestorNotificacionCU29.notificarRecepcion()`. Ese gestor arma
  el correo y lo envía con `ServicioCorreo.enviar()` (SMTP con Jakarta Mail) en segundo plano.
- **Pantalla:** `mostrarRecepcionRegistrada()` (resultado de la recepción),
  `mostrarMensaje()` (flujos alternativos) y `finCU()`.
- **Flujos alternativos de la ERS** (no están en el diagrama de la Entrega 1):

| Flujo | Mensajes |
|---|---|
| Paso 4, A1 y A2: filtrar por precinto o CM origen | Pantalla → Gestor `tomarFiltroBolsines(precinto, cmOrigen)`; Gestor → :Bolsin (loop) `tenesPrecinto()` y `sosDeCMOrigen()` → :ComisionMedica `coincideNombre()`; Gestor → Pantalla `mostrarMensaje()` si no hay ninguno y `solicitarSelBolsin()` |
| A3, A4 y A5: opciones 2, 3 y 4 | Pantalla → Gestor `tomarSeleccionSegundaOpcion()`, `tomarSeleccionTerceraOpcion()` o `tomarSeleccionCuartaOpcion()` (esta última valida `Bolsin.esDestinoCMC()` → `ComisionMedica.esCentral()`); Gestor → Pantalla `solicitarSelDocumentacion()`; Pantalla → Gestor `tomarSeleccionDocumentacion(números)`; Gestor → :Bolsin `contenesDocumentacion()` (loop) → :Remito → :DetalleRemito `esDocumentacion()`; después sigue igual que el flujo principal desde `solicitarConfirmacion()` |
| A6: no confirma | Gestor → Pantalla `mostrarMensaje()` y `solicitarSelOpcionesRecBolsin()` (vuelve al paso 7) |
| Observación 1: cancelar | Pantalla → Gestor `cancelarCU()` |

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
