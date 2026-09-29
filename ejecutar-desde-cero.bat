@echo off
rem Vuelve la base de datos a los datos de prueba y abre la aplicacion.
rem Util para repetir la demo: los bolsines 101 y 102 vuelven a estar pendientes.
cd /d "%~dp0"
call ejecutar.bat --reiniciar-datos
