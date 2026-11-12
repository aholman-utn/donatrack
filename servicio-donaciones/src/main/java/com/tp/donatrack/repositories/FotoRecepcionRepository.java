package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.donacion.FotoRecepcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface FotoRecepcionRepository extends JpaRepository<FotoRecepcion, Long> {

    /** Ids de fotos por segmento, sin traer el contenido binario. */
    @Query("SELECT f.donacionSegmentadaId, f.id FROM FotoRecepcion f WHERE f.donacionSegmentadaId IN :segmentos ORDER BY f.id")
    List<Object[]> findIdsPorSegmento(@Param("segmentos") Collection<Long> segmentos);

    Optional<FotoRecepcion> findByIdAndEntidadBeneficiariaId(Long id, Long entidadBeneficiariaId);
}
