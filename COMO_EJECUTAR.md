# Cómo ejecutar y probar el CU 28 (paso a paso)

Guía pensada para alguien que nunca usó Java ni la terminal. Si seguís los pasos en orden,
no hace falta instalar nada más que **Java**.

> **Versión corta** (si ya tenés Java 17+): descargá el proyecto, entrá a la carpeta que
> tiene el archivo `ejecutar.bat` y hacé **doble clic en `ejecutar.bat`**.
> En Mac/Linux: `./ejecutar.sh`.

---

## Qué se necesita

| Qué | ¿Hay que instalarlo? |
|-----|----------------------|
| **Java 17 o superior** | **Sí**, una sola vez (Paso 1) |
| Maven | No: el proyecto trae `mvnw.cmd`, que lo descarga solo |
| Base de datos | No: es SQLite, un archivo (`bolsines.db`) que se crea solo |
| Git | No: se puede bajar el proyecto como ZIP |
| Internet | Sólo **la primera vez** que se compila (descarga dependencias), y para enviar el correo |
| Cuenta de correo | Para que el correo de notificación se envíe de verdad (Paso 3b). Si no se configura, el programa funciona igual y guarda el correo como archivo |

> ⚠️ **Para el día de la defensa:** compilen al menos una vez **antes** en la PC que van a
> usar, con Internet. Después funciona sin conexión.

---

## Paso 1 — Instalar Java (una sola vez)

### Windows

1. Abrí **PowerShell**: tecla Windows → escribí `powershell` → Enter.
2. Pegá este comando y apretá Enter:
   ```powershell
   winget install Microsoft.OpenJDK.21
   ```
   - Si pregunta `Do you agree to all the source agreements terms? [Y] Yes [N] No`, escribí
     `y` y Enter.
   - Si Windows pide permiso de administrador, aceptá.
   - Tiene que terminar con `Successfully installed`.
3. **Cerrá PowerShell y abrí uno nuevo.** Si no lo hacés, el comando `java` no se reconoce.
4. Verificá:
   ```powershell
   java -version
   ```
   Tiene que mostrar algo como `openjdk version "21.0.x"`.

Si `winget` no existe en tu PC, descargá el instalador `.msi` de Java 21 desde
<https://learn.microsoft.com/java/openjdk/download> (o <https://adoptium.net>), instalalo
con "Siguiente, Siguiente…" y seguí desde el punto 3.

### Mac

