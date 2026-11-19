
package unam.fes.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unam.fes.entity.bloque.Bloque;
import unam.fes.entity.producto.Producto;
import unam.fes.repository.BloqueRepository;
import unam.fes.repository.ProductoRepository;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final BloqueRepository bloqueRepository;

    public ProductoService(
            ProductoRepository productoRepository,
            BloqueRepository bloqueRepository) {
        this.productoRepository = productoRepository;
        this.bloqueRepository = bloqueRepository;
    }

    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    public List<Producto> listarProductosActivos() {
        return productoRepository.findByActivoTrue();
    }

    public Producto obtenerProductoPorId(Long idProducto) {
        return productoRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el producto"));
    }

    public Producto registrarProducto(
            String nombre,
            String descripcion,
            Long idBloque) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del producto es obligatorio");
        }

        Bloque bloque = bloqueRepository.findById(idBloque)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el bloque seleccionado"));

        if (!bloque.getActivo()) {
            throw new IllegalArgumentException(
                    "No se pueden agregar productos a un bloque inactivo");
        }

        nombre = nombre.trim();

        if (productoRepository.existsByNombreIgnoreCaseAndBloqueId(
                nombre, idBloque)) {
            throw new IllegalArgumentException(
                    "Ya existe un producto con ese nombre en el bloque");
        }

        Producto producto = Producto.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .bloque(bloque)
                .activo(true)
                .build();

        return productoRepository.save(producto);
    }

    @Transactional
    public Producto actualizarProducto(
            Long idProducto,
            String nombre,
            String descripcion) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del producto es obligatorio");
        }

        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el producto"));

        nombre = nombre.trim();

        Long idBloque = producto.getBloque().getId();

        if (productoRepository.existsByNombreIgnoreCaseAndBloqueIdAndIdNot(
                nombre, idBloque, idProducto)) {
            throw new IllegalArgumentException(
                    "Ya existe un producto con ese nombre en el bloque");
        }

        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);

        return productoRepository.save(producto);
    }

    @Transactional
    public void cambiarEstadoProducto(Long idProducto) {

        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el producto"));

        producto.setActivo(!producto.getActivo());

        productoRepository.save(producto);
    }
}
