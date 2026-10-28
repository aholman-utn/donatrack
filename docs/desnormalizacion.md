# Estrategias de Desnormalización (Entrega 5 - Punto 3)

## 1. Justificación de Diseño (Trade-off 3NF vs. Desnormalización)

En **DonaTrack**, el dominio presenta una **proporción de lecturas muy superior a la de escrituras**: consultas frecuentes de perfiles, rankings sobre miles de donantes importados por CSV (+10.000 filas), algoritmos de matchmaking y bandejas operativas de administración.

Mantener un esquema estrictamente en **Tercera Forma Normal (3NF)** obliga a resolver estas consultas mediante múltiples `JOIN`, `GROUP BY`, `COUNT(*)` o cálculos en memoria. Aplicamos **desnormalización controlada** en el `servicio-donaciones` para transformar agregaciones y cruces costosos en lecturas directas $O(1)$ o filtrados indexados sobre una única tabla, asumiendo el costo marginal de mantener sincronizados estos campos al momento de escribir/transicionar estados.

---

## 2. Resumen de Campos Desnormalizados

| Tabla / Entidad | Campo Desnormalizado | Origen Normalizado (3NF) | Consulta / Caso de Uso que Optimiza |
| :--- | :--- | :--- | :--- |
| **`donaciones_segmentadas`** (`DonacionSegmentada`) | `categoria_bien` (`VARCHAR`) | `donaciones_segmentadas` $\to$ `subcategorias.categoria` | Filtrar donaciones de un donante por estado y categoría (`ALIMENTOS`, `ROPA`, etc.) sin hacer `JOIN` con `subcategorias`. |
| **`donaciones_segmentadas`** (`DonacionSegmentada`) | `entidad_beneficiaria_nombre` (`VARCHAR`) | `donaciones_segmentadas` $\to$ `entidades_beneficiarias` $\to$ `personas.razon_social` | Mostrar el historial de donaciones y el mapa interactivo de entregas con la razón social receptora sin cruzar 3 tablas. |
| **`donaciones_segmentadas`** (`DonacionSegmentada`) | `donante_id` (`BIGINT`) | `donaciones_segmentadas` $\to$ `donaciones.donante_id` | Consultar segmentos directamente por donante (`findByDonanteId`) sin pasar por la tabla padre `donaciones`. |
| **`donaciones`** (`Donacion`) | `estado` (`VARCHAR`) | Cálculo dinámico sobre todas sus `donaciones_segmentadas` hijas | Bandeja de donaciones pendientes del administrador (`WHERE estado = 'PENDIENTE'`) sin `LEFT JOIN` ni evaluación en memoria. |
| **`entidades_beneficiarias`** (`EntidadBeneficiaria`) | `total_donaciones_recibidas` (`INT`) | `COUNT(*)` de donaciones asignadas a sus necesidades | **Algoritmo de Prioridad a Sub-Atendidos** en Matchmaking (`ORDER BY total_donaciones_recibidas ASC`). |
| **`donantes`** (`Donante` + `@Embedded PerfilDonante`) | `total_donaciones_exitosas`, `total_misiones_completadas`, `fecha_ultima_donacion`, `nivel_donante` | Agregaciones (`COUNT`, `MAX(fecha)`) sobre `donaciones_segmentadas` y tablas anexas | **Ranking Top Donantes** (`ORDER BY total_donaciones_exitosas DESC`), vista rápida del perfil y detección de donantes inactivos. |

---

## 3. Ejemplos Puntuales de Implementación en Código

### Caso A: Eliminación de JOINs al filtrar por Categoría y consultar Historial (`DonacionSegmentada`)

Se agregaron las columnas `categoria_bien` y `entidad_beneficiaria_nombre` directamente en `DonacionSegmentada`, sincronizándolas al instanciar el segmento y al asignarlo a una entidad beneficiaria.

**Antes (3NF):**
Para filtrar por categoría o mostrar el nombre de la entidad en el historial, era necesario navegar relaciones (`ds.getSubCategoria().getCategoria()`) y consultar `EntidadBeneficiaria` $\to$ `PersonaJuridica`.

