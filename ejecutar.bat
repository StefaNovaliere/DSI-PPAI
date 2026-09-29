@echo off
rem Compila (si hace falta) y abre la aplicacion del CU 28.
rem Uso: doble clic sobre este archivo.
cd /d "%~dp0"

where java >nul 2>nul
if errorlevel 1 goto sinJava

echo.
echo Compilando el proyecto. La primera vez descarga dependencias y puede tardar unos minutos...
call mvnw.cmd -q package -DskipTests
if errorlevel 1 goto errorCompilacion

echo Abriendo la aplicacion...
java -jar "target\ppai-bolsines-g10-1.0.0.jar" %*
if errorlevel 1 pause
exit /b 0

:sinJava
echo.
echo [ERROR] No se encontro Java en esta PC.
echo Instale Java 17 o superior siguiendo COMO_EJECUTAR.md - Paso 1.
echo Si ya lo instalo, cierre esta ventana y vuelva a intentar.
echo.
pause
exit /b 1

:errorCompilacion
echo.
echo [ERROR] Fallo la compilacion. Revise COMO_EJECUTAR.md - Problemas frecuentes.
echo La primera vez se necesita conexion a Internet.
echo.
pause
exit /b 1
