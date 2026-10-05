<#
.SYNOPSIS
    Prueba end-to-end del flujo completo de DonaTrack: donante -> donacion ->
    matchmaking -> planificacion logistica -> ruta -> entrega.

.DESCRIPTION
    Ejercita donaciones, logistica y la mensajeria RabbitMQ que reemplazo al polling.
    Todo el estado es en memoria, asi que cada corrida crea datos nuevos (los IDs avanzan).

.PARAMETER ModoPlanificacion
    'cola' (default) : publica el lote en logistica.planificar.queue via la API de management.
                       Prueba PlanificacionListener (consumidor del comando).
    'http'           : POST directo a /api/logistica/planificar, salteando la cola.
                       Mas robusto, pero NO prueba la direccion de comando.
    'cron'           : espera al cron real, incluyendo LogisticaQueueClient.
                       Requiere iniciar donaciones con el cron acelerado.

    Los modos cola/http no ejercitan LogisticaQueueClient; usar cron para el recorrido completo.

.PARAMETER EsperaDuplicadosSegundos
    Espera antes de iniciar la ruta y exige exactamente un envio del segmento.
    Usar al menos 65 segundos si el cron corre cada minuto.

.EXAMPLE
    .\herramientas\probar-flujo.ps1
    .\herramientas\probar-flujo.ps1 -ModoPlanificacion http
#>
[CmdletBinding()]
param(
    [ValidateSet('cola', 'http', 'cron')]
    [string] $ModoPlanificacion = 'cola',
    [string] $DonacionesUrl = 'http://localhost:8080',
    [string] $LogisticaUrl = 'http://localhost:8083',
    [string] $RabbitManagementUrl = 'http://localhost:15672',
    [string] $RabbitVhost = '/',
    [int] $RabbitPort = 5672,
    [int] $EsperaDuplicadosSegundos = 0
)

$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$D    = $DonacionesUrl.TrimEnd('/')
$L    = $LogisticaUrl.TrimEnd('/')
$MGMT = $RabbitManagementUrl.TrimEnd('/')

function Write-Paso  { param($n, $t) Write-Host "`n[$n] $t" -ForegroundColor Cyan }
function Write-Ok    { param($t) Write-Host "    OK  $t" -ForegroundColor Green }
function Write-Info  { param($t) Write-Host "    ->  $t" -ForegroundColor DarkGray }
function Write-Falla { param($t) Write-Host "    X   $t" -ForegroundColor Red }

function Test-Puerto {
    param($Puerto)
    $c = New-Object System.Net.Sockets.TcpClient
    try {
        $espera = $c.BeginConnect("127.0.0.1", $Puerto, $null, $null)
        if (-not $espera.AsyncWaitHandle.WaitOne(2000)) { return $false }
        $c.EndConnect($espera)
        return $true
    } catch {
        return $false
    } finally {
        $c.Close()
    }
}

# ====================================================================
# PREFLIGHT
# ====================================================================
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "   PRUEBA END-TO-END DONATRACK (modo: $ModoPlanificacion)" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

Write-Paso "0" "Verificando que este todo arriba"

$requeridos = @(
    @{ Nombre = "servicio-donaciones"; Puerto = ([uri]$D).Port },
    @{ Nombre = "servicio-logistica";  Puerto = ([uri]$L).Port },
    @{ Nombre = "RabbitMQ (AMQP)";     Puerto = $RabbitPort }
)
if ($ModoPlanificacion -eq 'cola') {
    $requeridos += @{ Nombre = "RabbitMQ (panel)"; Puerto = ([uri]$MGMT).Port }
}

$faltan = @()
foreach ($r in $requeridos) {
    if (Test-Puerto $r.Puerto) {
        Write-Ok "$($r.Nombre) escuchando en $($r.Puerto)"
    } else {
        Write-Falla "$($r.Nombre) NO responde en $($r.Puerto)"
        $faltan += $r.Nombre
    }
}

if ($faltan.Count -gt 0) {
    Write-Host "`nFaltan servicios: $($faltan -join ', ')" -ForegroundColor Red
    Write-Host "Levanta el entorno con:" -ForegroundColor Yellow
    Write-Host "  docker compose -f herramientas/docker-compose.yml up -d rabbitmq" -ForegroundColor Yellow
    Write-Host "  .\start-dev.ps1" -ForegroundColor Yellow
    Write-Host "`nSin RabbitMQ los servicios arrancan igual, pero NINGUN mensaje viaja" -ForegroundColor Yellow
    Write-Host "y el flujo se corta despues del paso 8." -ForegroundColor Yellow
    exit 1
}