```bash
brew install openjdk@21
```
(o descargá el instalador desde <https://adoptium.net>). Verificá con `java -version`.

### Linux (Ubuntu/Debian)

```bash
sudo apt install openjdk-21-jdk
```

---

## Paso 2 — Descargar el proyecto

### Opción A: como ZIP (no necesita Git)

1. Entrá a <https://github.com/StefaNovaliere/DSI-PPAI>.
2. Botón verde **`<> Code`** → **Download ZIP**.
3. Descomprimí el ZIP (clic derecho → **Extraer todo…**), por ejemplo en `Documentos`.
4. **Ojo con la carpeta duplicada:** Windows suele crear una carpeta dentro de otra con el
   mismo nombre:
   ```
   Documentos\DSI-PPAI-claude-nice-ramanujan-nzbzys\          ← ésta NO
   Documentos\DSI-PPAI-claude-nice-ramanujan-nzbzys\DSI-PPAI-claude-nice-ramanujan-nzbzys\   ← ésta SÍ
   ```
   La carpeta correcta es la que tiene adentro `ejecutar.bat`, `mvnw.cmd`, `pom.xml` y `src`.

### Opción B: con Git

```powershell
cd $HOME\Documents
git clone https://github.com/StefaNovaliere/DSI-PPAI.git
cd DSI-PPAI
```
(Si `git` no se reconoce: `winget install Git.Git`, cerrar y reabrir PowerShell.)

---

## Paso 3 — Ejecutar

### Forma fácil (Windows): doble clic

En la carpeta correcta del Paso 2, hacé **doble clic en `ejecutar.bat`**.

- Se abre una ventana negra que dice `Compilando el proyecto...`. **La primera vez tarda
  unos minutos** (descarga Maven, Hibernate y el driver de SQLite). Las siguientes veces,
  unos segundos.
- Después se abre la ventana **"Sistema de Bolsines"**. No cierres la ventana negra mientras
  usás la aplicación.
- Si Windows muestra *"Windows protegió su PC"*: **Más información → Ejecutar de todas
  formas** (pasa con cualquier archivo `.bat` descargado de Internet).

Para **volver los datos al estado inicial** (por ejemplo, antes de la defensa, o para repetir
la prueba) hacé doble clic en **`ejecutar-desde-cero.bat`**.

### Forma manual (PowerShell)

1. Abrí PowerShell **en la carpeta del proyecto**. La forma más fácil: en el Explorador de
   archivos, entrá a la carpeta correcta, hacé clic en la barra de direcciones, escribí
   `powershell` y Enter.
2. Compilá (tiene que terminar con `BUILD SUCCESS`):
   ```powershell
   .\mvnw.cmd package
   ```
   Esto además corre las 16 pruebas automáticas.
3. Ejecutá:
   ```powershell
   java -jar target\ppai-bolsines-g10-1.0.0.jar
   ```
   Variantes:
   ```powershell
   java -jar target\ppai-bolsines-g10-1.0.0.jar --reiniciar-datos   # vuelve a los datos de prueba
   java -jar target\ppai-bolsines-g10-1.0.0.jar --consola           # versión por consola, sin ventana
   ```

### Mac / Linux

```bash
./ejecutar.sh                    # o: ./ejecutar.sh --reiniciar-datos
```

### Desde IntelliJ IDEA (alternativa)

1. **File → Open…** → elegir la carpeta del proyecto (la que tiene `pom.xml`) → *Open as
   Project*. IntelliJ reconoce el proyecto Maven y descarga las dependencias solo.
2. Abrir `src/main/java/ppai/App.java` y apretar el ▶ verde al lado de `main`.
3. Para reiniciar los datos: **Run → Edit Configurations… → Program arguments:**
   `--reiniciar-datos`.

---

## Paso 3b — Configurar el envío de correo

Al registrar una recepción, el sistema envía un correo de notificación (CU 29). Para que se
envíe de verdad hay que decirle qué servidor de correo usar. **Sin este paso el programa
funciona igual**, pero guarda el correo como archivo en la carpeta `correos-simulados`.

1. En la carpeta del proyecto, copiá `correo.properties.ejemplo` con el nombre
   **`correo.properties`**.
   > ⚠️ En Windows, si lo creás con el Bloc de notas, fijate que no quede
   > `correo.properties.txt`: en "Guardar como" elegí *Tipo: Todos los archivos*.
2. Abrilo y completá **una** de las dos opciones.

### Opción A: Gmail (el correo llega a una casilla real)

Gmail no deja usar la contraseña normal: hace falta una **contraseña de aplicación**.

1. Activá la **verificación en dos pasos** de la cuenta: <https://myaccount.google.com/security>.
2. Entrá a <https://myaccount.google.com/apppasswords>, escribí un nombre (por ejemplo
   "PPAI Bolsines") y tocá **Crear**.
3. Copiá la contraseña de 16 letras que aparece. Va en `correo.contrasenia`, con o sin
   espacios.
4. Completá en `correo.properties`:
   ```properties
   correo.smtp.host=smtp.gmail.com
   correo.smtp.puerto=587
   correo.smtp.starttls=true
   correo.smtp.ssl=false
   correo.usuario=tu.cuenta@gmail.com
   correo.contrasenia=abcd efgh ijkl mnop
   correo.remitente=tu.cuenta@gmail.com
   correo.redirigirA=tu.cuenta@gmail.com
   ```

Las cuentas de la facultad o de una empresa a veces no permiten contraseñas de aplicación:
en ese caso usá una cuenta de Gmail personal.

### Opción B: Mailtrap (bandeja de prueba, no le llega a nadie)

