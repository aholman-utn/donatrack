# Documentación UML — DonaTrack (Entrega 3)

Este directorio contiene toda la documentación UML del proyecto, generada en **PlantUML** a partir del código realmente implementado en el monorepo (módulos `servicio-donaciones`, `servicio-incentivos`, `servicio-notificaciones`, `servicio-logistica` y `commons`), más los artefactos reales de infraestructura (`Dockerfile.*`, `render.yaml`, `herramientas/docker-compose.yml`, flujos de `herramientas/Flujos n8n/`). Reflejan el estado del repositorio en el commit **`1cb5264`**.

Los diagramas siguen las convenciones UML estándar que la cátedra referencia en la **Unidad 2 — Herramientas de Concepción y Comunicación del Diseño** del programa (diagramas estáticos orientados al diseño: clases, componentes y despliegue; Modelo 4+1), sobre la base bibliográfica de Booch/Rumbaugh/Jacobson y Larman (*UML y Patrones*). Los patrones de diseño representados se corresponden con la **Unidad 3** (Diseño con Objetos y Patrones) y las decisiones de integración con la **Unidad 6 (Arquitectura)** y **Unidad 7 (Integración de Sistemas)**.

**Cada diagrama de servicio modela el módulo completo** envuelto en un **componente UML** (`servicio-XXX`), con sus capas reales como packages Java: `controllers`, `services`, `repositories`, `clients` y `tasks` (según corresponda) además del `domain` con sus sub-packages. El cableado refleja las dependencias reales del código: **controller → service → repository / domain**, y los `clients` hacia los servicios externos. El código es la fuente de verdad.

No se modeló nada que no tuviera respaldo verificable en el repositorio. Donde el código presenta una inconsistencia real (por ejemplo una URL que no coincide con el endpoint que dice invocar), se dejó documentada en la sección de *Hallazgos* de este README.

## Estructura

```
docs/uml/
├── _style.puml                     # skinparam compartido, incluido por !include en todos los diagramas
├── domain/                         # un diagrama de MÓDULO por servicio (todas las capas)
│   ├── donaciones.puml / .png
│   ├── incentivos.puml / .png
│   ├── notificaciones.puml / .png
│   └── logistica.puml / .png
├── architecture/
│   ├── componentes.puml / .png     # Diagrama de Componentes — solución completa (único)
│   └── despliegue.puml / .png      # Diagrama de Despliegue — solución completa (único)
└── casos-de-uso/
    └── general.puml / .png         # Diagrama General de Casos de Uso (único)
```

## Índice de diagramas

| # | Diagrama | Archivo | Alcance |
|---|----------|---------|---------|
| 1 | Módulo servicio-donaciones | [domain/donaciones.puml](domain/donaciones.puml) | Capas controllers (7), services (8), repositories (4), clients (1), tasks (2) + `domain` (9 sub-packages). |
| 2 | Módulo servicio-incentivos | [domain/incentivos.puml](domain/incentivos.puml) | Capas controllers, services (2), repository, clients (2) + `domain.misiones`. |
| 3 | Módulo servicio-notificaciones | [domain/notificaciones.puml](domain/notificaciones.puml) | Capas controller, service, repository + `domain.entities` y `domain.notificadores.{email,sms,whatsapp}` (sin clients). |
| 4 | Módulo servicio-logistica | [domain/logistica.puml](domain/logistica.puml) | Capas controllers (2), service, repositories (2) + `domain` y `domain.planificacion` (sin clients). |
| 5 | Diagrama de Componentes | [architecture/componentes.puml](architecture/componentes.puml) | **Único**, toda la solución: los 4 servicios + `commons`, integrados por **interfaces provistas/requeridas** (REST), más los sistemas externos reales. |
| 6 | Diagrama de Despliegue | [architecture/despliegue.puml](architecture/despliegue.puml) | **Único**, toda la solución: nodos `<<device>>`/`<<execution environment>>`, artefactos y communication paths con protocolo. **Sin actores** (vista física). |
| 7 | Diagrama General de Casos de Uso | [casos-de-uso/general.puml](casos-de-uso/general.puml) | **Único**: 4 actores y ~25 casos de uso agrupados por servicio dentro del límite del sistema. |

## Correcciones UML aplicadas

Se realizó una revisión de buenas prácticas de modelado UML por tipo de diagrama, según lo que la cátedra evalúa:

- **Diagramas de servicio → módulo completo con todas las capas**: cada diagrama por servicio se envolvió en un **componente UML** y ahora incluye, además del `domain`, los packages reales `controllers`, `services`, `repositories`, `clients` y `tasks` (según corresponda). El cableado muestra la semántica de capas: **controller → service**, **service → repository** y **service → clases de dominio**, y los `clients`/`tasks` hacia los servicios externos.
- **Dominio — packages reales**: los packages reflejan la estructura Java real (PlantUML los anida automáticamente por su nombre con puntos): `domain.persona`, `domain.notificadores.email.providers`, `domain.planificacion`, `domain.misiones`, etc., en lugar de agrupaciones conceptuales inventadas.
- **Despliegue**: se **eliminó el actor** (vista física, sin actores). Cliente como nodo `<<device>>`; contenedores como `<<execution environment>>`; `.jar` como `<<artifact>>`; conexiones como *communication paths* con protocolo (`<<HTTP>>` / `<<HTTPS>>`).
- **Componentes**: integraciones entre servicios con **interfaces provistas (lollipop ○—)** y **requeridas (dependencia `..>` «use»)**.
- **Casos de uso**: asociaciones actor–caso como **líneas simples sin punta de flecha**; **límite del sistema**; `<<include>>`/`<<extend>>` con dirección correcta.
- **Dominio — agregación/composición**: `Bien` pasó de composición a **agregación** desde `DonacionSegmentada` (no puede tener dos dueños de ciclo de vida).

## Justificación de diseño: un módulo por servicio, arquitectura y despliegue únicos

- **Un diagrama de módulo por servicio** porque cada servicio (Donaciones, Incentivos, Notificaciones, Logística) es un **bounded context independiente** (módulo Maven separado), con su propio dominio, sus propias capas y su propio ciclo de vida. La consigna pide "un diagrama de clases por servicio"; mostrar cada servicio como un módulo con sus capas hace explícita la arquitectura en capas (controllers/services/repositories) de cada uno sin mezclar dominios que en el código no se referencian por objetos sino por HTTP.

- **La arquitectura (componentes) y el despliegue en un único diagrama** porque describen la **solución distribuida como un todo**: las integraciones cruzadas, los protocolos entre nodos y los puntos de acoplamiento. La consigna lo pide así: "diagrama de componentes y diagrama de despliegue **de la solución completa**".

- **El diagrama de casos de uso es único** porque los actores interactúan con el sistema como un todo; se usan *packages* internos para trazar cada caso de uso a su servicio.

## Hallazgos documentados durante el relevamiento

Surgieron de leer el código real. No se corrigieron (fuera del alcance de esta tarea de documentación):