# ====================================================================
# PASO 1 - Donante
# ====================================================================
Write-Paso "1" "Creando donante"
$donante = Invoke-RestMethod -Method Post "$D/donantes/humano" -ContentType "application/json" -Body (@{
    nombre              = "Juan"
    apellido            = "Perez"
    nroDocumento        = "30111222"
    mediosDeContacto    = @(@{ medio = "EMAIL"; valor = "juan@mail.com" })
    medioPredeterminado = @{ medio = "EMAIL"; valor = "juan@mail.com" }
} | ConvertTo-Json -Depth 5)
$donanteId = $donante.persona.id
Write-Ok "Donante id=$donanteId"

# ====================================================================
# PASO 2 - Entidad beneficiaria
# ====================================================================
# calle1 NO puede estar vacio: Direccion.getDireccion() devuelve "" y el cron
# filtra la donacion silenciosamente.
Write-Paso "2" "Creando entidad beneficiaria"
$entidad = Invoke-RestMethod -Method Post "$D/entidadBeneficiaria" -ContentType "application/json" -Body (@{
    razonSocial         = "Comedor Los Pinos"
    tipo                = "ONG"
    rubro               = "Asistencia alimentaria"
    direccion           = @{
        calle1         = "Av. Rivadavia"
        altura         = 1234
        ciudadNombre   = "CABA"
        provinciaNombre = "Buenos Aires"
        paisNombre     = "Argentina"
    }
    mediosDeContacto    = @(@{ medio = "EMAIL"; valor = "comedor@mail.com" })
    medioPredeterminado = @{ medio = "EMAIL"; valor = "comedor@mail.com" }
} | ConvertTo-Json -Depth 5)
$entidadId  = $entidad.datosDeEntidad.id
$direccion  = "Av. Rivadavia 1234"
Write-Ok "Entidad id=$entidadId (donantes y entidades comparten el contador Persona.nextId)"

# ====================================================================
# PASO 3 - Necesidad
# ====================================================================
# SubCategoria.equals() compara SOLO descripcion, case-insensitive.
# "Fideos" tiene que coincidir con la subcategoria de los bienes del paso 4.
Write-Paso "3" "Declarando necesidad de la entidad"
$necesidad = Invoke-RestMethod -Method Post "$D/api/necesidades" -ContentType "application/json" -Body (@{
    entidadBeneficiariaId = $entidadId
    subCategoriaNombre    = "Fideos"
    cantidad              = 100
    fechaLimite           = (Get-Date).AddMonths(6).ToString("yyyy-MM-dd")
    tipoNecesidad         = "EXTRAORDINARIA"
    causa                 = "Demanda sostenida"
} | ConvertTo-Json)
Write-Ok "Necesidad id=$($necesidad.id) '$($necesidad.subCategoriaNombre)' x$($necesidad.cantidadSolicitada) estado=$($necesidad.estado)"

# ====================================================================
# PASO 4 - Donacion
# ====================================================================
# Bienes con misma subcategoria + mismo vencimiento se agrupan en UN segmento.
Write-Paso "4" "Registrando donacion (3 bienes -> 1 segmento)"
$bien = @{
    tipo             = "PERECEDERO"
    nombre           = "Fideos secos"
    descripcion      = "Paquete 500g"
    subCategoria     = @{ categoria = "ALIMENTOS"; descripcion = "Fideos"; unidad = "KG" }
    fechaVencimiento = (Get-Date).AddMonths(9).ToString("yyyy-MM-dd")
}
$donacion = Invoke-RestMethod -Method Post "$D/donaciones" -ContentType "application/json" -Body (@{
    donanteId   = $donanteId
    descripcion = "Donacion de prueba automatizada"
    bienes      = @($bien, $bien, $bien)
} | ConvertTo-Json -Depth 6)
$donacionId = $donacion.id
$segId      = $donacion.donacionesSegmentadas[0].id
$cantidad   = $donacion.donacionesSegmentadas[0].bienes.Count
Write-Ok "Donacion id=$donacionId, segmento id=$segId ($cantidad bienes), estado=$($donacion.donacionesSegmentadas[0].estado)"

# ====================================================================
# PASO 5 - Matchmaking
# ====================================================================
Write-Paso "5" "Matchmaking y asignacion"
$ranking = Invoke-RestMethod "$D/matchmaking/ranking?donacionSegmentadaId=$segId"
if (-not $ranking.huboCoincidencias) {
    Write-Falla "El matchmaking no encontro coincidencias."
    Write-Falla "Revisa que la descripcion de la subcategoria coincida con subCategoriaNombre."
    exit 1
}
Write-Ok "Coincidencias: $(($ranking.coincidencias | ForEach-Object { $_.razonSocial }) -join ', ')"

