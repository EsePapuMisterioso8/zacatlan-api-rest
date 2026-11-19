
package unam.fes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unam.fes.entity.producto.Producto;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByBloqueId(Long idBloque);

    List<Producto> findByActivoTrue();

    List<Producto> findByBloqueIdAndActivoTrue(Long idBloque);

    boolean existsByNombreIgnoreCaseAndBloqueId(
            String nombre,
            Long idBloque
    );

    boolean existsByNombreIgnoreCaseAndBloqueIdAndIdNot(
            String nombre,
            Long idBloque,
            Long id
    );

    boolean existsByBloqueId(Long idBloque);
}
