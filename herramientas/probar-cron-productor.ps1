<#
.SYNOPSIS
    Prueba el cron DonacionesListasParaEntregarCron y su productor LogisticaQueueClient,
    la unica pieza de la migracion a RabbitMQ que probar-flujo.ps1 no ejercita.

.DESCRIPTION
    Deja DOS donaciones en LISTA_PARA_ENTREGAR y espera a que el cron dispare solo:

      A) entidad CON direccion  -> debe viajar por la cola y quedar EN_PLANIFICACION
      B) entidad SIN direccion  -> debe ser descartada y seguir en LISTA_PARA_ENTREGAR

    El caso B es la verificacion del bug corregido en el cron: antes se marcaban
    EN_PLANIFICACION donaciones que nunca se habian enviado, y quedaban trabadas
    para siempre porque de ese estado solo se sale con un INICIO_RUTA que nunca llega.

.NOTES
    REQUIERE que servicio-donaciones corra con el cron acelerado:

        $env:DONATRACK_CRON_DONACIONESLISTAS = "0 * * * * *"
        .\mvnw.cmd spring-boot:run -pl servicio-donaciones

    Con el cron por defecto (8:00 AM) el script espera al pedo y falla por timeout.
#>
[CmdletBinding()]
param(
    [int] $TimeoutSegundos = 90
)

$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$D = "http://localhost:8080"
$L = "http://localhost:8083"

function Write-Paso  { param($n, $t) Write-Host "`n[$n] $t" -ForegroundColor Cyan }
function Write-Ok    { param($t) Write-Host "    OK  $t" -ForegroundColor Green }
function Write-Info  { param($t) Write-Host "    ->  $t" -ForegroundColor DarkGray }
function Write-Falla { param($t) Write-Host "    X   $t" -ForegroundColor Red }

function Test-Puerto {
    param($Puerto)
    $c = New-Object System.Net.Sockets.TcpClient
    try {
        $e = $c.BeginConnect("127.0.0.1", $Puerto, $null, $null)
        if (-not $e.AsyncWaitHandle.WaitOne(2000)) { return $false }
        $c.EndConnect($e); return $true
    } catch { return $false } finally { $c.Close() }
}

function Get-EstadoSegmento {
    param($DonacionId)
    # Asignar a variable antes de indexar: en PS 5.1 Invoke-RestMethod no
    # desenrolla los arrays JSON al pipearlos directamente.
    $traza  = Invoke-RestMethod "$D/trazabilidad/$DonacionId"
    $evs    = @($traza.segmentos[0].eventos)
    return $evs[$evs.Count - 1].estadoNuevo
}

function New-EscenarioDonacion {
    param($DonanteId, $RazonSocial, $SubCategoria, $ConDireccion)

    $body = @{
        razonSocial         = $RazonSocial
        tipo                = "ONG"
        rubro               = "Asistencia"
        mediosDeContacto    = @(@{ medio = "EMAIL"; valor = "test@mail.com" })
        medioPredeterminado = @{ medio = "EMAIL"; valor = "test@mail.com" }
    }
    if ($ConDireccion) {
        $body.direccion = @{
            calle1 = "Av. Rivadavia"; altura = 1234
            ciudadNombre = "CABA"; provinciaNombre = "Buenos Aires"; paisNombre = "Argentina"
        }
    }
    $entidad   = Invoke-RestMethod -Method Post "$D/entidadBeneficiaria" -ContentType "application/json" -Body ($body | ConvertTo-Json -Depth 5)
    $entidadId = $entidad.datosDeEntidad.id

    Invoke-RestMethod -Method Post "$D/api/necesidades" -ContentType "application/json" -Body (@{
        entidadBeneficiariaId = $entidadId
        subCategoriaNombre    = $SubCategoria
        cantidad              = 100
        fechaLimite           = (Get-Date).AddMonths(6).ToString("yyyy-MM-dd")
        tipoNecesidad         = "EXTRAORDINARIA"
        causa                 = "Prueba de cron"
    } | ConvertTo-Json) | Out-Null

    $bien = @{
        tipo             = "PERECEDERO"
        nombre           = $SubCategoria
        descripcion      = "Paquete 500g"
        subCategoria     = @{ categoria = "ALIMENTOS"; descripcion = $SubCategoria; unidad = "KG" }
        fechaVencimiento = (Get-Date).AddMonths(9).ToString("yyyy-MM-dd")
    }
    $donacion = Invoke-RestMethod -Method Post "$D/donaciones" -ContentType "application/json" -Body (@{
        donanteId   = $DonanteId
        descripcion = "Prueba cron - $RazonSocial"
        bienes      = @($bien, $bien)
    } | ConvertTo-Json -Depth 6)

    $donacionId = $donacion.id
    $segId      = $donacion.donacionesSegmentadas[0].id

    Invoke-RestMethod -Method Post "$D/matchmaking/asignar" -ContentType "application/json" -Body (@{
        donacionSegmentadaId = $segId; entidadBeneficiariaId = $entidadId } | ConvertTo-Json) | Out-Null

    Invoke-RestMethod -Method Post "$D/trazabilidad/$donacionId/$segId/transicionar/lista_entregar?actor=Admin" | Out-Null

    return [pscustomobject]@{
        EntidadId  = $entidadId
        DonacionId = $donacionId
        SegmentoId = $segId
    }
}

