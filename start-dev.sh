#!/bin/bash

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
else
    CMD="./mvnw"
    OS_TYPE="windows"
    echo "🪟 Entorno Windows detectado: Ejecutando wrapper '$CMD'"
fi

echo "🧹 Ejecutando clean y compile del proyecto base..."
$CMD clean install

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
            gnome-terminal --title="$SERVICIO" -- bash -c "$CMD spring-boot:run -pl $SERVICIO; exec bash" &
        elif [ "$TERM_EMULATOR" = "konsole" ]; then
            konsole -e bash -c "$CMD spring-boot:run -pl $SERVICIO; exec bash" &
        else
            xterm -title "$SERVICIO" -e bash -c "$CMD spring-boot:run -pl $SERVICIO; exec bash" &
        fi
    else
        # Entorno Windows / Git Bash
        start "$SERVICIO" cmd /k "$CMD spring-boot:run -pl $SERVICIO"
    fi

    sleep 2
}

start_service "servicio-donaciones"
start_service "servicio-incentivos"
start_service "servicio-notificaciones"
start_service "servicio-logistica"

echo "✅ Despliegue local completado. Las terminales se abrirán en breve."