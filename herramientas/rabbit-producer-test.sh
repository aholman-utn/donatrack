#!/bin/bash
#
# Prueba del lado PRODUCTOR de la mensajería.
#
# Levanta "servicio-donaciones" disparando el procesamiento de envío a
# logística INMEDIATAMENTE al arrancar (no espera al horario del cron), para
# observar en el RabbitMQ Manager cómo se publican mensajes reales en la cola
# 'logistica.planificar.queue'. Además deja el cron acelerado (cada 1 minuto)
# como red de seguridad.
#
# El cron (DonacionesListasParaEntregarCron) solo publica si encuentra
# donaciones segmentadas en estado LISTA_PARA_ENTREGAR con una entidad
# beneficiaria que tenga dirección válida. Si no hay datos, no publica nada
# (lo vas a ver en el log: "No se encontraron donaciones listas para entregar").
#
# Para VER el mensaje retenido en la cola, el CONSUMIDOR (servicio-logistica)
# debe estar APAGADO. Si logística está arriba, consume el lote al instante.
#
# Uso:
#   ./herramientas/rabbit-producer-test.sh up        # levanta donaciones con cron cada 1 min
#   ./herramientas/rabbit-producer-test.sh down      # lo detiene
#   ./herramientas/rabbit-producer-test.sh status
#   ./herramientas/rabbit-producer-test.sh logs      # sigue el log (ver cuándo corre el cron)
#
# Se puede cambiar la expresión del cron con CRON (formato Spring, 6 campos):
#   CRON="0 * * * * *" ./herramientas/rabbit-producer-test.sh up   # cada minuto (default)
#
set -euo pipefail

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

SERVICIO="servicio-donaciones"
# Cron acelerado: cada minuto, al segundo 0.
CRON="${CRON:-0 * * * * *}"
PID_FILE="$ROOT_DIR/.rabbit-producer-${SERVICIO}.pid"
LOG_FILE="$ROOT_DIR/.rabbit-producer-${SERVICIO}.log"

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
            echo "⚠️  $SERVICIO (productor) ya está corriendo (PID $(cat "$PID_FILE"))."
            exit 0
        fi
        echo "🚀 Levantando $SERVICIO como PRODUCTOR."
        echo "   Dispara el envío INMEDIATAMENTE al arrancar (ejecutar-al-iniciar=true)."
        echo "   Log: $LOG_FILE"
        echo "   Recordá: para ver el mensaje retenido, servicio-logistica debe estar APAGADO."
        echo "📦 Instalando módulos compartidos (commons) para evitar usar versiones viejas..."
        if ! "$MVN" -q -pl commons -am install -DskipTests; then
            echo "❌ Falló la compilación/instalación de 'commons'. Revisá los errores arriba."
            exit 1
        fi
        # El disparo inmediato (ejecutar-al-iniciar=true) hace el envío apenas
        # arranca, sin esperar al cron. La expresión del cron se pasa como
        # propiedad de sistema de la JVM para no romperse por los espacios
        # (los 6 campos). Entre comillas simples para que no los parta el shell.
        nohup "$MVN" spring-boot:run -pl "$SERVICIO" \
            -Dspring-boot.run.jvmArguments="-Ddonatrack.cron.donaciones-listas='$CRON' -Ddonatrack.cron.donaciones-listas.ejecutar-al-iniciar=true" \
            > "$LOG_FILE" 2>&1 &
        echo $! > "$PID_FILE"
        echo "✅ Iniciado con PID $(cat "$PID_FILE")."
        echo "   Seguí el arranque y los ciclos del cron con: $0 logs"
        ;;
    down)
        if ! esta_corriendo; then
            echo "ℹ️  $SERVICIO (productor) no está corriendo."
            rm -f "$PID_FILE"
            exit 0
        fi
        PID="$(cat "$PID_FILE")"
        echo "🛑 Deteniendo $SERVICIO (PID $PID)..."
        pkill -TERM -P "$PID" 2>/dev/null || true
        kill -TERM "$PID" 2>/dev/null || true
        sleep 3
        if kill -0 "$PID" 2>/dev/null; then
            pkill -KILL -P "$PID" 2>/dev/null || true
            kill -KILL "$PID" 2>/dev/null || true
        fi
        rm -f "$PID_FILE"
        echo "✅ Detenido."
        ;;
    status)
        if esta_corriendo; then
            echo "🟢 $SERVICIO (productor) está CORRIENDO (PID $(cat "$PID_FILE"))."
        else
            echo "🔴 $SERVICIO (productor) está DETENIDO."
        fi
        ;;
    logs)
        if [ ! -f "$LOG_FILE" ]; then
            echo "No hay log todavía ($LOG_FILE). Levantá el servicio con: $0 up"
            exit 1
        fi
        echo "📜 Siguiendo $LOG_FILE (Ctrl+C para salir)..."
        echo "   Buscá líneas del cron: 'Iniciando procesamiento de cron...' / 'Se enviaron exitosamente N donaciones.'"
        tail -f "$LOG_FILE"
        ;;
    *)
        echo "Uso: $0 {up|down|status|logs}"
        echo "Variable opcional: CRON=\"0 * * * * *\" (expresion Spring de 6 campos)"
        exit 1
        ;;
esac