# ====================================================================
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "   PRUEBA DEL CRON PRODUCTOR (LogisticaQueueClient)" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

Write-Paso "0" "Verificando entorno"
foreach ($p in @(@{N="donaciones";P=8080}, @{N="logistica";P=8083}, @{N="RabbitMQ";P=5672})) {
    if (Test-Puerto $p.P) { Write-Ok "$($p.N) en $($p.P)" }
    else { Write-Falla "$($p.N) NO responde en $($p.P)"; exit 1 }
}
Write-Info "Recorda: donaciones debe correr con DONATRACK_CRON_DONACIONESLISTAS='0 * * * * *'"

# ====================================================================
Write-Paso "1" "Preparando escenario"
$donante = Invoke-RestMethod -Method Post "$D/donantes/humano" -ContentType "application/json" -Body (@{
    nombre = "Ana"; apellido = "Lopez"; nroDocumento = "27888333"
    mediosDeContacto    = @(@{ medio = "EMAIL"; valor = "ana@mail.com" })
    medioPredeterminado = @{ medio = "EMAIL"; valor = "ana@mail.com" }
} | ConvertTo-Json -Depth 5)
$donanteId = $donante.persona.id

# Subcategorias distintas para que cada donacion matchee solo con su entidad.
$sufijo = Get-Random -Minimum 1000 -Maximum 9999
$caso   = New-EscenarioDonacion -DonanteId $donanteId -RazonSocial "Comedor CON direccion $sufijo" -SubCategoria "Fideos$sufijo" -ConDireccion $true
$control= New-EscenarioDonacion -DonanteId $donanteId -RazonSocial "Comedor SIN direccion $sufijo" -SubCategoria "Arroz$sufijo"  -ConDireccion $false

Write-Ok "A (con direccion): donacion=$($caso.DonacionId) segmento=$($caso.SegmentoId) -> LISTA_PARA_ENTREGAR"
Write-Ok "B (sin direccion): donacion=$($control.DonacionId) segmento=$($control.SegmentoId) -> LISTA_PARA_ENTREGAR"

# ====================================================================
Write-Paso "2" "Esperando que el cron dispare solo (hasta ${TimeoutSegundos}s)"
Write-Info "No se toca ningun endpoint: el cron tiene que hacerlo por su cuenta."

$disparo = $false
$t0 = Get-Date
while (((Get-Date) - $t0).TotalSeconds -lt $TimeoutSegundos) {
    Start-Sleep -Seconds 2
    if ((Get-EstadoSegmento $caso.DonacionId) -eq "EN_PLANIFICACION") { $disparo = $true; break }
    $seg = [int]((Get-Date) - $t0).TotalSeconds
    Write-Host "`r    esperando... ${seg}s" -NoNewline -ForegroundColor DarkGray
}
Write-Host ""