1. **`LogisticaRestClient` (servicio-donaciones) arma la URL sin el prefijo `/api/logistica`**: hace `POST {services.logistica.url}/planificar`, pero `LogisticaController` expone `POST /api/logistica/planificar`.
2. **El flujo N8N de ranking mensual llama a `GET /perfil/ranking`**, pero el endpoint real en `IncentivosController` es `GET /incentivos/ranking`.
3. **`servicio-incentivos` declara dependencia Maven y `scanBasePackages` sobre `servicio-notificaciones` y `servicio-donaciones`** además de `commons`, embebiendo beans de Notificaciones en su proceso, aunque en runtime la comunicación observada es HTTP hacia el proceso independiente (puerto 8082).
4. **`servicio-logistica` no tiene `Dockerfile` propio ni entrada en `render.yaml`**. Se documentó como proceso Java sobre la JVM local.
5. **Acoplamiento por configuración a `localhost`** en las URLs de integración; en Render requieren sobreescribirse por entorno.
6. **Ningún servicio tiene persistencia real** (`spring.datasource.*` ausente): colecciones en memoria.
7. **`servicio-logistica/pom.xml` declara `spring-boot-starter-data-jpa`** sin usarlo (no hay `@Entity`).

## Cómo se renderizaron los diagramas

```bash
# PlantUML 1.2026.6 (jar oficial: https://github.com/plantuml/plantuml/releases)
java -DPLANTUML_LIMIT_SIZE=32768 -jar plantuml.jar docs/uml/domain/*.puml
java -DPLANTUML_LIMIT_SIZE=16384 -jar plantuml.jar docs/uml/architecture/*.puml
java -DPLANTUML_LIMIT_SIZE=16384 -jar plantuml.jar docs/uml/casos-de-uso/*.puml
```

> **Nota:** el flag `-DPLANTUML_LIMIT_SIZE` (16384 / 32768) es necesario porque el módulo de Donaciones y el de componentes superan el límite por defecto de PlantUML (4096 px), lo que generaría un PNG recortado sin avisar.

### Alternativas de render
- **Docker:** `docker run --rm -v "${PWD}:/data" plantuml/plantuml -tpng /data/docs/uml/domain/*.puml`
- **VS Code:** extensión "PlantUML" (jebbs.plantuml), `Alt+D`.
- **Online:** https://www.plantuml.com/plantuml/uml/ (reemplazando el `!include ../_style.puml` inline).

---

## 1. Estilo compartido — `_style.puml`

```plantuml
' =========================================================
' Estilo compartido para todos los diagramas UML de DonaTrack
' Incluir con: !include ../_style.puml  (desde domain/, architecture/, casos-de-uso/)
' =========================================================

skinparam backgroundColor #FEFEFE
skinparam shadowing false
skinparam roundCorner 8
skinparam defaultFontName Helvetica
skinparam defaultFontSize 13
skinparam arrowThickness 1.2

skinparam titleFontSize 20
skinparam titleFontStyle bold

' ---- Clases (diagramas de dominio) ----
skinparam class {
    BackgroundColor #EFF6FF
    BorderColor #2B5B84
    ArrowColor #2B5B84
    FontColor #10222E
    AttributeFontColor #10222E
    AttributeFontSize 12
    StereotypeFontColor #2B5B84
    HeaderBackgroundColor #D6E7F7
}

skinparam interface {
    BackgroundColor #FFF3E0
    BorderColor #B9700A
    FontColor #4A2E00
}

skinparam enum {
    BackgroundColor #F1F4E8
    BorderColor #5B7A2B
    FontColor #2E3B14
}

skinparam package {
    BackgroundColor #FBFBFB
    BorderColor #8A8A8A
    FontColor #333333
    FontStyle bold
}

skinparam note {
    BackgroundColor #FFFDE7
    BorderColor #C9B458
    FontColor #4A4326
}

' ---- Componentes / despliegue (arquitectura) ----
skinparam component {
    BackgroundColor #EFF6FF
    BorderColor #2B5B84
    ArrowColor #2B5B84
    FontColor #10222E
}

skinparam database {
    BackgroundColor #F1F4E8
    BorderColor #5B7A2B
    FontColor #2E3B14
}

skinparam node {
    BackgroundColor #FBFBFB
    BorderColor #555555
    FontColor #222222
}

skinparam cloud {
    BackgroundColor #FFF3E0
    BorderColor #B9700A
    FontColor #4A2E00
}

skinparam queue {
    BackgroundColor #FDE8EC
    BorderColor #A03A52
    FontColor #4A1220
}

' ---- Casos de uso ----
skinparam usecase {
    BackgroundColor #EFF6FF
    BorderColor #2B5B84
    ArrowColor #2B5B84
    FontColor #10222E
}

skinparam actor {
    BackgroundColor #FFF3E0
    BorderColor #B9700A
    FontColor #10222E
}

skinparam linetype ortho
skinparam nodesep 45
skinparam ranksep 55
```

---

## 2. Módulo servicio-donaciones

![Módulo Donaciones](domain/donaciones.png)

El módulo completo con sus capas reales: `controllers` (7), `services` (8, incluye el `HttpDonacionEventPublisher` como Adapter y `LogisticaPollingTask` como Scheduled), `repositories` (4), `clients` (`LogisticaRestClient`), `tasks` (2 crons) y `domain` (9 sub-packages). Cableado: cada controller usa su service; los services usan sus repositories y las raíces de agregado del dominio; el `HttpDonacionEventPublisher` implementa el puerto `DonacionEventPublisher` y sale hacia Incentivos; los clients/tasks salen hacia Logística y (vía `commons.NotificacionRestClient`) hacia Notificaciones. Patrones: **Strategy** (`AlgoritmoAsignacion`), **Facade** (`ServicioMatchmaking`), **Template Method** (`Bien`), **State** (`DonacionSegmentada`) y **puerto/Observer** (`DonacionEventPublisher`).

