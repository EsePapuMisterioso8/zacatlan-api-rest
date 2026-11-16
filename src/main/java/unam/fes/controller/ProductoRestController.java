package unam.fes.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unam.fes.entity.producto.Producto;
import unam.fes.service.ProductoService;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoRestController {
    private final ProductoService productoService;

    //Inyección de dependencias
    public ProductoRestController(ProductoService productoService) {
        this.productoService = productoService;
    }

    //Metodo para listar los productos
    @GetMapping
    public ResponseEntity<List<Producto>> listarTodos(){
        return new ResponseEntity<>(productoService.listarProductos(), HttpStatus.OK);
    }

    //Listar los productos activos
    @GetMapping("/activos")
    public ResponseEntity<List<Producto>> listarActivos(){
        return new ResponseEntity<>(productoService.listarProductosActivos(), HttpStatus.OK);
    }

    //Obtener producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id){
        try {
            Producto producto = productoService.obtenerProductoPorId(id);
            return new ResponseEntity<>(producto, HttpStatus.OK);
        } catch (IllegalArgumentException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    //Registrar un nuevo producto
    @PostMapping
    public ResponseEntity<String> registrarProducto(@RequestBody Producto producto){
        try {
            String nombre = producto.getNombre();
            String descripcion = producto.getDescripcion();
            Long idBloque = producto.getBloque().getId();

            productoService.registrarProducto(nombre, descripcion, idBloque);
            return new ResponseEntity<>("Producto registrado exitosamente", HttpStatus.CREATED);
        } catch (IllegalArgumentException | NullPointerException e){
            return new ResponseEntity<>("Verifica que los datos y el bloque sean correctos " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    //Cambiar el estado de un producto
    @PutMapping("/{id}/estado")
    public ResponseEntity<String> cambiarEstado(@PathVariable Long id){
        try {
            productoService.cambiarEstadoProducto(id);
            return new ResponseEntity<>("Estado del producto invertido exitosamente", HttpStatus.OK);
        } catch (IllegalArgumentException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
