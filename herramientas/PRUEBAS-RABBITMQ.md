# Pruebas de logistica con RabbitMQ

## Ejecucion local

Con Docker Desktop disponible, iniciar los servicios desde la raiz. La variable
debe establecerse **antes de iniciar donaciones**, no solamente en la terminal
que ejecuta los scripts:

```powershell
$env:DONATRACK_CRON_DONACIONESLISTAS = "0 * * * * *"
.\start-dev.ps1
```

Esperar que los servicios terminen de arrancar. Si ya estan ejecutandose, no
levantar una segunda copia en los mismos puertos. Para cambiar el cron hay que
reiniciar donaciones; sus repositorios actuales son en memoria.

```powershell
.\herramientas\probar-flujo.ps1 -ModoPlanificacion cron -EsperaDuplicadosSegundos 65
.\herramientas\probar-cron-productor.ps1
```

El modo `cron` no publica manualmente ni fuerza EN_PLANIFICACION: espera al
productor real. El modo `cola` publica mediante la API de RabbitMQ y comprueba
consumidores y eventos de vuelta. El modo `http` omite la cola de planificacion.

Los scripts admiten `-DonacionesUrl` y `-LogisticaUrl` para puertos alternativos.
`probar-flujo.ps1` tambien admite `-RabbitManagementUrl` y `-RabbitVhost`.
Ambos admiten `-RabbitPort`.

## Resultado verificado el 2026-10-04

Servicios compilados del arbol de trabajo, puertos 18080/18083 y un vhost
temporal exclusivo en RabbitMQ real. Cron configurado cada cinco segundos.
No se inicio un consumidor de notificaciones en ese vhost: no se verifico
entrega de correo/SMS ni se enviaron notificaciones externas desde la prueba.

| Prueba | Resultado |
| --- | --- |
| Donante, entidad, necesidad, donacion y asignacion por HTTP | OK |
| Cron -> LogisticaQueueClient -> RabbitMQ -> PlanificacionListener -> ruta/envio | OK |
| Doce segundos adicionales con cron cada cinco segundos | Un solo envio |
| Inicio de ruta -> evento RabbitMQ -> EN_TRASLADO | OK |
| Llegada -> evento de trazabilidad EN_TRASLADO a EN_TRASLADO | OK |
| Recepcion -> evento RabbitMQ -> ENTREGADA | OK |
| Donacion padre ADJUDICADA y exactamente un comprobante | OK |
| Productor con direccion | EN_PLANIFICACION y envio creado |
| Productor sin direccion | LISTA_PARA_ENTREGAR, sin envio |
| Repeticion del flujo completo en modo cola | OK |

Los dos eventos consecutivos EN_TRASLADO son esperados: el segundo registra
la llegada sin cambiar el estado. El comprobante se verifica en
`GET /api/comprobantes`, filtrando por donacionSegmentadaId.

Las instancias y el vhost temporales se retiran al terminar. Estas pruebas
demuestran el recorrido normal y la exclusion en ciclos posteriores; no una
garantia de entrega exactamente una vez ante caidas o publicaciones ambiguas.
