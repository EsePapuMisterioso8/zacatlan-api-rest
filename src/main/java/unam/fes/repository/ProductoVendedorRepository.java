
package unam.fes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unam.fes.entity.proven.ProductoVendedor;

import java.util.List;
import java.util.Optional;

public interface ProductoVendedorRepository
        extends JpaRepository<ProductoVendedor, Long> {

    // Consultar los productos asignados a un vendedor
    List<ProductoVendedor> findByVendedorId(Long idVendedor);

    // Consultar solamente los productos activos de un vendedor
    List<ProductoVendedor> findByVendedorIdAndActivoTrue(Long idVendedor);

    // Comprobar si un vendedor ya ofrece un producto
    boolean existsByVendedorIdAndProductoId(
            Long idVendedor,
            Long idProducto
    );

    // Buscar la relación entre un vendedor y un producto
    Optional<ProductoVendedor> findByVendedorIdAndProductoId(
            Long idVendedor,
            Long idProducto
    );
}