```plantuml
@startuml donaciones
!include ../_style.puml

title Módulo servicio-donaciones\n(capas controllers / services / repositories / clients / tasks / domain, según el código)

skinparam componentStyle uml2

component "servicio-donaciones" as Modulo {

    package "controllers" {
        class DonanteController <<Controller>>
        class DonacionController <<Controller>>
        class DonacionSegmentadaController <<Controller>>
        class EndidadBeneficiariaController <<Controller>>
        class NecesidadController <<Controller>>
        class MatchmakingController <<Controller>>
        class TrazabilidadController <<Controller>>
    }

    package "services" {
        class DonanteService <<Service>>
        class DonacionService <<Service>>
        class EntidadBeneficiariaService <<Service>>
        class NecesidadService <<Service>>
        class MatchmakingService <<Service>>
        class TrazabilidadService <<Service>>
        class HttpDonacionEventPublisher <<Adapter>>
        class LogisticaPollingTask <<Scheduled>>
    }

    package "repositories" {
        class DonanteRepository <<Repository>>
        class DonacionRepository <<Repository>>
        class EntidadBeneficiariaRepository <<Repository>>
        class NecesidadRepository <<Repository>>
    }

    package "clients" {
        class LogisticaRestClient <<Client>>
    }

    package "tasks" {
        class DonantesInactivosCron <<Scheduled>>
        class DonacionesListasParaEntregarCron <<Scheduled>>
    }

    package "domain.persona" {
        abstract class Persona {
            -id: Long
            -medioDeContacto: Map
            -fechaUltimaInteraccion: LocalDateTime
            --
            +getTipoNotificadorPreferido(): TipoNotificador
        }
        class PersonaHumana extends Persona
        class PersonaJuridica extends Persona {
            -razonSocial: String
            +agregarRepresentante(r)
        }
        class PersonaRepresentante
        enum TipoOrganizacion
        enum TipoPersona
    }

    package "domain.donante" {
        class Donante {
            -password: String
            +getNombreCompleto(): String
        }
        class PerfilDonante {
            -nivelDonante: Nivel
            +registrarEntrega(segmentada)
            +calcularRachaMeses(): int
            +contarCategoriasUnicas(): int
        }
        class Metrica
        class ItemHistoralDonaciones
        class EntidadAyudada
    }

    package "domain.bien" {
        abstract class Bien {
            #subCategoria: SubCategoria
            +{abstract} getCriterioSegmentacion(): Object
            +getClaveAgrupacion(): ClaveAgrupacion
        }
        class BienDuradero extends Bien
        class BienPerecedero extends Bien
        enum EstadoBien
        enum CategoriaBien
        class SubCategoria
        class ClaveAgrupacion <<record>>
        note top of Bien : **Template Method** sobre getCriterioSegmentacion()
    }

    package "domain.donacion" {
        class Donacion {
            -segmentar(bienes): List<DonacionSegmentada>
            +getEstado(): EstadoDonacion
        }
        enum EstadoDonacion
        class DonacionSegmentada {
            -estado: EstadoDonacionSegmentada
            +transicionar(nuevoEstado, actor, desc)
            +asignar(entidad, actor)
            +confirmarEntrega(id)
            +registrarEntregaFallida(actor, just)
            +transicionPosible(ant, nuevo): boolean
        }
        enum EstadoDonacionSegmentada
        interface DonacionEventPublisher {
            +publicar(event)
        }
        class DonacionEntregadaEvent <<record>>
        note bottom of EstadoDonacionSegmentada : **State**: máquina de estados en transicionPosible()
        note bottom of DonacionEventPublisher : **Puerto de dominio** (Observer desacoplado)
    }

    package "domain.trazabilidad" {
        class EventoTrazabilidad {
            -estadoAnterior: EstadoDonacionSegmentada
            -estadoNuevo: EstadoDonacionSegmentada
            -fecha: LocalDateTime
            -actor: String
        }
    }

    package "domain.necesidad" {
        abstract class NecesidadMaterial {
            -cantidadObjetivo: int
            -cantidadRecibida: int
            +activo(): boolean
            +recibirDonacion(donacion)
        }
        class NecesidadRecurrente extends NecesidadMaterial {
            +enPeriodo(): boolean
        }
        class NecesidadExtraordinaria extends NecesidadMaterial
        enum EstadoNecesidad
    }

    package "domain.entidad" {
        class EntidadBeneficiaria {
            +agregarNecesidad(n)
            +implementarDonacion(donacion)
        }
    }

    package "domain.asignacion" {
        interface AlgoritmoAsignacion {
            +rankear(donacion, entidades): List<EntidadBeneficiaria>
        }
        class AlgoritmoCompatibilidadSemantica
        class AlgoritmoPrioridadSubAtendidos
        class ServicioMatchmaking {
            +ejecutar(donacion, entidades): ResultadoMatchmaking
        }
        class ResultadoMatchmaking
        note bottom of AlgoritmoAsignacion : **Strategy** (2 criterios)
        note bottom of ServicioMatchmaking : **Facade** que orquesta ambas estrategias
    }

    package "domain.ubicacion" {
        class Direccion {
            +getDireccion(): String
        }
        class Ciudad
        class Provincia
        class Pais
    }
}

package "commons (módulo compartido)" #F7F7F7 {
    enum Nivel
    enum TipoNotificador
    enum Unidad
    class NotificacionRestClient <<Client>>
    class DonacionSegmentadaListaParaEntregarALogisticaDTO <<record>>
}

package "Sistemas externos" #EEEEEE {
    class "servicio-incentivos" as ExtInc <<external>>
    class "servicio-logistica" as ExtLog <<external>>
    class "servicio-notificaciones" as ExtNot <<external>>
}

' ===================== Controllers -> Services =====================
DonanteController ..> DonanteService
DonacionSegmentadaController ..> DonanteService
DonacionController ..> DonacionService
EndidadBeneficiariaController ..> EntidadBeneficiariaService
NecesidadController ..> NecesidadService
MatchmakingController ..> MatchmakingService
TrazabilidadController ..> TrazabilidadService

' ===================== Services -> Repositories =====================
DonanteService ..> DonanteRepository
DonanteService ..> DonacionRepository
DonacionService ..> DonacionRepository
EntidadBeneficiariaService ..> EntidadBeneficiariaRepository
NecesidadService ..> NecesidadRepository
NecesidadService ..> EntidadBeneficiariaRepository
MatchmakingService ..> DonacionRepository
MatchmakingService ..> EntidadBeneficiariaRepository
TrazabilidadService ..> DonacionRepository
TrazabilidadService ..> DonanteRepository
TrazabilidadService ..> EntidadBeneficiariaRepository
HttpDonacionEventPublisher ..> DonanteRepository

' ===================== Services -> Services =====================
DonacionService ..> DonanteService
DonacionService ..> EntidadBeneficiariaService
DonacionService ..> DonacionEventPublisher : publica evento >
LogisticaPollingTask ..> DonacionService
LogisticaPollingTask ..> TrazabilidadService
LogisticaPollingTask ..> DonacionRepository
DonacionEventPublisher <|.. HttpDonacionEventPublisher

' ===================== Services -> Dominio (raíces de agregado) =====================
DonanteService ..> Donante
DonacionService ..> Donacion
EntidadBeneficiariaService ..> EntidadBeneficiaria
NecesidadService ..> NecesidadMaterial
MatchmakingService ..> ServicioMatchmaking
TrazabilidadService ..> DonacionSegmentada

' ===================== Tasks -> clients / services =====================
DonacionesListasParaEntregarCron ..> LogisticaRestClient
DonacionesListasParaEntregarCron ..> DonacionRepository
DonantesInactivosCron ..> DonanteService

' ===================== Uso de commons y sistemas externos =====================
DonanteService ..> NotificacionRestClient
EntidadBeneficiariaService ..> NotificacionRestClient
TrazabilidadService ..> NotificacionRestClient
DonantesInactivosCron ..> NotificacionRestClient
NotificacionRestClient ..> ExtNot : HTTP POST /notificar
HttpDonacionEventPublisher ..> ExtInc : HTTP POST /entrega
LogisticaRestClient ..> ExtLog : HTTP POST /planificar
LogisticaPollingTask ..> ExtLog : HTTP GET /rutas/eventos (polling)
LogisticaRestClient ..> DonacionSegmentadaListaParaEntregarALogisticaDTO

' ===================== Relaciones internas del dominio (resumen) =====================
Persona <|-- PersonaHumana
Persona <|-- PersonaJuridica
Donante "1" *-- "1" Persona
Donante "1" *-- "1" PerfilDonante
PerfilDonante --> Nivel
Bien <|-- BienDuradero
Bien <|-- BienPerecedero
Bien --> SubCategoria
SubCategoria --> Unidad
Donacion "1" *-- "1..*" DonacionSegmentada
Donacion "1" o-- "1" Donante
DonacionSegmentada --> EstadoDonacionSegmentada
DonacionSegmentada "1" *-- "0..*" EventoTrazabilidad
DonacionSegmentada ..> EntidadBeneficiaria : asigna >
NecesidadMaterial <|-- NecesidadRecurrente
NecesidadMaterial <|-- NecesidadExtraordinaria
EntidadBeneficiaria "1" *-- "1" PersonaJuridica
EntidadBeneficiaria "1" *-- "0..*" NecesidadMaterial
AlgoritmoAsignacion <|.. AlgoritmoCompatibilidadSemantica
AlgoritmoAsignacion <|.. AlgoritmoPrioridadSubAtendidos
ServicioMatchmaking "1" o-- "2" AlgoritmoAsignacion
Persona "1" *-- "1" Direccion

legend right
    |= Color / estereotipo |= Significado |
    | <back:#EFF6FF>     </back> | Clase de dominio |
    | <<Controller>> | Capa de exposición REST |
    | <<Service>> | Capa de aplicación / negocio |
    | <<Repository>> | Persistencia en memoria |
    | <<Client>> | Cliente HTTP saliente |
    | <<Scheduled>> | Tarea programada (@Scheduled) |
    | <<Adapter>> | Adaptador evento de dominio → HTTP |
    | <back:#FFF3E0>     </back> | Interfaz (Strategy / puerto) |
    | <back:#F1F4E8>     </back> | Enum de dominio |
    | <back:#F7F7F7>     </back> | Módulo `commons` |
    | <<external>> | Servicio externo al módulo |
endlegend

@enduml
```