Invoke-RestMethod -Method Post "$D/matchmaking/asignar" -ContentType "application/json" -Body (@{
    donacionSegmentadaId  = $segId
    entidadBeneficiariaId = $entidadId
} | ConvertTo-Json) | Out-Null
Write-Ok "Asignada -> ASIGNACION_REALIZADA"

# ====================================================================
# PASO 6 - Lista para entregar
# ====================================================================
Write-Paso "6" "Marcando lista para entregar"
if ($ModoPlanificacion -ne 'cron') { Invoke-RestMethod -Method Post "$D/trazabilidad/$donacionId/$segId/transicionar/lista_entregar?actor=Admin" | Out-Null }
if ($ModoPlanificacion -ne 'cron') { Write-Ok "-> LISTA_PARA_ENTREGAR" }

# ====================================================================
# PASO 7 - Flota
# ====================================================================
# Tiene que existir ANTES de planificar: RutaService.planificarLote lanza
# IllegalStateException si no hay camiones o choferes.
Write-Paso "7" "Registrando flota en logistica"
$camion = Invoke-RestMethod -Method Post "$L/api/logistica/camiones" -ContentType "application/json" -Body (@{
    patente = "AB123CD"; marca = "Iveco"; modelo = "Daily"
    capacidadCarga = 1000; volumen = 15; altura = 2
} | ConvertTo-Json)
$chofer = Invoke-RestMethod -Method Post "$L/api/logistica/choferes" -ContentType "application/json" -Body (@{
    nombre = "Carlos"; apellido = "Gomez"; dni = "28999111"
} | ConvertTo-Json)
Write-Ok "Camion id=$($camion.id) patente=$($camion.patente), chofer id=$($chofer.id)"

# ====================================================================
# PASO 8 - Planificacion (dos mitades: estado en donaciones, ruta en logistica)
# ====================================================================
Write-Paso "8" "Enviando a planificacion (modo: $ModoPlanificacion)"

if ($ModoPlanificacion -eq 'cron') {
    Invoke-RestMethod -Method Post "$D/trazabilidad/$donacionId/$segId/transicionar/lista_entregar?actor=Admin" | Out-Null
    Write-Info "Esperando al cron real: no se publica ni se fuerza EN_PLANIFICACION desde el script."
} else {
# 8a. Lado donaciones: el segmento pasa a EN_PLANIFICACION.
#     Sin esto, procesarInicioRuta descarta el evento (exige EN_PLANIFICACION).
Invoke-RestMethod -Method Post "$D/trazabilidad/$donacionId/$segId/transicionar/planificacion?actor=Admin" | Out-Null
Write-Ok "8a. Donaciones -> EN_PLANIFICACION"

# 8b. Lado logistica: recibe el lote y arma rutas + envios.
# PowerShell 5.1 desenvuelve arrays de un solo elemento en ConvertTo-Json,
# por eso el DTO se serializa suelto y se envuelve a mano entre corchetes.
$dtoJson  = @{
    donacionSegmentadaId         = $segId
    entidadBeneficiariaId        = $entidadId
    direccionEntidadBeneficiaria = $direccion
    cantidad                     = $cantidad
    unidad                       = "KG"
} | ConvertTo-Json -Compress
$lote = "[$dtoJson]"

if ($ModoPlanificacion -eq 'cola') {
    $publish = @{
        properties       = @{ content_type = "application/json" }
        routing_key      = "logistica.planificar.queue"
        payload          = $lote
        payload_encoding = "string"
    } | ConvertTo-Json -Depth 5 -Compress

    $tmp = Join-Path $env:TEMP "donatrack-publish.json"
    # Set-Content -Encoding UTF8 escribe BOM en PS 5.1 y el parser JSON de
    # RabbitMQ lo rechaza con {"error":"bad_request","reason":"not_json"}.
    [System.IO.File]::WriteAllText($tmp, $publish, (New-Object System.Text.UTF8Encoding($false)))

    # curl.exe en vez de Invoke-RestMethod: PS 5.1 desarma el %2F del vhost en la URL.
    $resp = & curl.exe -s -u guest:guest -H "Content-Type: application/json" `
                 -d "@$tmp" "$MGMT/api/exchanges/$([uri]::EscapeDataString($RabbitVhost))/amq.default/publish"
    Remove-Item $tmp -ErrorAction SilentlyContinue

    if ($resp -match '"routed"\s*:\s*true') {
        Write-Ok "8b. Lote publicado en logistica.planificar.queue (ruteado)"
    } else {
        Write-Falla "8b. No se pudo publicar en la cola. Respuesta: $resp"
        Write-Host "`n    Publicalo a mano en $MGMT -> Queues -> logistica.planificar.queue -> Publish message:" -ForegroundColor Yellow
        Write-Host "    $lote" -ForegroundColor Yellow
        Write-Host "    O volve a correr con: -ModoPlanificacion http" -ForegroundColor Yellow
        exit 1
    }
} else {
    Invoke-RestMethod -Method Post "$L/api/logistica/planificar" -ContentType "application/json" -Body $lote | Out-Null
    Write-Ok "8b. Lote enviado por HTTP (la cola de comando NO se ejercita en este modo)"
}

}