**Después (Desnormalizado):**
```java
// DonacionSegmentada.java
@Enumerated(EnumType.STRING)
@Column(name = "categoria_bien")
private CategoriaBien categoriaBien;

@Column(name = "entidad_beneficiaria_nombre")
private String entidadBeneficiariaNombre;

public DonacionSegmentada(int cantidad, SubCategoria subCategoria, List<Bien> bienes, Long donanteId) {
    this.cantidad = cantidad;
    this.subCategoria = subCategoria;
    // Se sincroniza automáticamente la categoría al crear/modificar la subcategoría
    this.categoriaBien = subCategoria != null ? subCategoria.getCategoria() : null;
    this.bienes = bienes;
    this.donanteId = donanteId;
    this.estado = new EnDeposito();
}
```

Al asignar la donación en `EnDeposito.java`, se persiste el snapshot de la razón social:
```java
// EnDeposito.java
@Override
public void asignar(DonacionSegmentada donacion, EntidadBeneficiaria entidad, String actor) {
    entidad.implementarDonacion(donacion);
    if (entidad.getDatosDeEntidad() != null) {
        donacion.setEntidadBeneficiariaAsignadaId(entidad.getDatosDeEntidad().getId());
        donacion.setEntidadBeneficiariaNombre(entidad.getDatosDeEntidad().getRazonSocial());
    }
    donacion.transicionar(new AsignacionRealizada(), actor, "Donación asignada a entidad beneficiaria");
}
```

Esto habilita consultas directas en `DonacionSegmentadaRepository` sobre una única tabla:
```java
// DonacionSegmentadaRepository.java
List<DonacionSegmentada> findByDonanteIdAndEstadoAndCategoriaBien(
        Long donanteId, EstadoDonacionSegmentada estado, CategoriaBien categoriaBien);
```

---

### Caso B: Contador Acumulado para Matchmaking (`EntidadBeneficiaria`)

En lugar de contar todas las donaciones históricas de cada necesidad de una entidad para priorizar a las organizaciones sub-atendidas, se mantiene un contador incremental `total_donaciones_recibidas`.

```java
// EntidadBeneficiaria.java
@Column(name = "total_donaciones_recibidas")
private int totalDonacionesRecibidas = 0;

public void implementarDonacion(DonacionSegmentada donacion) {
    // ... vinculación con la necesidad activa ...
    necesidadCorrespondiente.agregarDonacion(donacion);
    this.totalDonacionesRecibidas++; // Actualización transaccional del contador
}
```

```java
// EntidadBeneficiariaRepository.java
// Consulta directa O(1) por fila sin subconsultas COUNT(*)
List<EntidadBeneficiaria> findAllByOrderByTotalDonacionesRecibidasAsc();
```

---

### Caso C: Aplanado del Perfil y Métricas en `Donante` (`@Embedded PerfilDonante`)

`PerfilDonante` pasó de ser `@Transient` (o requerir una tabla separada y cálculos en tiempo de lectura) a embeberse con `@Embedded` en la tabla `donantes`, actualizando sus acumuladores en cada entrega confirmada:

```java
// Donante.java
@Embedded
private PerfilDonante perfil = new PerfilDonante();

// PerfilDonante.java (@Embeddable)
@Column(name = "total_donaciones_exitosas")
private int totalDonacionesExitosas = 0;

@Column(name = "fecha_ultima_donacion")
private LocalDate fechaUltimaDonacion;

public void registrarEntrega(DonacionSegmentada segmentada) {
    // ... registro en historial ...
    this.totalDonacionesExitosas++;
    this.fechaUltimaDonacion = LocalDate.now();
}
```

Y en `DonanteRepository` el ranking se resuelve sin `GROUP BY`:
```java
// DonanteRepository.java
@Query("SELECT d FROM Donante d ORDER BY d.perfil.totalDonacionesExitosas DESC")
List<Donante> findTopDonantesRanking();
```
