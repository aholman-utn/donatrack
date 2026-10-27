#!/bin/bash

echo "========================================================"
echo "       🛑 DETENIENDO ENTORNO COMPLETO DONATRACK 🛑       "
echo "========================================================"

PUERTOS=(8080 8081 8082 8083 8084)

echo -e "\n[1/2] Liberando puertos de microservicios (${PUERTOS[*]})..."

for PUERTO in "${PUERTOS[@]}"; do
    PID=$(lsof -ti :$PUERTO)
    if [ -n "$PID" ]; then
        echo "  -> Deteniendo proceso (PID: $PID) en puerto $PUERTO..."
        kill -9 $PID 2>/dev/null
        echo "  ✅ Puerto $PUERTO liberado."
    else
        echo "  - Puerto $PUERTO ya está libre."
    fi
done

echo -e "\n[2/2] Verificando Docker..."
if command -v docker &> /dev/null; then
    if [ -f "herramientas/docker-compose.yml" ]; then
        docker compose -f herramientas/docker-compose.yml down
        echo "  ✅ Contenedores de herramientas detenidos."
    fi
else
    echo "  - Docker no está en uso/instalado."
fi

echo -e "\n========================================================"
echo "  ✅ Todo el entorno fue detenido con éxito! "
echo "========================================================"
