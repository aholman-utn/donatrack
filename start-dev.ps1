# ====================================================================
# DonaTrack - Script de Inicio del Entorno de Desarrollo Completo
# ====================================================================
Set-Location -LiteralPath $PSScriptRoot

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "       🚀 INICIANDO ENTORNO COMPLETO DONATRACK 🚀       " -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

# 1. Levantar contenedores Docker (RabbitMQ, n8n, Browserless)
Write-Host "`n[1/3] Verificando e iniciando contenedores Docker (RabbitMQ, n8n)..." -ForegroundColor Yellow
if (Get-Command docker -ErrorAction SilentlyContinue) {
    try {
        docker compose -f herramientas/docker-compose.yml up -d
        if ($LASTEXITCODE -ne 0) { throw "Docker Compose fallo." }
        Write-Host "  ✅ Contenedores Docker iniciados correctamente." -ForegroundColor Green
    } catch {
        throw "No se pudo iniciar Docker Compose. Verifica Docker Desktop."
    }
} else {
    throw "Docker no esta instalado o no se encuentra en PATH."
}

# 2. Compilar e instalar dependencias (principalmente 'commons')
Write-Host "`n[2/3] Compilando e instalando dependencias (mvn clean install)..." -ForegroundColor Yellow
& .\mvnw.cmd clean install -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error fatal: Falló la compilación de Maven. Abortando inicio." -ForegroundColor Red
    exit 1
}
Write-Host "  ✅ Proyecto compilado con éxito." -ForegroundColor Green

# 3. Levantar cada microservicio en una ventana independiente
Write-Host "`n[3/3] Desplegando microservicios en terminales separadas..." -ForegroundColor Yellow

$servicios = @(
    @{ Nombre = "servicio-donaciones"; Puerto = "8080"; Color = "Green" },
    @{ Nombre = "servicio-incentivos"; Puerto = "8081"; Color = "Magenta" },
    @{ Nombre = "servicio-notificaciones"; Puerto = "8082"; Color = "Cyan" },
    @{ Nombre = "servicio-logistica"; Puerto = "8083"; Color = "Yellow" }
)

$currentDir = $PSScriptRoot.Replace("'", "''")

foreach ($s in $servicios) {
    $nombre = $s.Nombre
    $puerto = $s.Puerto
    Write-Host "  -> Iniciando $nombre (Puerto $puerto)..." -ForegroundColor $s.Color
    
    $cmd = "`$host.UI.RawUI.WindowTitle = '$nombre - Puerto $puerto'; cd '$currentDir'; .\mvnw.cmd spring-boot:run -pl $nombre"
    Start-Process powershell.exe -ArgumentList "-NoExit", "-Command", $cmd
    Start-Sleep -Seconds 2
}

Write-Host "`n========================================================" -ForegroundColor Green
Write-Host "  Procesos lanzados; espera el arranque de cada servicio." -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Green
Write-Host "Puertos configurados:" -ForegroundColor White
Write-Host "  - Servicio Donaciones:     http://localhost:8080" -ForegroundColor White
Write-Host "  - Servicio Incentivos:     http://localhost:8081" -ForegroundColor White
Write-Host "  - Servicio Notificaciones: http://localhost:8082" -ForegroundColor White
Write-Host "  - Servicio Logística:      http://localhost:8083" -ForegroundColor White
Write-Host "  - RabbitMQ Management:     http://localhost:15672 (guest/guest)" -ForegroundColor White
Write-Host "  - n8n Webhook / Workflows: http://localhost:5678" -ForegroundColor White
Write-Host "`nYa podés probar la colección de Postman en 'herramientas/DonaTrack.postman_collection.json'." -ForegroundColor Cyan
