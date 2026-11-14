# Máquinas de estado

| Clase | Diagrama | De dónde sale |
|---|---|---|
| Documentación | [maquina_estados_documentacion.png](../maquina_estados_documentacion.png) | **La de la Entrega 1**, que la profesora dio por correcta. No se modificó. |
| Bolsín | [bolsin.png](bolsin.png) ([fuente](bolsin.puml)) | Armada con la ERS v1.2 (en la Entrega 1 no estaba) |
| Remito | [remito.png](remito.png) ([fuente](remito.puml)) | Armada con la ERS v1.2 (en la Entrega 1 no estaba) |

Las transiciones en **rojo** de Bolsín y Remito son las del CU 28. Los `.puml` son el código
fuente de cada diagrama: se pueden editar y volver a generar en
<https://www.plantuml.com/plantuml> o con la extensión PlantUML de VS Code o IntelliJ.

## Cómo las implementa el código (patrón State)

En las tres clases el código implementa **sólo las transiciones del CU 28**. Los eventos de
los otros casos de uso no se programan, y los estados concretos que no tienen transiciones del
CU 28 heredan el rechazo del estado abstracto.

| Clase (contexto) | Estado abstracto | Estados concretos que redefinen el evento del CU 28 |
|---|---|---|
| `Documentacion` | `EstadoDocumentacion` | `EnBolsinEnviado.recibir()` y `ParaRedirigir.recibir()` |
| `Bolsin` | `EstadoBolsin` | `Enviado.recibir()` |
| `Remito` | `EstadoRemito` | `EnBolsinEnviado.recibirYAceptar()` y `EnBolsinEnviado.recibirYAceptarParcial()` |

### Documentación: el evento `recibir()` con guardas

En la máquina de estados de la Entrega 1, todas las transiciones del CU 28 (y las del CU 31)
usan el mismo evento **`recibir()`**, y la **guarda** decide a qué estado pasa. En el código,
la guarda es la situación de la documentación (`SituacionDocumentacion`), que sale de la
opción de recepción que elige el usuario:

| Estado actual | Guarda en la máquina de estados | `SituacionDocumentacion` | Estado siguiente |
|---|---|---|---|
| EnBolsinEnviado | [todo remito y documentación igual a la registrada = True] | `IGUAL_A_LA_REGISTRADA` | Recibida&Aceptada |
| EnBolsinEnviado | [todo remito y documentación igual a la registrada = False] | `NO_RECIBIDA` | NoRecibida |
| EnBolsinEnviado | [Si la CMDestino no es la correcta] | `NO_CORRESPONDE_A_LA_CM_DESTINO` | Recibida&Rechazada |
| EnBolsinEnviado | [CMDestino = CMC] | `PARA_REDIRIGIR` | ParaRedirigir |
| ParaRedirigir | [documentación correcta = True] | `IGUAL_A_LA_REGISTRADA` | Recibida&Aceptada |
| ParaRedirigir | [documentación correcta = False] | `NO_CORRESPONDE_A_LA_CM_DESTINO` | Recibida&Rechazada |

Desde *ParaRedirigir* no hay transición si la documentación no llegó o se vuelve a
redirigir: en ese caso el estado rechaza la recepción y no se guarda ningún cambio.

**Para revisar con la profesora:** en la máquina de estados de la Entrega 1, las tres salidas
de *EnBolsinEnviado* hacia *NoRecibida*, *Recibida&Rechazada* y *ParaRedirigir* tienen la
etiqueta CU 31. En la ERS v1.2 esas situaciones son las opciones 2, 3 y 4 del paso 7 del
**CU 28** (flujos A3, A4 y A5), y así las implementa el programa. Las transiciones son las
mismas; lo único que cambiaría es el número de CU de la etiqueta.

## De dónde sale cada transición de Bolsín y Remito

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

## Decisiones tomadas donde la ERS no es explícita (validar con el tutor)
1. **Remito *Cancelado*:** la ERS dice que el remito "puede cancelarse" o "anularse", pero no
   nombra el estado. Se llamó *Cancelado*.
2. **Remito en las opciones 3 y 4 del CU 28:** la ERS sólo define *Recibido y aceptado
   parcial* para la opción 2. Se decidió que con la opción 3 (documentación que no
   corresponde) el remito también queda **parcial**, porque parte de su documentación se
   rechaza; y que con la opción 4 (redirigir) queda **Recibido y aceptado**, porque la CMC
   acepta la documentación y sólo la pasa a otra área. Está en `OpcionRecepcion`.
3. **Modificar un bolsín cerrado:** el CU 20 dice "mientras no esté cerrado", pero la regla
   "Contenido de un Bolsín" dice que puede modificarse "ya sea que esté cerrado o no". Se
   resolvió con la regla del precinto: para modificar un bolsín cerrado primero se reabre
   (CU 40), y después se cierra con un precinto nuevo.
4. **Estado inicial del remito:** la ERS usa "creado", salvo el CU 19 A1, que dice "generado".
   Se usó *Creado*.
5. **Opciones 2 a 4 del CU 28:** la documentación que el usuario no marca pasa a *Recibida y
   aceptada*. La ERS sólo dice qué pasa con la documentación marcada.
