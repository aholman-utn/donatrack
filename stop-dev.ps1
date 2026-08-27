# ====================================================================
# DonaTrack - Script para Detener el Entorno Completo
# ====================================================================
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "========================================================" -ForegroundColor Yellow
Write-Host "       🛑 DETENIENDO ENTORNO COMPLETO DONATRACK 🛑       " -ForegroundColor Yellow
Write-Host "========================================================" -ForegroundColor Yellow

# 1. Matar procesos en los puertos 8080 a 8083
$puertos = @(8080, 8081, 8082, 8083)

Write-Host "`n[1/2] Liberando puertos de microservicios (8080, 8081, 8082, 8083)..." -ForegroundColor Yellow

foreach ($puerto in $puertos) {
    $connections = Get-NetTCPConnection -LocalPort $puerto -State Listen -ErrorAction SilentlyContinue
    if ($connections) {
        $pids = $connections | Select-Object -ExpandProperty OwningProcess -Unique
        foreach ($p in $pids) {
            try {
                $procName = (Get-Process -Id $p -ErrorAction SilentlyContinue).ProcessName
                Write-Host "  -> Deteniendo proceso '$procName' (PID: $p) en puerto $puerto..." -ForegroundColor Magenta
                Stop-Process -Id $p -Force -ErrorAction SilentlyContinue
            } catch {
                # Proceso ya no existe
            }
        }
        Write-Host "  ✅ Puerto $puerto liberado." -ForegroundColor Green
    } else {
        Write-Host "  - Puerto $puerto ya está libre." -ForegroundColor Gray
    }
}

# 2. Bajar contenedores Docker (RabbitMQ, n8n, Browserless)
Write-Host "`n[2/2] Deteniendo contenedores Docker..." -ForegroundColor Yellow
if (Get-Command docker -ErrorAction SilentlyContinue) {
    docker compose -f herramientas/docker-compose.yml down
    Write-Host "  ✅ Contenedores Docker detenidos." -ForegroundColor Green
}

Write-Host "`n========================================================" -ForegroundColor Green
Write-Host "  ✅ Todo el entorno fue detenido y liberado con éxito! " -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Green
