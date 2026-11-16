
package unam.fes.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unam.fes.entity.producto.Producto;
import unam.fes.entity.proven.ProductoVendedor;
import unam.fes.entity.vendedor.Vendedor;
import unam.fes.repository.ProductoRepository;
import unam.fes.repository.ProductoVendedorRepository;
import unam.fes.repository.VendedorRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductoVendedorService {

    private final ProductoVendedorRepository productoVendedorRepository;
    private final ProductoRepository productoRepository;
    private final VendedorRepository vendedorRepository;

    public ProductoVendedorService(
            ProductoVendedorRepository productoVendedorRepository,
            ProductoRepository productoRepository,
            VendedorRepository vendedorRepository) {

        this.productoVendedorRepository = productoVendedorRepository;
        this.productoRepository = productoRepository;
        this.vendedorRepository = vendedorRepository;
    }

    // Consultar solamente los productos activos del bloque del vendedor
    public List<Producto> listarCatalogo(Long idBloque) {
        return productoRepository.findByBloqueIdAndActivoTrue(idBloque);
    }

    // Consultar todos los productos asignados al vendedor,
    // tanto activos como inactivos
    public List<ProductoVendedor> listarProductosVendedor(Long idVendedor) {
        return productoVendedorRepository
                .findByVendedorId(idVendedor);
    }

    // Asignar un producto a un vendedor con su precio inicial
    @Transactional
    public void asignarProducto(
            Long idVendedor,
            Long idProducto,
            BigDecimal precio) {

        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El precio debe ser mayor que cero.");
        }

        Vendedor vendedor = vendedorRepository.findById(idVendedor)
                .orElseThrow(() ->
                        new IllegalArgumentException("Vendedor no encontrado."));

        if (!Boolean.TRUE.equals(vendedor.getActivo())) {
            throw new IllegalArgumentException(
                    "Tu cuenta de vendedor está inactiva.");
        }

        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() ->
                        new IllegalArgumentException("Producto no encontrado."));

        if (!Boolean.TRUE.equals(producto.getActivo())) {
            throw new IllegalArgumentException(
                    "Este producto no está disponible.");
        }

        // Validar que el producto pertenezca al bloque del vendedor
        if (!producto.getBloque().getId().equals(vendedor.getBloque().getId())) {
            throw new IllegalArgumentException(
                    "No puedes agregar productos de otro bloque.");
        }

        // Evitar productos duplicados
        if (productoVendedorRepository
                .existsByVendedorIdAndProductoId(idVendedor, idProducto)) {

            throw new IllegalArgumentException(
                    "Ya tienes registrado este producto.");
        }

        ProductoVendedor productoVendedor = ProductoVendedor.builder()
                .vendedor(vendedor)
                .producto(producto)
                .precio(precio)
                .activo(true)
                .build();

        productoVendedorRepository.save(productoVendedor);
    }
}