# La planificacion es asincronica en modo cola: esperamos a que aparezca el envio.
# Se busca el envio de NUESTRA donacion segmentada en vez de "la ultima ruta":
# el estado es en memoria y se acumula entre corridas, asi que quedarse con el
# ultimo elemento es fragil. Where-Object ademas fuerza el aplanado del array,
# que en PS 5.1 no siempre ocurre al indexar el resultado de Invoke-RestMethod.
Write-Info "Esperando que logistica arme la ruta..."
$envio = $null
for ($i = 0; $i -lt 130; $i++) {
    Start-Sleep -Milliseconds 700
    # OJO: hay que asignar a una variable ANTES de pipear. En PS 5.1
    # "Invoke-RestMethod | Where-Object" manda el array JSON como UN solo item
    # sin desenrollar; ahi $_.donacionSegmentadaId enumera todos los ids y
    # "array -eq escalar" devuelve los elementos que matchean (truthy), con lo
    # cual el filtro pasa y te entrega el array entero. Pipear una variable si enumera.
    $todosLosEnvios = Invoke-RestMethod "$L/api/logistica/envios"
    $envio = $todosLosEnvios |
             Where-Object { $_.donacionSegmentadaId -eq $segId } |
             Select-Object -First 1
    if ($null -ne $envio) { break }
}
if ($null -eq $envio) {
    Write-Falla "Logistica no genero ningun envio para la donacion segmentada $segId."
    Write-Falla "Mira los logs de servicio-logistica: probablemente 'No hay camiones o choferes'."
    exit 1
}
# El cast explicito falla ruidosamente si esto no es un escalar,
# en vez de armar una URL invalida y devolver un 500 opaco.
$envioId = [int] $envio.id
$rutaId  = [int] $envio.rutaId
Write-Ok "Ruta id=$rutaId, envio id=$envioId (donacion segmentada $segId), estado=$($envio.estado)"

if ($EsperaDuplicadosSegundos -gt 0) {
    Write-Info "Verificando ausencia de reencolado durante ${EsperaDuplicadosSegundos}s..."
    for ($segundo = 0; $segundo -lt $EsperaDuplicadosSegundos; $segundo++) {
        Start-Sleep -Seconds 1
        $enviosActuales = Invoke-RestMethod "$L/api/logistica/envios"
        $propios = @($enviosActuales | Where-Object { $_.donacionSegmentadaId -eq $segId })
        if ($propios.Count -ne 1) { throw "Se generaron $($propios.Count) envios para el mismo segmento." }
    }
    Write-Ok "Un solo envio despues de los ciclos adicionales del cron."
}

# ====================================================================
# PASO 9 - Iniciar ruta -> evento INICIO_RUTA por RabbitMQ
# ====================================================================
Write-Paso "9" "Iniciando ruta (dispara INICIO_RUTA por la cola de eventos)"
$t0 = Get-Date
Invoke-RestMethod -Method Post "$L/api/logistica/rutas/$rutaId/iniciar" | Out-Null

function Wait-Estado {
    param($EstadoEsperado, $TimeoutSeg = 15)
    for ($i = 0; $i -lt ($TimeoutSeg * 2); $i++) {
        Start-Sleep -Milliseconds 500
        $traza = Invoke-RestMethod "$D/trazabilidad/$donacionId"
        $estados = @($traza.segmentos[0].eventos | ForEach-Object { $_.estadoNuevo })
        if ($estados -contains $EstadoEsperado) { return $true }
    }
    return $false
}

