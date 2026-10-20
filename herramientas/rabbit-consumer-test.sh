#!/bin/bash
#
# Script de ayuda para probar la mensajería RabbitMQ (CloudAMQP).
#
# Levanta/baja un servicio consumidor en segundo plano para poder observar
# el comportamiento de las colas en el RabbitMQ Manager:
#   - Con el servicio ARRIBA  -> la cola tiene Consumers = 1 y los mensajes
#                                se consumen al instante (Messages Ready = 0).
#   - Con el servicio ABAJO   -> Consumers = 0 y los mensajes quedan
#                                retenidos (Messages Ready crece), por lo que
#                                se pueden inspeccionar con "Get messages".
#
# Uso:
#   ./herramientas/rabbit-consumer-test.sh up        # levanta el servicio
#   ./herramientas/rabbit-consumer-test.sh down      # lo detiene
#   ./herramientas/rabbit-consumer-test.sh status    # dice si está corriendo
#   ./herramientas/rabbit-consumer-test.sh logs      # sigue el log en vivo
#
# El servicio consumidor por defecto es "servicio-donaciones" (consume la cola
# donaciones.logistica.eventos.queue). Se puede cambiar con la variable SERVICIO:
#   SERVICIO=servicio-notificaciones ./herramientas/rabbit-consumer-test.sh up
#
set -euo pipefail

# Raíz del proyecto (un nivel arriba de herramientas/).
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

# Carga credenciales locales (DB, etc.) si existe el archivo ignorado por git.
ENV_LOCAL="$ROOT_DIR/herramientas/.env.local"
if [ -f "$ENV_LOCAL" ]; then
    set -a
    # shellcheck disable=SC1090
    . "$ENV_LOCAL"
    set +a
fi

SERVICIO="${SERVICIO:-servicio-donaciones}"
PID_FILE="$ROOT_DIR/.rabbit-consumer-${SERVICIO}.pid"
LOG_FILE="$ROOT_DIR/.rabbit-consumer-${SERVICIO}.log"

# Usa el wrapper de Maven si existe; si no, mvn del sistema.
if [ -x "$ROOT_DIR/mvnw" ]; then
    MVN="$ROOT_DIR/mvnw"
else
    MVN="mvn"
fi

esta_corriendo() {
    [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null
}

case "${1:-}" in
    up)
        if esta_corriendo; then
            echo "⚠️  $SERVICIO ya está corriendo (PID $(cat "$PID_FILE"))."
            exit 0
        fi
        echo "🚀 Levantando $SERVICIO (consumidor de RabbitMQ) en segundo plano..."
        echo "   Log: $LOG_FILE"
        nohup "$MVN" spring-boot:run -pl "$SERVICIO" > "$LOG_FILE" 2>&1 &
        echo $! > "$PID_FILE"
        echo "✅ Iniciado con PID $(cat "$PID_FILE")."
        echo "   Seguí el arranque con: $0 logs"
        echo "   Cuando veas que conectó a RabbitMQ, la cola mostrará Consumers = 1."
        ;;
    down)
        if ! esta_corriendo; then
            echo "ℹ️  $SERVICIO no está corriendo."
            rm -f "$PID_FILE"
            exit 0
        fi
        PID="$(cat "$PID_FILE")"
        echo "🛑 Deteniendo $SERVICIO (PID $PID)..."
        # Mata el proceso y sus hijos (el spring-boot:run lanza un proceso Java hijo).
        pkill -TERM -P "$PID" 2>/dev/null || true
        kill -TERM "$PID" 2>/dev/null || true
        sleep 3
        if kill -0 "$PID" 2>/dev/null; then
            echo "   No respondió a TERM, forzando KILL..."
            pkill -KILL -P "$PID" 2>/dev/null || true
            kill -KILL "$PID" 2>/dev/null || true
        fi
        rm -f "$PID_FILE"
        echo "✅ Detenido. Ahora la cola debería mostrar Consumers = 0."
        ;;
    status)
        if esta_corriendo; then
            echo "🟢 $SERVICIO está CORRIENDO (PID $(cat "$PID_FILE"))."
        else
            echo "🔴 $SERVICIO está DETENIDO."
        fi
        ;;
    logs)
        if [ ! -f "$LOG_FILE" ]; then
            echo "No hay log todavía ($LOG_FILE). Levantá el servicio primero con: $0 up"
            exit 1
        fi
        echo "📜 Siguiendo $LOG_FILE (Ctrl+C para salir)..."
        tail -f "$LOG_FILE"
        ;;
    *)
        echo "Uso: $0 {up|down|status|logs}"
        echo "Variable opcional: SERVICIO=<modulo> (default: servicio-donaciones)"
        exit 1
        ;;
esac
