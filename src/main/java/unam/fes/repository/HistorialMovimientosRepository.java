
package unam.fes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unam.fes.entity.historial.HistorialMovimientos;

import java.util.List;

public interface HistorialMovimientosRepository
        extends JpaRepository<HistorialMovimientos, Long> {

    List<HistorialMovimientos> findAllByOrderByFechaHoraDesc();

    List<HistorialMovimientos> findByAdministradorIdOrderByFechaHoraDesc(
            Long idAdministrador
    );
}