---

## 3. Módulo servicio-incentivos

![Módulo Incentivos](domain/incentivos.png)

Capas `controllers` (IncentivosController), `services` (IncentivosService, ServicioRanking), `repositories` (MisionRepository), `clients` (DonacionesRestClient, InsigniasRestClient) y `domain.misiones`. Cableado: el controller usa ambos services; `IncentivosService` consulta el repositorio de misiones, evalúa el cumplimiento sobre `Mision` (Strategy/Template Method) y sale hacia Donaciones, N8N y Notificaciones vía sus clients; `ServicioRanking` lee donantes de Donaciones.

```plantuml
@startuml incentivos
!include ../_style.puml

title Módulo servicio-incentivos\n(capas controllers / services / repositories / clients / domain, según el código)

skinparam componentStyle uml2

component "servicio-incentivos" as Modulo {

    package "controllers" {
        class IncentivosController <<Controller>> {
            +procesarEntrega(dto): EvaluacionMisionResponseDTO
            +obtenerRanking(): List<RankingItemDTO>
        }
    }

    package "services" {
        class IncentivosService <<Service>> {
            +procesarNuevaEntrega(dto): EvaluacionMisionResponseDTO
        }
        class ServicioRanking <<Service>> {
            +obtenerRankingCompleto(): List<RankingItemDTO>
        }
    }

    package "repositories" {
        class MisionRepository <<Repository>> {
            +findById(id): Mision
            +findSiguiente(misionActualId): Mision
        }
    }

    package "clients" {
        class DonacionesRestClient <<Client>> {
            +obtenerIndicadores(...): IndicadoresDonanteDTO
            +obtenerTodosDonantes(): List
            +obtenerDatosParaNotificar(id): NotificacionRequestDTO
        }
        class InsigniasRestClient <<Client>> {
            +notificarInsigniaObtenida(user, mision, desc)
        }
    }

    package "domain.misiones" {
        abstract class Mision {
            -id: Long
            -objetivo: int
            -nivel: Nivel
            -titulo: String
            -orden: int
            --
            +tieneSiguiente(): boolean
            +{abstract} calcularNuevoProgreso(entrega, indicadores): double
            +{abstract} estaCumplida(entrega, indicadores): boolean
        }
        class MisionRacha extends Mision
        class MisionCompletitud extends Mision
        class MisionHabilDonador extends Mision
        class MisionDonacionesExitosas extends Mision {
            +tieneSiguiente(): boolean
        }

        note top of Mision
            **Strategy / Template Method por herencia**: cada subclase
            define su regla de progreso y de cumplimiento; el servicio
            opera polimórficamente sobre la "misión actual" sin
            switch/instanceof.
        end note
    }
}

package "commons (módulo compartido)" #F7F7F7 {
    class Insignia {
        -titulo: String
        -descripcion: String
        -fechaObtencion: Date
    }
    enum Nivel {
        COLABORADOR
        SOSTENEDOR
        TRANSFORMADOR
    }
    class NotificacionRestClient <<Client>>
}

package "Sistemas externos" #EEEEEE {
    class "servicio-donaciones" as ExtDon <<external>>
    class "N8N — webhook insignias" as ExtN8N <<external>>
    class "servicio-notificaciones" as ExtNot <<external>>
}

' ===================== Cableado entre capas =====================
IncentivosController ..> IncentivosService : usa >
IncentivosController ..> ServicioRanking : usa >
IncentivosService ..> MisionRepository : consulta misiones >
IncentivosService ..> DonacionesRestClient : pide indicadores >
IncentivosService ..> InsigniasRestClient : notifica insignia >
IncentivosService ..> NotificacionRestClient : despacha notificación >
IncentivosService ..> Mision : evalúa cumplimiento >
ServicioRanking ..> DonacionesRestClient : lee donantes >
MisionRepository ..> Mision : cataloga >

' ===================== Dominio =====================
Mision "1" *-- "1" Insignia : insigniaAsociada
Mision --> Nivel : nivel

' ===================== Clients -> sistemas externos =====================
DonacionesRestClient ..> ExtDon : HTTP GET
InsigniasRestClient ..> ExtN8N : HTTP POST webhook
NotificacionRestClient ..> ExtNot : HTTP POST /notificar

legend right
    |= Color / estereotipo |= Significado |
    | <back:#EFF6FF>     </back> | Clase de dominio |
    | <<Controller>> | Capa de exposición REST |
    | <<Service>> | Capa de aplicación / negocio |
    | <<Repository>> | Persistencia en memoria |
    | <<Client>> | Cliente HTTP saliente (RestTemplate) |
    | <back:#F1F4E8>     </back> | Enum de dominio |
    | <back:#F7F7F7>     </back> | Módulo `commons` |
    | <<external>> | Servicio externo al módulo |
endlegend

@enduml
```

---

## 4. Módulo servicio-notificaciones

![Módulo Notificaciones](domain/notificaciones.png)

Capas `controllers` (NotificacionController), `services` (NotificacionService), `repositories` (NotificacionRepository) y `domain` (`domain.entities` + `domain.notificadores.{email,sms,whatsapp}` con el sub-package `email.providers`). Sin clients (es el destino de las notificaciones). Cableado: el controller usa el service; el service persiste en el repositorio y selecciona el `iNotificador` cuyo `getMedio()` coincide. **Strategy de dos niveles / Bridge**: medio de envío (nivel 1) y proveedor concreto (nivel 2).

