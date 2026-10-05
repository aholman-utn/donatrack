# Notificaciones de Trazabilidad de Entregas

## Descripción general

Se implementaron 3 notificaciones automáticas que se disparan cuando una donación segmentada cambia de estado durante el flujo de entrega. Las notificaciones se envían tanto al **donante** como a la **entidad beneficiaria** asignada, utilizando su medio de contacto preferido (email, SMS o WhatsApp).

---

## Archivos modificados

| Archivo | Ubicación | Cambio |
|---------|-----------|--------|
| `TrazabilidadService.java` | `servicio-donaciones/src/main/java/com/tp/donatrack/services/` | Se inyectaron `NotificacionRestClient`, `DonanteRepository` y `EntidadBeneficiariaRepository`. Se agregaron métodos de notificación y el nuevo método `transicionEntregaExitosa`. |
| `TrazabilidadController.java` | `servicio-donaciones/src/main/java/com/tp/donatrack/controllers/` | Se agregó el endpoint para entrega exitosa. |

---

## Endpoints de trazabilidad (completos)

Base URL: `/trazabilidad`

| Método | Endpoint | Parámetros | Descripción | ¿Notifica? |
|--------|----------|------------|-------------|------------|
| `GET` | `/{id}` | — | Obtener trazabilidad completa de una donación (todos sus segmentos) | No |
| `GET` | `/{idDonacion}/{idSegmento}` | — | Obtener trazabilidad de un segmento específico | No |
| `POST` | `/{idDonacion}/{idSegmento}/transicionar` | Body: `CrearEventoRequest` (nuevoEstado, actor, descripcion) | Transición genérica de estado | No |
| `POST` | `/{idDonacion}/{idSegmento}/transicionar/lista_entregar` | Query: `actor` | Marcar segmento como listo para entregar | No |
| `POST` | `/{idDonacion}/{idSegmento}/transicionar/en_traslado` | Query: `actor` | Iniciar el traslado (camión sale) | **Sí** — Notifica inicio de ruta |
| `POST` | `/{idDonacion}/{idSegmento}/transicionar/entrega_exitosa` | Query: `actor` | Confirmar entrega exitosa | **Sí** — Notifica entrega exitosa |
| `POST` | `/{idDonacion}/{idSegmento}/transicionar/entrega_fallida` | Query: `actor`, `justificacion` | Registrar entrega fallida | **Sí** — Notifica entrega no satisfactoria |
| `POST` | `/{idDonacion}/{idSegmento}/transicionar/marcar_vencida` | Query: `actor` | Marcar como vencida | No |

---

## Detalle de las 3 notificaciones

### 1. Notificación de inicio de ruta (EN_TRASLADO)

**Se dispara cuando:** se llama al endpoint `/transicionar/en_traslado`

**Destinatarios y mensajes:**

- **Al donante:**
  - Asunto: `"Tu donación está en camino"`
  - Mensaje: `"Hola {nombre}, tu donación de {categoría} ya inició la ruta de entrega hacia la entidad beneficiaria. ¡Gracias por tu generosidad!"`

- **A la entidad beneficiaria:**
  - Asunto: `"Una donación está en camino hacia tu entidad"`
  - Mensaje: `"La donación de {categoría} ya se encuentra en traslado hacia tu ubicación. Preparate para recibirla."`

---

### 2. Notificación de entrega exitosa (ENTREGADA)

**Se dispara cuando:** se llama al endpoint `/transicionar/entrega_exitosa`

**Destinatarios y mensajes:**

- **Al donante:**
  - Asunto: `"¡Tu donación fue entregada con éxito!"`
  - Mensaje: `"Hola {nombre}, tu donación de {categoría} fue entregada exitosamente a la entidad beneficiaria. ¡Gracias por hacer la diferencia!"`

- **A la entidad beneficiaria:**
  - Asunto: `"Entrega recibida exitosamente"`
  - Mensaje: `"La donación de {categoría} fue entregada y confirmada en tu entidad. ¡Gracias por ser parte de la red DonaTrack!"`

---

### 3. Notificación de entrega no satisfactoria (ENTREGA_FALLIDA)

**Se dispara cuando:** se llama al endpoint `/transicionar/entrega_fallida`

**Destinatarios y mensajes:**

- **Al donante:**
  - Asunto: `"Hubo un problema con la entrega de tu donación"`
  - Mensaje: `"Hola {nombre}, lamentamos informarte que la entrega de tu donación de {categoría} no pudo completarse. Motivo: {justificación}. La donación fue devuelta al depósito y se intentará nuevamente."`

- **A la entidad beneficiaria:**
  - Asunto: `"La entrega no pudo completarse"`
  - Mensaje: `"La donación de {categoría} no pudo ser entregada. Motivo: {justificación}. Se intentará realizar una nueva entrega próximamente."`

---

## Flujo de estados de una donación segmentada

```
EN_DEPOSITO
    ↓
ASIGNACION_REALIZADA
    ↓
LISTA_PARA_ENTREGAR
    ↓
EN_TRASLADO              ← 🔔 Notificación: inicio de ruta
    ↓           ↓
ENTREGADA    ENTREGA_FALLIDA
  ↑ 🔔          ↑ 🔔
  (exitosa)     (no satisfactoria → vuelve a EN_DEPOSITO)
```

---

## Integración técnica

- Las notificaciones se envían vía HTTP al `servicio-notificaciones` usando `NotificacionRestClient` (módulo `commons`), endpoint `POST http://localhost:8082/api/notificaciones/notificar`.
- Se usa el medio de contacto preferido de cada persona (`getTipoNotificadorPreferido()` y `getContactoPredeterminado()`), definido en el mapa `medioPredeterminado` de la entidad `Persona`.
- Si no hay contacto configurado o falla el envío, se loguea el error y la transición de estado **NO** se interrumpe (fire-and-forget).
- No se necesitaron cambios en el `servicio-notificaciones` porque ya soporta recibir cualquier notificación genérica con el DTO existente.

---

## Ejemplo de uso con curl

```bash
# 1. Iniciar traslado (dispara notificación de inicio de ruta)
curl -X POST "http://localhost:8080/trazabilidad/1/1/transicionar/en_traslado?actor=Transportista"

# 2. Confirmar entrega exitosa (dispara notificación de entrega exitosa)
curl -X POST "http://localhost:8080/trazabilidad/1/1/transicionar/entrega_exitosa?actor=EntidadBeneficiaria"

# 3. Registrar entrega fallida (dispara notificación de entrega no satisfactoria)
curl -X POST "http://localhost:8080/trazabilidad/1/1/transicionar/entrega_fallida?actor=Transportista&justificacion=Dirección%20incorrecta"
```

---

## Dependencias entre servicios

```
servicio-donaciones (puerto 8080)
    │
    │  HTTP POST /api/notificaciones/notificar
    ▼
servicio-notificaciones (puerto 8082)
    │
    ├── NotificadorEmail (via EmailProvider)
    ├── NotificadorSMS (via SMSProvider)
    └── NotificadorWhatsApp (via WhatsAppProvider)
```

> **Nota:** Los providers actualmente están simulados con `System.out.println`. Cuando se integre con un servicio real (Resend, Twilio, etc.), las notificaciones se enviarán efectivamente sin cambios en esta lógica.
