#!/bin/sh
# Compila y abre la aplicacion del CU 28 (Mac / Linux).
# Uso: ./ejecutar.sh            (datos guardados)
#      ./ejecutar.sh --reiniciar-datos   (vuelve a los datos de prueba)
cd "$(dirname "$0")" || exit 1

if ! command -v java >/dev/null 2>&1; then
    echo "[ERROR] No se encontro Java. Instale Java 17 o superior (ver COMO_EJECUTAR.md - Paso 1)."
    exit 1
fi

echo "Compilando el proyecto. La primera vez descarga dependencias y puede tardar unos minutos..."
sh ./mvnw -q package -DskipTests || { echo "[ERROR] Fallo la compilacion. Ver COMO_EJECUTAR.md - Problemas frecuentes."; exit 1; }

echo "Abriendo la aplicacion..."
exec java -jar target/ppai-bolsines-g10-1.0.0.jar "$@"