```plantuml
@startuml notificaciones
!include ../_style.puml

title Módulo servicio-notificaciones\n(capas controllers / services / repositories / domain, según el código)

skinparam componentStyle uml2

component "servicio-notificaciones" as Modulo {

    package "controllers" {
        class NotificacionController <<Controller>> {
            +notificar(body: NotificacionRequestDTO)
            +listarTodas()
            +listarPorPersona(idPersona: Long)
        }
    }

    package "services" {
        class NotificacionService <<Service>> {
            +notificar(body: NotificacionRequestDTO)
            +buscar(idPersona: Long): List<Notificacion>
            +buscarTodas(): List<Notificacion>
            -seleccionarNotificador(medio): Optional<iNotificador>
        }
    }

    package "repositories" {
        class NotificacionRepository <<Repository>> {
            +save(n: Notificacion): Notificacion
            +findByIdPersona(id: Long): List<Notificacion>
            +findAll(): List<Notificacion>
        }
    }

    package "domain.entities" {
        class Notificacion {
            -id: Long
            -id_persona: Long
            -asunto: String
            -mensaje: String
            -destinatario: String
            -fecha: LocalDateTime
        }

        interface iNotificador {
            +enviarNotificacion(destinatario, mensaje, asunto)
            +getMedio(): TipoNotificador
        }
    }

    package "domain.notificadores.email" {
        interface iEmailProvider {
            +enviarEmail(destinatario, mensaje, asunto)
        }
        class NotificadorEmail {
            +getMedio(): TipoNotificador
        }
        class EmailProvider
        package "providers" {
            class Resend
        }
    }

    package "domain.notificadores.sms" {
        interface iSMSProvider {
            +enviarSMS(numero, mensaje)
        }
        class NotificadorSMS {
            +getMedio(): TipoNotificador
        }
        class SMSProvider
    }

    package "domain.notificadores.whatsapp" {
        interface iWhatsAppProvider {
            +enviarWhatsApp(numero, mensaje)
        }
        class NotificadorWhatsApp {
            +getMedio(): TipoNotificador
        }
        class WhatsAppProvider
    }
}

package "commons (módulo compartido)" #F7F7F7 {
    enum TipoNotificador {
        EMAIL
        SMS
        WHATSAPP
    }
    class NotificacionRequestDTO <<DTO>>
}

' ===================== Cableado entre capas =====================
NotificacionController ..> NotificacionService : usa >
NotificacionService ..> NotificacionRepository : persiste >
NotificacionService ..> iNotificador : selecciona por medio >
NotificacionController ..> NotificacionRequestDTO
NotificacionService ..> Notificacion : crea >

' ===================== Realizaciones de dominio (Strategy) =====================
iNotificador <|.. NotificadorEmail
iNotificador <|.. NotificadorSMS
iNotificador <|.. NotificadorWhatsApp
iEmailProvider <|.. EmailProvider
iEmailProvider <|.. Resend
iSMSProvider <|.. SMSProvider
iWhatsAppProvider <|.. WhatsAppProvider
NotificadorEmail "1" o-- "1" iEmailProvider : emailProvider
NotificadorSMS "1" o-- "1" iSMSProvider : smsProvider
NotificadorWhatsApp "1" o-- "1" iWhatsAppProvider : whatsappProvider
iNotificador ..> TipoNotificador

note as N1
    **Strategy de dos niveles**: NotificacionService selecciona el
    iNotificador cuyo getMedio() coincide con el TipoNotificador pedido
    (nivel 1); cada Notificador* delega el envío en un proveedor concreto
    inyectado por composición (nivel 2). Los 3 proveedores activos son
    simulaciones; Resend es una impl. alternativa no wireada (sin @Component).
end note

legend right
    |= Color / estereotipo |= Significado |
    | <back:#EFF6FF>     </back> | Clase de dominio |
    | <<Controller>> | Capa de exposición REST |
    | <<Service>> | Capa de aplicación / negocio |
    | <<Repository>> | Persistencia en memoria |
    | <back:#FFF3E0>     </back> | Interfaz (Strategy) |
    | <back:#F1F4E8>     </back> | Enum de dominio |
    | <back:#F7F7F7>     </back> | Módulo `commons` |
endlegend

@enduml
```

---

## 5. Módulo servicio-logistica

![Módulo Logística](domain/logistica.png)

Capas `controllers` (LogisticaController, LogisticaEventController), `services` (LogisticaService), `repositories` (RutaRepository, LogisticaEventRepository) y `domain` (+ `domain.planificacion`). Sin clients (servicio pasivo por consigna). Cableado: `LogisticaController` usa el service; `LogisticaEventController` lee directamente el repositorio de eventos (es lo que consume Donaciones por polling); el service persiste rutas/eventos y delega la planificación en la Strategy `Planificacion`/`CapacidadFisica`.

