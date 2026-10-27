#!/bin/bash

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [ "$(uname -s)" = "Linux" ]; then
    CMD="mvn"
    OS_TYPE="linux"
    echo "🐧 Entorno Linux detectado: Ejecutando binario nativo '$CMD'"

    if command -v gnome-terminal &> /dev/null; then
        TERM_EMULATOR="gnome-terminal"
    elif command -v konsole &> /dev/null; then
        TERM_EMULATOR="konsole"
    elif command -v xterm &> /dev/null; then
        TERM_EMULATOR="xterm"
    else
        echo "❌ Error: No se encontró gnome-terminal, konsole ni xterm."
        exit 1
    fi
elif [ "$(uname -s)" = "Darwin" ]; then
    CMD="./mvnw"
    OS_TYPE="macos"
    echo "🍎 Entorno macOS detectado: Ejecutando wrapper '$CMD'"
else
    CMD="./mvnw"
    OS_TYPE="windows"
    echo "🪟 Entorno Windows detectado: Ejecutando wrapper '$CMD'"
fi

# Cargar variables de entorno de .env si existe
if [ -f "$ROOT_DIR/.env" ]; then
    echo "📄 Cargando variables desde .env..."
    set -a
    source "$ROOT_DIR/.env"
    set +a
fi

echo "🧹 Ejecutando clean y compile del proyecto base..."
$CMD clean install -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Error fatal: Falló la fase de compilación. Abortando inicio."
    exit 1
fi

echo "🚀 Inicializando arquitectura de microservicios..."

start_service() {
    local SERVICIO=$1
    echo "-> Arrancando $SERVICIO en una nueva instancia..."

    if [ "$OS_TYPE" = "linux" ]; then
        if [ "$TERM_EMULATOR" = "gnome-terminal" ]; then
            gnome-terminal --title="$SERVICIO" -- bash -c "cd '$ROOT_DIR' && set -a && [ -f .env ] && . ./.env; set +a; $CMD spring-boot:run -pl $SERVICIO; exec bash" &
        elif [ "$TERM_EMULATOR" = "konsole" ]; then
            konsole -e bash -c "cd '$ROOT_DIR' && set -a && [ -f .env ] && . ./.env; set +a; $CMD spring-boot:run -pl $SERVICIO; exec bash" &
        else
            xterm -title "$SERVICIO" -e bash -c "cd '$ROOT_DIR' && set -a && [ -f .env ] && . ./.env; set +a; $CMD spring-boot:run -pl $SERVICIO; exec bash" &
        fi
    elif [ "$OS_TYPE" = "macos" ]; then
        osascript -e "tell application \"Terminal\" to do script \"cd \\\"$ROOT_DIR\\\" && set -a && [ -f .env ] && . ./.env && set +a && $CMD spring-boot:run -pl $SERVICIO\"" > /dev/null
    else
        # Entorno Windows / Git Bash
        start "$SERVICIO" cmd /k "$CMD spring-boot:run -pl $SERVICIO"
    fi

    sleep 2
}

start_service "api-gateway"
start_service "servicio-donaciones"
start_service "servicio-incentivos"
start_service "servicio-notificaciones"
start_service "servicio-logistica"

echo "✅ Despliegue local completado. Las terminales se abrirán en breve."