if (Wait-Estado "EN_TRASLADO") {
    $ms = [int]((Get-Date) - $t0).TotalMilliseconds
    Write-Ok "Evento consumido: donaciones -> EN_TRASLADO (~${ms} ms)"
    Write-Info "Con el polling viejo esto tardaba hasta 10 segundos."
} else {
    Write-Falla "El evento INICIO_RUTA no llego a donaciones en 15s."
    Write-Falla "Revisa que RabbitMQ este arriba y mira los logs de ambos servicios."
    exit 1
}

# ====================================================================
# PASO 10 - Entrega
# ====================================================================
Write-Paso "10" "Registrando llegada y entrega"
Invoke-RestMethod -Method Post "$L/api/logistica/envios/$envioId/llegada" | Out-Null
Write-Info "LLEGADA_A_DESTINO registra trazabilidad y conserva EN_TRASLADO hasta confirmar la entrega."

Invoke-RestMethod -Method Post "$L/api/logistica/envios/$envioId/recibir" -ContentType "application/json" -Body (@{
    detalles = "Recibido conforme por el comedor"
} | ConvertTo-Json) | Out-Null

if (Wait-Estado "ENTREGADA") {
    Write-Ok "Evento consumido: donaciones -> ENTREGADA"
} else {
    Write-Falla "El evento ENTREGA_EXITOSA no llego a donaciones en 15s."
    exit 1
}

# ====================================================================
# PASO 11 - Verificacion final
# ====================================================================
Write-Paso "11" "Verificacion final"

$traza    = Invoke-RestMethod "$D/trazabilidad/$donacionId"
$estados  = @($traza.segmentos[0].eventos | ForEach-Object { $_.estadoNuevo })
$esperado = @("EN_DEPOSITO", "ASIGNACION_REALIZADA", "LISTA_PARA_ENTREGAR", "EN_PLANIFICACION", "EN_TRASLADO", "EN_TRASLADO", "ENTREGADA")

Write-Host ""
$traza.segmentos[0].eventos | Format-Table `
    @{ L = "Anterior"; E = { $_.estadoAnterior } },
    @{ L = "Nuevo";    E = { $_.estadoNuevo } },
    @{ L = "Actor";    E = { $_.actor } },
    @{ L = "Detalle";  E = { $_.descripcion } } -AutoSize

$fallas = 0

if (($estados -join ",") -eq ($esperado -join ",")) {
    Write-Ok "Secuencia de estados correcta"
} else {
    Write-Falla "Secuencia inesperada."
    Write-Falla "  esperado: $($esperado -join ' -> ')"
    Write-Falla "  obtenido: $($estados -join ' -> ')"
    $fallas++
}

# getEstado() de la donacion padre debe contemplar todos los estados nuevos
if ($traza.estado -eq "ADJUDICADA") {
    Write-Ok "Donacion padre: ADJUDICADA"
} else {
    Write-Falla "Donacion padre reporta '$($traza.estado)', se esperaba ADJUDICADA (revisar Donacion.getEstado)"
    $fallas++
}

# El comprobante se genera al recepcionar la entrega
try {
    $comprobantes = Invoke-RestMethod "$D/api/comprobantes"
    $comprobante = @($comprobantes | Where-Object { $_.donacionSegmentadaId -eq $segId })
    if ($comprobante.Count -eq 1) {
        Write-Ok "Comprobante generado: id=$($comprobante[0].id)"
    } else {
        Write-Falla "Se esperaba un comprobante para el segmento $segId, encontrados: $($comprobante.Count)"
        $fallas++
    }
} catch {
    Write-Falla "No se pudo leer el comprobante: $($_.Exception.Message)"
    $fallas++
}

Write-Host ""
if ($fallas -eq 0) {
    Write-Host "========================================================" -ForegroundColor Green
    Write-Host "  FLUJO COMPLETO OK" -ForegroundColor Green
    Write-Host "========================================================" -ForegroundColor Green
    if ($ModoPlanificacion -eq 'http') {
        Write-Host "Nota: modo 'http' no ejercito la cola de comando." -ForegroundColor Yellow
        Write-Host "Volve a correr sin parametros para probarla." -ForegroundColor Yellow
    }
    if ($ModoPlanificacion -ne 'cron') {
        Write-Host "Para probar el productor, inicia donaciones con el cron acelerado y usa -ModoPlanificacion cron." -ForegroundColor DarkGray
    }
    exit 0
} else {
    Write-Host "========================================================" -ForegroundColor Red
    Write-Host "  $fallas VERIFICACION(ES) FALLIDA(S)" -ForegroundColor Red
    Write-Host "========================================================" -ForegroundColor Red
    exit 1
}