1. Creá una cuenta gratis en <https://mailtrap.io>.
2. Entrá a **Email Testing → Inboxes → My Inbox → SMTP** y copiá host, puerto, usuario y
   contraseña.
3. Completá la "Opción B" del archivo de ejemplo. Los correos se ven en la página de Mailtrap.

### `correo.redirigirA`: para la demo

El correo se le envía a la **CM origen** del bolsín, y las CM de prueba tienen correos
ficticios (por ejemplo `mesa.entradas@cmrosario.example`). Con
`correo.redirigirA=tu.cuenta@gmail.com`, **todos los correos te llegan a vos**, y el correo
aclara a quién iba dirigido. Dejalo vacío solo si las CM tienen correos reales.

### Comprobar que quedó bien

Abrí el programa. Debajo de la opción del menú tiene que decir:
*"Notificaciones por correo: se envían por SMTP (smtp.gmail.com:587)…"*. Si dice "no se
envían porque…", el motivo indica qué falta.

> ⚠️ **Para la defensa:** probalo antes **en la red de la facultad**. Algunas redes bloquean
> el puerto 587. Si pasa, probá `correo.smtp.puerto=465`, `correo.smtp.ssl=true` y
> `correo.smtp.starttls=false`, o usá los datos del celular.

---

## Paso 4 — Probar el caso de uso (guion)

Datos de prueba: el usuario logueado es **Ana Pérez (`aperez`), de la Comisión Médica
Central (CMC)**. Hay tres bolsines enviados a la CMC pendientes de recepción:

| Bolsín | Precinto | Desde | Contenido |
|---|---|---|---|
| **101** | PR-0101 | CM Rosario | Remito 5001 (docs 1001 y 1002) y remito 5002 (doc 1003, que está **Para redirigir**) |
| **102** | PR-0102 | CM Mendoza | Remito 6001 (docs 1004 y 1005) |
| **105** | PR-0105 | CM Córdoba | Remito 9001 (docs 1008 y 1009) y remito 9002 (doc 1010) |

Los bolsines 103 (va a CM Rosario) y 104 (ya recibido) no deben aparecer.

> Empezá con `ejecutar-desde-cero.bat` para que los datos estén en su estado inicial. Cada
> bolsín se puede recibir una sola vez; para repetir la prueba, volvé a ejecutar
> `ejecutar-desde-cero.bat`.

### Flujo principal (opción 1)

1. En el menú, elegí **Registrar recepción de bolsín**.
   → A la izquierda aparecen los bolsines por recibir: **101**, **102** y **105**.
2. Hacé clic en el **101**.
   → A la derecha aparece su contenido: 2 remitos con 3 documentos, dos **En bolsín
   enviado** (celeste) y uno **Para redirigir** (naranja). Abajo están las cuatro opciones
   de recepción; está elegida la 1, *"El contenido del bolsín es igual al registrado"*.
3. Apretá **Registrar recepción** y, en el diálogo, **Registrar recepción**.
   → Arriba aparece un aviso verde: *"Se registró la recepción del bolsín N° 101. Remitos
   N° 5001 «Recibido y aceptado», N° 5002 «Recibido y aceptado». Sus 3 documentos quedaron
   en estado «Recibida y aceptada»"*. Debajo dice *"Enviando la notificación por correo a
   CM Rosario…"*, y en unos segundos cambia a *"…se envió a …"*. El 101 desaparece de la
   lista.
4. Revisá la casilla de `correo.redirigirA` (o Mailtrap): llega el correo *"Recepción del
   bolsín N° 101 (precinto PR-0101) en Comisión Médica Central"*, dirigido a CM Rosario, con
   la situación de cada remito y documento. Si no configuraste el correo, el aviso indica en
   qué archivo de `correos-simulados` quedó guardado.

### Flujos alternativos

