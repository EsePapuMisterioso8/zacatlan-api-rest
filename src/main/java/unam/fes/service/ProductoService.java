
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
    private final HistorialMovimientosService historialService;

    public ProductoService(
            ProductoRepository productoRepository,
            BloqueRepository bloqueRepository,
            HistorialMovimientosService historialService) {
        this.productoRepository = productoRepository;
        this.bloqueRepository = bloqueRepository;
        this.historialService = historialService;
    }

    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    public List<Producto> listarProductosActivos() {
        return productoRepository.findByActivoTrue();
    }

    public Producto obtenerProductoPorId(Long idProducto) {
        return productoRepository.findById(idProducto)
                .orElseThrow(() ->
                        new IllegalArgumentException("No se encontró el producto"));
    }

    @Transactional
    public Producto registrarProducto(
            String nombre,
            String descripcion,
            Long idBloque,
            String usernameAdministrador) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del producto es obligatorio");
        }

        Bloque bloque = bloqueRepository.findById(idBloque)
                .orElseThrow(() ->
                        new IllegalArgumentException(
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

        Producto guardado = productoRepository.save(producto);

        historialService.registrarMovimiento(
                usernameAdministrador,
                "CREAR",
                "PRODUCTO",
                guardado.getId(),
                "Se creó el producto " + guardado.getNombre(),
                null,
                "Nombre: " + guardado.getNombre()
                        + ", Descripción: " + guardado.getDescripcion()
                        + ", Bloque: " + bloque.getNombre()
                        + ", Activo: true"
        );

        return guardado;
    }

    @Transactional
    public Producto actualizarProducto(
            Long idProducto,
            String nombre,
            String descripcion,
            String usernameAdministrador) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del producto es obligatorio");
        }

        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró el producto"));

        nombre = nombre.trim();

        Long idBloque = producto.getBloque().getId();

        if (productoRepository.existsByNombreIgnoreCaseAndBloqueIdAndIdNot(
                nombre, idBloque, idProducto)) {
            throw new IllegalArgumentException(
                    "Ya existe un producto con ese nombre en el bloque");
        }

        String datosAnteriores =
                "Nombre: " + producto.getNombre()
                        + ", Descripción: " + producto.getDescripcion()
                        + ", Bloque: " + producto.getBloque().getNombre()
                        + ", Activo: " + producto.getActivo();

        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);

        Producto actualizado = productoRepository.save(producto);

        String datosNuevos =
                "Nombre: " + actualizado.getNombre()
                        + ", Descripción: " + actualizado.getDescripcion()
                        + ", Bloque: " + actualizado.getBloque().getNombre()
                        + ", Activo: " + actualizado.getActivo();

        historialService.registrarMovimiento(
                usernameAdministrador,
                "MODIFICAR",
                "PRODUCTO",
                actualizado.getId(),
                "Se modificó el producto " + actualizado.getNombre(),
                datosAnteriores,
                datosNuevos
        );

        return actualizado;
    }

    @Transactional
    public void cambiarEstadoProducto(
            Long idProducto,
            String usernameAdministrador) {

        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró el producto"));

        boolean estadoAnterior = Boolean.TRUE.equals(producto.getActivo());
        boolean nuevoEstado = !estadoAnterior;

        producto.setActivo(nuevoEstado);
        productoRepository.save(producto);

        historialService.registrarMovimiento(
                usernameAdministrador,
                nuevoEstado ? "ACTIVAR" : "DESACTIVAR",
                "PRODUCTO",
                producto.getId(),
                (nuevoEstado ? "Se activó el producto "
                        : "Se desactivó el producto ")
                        + producto.getNombre(),
                "Activo: " + estadoAnterior,
                "Activo: " + nuevoEstado
        );
    }
}