```plantuml
@startuml logistica
!include ../_style.puml

title Módulo servicio-logistica\n(capas controllers / services / repositories / domain, según el código)

skinparam componentStyle uml2

component "servicio-logistica" as Modulo {

    package "controllers" {
        class LogisticaController <<Controller>> {
            +registrarCamion() / listarCamiones()
            +registrarChofer() / listarChoferes()
            +registrarEnvio() / listarEnvios()
            +planificarRutas(lote)
            +registrarRuta() / listarRutas()
            +iniciarRuta(id)
            +entregarEnvio(id) / fallarEnvio(id)
        }
        class LogisticaEventController <<Controller>> {
            +obtenerEventos(): List<EventoLogistica>
        }
    }

    package "services" {
        class LogisticaService <<Service>> {
            +planificarLote(lote)
            +registrarCamion() / registrarChofer()
            +registrarEnvio() / registrarRuta()
            +iniciarRuta(id)
            +registrarEntregaExitosa(id, detalles)
            +registrarEntregaFallida(id, motivo)
        }
    }

    package "repositories" {
        class RutaRepository <<Repository>> {
            +save(r: Ruta) / saveAll()
            +findById() / findAll() / delete()
        }
        class LogisticaEventRepository <<Repository>> {
            +registrar(e: EventoLogistica)
            +obtenerTodos(): List<EventoLogistica>
        }
    }

    package "domain" {
        class Camion {
            -id: Long
            -patente: String
            -volumen: double
            -altura: double
            -capacidadCarga: double
        }
        class Chofer {
            -id: Long
            -nombre: String
            -apellido: String
            -dni: String
        }
        class Ruta {
            -id: Long
            -iniciada: Boolean
            --
            +iniciarRuta()
        }
        class Parada {
            -orden: Integer
            -direccion: String
            -enviosIds: List<Long>
        }
        class Envio {
            -id: Long
            -donacionSegmentadaId: Long
            -entidadBeneficiariaId: Long
            -estado: EstadoEnvio
            --
            +registrarRecepcionExitosa()
            +registrarRecepcionFallida()
        }
        enum EstadoEnvio {
            PENDIENTE
            ASIGNACION_REALIZADA
            EN_TRASLADO
            ENTREGADA
            NO_RECIBIDA
        }
        class EventoLogistica {
            -tipoEvento: String
            -donacionSegmentadaId: Long
            -entidadBeneficiariaId: Long
            -timestamp: LocalDateTime
            -detalles: String
        }

        note top of Envio
            **State**: las transiciones válidas se validan dentro de la
            propia entidad (IllegalStateException si el envío no está
            EN_TRASLADO o ASIGNACION_REALIZADA).
        end note

        package "planificacion" {
            interface Planificacion {
                +planificar(donaciones, camiones, choferes): List<Ruta>
            }
            class CapacidadFisica {
                +planificar(donaciones, camiones, choferes): List<Ruta>
            }
            class CalculadorDimensiones {
                +calcular(cantidad, unidad: Unidad): DimensionesFisicas
            }
            class DimensionesFisicas <<record>> {
                +pesoKg: double
                +volumenM3: double
            }
            note bottom of CapacidadFisica
                **Strategy**: única implementación activa; agrupa
                donaciones por capacidad de peso/volumen y las
                paradas por dirección de la entidad beneficiaria.
            end note
        }
    }
}

package "commons (módulo compartido)" #F7F7F7 {
    class DonacionSegmentadaListaParaEntregarALogisticaDTO <<record>> {
        +donacionSegmentadaId: Long
        +entidadBeneficiariaId: Long
        +direccionEntidadBeneficiaria: String
        +cantidad: int
        +unidad: Unidad
    }
    enum Unidad {
        UNIDADES
        KG
        METROS
        LITROS
    }
}

' ===================== Cableado entre capas =====================
LogisticaController ..> LogisticaService : usa >
LogisticaEventController ..> LogisticaEventRepository : lee eventos >
LogisticaService ..> RutaRepository : persiste >
LogisticaService ..> LogisticaEventRepository : registra eventos >
LogisticaService ..> Planificacion : delega planificación >
LogisticaService ..> Camion
LogisticaService ..> Chofer
LogisticaService ..> Envio
LogisticaService ..> Ruta

' ===================== Relaciones de dominio =====================
Ruta "1" o-- "1" Camion : camion
Ruta "1" o-- "1" Chofer : chofer
Ruta "1" *-- "0..*" Parada : paradas
Envio --> EstadoEnvio : estado
Parada "1" o-- "1..*" Envio : envíos (por ID)
Planificacion <|.. CapacidadFisica
CapacidadFisica "1" --> "1" CalculadorDimensiones
CalculadorDimensiones ..> DimensionesFisicas : crea >
CapacidadFisica ..> Ruta : construye >

' ===================== commons =====================
LogisticaController ..> DonacionSegmentadaListaParaEntregarALogisticaDTO : recibe lote >
Planificacion ..> DonacionSegmentadaListaParaEntregarALogisticaDTO
CalculadorDimensiones --> Unidad

note as N2
    **Alcance acotado por consigna**: servicio-logistica no invoca a
    Donaciones, Incentivos ni Notificaciones (no tiene package clients).
    Solo recibe el lote a planificar (POST /planificar) y expone sus
    eventos para que Donaciones los consulte por polling — ver componentes.puml.
end note

legend right
    |= Color / estereotipo |= Significado |
    | <back:#EFF6FF>     </back> | Clase de dominio |
    | <<Controller>> | Capa de exposición REST |
    | <<Service>> | Capa de aplicación / negocio |
    | <<Repository>> | Persistencia en memoria |
    | <back:#FFF3E0>     </back> | Interfaz (Strategy) |
    | <back:#F1F4E8>     </back> | Enum de dominio |
    | <back:#F7F7F7>     </back> | Módulo `commons` |
endlegend

@enduml
```

---

## 6. Diagrama de Componentes (único, solución completa)

![Componentes](architecture/componentes.png)

Cada servicio se representa como un `package` con sus componentes internos estereotipados. Las integraciones entre servicios se modelan con **interfaces provistas (lollipop ○—)** y **requeridas (dependencia `..>` «use»)**. Se incluyen los sistemas externos reales (N8N, Browserless/Chrome, LinkedIn, Google Sheets).

