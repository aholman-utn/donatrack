package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.bien.CategoriaBien;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonacionSegmentadaRepository extends JpaRepository<DonacionSegmentada, Long> {
    @org.springframework.data.jpa.repository.Query("""
            select s.id from DonacionSegmentada s where s.estado = :estado
                and s.comprobanteRecepcionDonacion is not null and s.donanteId is not null
                and (s.incentivosProcesados is null or s.incentivosProcesados = false)
            order by s.comprobanteRecepcionDonacion.fechaEntrega, s.id
            """)
    List<Long> buscarEntregasPendientes(EstadoDonacionSegmentada estado, org.springframework.data.domain.Pageable pagina);

    List<DonacionSegmentada> findByEstadoAndRutaLogisticaIdIsNullOrderByIdAsc(EstadoDonacionSegmentada estado);
    List<DonacionSegmentada> findByRutaLogisticaIdIn(java.util.Collection<String> rutas);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select s from DonacionSegmentada s where s.id = :id")
    java.util.Optional<DonacionSegmentada> bloquear(Long id);

    List<DonacionSegmentada> findByDonanteIdAndEstado(Long donanteId, EstadoDonacionSegmentada estado);

    List<DonacionSegmentada> findByDonanteIdAndCategoriaBien(Long donanteId, CategoriaBien categoriaBien);

    List<DonacionSegmentada> findByDonanteIdAndEstadoAndCategoriaBien(Long donanteId, EstadoDonacionSegmentada estado, CategoriaBien categoriaBien);

    List<DonacionSegmentada> findByCategoriaBien(CategoriaBien categoriaBien);
}