| Flujo | Cómo probarlo | Resultado esperado |
|-------|---------------|--------------------|
| **A1 – Precinto inexistente** | En *Filtrar*, escribí `PR-9999` en **Precinto** → **Buscar** | Aviso: *"No se encontró ningún bolsín enviado a Comisión Médica Central con el número de precinto «PR-9999». Se muestran todos los bolsines pendientes."* |
| **A2 – CM origen inexistente** | Escribí `Salta` en **CM origen** → **Buscar** | Aviso parecido, *"…desde la CM de origen «Salta»…"* |
| Filtro con resultado | Escribí `rosario` en **CM origen** → **Buscar** (no importan mayúsculas ni tildes) | La lista muestra sólo el **101**. **Limpiar** vuelve a mostrar todos |
| **A3 – No llegó toda la documentación** | Clic en el **105** → opción **2** → tildá la **1009** en la columna *No llegó* → **Registrar recepción** → **Registrar recepción** | La 1009 queda **No recibida** (rojo), la 1008 y la 1010 **Recibida y aceptada**. El remito 9001 queda **Recibido y aceptado parcial** y el 9002, **Recibido y aceptado** |
| **A4 – Documentación que no corresponde** | Clic en el **102** → opción **3** → tildá la **1005** → registrar | La 1005 queda **Recibida y rechazada** y el remito 6001, **Recibido y aceptado parcial** |
| **A5 – Redirigir a otra área** | Clic en el **102** → opción **4** → tildá la **1004** → registrar | La 1004 queda **Para redirigir** y el remito 6001, **Recibido y aceptado** (la opción 4 sólo vale cuando la CM destino es la CMC) |
| **A6 – No confirmar** | Elegí una opción → **Registrar recepción** → **Volver** | Aviso: *"No se registró la recepción … porque no se confirmó"*. Vuelve a las opciones con lo que habías tildado; nada cambia |
| Opción 2 a 4 sin tildar nada | Elegí la opción 2 y apretá **Registrar recepción** sin tildar | Aviso: *"No se indicó ninguna documentación…"* |
| **Transición que no existe** | Clic en el **101** → opción **2** → tildá la **1003** (Para redirigir) → registrar | Aviso: *"No se puede recibir la documentación N° 1003 como «no recibida» porque está en estado «Para redirigir». No se guardó ningún cambio."* (en la máquina de estados no hay esa transición) |
| No quedan bolsines | Recibí el 101, el 102 y el 105 | Aviso *"No hay bolsines enviados pendientes de recepción"* y la lista queda vacía |
| Falla el correo | Poner una contraseña incorrecta en `correo.properties`, abrir el programa y recibir un bolsín | Aviso *"No se pudo enviar la notificación por correo… rechazó el usuario o la contraseña. La recepción quedó registrada igual."* |

![A3: remito recibido y aceptado parcial](docs/capturas/08_recepcion_parcial.png)

### Verificar que se guardó en la base de datos

1. Cerrá la aplicación y volvé a abrirla con **`ejecutar.bat`** (no el "desde cero").
2. Menú → Registrar recepción de bolsín → los bolsines que recibiste **ya no aparecen**: la
   recepción quedó guardada en `bolsines.db`.

### Dónde está el patrón State

El patrón no se ve en la pantalla: es una decisión de diseño interno. Se aplica a
**Documentación, Bolsín y Remito**, y se refleja en el comportamiento:

- El bolsín 101 trae documentación en **dos estados distintos** y las dos se reciben
  correctamente con el mismo evento `recibir()`, cada una resuelta por su propio objeto
  estado.
- Con las opciones 2 a 4, la misma documentación *En bolsín enviado* pasa a estados
  distintos según la guarda, como en la máquina de estados de la Entrega 1.
- Si se pide una transición que no existe (la 1003 como "no llegó"), el objeto estado la
  rechaza y no se guarda nada.

En el código, ver `Documentacion.recibir()` y los estados de los paquetes
`estadodocumentacion`, `estadobolsin` y `estadoremito`; en las pruebas,
`EstadoDocumentacionTest` y `EstadoBolsinYRemitoTest`.