if (-not $disparo) {
    Write-Falla "El cron no disparo en ${TimeoutSegundos}s."
    Write-Falla "Casi seguro donaciones esta corriendo con el cron por defecto (8:00 AM)."
    Write-Falla "Reinicialo asi:"
    Write-Host  "    `$env:DONATRACK_CRON_DONACIONESLISTAS = `"0 * * * * *`"" -ForegroundColor Yellow
    Write-Host  "    .\mvnw.cmd spring-boot:run -pl servicio-donaciones" -ForegroundColor Yellow
    exit 1
}
$seg = [int]((Get-Date) - $t0).TotalSeconds
Write-Ok "El cron disparo a los ${seg}s"

# ====================================================================
Write-Paso "3" "Verificaciones"
$fallas = 0

# 3.1 - El productor publico y logistica consumio
$estadoA = Get-EstadoSegmento $caso.DonacionId
if ($estadoA -eq "EN_PLANIFICACION") {
    Write-Ok "A quedo EN_PLANIFICACION (LogisticaQueueClient publico el lote)"
} else {
    Write-Falla "A quedo en '$estadoA', se esperaba EN_PLANIFICACION"; $fallas++
}

$envios  = Invoke-RestMethod "$L/api/logistica/envios"
$envioA  = $envios | Where-Object { $_.donacionSegmentadaId -eq $caso.SegmentoId } | Select-Object -First 1
if ($null -ne $envioA) {
    Write-Ok "Logistica creo el envio id=$([int]$envioA.id) para el segmento $($caso.SegmentoId)"
    Write-Info "El mensaje viajo donaciones -> logistica.planificar.queue -> PlanificacionListener"
} else {
    Write-Falla "Logistica no genero envio para el segmento $($caso.SegmentoId)"
    Write-Falla "El lote no llego por la cola. Revisa los logs de ambos servicios."
    $fallas++
}

# 3.2 - El descarte por falta de direccion (bug corregido en el cron)
$estadoB = Get-EstadoSegmento $control.DonacionId
if ($estadoB -eq "LISTA_PARA_ENTREGAR") {
    Write-Ok "B sigue en LISTA_PARA_ENTREGAR (descartada por no tener direccion)"
    Write-Info "Se reintentara sola en el proximo ciclo del cron"
} else {
    Write-Falla "B quedo en '$estadoB'. Se esperaba LISTA_PARA_ENTREGAR."
    Write-Falla "REGRESION: se esta marcando EN_PLANIFICACION una donacion que nunca se envio."
    $fallas++
}

$envioB = $envios | Where-Object { $_.donacionSegmentadaId -eq $control.SegmentoId } | Select-Object -First 1
if ($null -eq $envioB) {
    Write-Ok "Logistica no recibio la donacion sin direccion"
} else {
    Write-Falla "Logistica genero un envio para B, que no deberia haberse enviado"; $fallas++
}

Write-Host ""
Write-Host "En los logs de servicio-donaciones tiene que aparecer:" -ForegroundColor DarkGray
Write-Host "  WARN  Se descartaron 1 donaciones segmentadas por no tener direccion de entrega." -ForegroundColor DarkGray
Write-Host "  INFO  Se enviaron exitosamente 1 donaciones." -ForegroundColor DarkGray

Write-Host ""
if ($fallas -eq 0) {
    Write-Host "========================================================" -ForegroundColor Green
    Write-Host "  CRON PRODUCTOR OK" -ForegroundColor Green
    Write-Host "========================================================" -ForegroundColor Green
    Write-Host "No te olvides de sacar la variable de entorno:" -ForegroundColor Yellow
    Write-Host "  Remove-Item Env:\DONATRACK_CRON_DONACIONESLISTAS" -ForegroundColor Yellow
    exit 0
} else {
    Write-Host "========================================================" -ForegroundColor Red
    Write-Host "  $fallas VERIFICACION(ES) FALLIDA(S)" -ForegroundColor Red
    Write-Host "========================================================" -ForegroundColor Red
    exit 1
}
