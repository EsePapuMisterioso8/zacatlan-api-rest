
package unam.fes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unam.fes.entity.venta.Venta;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    List<Venta> findByVendedorIdOrderByFechaHoraDesc(Long idVendedor);

    List<Venta> findByFechaHoraGreaterThanEqualAndFechaHoraLessThanOrderByFechaHoraAsc(
            LocalDateTime inicio,
            LocalDateTime fin
    );

    List<Venta> findAllByOrderByFechaHoraDesc();
}
