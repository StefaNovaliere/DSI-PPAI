# Máquinas de estado (borrador según la ERS v1.2)

Armadas a partir de la ERS *2026 DSI Bolsines ERS v1.2*: reglas de negocio (sección 2.2),
listado de casos de uso (3.3) y descripción del CU 28 y del CU 19 (4.1). Las transiciones en
**rojo** son las del CU 28, que es donde se aplica el patrón State en nuestro caso de uso.

Los `.puml` son el código fuente de cada diagrama: se pueden editar y volver a generar en
<https://www.plantuml.com/plantuml> o con la extensión PlantUML de VS Code o IntelliJ.

| Diagrama | Imagen | Fuente |
|---|---|---|
| Bolsín | [bolsin.png](bolsin.png) | [bolsin.puml](bolsin.puml) |
| Remito | [remito.png](remito.png) | [remito.puml](remito.puml) |
| Documentación (corregida) | [documentacion.png](documentacion.png) | [documentacion.puml](documentacion.puml) |

## De dónde sale cada transición

### Bolsín
| Transición | Fuente en la ERS |
|---|---|
| → Creado | CU 19 Generar bolsín, paso 11: "lo registra con el estado creado" |
| Creado → Creado | CU 20 Modificar bolsín: "mientras no esté cerrado, quitando o agregando remitos" |
| Creado → Cerrado | CU 39 Cerrar bolsín: "actualizar el estado del bolsín a cerrado" |
| Cerrado → Creado | CU 40 Reabrir bolsín: "actualizar el estado del bolsín a creado" |
| Creado / Cerrado → De baja | Regla "Manejo de un Bolsín": "mientras se encuentre en estado creado o cerrado puede eliminarse, actualizando su estado a De baja" (CU 21) |
| Cerrado → Enviado | Regla "Retiro de un Bolsín": "el estado del bolsín de cerrado a enviado" (CU 27) |
| Enviado → Recibido en CM destino | CU 28, paso 11 |

### Remito
| Transición | Fuente en la ERS |
|---|---|
| → Creado | Regla "Remito por CM" y CU 19 paso 5: remitos "en estado creado" (CU 15) |
| Creado → Creado | CU 16 Modificar remito: "mientras no se encuentre asociado a un bolsín" |
| Creado → Cancelado | Regla "Remito por CM": "un remito puede cancelarse mientras esté en estado creado" (CU 17) |
| Creado → En bolsín saliente | CU 19, paso 11: "actualiza el estado de los remitos a en bolsín saliente" (también CU 20 al agregar) |
| En bolsín saliente → Creado | Regla "Contenido de un Bolsín": "el remito que se quita del bolsín vuelve al estado creado" (CU 20 y CU 21) |
| En bolsín saliente → En bolsín enviado | Regla "Retiro de un Bolsín" (CU 27) |
| En bolsín enviado → Recibido y aceptado | CU 28, paso 11 |
| En bolsín enviado → Recibido y aceptado parcial | Regla "Recepción de un Bolsín": si no se recibe toda la documentación, "el remito queda como recibido y aceptado parcial" (CU 28, A3) |

### Documentación: diferencias con la máquina de estados de la Entrega 1
- **Las tres salidas de *En bolsín enviado* hacia *No recibida*, *Recibida y rechazada* y *Para
  redirigir* son del CU 28**: son las opciones 2, 3 y 4 del paso 7, flujos alternativos A3, A4
  y A5. En la Entrega 1 estaban como CU 31.
- **De *No recibida* y *Recibida y rechazada* a *Registrada* o *De baja* es el CU 31**
  (Registrar revisión de documentación: "permitir su reenvío a otra CM o su baja"). En la
  Entrega 1 estaban como CU 7 y CU 9.
- **De *Para redirigir* a *Recibida y aceptada* o *Recibida y rechazada*** lo hace el área de
  la CMC al verificar la documentación (regla "Documentación para redirigir"). No es el CU 28
  ni viaja en un bolsín, y la ERS no le asigna un número de caso de uso.
- **Faltaba *Registrada → De baja*** (CU 9 Eliminar documentación: "validando que no se
  encuentre en un remito ni incluida en un bolsín").
- *En bolsín saliente → En remito* también ocurre con el **CU 21 Eliminar bolsín** (regla
  "Movimiento de la Documentación"), no solo con el CU 20.

## Decisiones tomadas donde la ERS no es explícita (validar con el tutor)
1. **Remito *Cancelado*:** la ERS dice que el remito "puede cancelarse" o "anularse", pero no
   nombra el estado. Se llamó *Cancelado*.
2. **Remito en las opciones 3 y 4 del CU 28** (documentación que no corresponde, o para
   redirigir): la ERS no dice en qué estado queda el remito. Solo define *Recibido y aceptado
   parcial* para la opción 2.
3. **Modificar un bolsín cerrado:** el CU 20 dice "mientras no esté cerrado", pero la regla
   "Contenido de un Bolsín" dice que puede modificarse "ya sea que esté cerrado o no". Se
   resolvió con la regla del precinto: para modificar un bolsín cerrado primero se reabre
   (CU 40), y después se cierra con un precinto nuevo.
4. **Estado inicial del remito:** la ERS usa "creado", salvo el CU 19 A1, que dice "generado".
   Se usó *Creado*.
5. **Opción 2 del CU 28:** la documentación que llegó pasa a *Recibida y aceptada* y la que no
   llegó, a *No recibida*. La ERS solo lo dice explícitamente para la que no llegó.
