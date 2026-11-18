
package unam.fes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unam.fes.entity.venta.Venta;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    // Ventas de un vendedor
    List<Venta> findByVendedorIdOrderByFechaHoraDesc(Long idVendedor);

    // Ventas de un vendedor dentro de un rango de fechas
    List<Venta> findByVendedorIdAndFechaHoraGreaterThanEqualAndFechaHoraLessThanOrderByFechaHoraDesc(
            Long idVendedor,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    // Ventas dentro de un rango de fechas
    List<Venta> findByFechaHoraGreaterThanEqualAndFechaHoraLessThanOrderByFechaHoraAsc(
            LocalDateTime inicio,
            LocalDateTime fin
    );

    // Todas las ventas
    List<Venta> findAllByOrderByFechaHoraDesc();
}