```plantuml
@startuml componentes
!include ../_style.puml

title Diagrama de Componentes — DonaTrack (solución completa)

' =====================================================================
' Diagrama de componentes UML. Cada servicio es un package con sus
' componentes internos estereotipados. La integración entre servicios
' se modela con interfaces PROVISTAS (lollipop) y REQUERIDAS (dependencia
' «use»), no con flechas directas entre componentes internos.
' =====================================================================

skinparam componentStyle uml2

component "Cliente\n(Navegador / Postman / Swagger UI)" <<external>> as Cliente

' ============================ Interfaces (contratos REST) ============================
interface "API Donaciones" as IApiDon
interface "API Incentivos" as IApiInc
interface "API Notificaciones" as IApiNot
interface "API Logística" as IApiLog
interface "Webhook Insignias" as IWebhook

' ============================ Servicio de Donaciones ============================
package "Servicio de Donaciones\n«Spring Boot :8080»" {
    component "Controllers REST\n(Donante, Donacion, Entidad,\nNecesidad, Matchmaking, Trazabilidad)" <<Controller>> as Don_Ctrl
    component "Services de negocio" <<Service>> as Don_Svc
    component "Repositorios en memoria" <<Repository>> as Don_Repo
    component "Tareas programadas\n(crons + polling logística)" <<Scheduled>> as Don_Task
    component "LogisticaRestClient" <<Client>> as Don_CliLog
    component "HttpDonacionEventPublisher" <<Adapter>> as Don_Pub

    Don_Ctrl --> Don_Svc
    Don_Svc --> Don_Repo
    Don_Svc --> Don_Pub
    Don_Task --> Don_Repo
    Don_Task --> Don_CliLog
}
Don_Ctrl -up- IApiDon

' ============================ Servicio de Incentivos ============================
package "Servicio de Incentivos\n«Spring Boot :8081»" {
    component "IncentivosController" <<Controller>> as Inc_Ctrl
    component "IncentivosService\nServicioRanking" <<Service>> as Inc_Svc
    component "MisionRepository\n(catálogo en memoria)" <<Repository>> as Inc_Repo
    component "DonacionesRestClient" <<Client>> as Inc_CliDon
    component "InsigniasRestClient" <<Client>> as Inc_CliN8N

    Inc_Ctrl --> Inc_Svc
    Inc_Svc --> Inc_Repo
    Inc_Svc --> Inc_CliDon
    Inc_Svc --> Inc_CliN8N
}
Inc_Ctrl -up- IApiInc

' ============================ Servicio de Notificaciones ============================
package "Servicio de Notificaciones\n«Spring Boot :8082»" {
    component "NotificacionController" <<Controller>> as Not_Ctrl
    component "NotificacionService" <<Service>> as Not_Svc
    component "NotificacionRepository\n(en memoria)" <<Repository>> as Not_Repo
    component "Notificadores\n(Email / SMS / WhatsApp — Strategy)" <<Strategy>> as Not_Strat

    Not_Ctrl --> Not_Svc
    Not_Svc --> Not_Repo
    Not_Svc --> Not_Strat
}
Not_Ctrl -up- IApiNot

' ============================ Servicio de Logística ============================
package "Servicio de Logística\n«Spring Boot :8083»" {
    component "LogisticaController\nLogisticaEventController" <<Controller>> as Log_Ctrl
    component "LogisticaService" <<Service>> as Log_Svc
    component "Planificador\n(CapacidadFisica — Strategy)" <<Strategy>> as Log_Strat
    component "Repositorios en memoria\n(Rutas, Eventos)" <<Repository>> as Log_Repo

    Log_Ctrl --> Log_Svc
    Log_Svc --> Log_Strat
    Log_Svc --> Log_Repo
}
Log_Ctrl -up- IApiLog

' ============================ Librería compartida ============================
package "commons «librería (JAR)»" #F7F7F7 {
    component "NotificacionRestClient" <<Client>> as Com_Cli
    component "DTOs y enums de integración" <<Library>> as Com_Dto
}

' ============================ Sistemas externos ============================
package "N8N «docker :5678»" <<external>> {
    component "Flujo Insignias" as N8N_Ins
    component "Flujo Ranking mensual" as N8N_Rank
}
component "Browserless/Chrome\n«docker :3000»" <<external>> as Browserless
component "LinkedIn API" <<external>> as LinkedIn
component "Google Sheets API" <<external>> as GSheets
N8N_Ins -up- IWebhook

' ============================ Cliente externo → APIs ============================
Cliente ..> IApiDon : «use»
Cliente ..> IApiInc : «use»
Cliente ..> IApiNot : «use»
Cliente ..> IApiLog : «use»

' ============================ Integraciones entre servicios (interfaces requeridas) ============================
Don_Pub    ..> IApiInc : «use»\nPOST /entrega
Don_CliLog ..> IApiLog : «use»\nPOST /planificar
Don_Task   ..> IApiLog : «use»\nGET /rutas/eventos (polling)
Inc_CliDon ..> IApiDon : «use»\nGET indicadores / donantes

Don_Svc ..> Com_Cli
Inc_Svc ..> Com_Cli
Com_Cli ..> IApiNot : «use»\nPOST /notificar

' commons: dependencia de compilación de los 4 servicios
Don_Svc ..> Com_Dto
Inc_Svc ..> Com_Dto
Not_Svc ..> Com_Dto
Log_Svc ..> Com_Dto

' ============================ Integraciones externas ============================
Inc_CliN8N ..> IWebhook : «use»\nPOST /nueva_insignia
N8N_Ins  --> Browserless : POST /screenshot
N8N_Ins  --> LinkedIn    : publica posteo
N8N_Rank ..> IApiInc     : «use»\nGET ranking (schedule)
N8N_Rank --> GSheets     : agrega fila (top 3)

legend right
    |= Símbolo / Estereotipo |= Significado |
    | ○— (lollipop) | Interfaz **provista** por el componente |
    | ..> «use» | Interfaz **requerida** (dependencia) |
    | <<Controller>> | Expone la API REST del servicio |
    | <<Service>> | Orquesta la lógica de aplicación |
    | <<Repository>> | Almacenamiento en memoria (sin BD real) |
    | <<Scheduled>> | Tarea programada (@Scheduled) |
    | <<Client>> | Cliente HTTP saliente (RestTemplate) |
    | <<Strategy>> | Aplica un algoritmo intercambiable |
    | <<Adapter>> | Traduce un evento de dominio a HTTP |
    | <<external>> | Sistema externo al monorepo |
endlegend

note as NotaPersistencia
    **Persistencia in-memory**: los 4 servicios usan colecciones
    (List / Map / ConcurrentHashMap); no hay componente de base de
    datos porque no existe datasource configurado (corresponde a la
    Entrega 5). La comunicación entre servicios es 100 % síncrona
    sobre HTTP (RestTemplate); el único mecanismo asincrónico es el
    **polling** de Donaciones sobre los eventos de Logística.
end note

@enduml
```

## 7. Diagrama de Despliegue (único, solución completa)

![Despliegue](architecture/despliegue.png)

Vista física del Modelo 4+1: **sin actores**. Cliente como nodo `<<device>>`; servicios containerizados como `<<execution environment>>` con `.jar` como `<<artifact>>`; conexiones como *communication paths* con protocolo. `servicio-logistica` se representa como proceso Java sobre la JVM local (sin Dockerfile).

```plantuml
@startuml despliegue
!include ../_style.puml

title Diagrama de Despliegue — DonaTrack (solución completa)

' =====================================================================
' Diagrama de despliegue UML: nodos (device / execution environment),
' artefactos desplegados y communication paths rotulados con el
' protocolo real. Sin actores ni casos de uso (no corresponden a esta
' vista física del Modelo 4+1).
' =====================================================================

node "Dispositivo Cliente" <<device>> as Cliente {
    artifact "Navegador Web /\nPostman / Swagger UI" as ClienteApp
}

node "Render.com — PaaS (plan Free)  «render.yaml»" <<cloud>> as Render {

    node "Contenedor Docker  «Dockerfile.donaciones»" <<execution environment>> as NodeDon {
        artifact "servicio-donaciones.jar\n{Spring Boot · puerto 8080}" as JarDon
    }

    node "Contenedor Docker  «Dockerfile.incentivos»" <<execution environment>> as NodeInc {
        artifact "servicio-incentivos.jar\n{Spring Boot · puerto 8081}" as JarInc
    }

    node "Contenedor Docker  «Dockerfile.notificaciones»" <<execution environment>> as NodeNot {
        artifact "servicio-notificaciones.jar\n{Spring Boot · puerto 8082}" as JarNot
    }
}

node "Host de Desarrollo Local" <<device>> as Local {

    node "JVM 21  «mvnw spring-boot:run»" <<execution environment>> as NodeLog {
        artifact "servicio-logistica.jar\n{Spring Boot · puerto 8083}" as JarLog
    }

    node "Docker Engine  «herramientas/docker-compose.yml»" <<execution environment>> as DockerLocal {
        artifact "n8nio/n8n\n{puerto 5678}" as ArtN8N
        artifact "browserless/chrome\n{puerto 3000}" as ArtBrowserless
    }
}

node "LinkedIn" <<cloud>> as LinkedIn
node "Google Sheets" <<cloud>> as GSheets

' ===================== Communication paths (protocolo real) =====================
Cliente --> NodeDon  : <<HTTPS>> REST/JSON
Cliente --> NodeInc  : <<HTTPS>> REST/JSON
Cliente --> NodeNot  : <<HTTPS>> REST/JSON
Cliente --> NodeLog  : <<HTTP>> REST/JSON

NodeDon --> NodeInc  : <<HTTP>>\nPOST /entrega
NodeInc --> NodeDon  : <<HTTP>>\nGET /donaciones-segmentadas/...\nGET /donantes
NodeDon --> NodeNot  : <<HTTP>>\nPOST /api/notificaciones/notificar
NodeInc --> NodeNot  : <<HTTP>>\nPOST /api/notificaciones/notificar
NodeDon --> NodeLog  : <<HTTP>>\nPOST /planificar
NodeDon --> NodeLog  : <<HTTP>>\nGET /rutas/eventos (polling 10 s)

NodeInc --> DockerLocal : <<HTTP>>\nPOST /webhook/nueva_insignia
DockerLocal --> NodeInc : <<HTTP>>\nGET ranking (schedule mensual)
DockerLocal --> LinkedIn : <<HTTPS>> API LinkedIn (OAuth2)
DockerLocal --> GSheets  : <<HTTPS>> API Google Sheets

note as NotaDespliegue
    **Notas de despliegue (estado real del repositorio, commit 1cb5264):**
    • **servicio-logistica** no tiene Dockerfile ni entrada en render.yaml:
      se ejecuta como proceso local sobre la JVM (mvnw spring-boot:run).
    • **Persistencia en memoria**: ningún nodo despliega una base de datos
      (no hay spring.datasource.* configurado). La persistencia real
      corresponde a la Entrega 5.
    • **Acoplamiento por configuración**: las URLs entre servicios están
      fijadas a `localhost` en application.yml/.properties; para operar
      en Render deben sobreescribirse por variable de entorno hacia los
      dominios *.onrender.com (ver docs/DEPLOY.md).
end note

@enduml
```

