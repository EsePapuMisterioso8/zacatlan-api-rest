
package unam.fes.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import unam.fes.entity.producto.Producto;
import unam.fes.service.ProductoService;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoRestController {

    private final ProductoService productoService;

    public ProductoRestController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<List<Producto>> listarTodos() {
        return new ResponseEntity<>(
                productoService.listarProductos(),
                HttpStatus.OK
        );
    }

    @GetMapping("/activos")
    public ResponseEntity<List<Producto>> listarActivos() {
        return new ResponseEntity<>(
                productoService.listarProductosActivos(),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            Producto producto = productoService.obtenerProductoPorId(id);
            return new ResponseEntity<>(producto, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    e.getMessage(),
                    HttpStatus.NOT_FOUND
            );
        }
    }

    @PostMapping
    public ResponseEntity<String> registrarProducto(
            @RequestBody Producto producto,
            Principal principal) {
        try {
            String nombre = producto.getNombre();
            String descripcion = producto.getDescripcion();
            Long idBloque = producto.getBloque().getId();

            productoService.registrarProducto(
                    nombre,
                    descripcion,
                    idBloque,
                    principal.getName()
            );

            return new ResponseEntity<>(
                    "Producto registrado exitosamente",
                    HttpStatus.CREATED
            );

        } catch (IllegalArgumentException | NullPointerException e) {
            return new ResponseEntity<>(
                    "Verifica que los datos y el bloque sean correctos "
                            + e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> actualizarProducto(
            @PathVariable Long id,
            @RequestBody Map<String, Object> datos,
            Principal principal) {
        try {
            String nombre = (String) datos.get("nombre");
            String descripcion = (String) datos.get("descripcion");

            productoService.actualizarProducto(
                    id,
                    nombre,
                    descripcion,
                    principal.getName()
            );

            return new ResponseEntity<>(
                    "Producto actualizado exitosamente",
                    HttpStatus.OK
            );

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<String> cambiarEstado(
            @PathVariable Long id,
            Principal principal) {
        try {
            productoService.cambiarEstadoProducto(
                    id,
                    principal.getName()
            );

            return new ResponseEntity<>(
                    "Estado del producto invertido exitosamente",
                    HttpStatus.OK
            );

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}