El detalle del diseño y de dónde interviene el patrón está en el [README](README.md#7-patrón-state).

---

## Actualizar a una versión nueva

- **Si lo bajaste como ZIP:** borrá la carpeta anterior y extraé el ZIP nuevo. **No lo
  extraigas encima**, porque quedan archivos viejos que ya no compilan. En PowerShell:
  ```powershell
  cd $HOME\Documents
  Remove-Item -Recurse -Force .\DSI-PPAI-claude-nice-ramanujan-nzbzys
  Expand-Archive "$HOME\Downloads\DSI-PPAI-claude-nice-ramanujan-nzbzys.zip" -DestinationPath $HOME\Documents
  ```
  (Si en Descargas hay varios ZIP con el mismo nombre, el más nuevo puede terminar en `(1).zip`.)
- **Si lo clonaste con Git:** `git pull`.

Después, volver a ejecutar con `ejecutar-desde-cero.bat`. (Si la base es de una versión
anterior, el programa la vuelve a crear solo y lo avisa en la ventana negra.)

---

## Problemas frecuentes

| Mensaje / síntoma | Causa | Solución |
|-------------------|-------|----------|
| `java : El término 'java' no se reconoce…` | Java no está instalado, o la terminal se abrió antes de instalarlo | Paso 1. Después **cerrar y reabrir** PowerShell |
| `ejecutar.bat` muestra "No se encontró Java" | Ídem | Ídem; si recién instalaste Java, cerrá la ventana y volvé a hacer doble clic |
| `.\mvnw.cmd : El término… no se reconoce` | Estás en la carpeta equivocada (la de afuera del ZIP) | Entrá a la carpeta que contiene `mvnw.cmd` (Paso 2, "carpeta duplicada"). Con `dir` ves qué hay |
| `git : El término 'git' no se reconoce…` | Git no está instalado | Usá la Opción A (ZIP) o `winget install Git.Git` |
| `Unable to access jarfile target\ppai-bolsines-g10-1.0.0.jar` | No se compiló, o la compilación falló | Correr `.\mvnw.cmd package` y esperar `BUILD SUCCESS` |
| La compilación falla con errores de descarga / `Could not resolve` | Sin Internet (la primera vez es necesaria) | Conectarse a Internet y volver a compilar |
| Errores de compilación en archivos que no existen en GitHub (por ejemplo `DemostracionPatronState.java`) | Se descomprimió una versión nueva **encima** de la anterior: Windows no borra los archivos que se eliminaron | Borrar la carpeta del proyecto y extraer el ZIP nuevo en una carpeta limpia (ver "Actualizar a una versión nueva") |
| `UnsupportedClassVersionError` | La versión de Java es menor a 17 | Instalar Java 21 (Paso 1) |
| La lista de bolsines aparece vacía | Ya se recibieron el 101, el 102 y el 105 (los cambios quedan guardados) | Usar `ejecutar-desde-cero.bat` o `--reiniciar-datos` |
| "Windows protegió su PC" al abrir el `.bat` | Protección de Windows para archivos descargados | **Más información → Ejecutar de todas formas** |
| *"No se pudo enviar la notificación… rechazó el usuario o la contraseña"* | Gmail no acepta la contraseña normal | Usar una **contraseña de aplicación** (Paso 3b, Opción A) |
| *"No se pudo enviar la notificación… no se pudo conectar con smtp.gmail.com:587"* | La red bloquea el puerto, o no hay Internet | Probar puerto 465 con `correo.smtp.ssl=true` y `correo.smtp.starttls=false`, o usar los datos del celular |
| El menú dice *"no se envían porque no hay un servidor de correo configurado"* | No está `correo.properties` en la carpeta del proyecto, o se llama `correo.properties.txt` | Revisar el nombre exacto y que esté junto a `ejecutar.bat` (Paso 3b) |
| El correo dice que se envió pero no llega | Fue a la casilla ficticia de la CM origen | Completar `correo.redirigirA` con tu casilla; revisar también Spam |
| En la versión por consola los acentos se ven como `?` | Codificación de la consola de Windows | Ejecutar `chcp 65001` antes, o usar la ventana gráfica |