## 8. Diagrama General de Casos de Uso (único)

![Casos de Uso](casos-de-uso/general.png)

4 actores (Persona Donante, Entidad Beneficiaria, Persona Administradora, Chofer) y ~25 casos de uso agrupados por servicio dentro del límite del sistema. Asociaciones actor–caso como líneas simples sin punta de flecha; `<<include>>`/`<<extend>>` con dirección correcta.

```plantuml
@startuml general
!include ../_style.puml
' Los diagramas de casos de uso se ven mejor con líneas rectas (no ortogonales)
skinparam linetype polyline

title Diagrama General de Casos de Uso — DonaTrack

left to right direction

' ---------------------------------------------------------------------
' Convenciones UML aplicadas:
'  • Asociación actor–caso de uso: línea simple, sin punta de flecha.
'  • <<include>>: el caso base SIEMPRE incorpora el caso incluido
'    (flecha punteada del base al incluido).
'  • <<extend>>: el caso extensor (opcional/excepcional) apunta al
'    caso base que extiende.
' ---------------------------------------------------------------------

actor "Persona\nDonante" as Donante
actor "Entidad\nBeneficiaria" as Entidad
actor "Persona\nAdministradora" as Admin
actor "Chofer" as Chofer

rectangle "DonaTrack" {

    package "Acceso" {
        usecase "Registrarse\n(humana o jurídica)" as UC_Registro
        usecase "Iniciar Sesión" as UC_Login
    }

    package "Servicio de Donaciones" {
        usecase "Importar Donantes por CSV" as UC_ImportarCSV
        usecase "Gestionar Personas Donantes" as UC_GestionarDonantes
        usecase "Registrar Donación\nrecibida en depósito" as UC_RegistrarDonacion
        usecase "Consultar Mis Donaciones\n(por estado / categoría)" as UC_ConsultarMisDonaciones
        usecase "Consultar Perfil e\nIndicadores de Donante" as UC_PerfilDonante
        usecase "Navegar Entidades\nBeneficiarias" as UC_NavegarEntidades
        usecase "Gestionar Entidades\nBeneficiarias" as UC_GestionarEntidades
        usecase "Registrar Necesidad Material\n(recurrente / extraordinaria)" as UC_RegistrarNecesidad
        usecase "Consultar Donaciones\nAsignadas" as UC_ConsultarAsignadas
        usecase "Ejecutar Algoritmos\nde Asignación" as UC_EjecutarMatchmaking
        usecase "Seleccionar Entidad\nBeneficiaria Final" as UC_SeleccionarEntidad
        usecase "Actualizar Estado de\nDonación (marcar vencida)" as UC_ActualizarEstado
        usecase "Confirmar Recepción\nde Donación (con fotos)" as UC_ConfirmarRecepcionDon
        usecase "Consultar Trazabilidad\nde una Donación" as UC_Trazabilidad
    }

    package "Servicio de Incentivos" {
        usecase "Consultar Misiones\ne Insignias" as UC_Misiones
        usecase "Consultar Ranking\nde Donantes" as UC_Ranking
    }

    package "Servicio de Notificaciones" {
        usecase "Notificar a\nPersona Donante" as UC_NotifDonante
        usecase "Notificar a\nEntidad Beneficiaria" as UC_NotifEntidad
    }

    package "Servicio de Logística" {
        usecase "Administrar Camiones" as UC_Camiones
        usecase "Administrar Choferes" as UC_Choferes
        usecase "Ver Ruta Asignada" as UC_VerRuta
        usecase "Iniciar Ruta de Entrega" as UC_IniciarRuta
        usecase "Registrar Recepción\nde Envío" as UC_RegistrarRecepcionEnvio
        usecase "Reportar Entrega\nNo Satisfactoria" as UC_EntregaFallida
        usecase "Seguir Entrega en Mapa" as UC_SeguirMapa
    }
}

' ============================ Asociaciones (línea simple, sin flecha) ============================
Donante -- UC_Registro
Entidad -- UC_Registro
Donante -- UC_Login
Entidad -- UC_Login
Admin   -- UC_Login
Chofer  -- UC_Login

Admin -- UC_ImportarCSV
Admin -- UC_GestionarDonantes
Admin -- UC_RegistrarDonacion
Admin -- UC_GestionarEntidades
Admin -- UC_EjecutarMatchmaking
Admin -- UC_SeleccionarEntidad
Admin -- UC_ActualizarEstado

Donante -- UC_ConsultarMisDonaciones
Donante -- UC_PerfilDonante
Donante -- UC_NavegarEntidades
Donante -- UC_Trazabilidad

Entidad -- UC_RegistrarNecesidad
Entidad -- UC_ConsultarAsignadas
Entidad -- UC_ConfirmarRecepcionDon
Entidad -- UC_Trazabilidad

Donante -- UC_Misiones
Donante -- UC_Ranking
Admin   -- UC_Ranking

Donante -- UC_NotifDonante
Entidad -- UC_NotifEntidad

Admin  -- UC_Camiones
Admin  -- UC_Choferes
Chofer -- UC_VerRuta
Chofer -- UC_IniciarRuta
Entidad -- UC_RegistrarRecepcionEnvio
Entidad -- UC_EntregaFallida
Admin   -- UC_EntregaFallida
Donante -- UC_SeguirMapa
Entidad -- UC_SeguirMapa

' ============================ <<include>> (base -> incluido) ============================
UC_SeleccionarEntidad ..> UC_EjecutarMatchmaking : <<include>>
UC_ConfirmarRecepcionDon ..> UC_NotifDonante : <<include>>
UC_IniciarRuta ..> UC_NotifEntidad : <<include>>
UC_SeleccionarEntidad ..> UC_NotifEntidad : <<include>>

' ============================ <<extend>> (extensor -> base) ============================
UC_EntregaFallida ..> UC_RegistrarRecepcionEnvio : <<extend>>

note bottom of UC_SeguirMapa
    Requerimiento de UI/UX de la consigna (mapa interactivo
    con ubicación del camión). No se halló un endpoint de
    geolocalización en tiempo real en el código de esta entrega.
end note

note bottom of UC_Login
    El PDF de la cátedra menciona un "servicio de autenticación"
    como componente propio, aún no implementado por ninguno de
    los 4 servicios de negocio relevados.
end note

@enduml
```
