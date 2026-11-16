package unam.fes.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unam.fes.entity.producto.Producto;
import unam.fes.entity.proven.ProductoVendedor;
import unam.fes.entity.venta.InventarioResponseDTO;
import unam.fes.service.ProductoVendedorService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventario")
public class ProductoVendedorRestController {

    private final ProductoVendedorService productoVendedorService;

    public ProductoVendedorRestController(ProductoVendedorService productoVendedorService) {
        this.productoVendedorService = productoVendedorService;
    }

    // 1. GET: Catálogo disponible para el vendedor (según su bloque)
    @GetMapping("/catalogo/bloque/{idBloque}")
    public ResponseEntity<List<Producto>> listarCatalogoBloque(@PathVariable Long idBloque) {
        return new ResponseEntity<>(productoVendedorService.listarCatalogo(idBloque), HttpStatus.OK);
    }

    // 2. GET: Mi inventario (Productos que el vendedor ya configuró para vender)
    /*@GetMapping("/vendedor/{idVendedor}")
    public ResponseEntity<List<ProductoVendedor>> listarInventarioVendedor(@PathVariable Long idVendedor) {
        return new ResponseEntity<>(productoVendedorService.listarProductosVendedor(idVendedor), HttpStatus.OK);
    }*/
    @GetMapping("/vendedor/{idVendedor}")
    public ResponseEntity<List<InventarioResponseDTO>> listarInventarioVendedor(@PathVariable Long idVendedor) {

        // 1. Obtenemos las entidades de la base de datos
        List<ProductoVendedor> inventario = productoVendedorService.listarProductosVendedor(idVendedor);

        // 2. Transformamos la entidad compleja en nuestro DTO seguro
        List<InventarioResponseDTO> respuesta = inventario.stream().map(pv -> {
            InventarioResponseDTO dto = new InventarioResponseDTO();
            dto.setIdProductoVendedor(pv.getId());
            dto.setNombreProducto(pv.getProducto().getNombre());
            dto.setDescripcionProducto(pv.getProducto().getDescripcion());
            dto.setPrecio(pv.getPrecio());
            dto.setActivo(pv.getActivo());
            return dto;
        }).collect(Collectors.toList());

        // 3. Devolvemos la lista limpia
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    // 3. POST: Asignar un producto al inventario del vendedor y fijarle un precio
    @PostMapping
    public ResponseEntity<String> asignarProducto(@RequestBody AsignarProductoDTO request) {
        try {
            productoVendedorService.asignarProducto(
                    request.getIdVendedor(),
                    request.getIdProducto(),
                    request.getPrecio()
            );
            return new ResponseEntity<>("Producto agregado a tu inventario exitosamente.", HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    //CLASE AUXILIAR DTO
    public static class AsignarProductoDTO {
        private Long idVendedor;
        private Long idProducto;
        private BigDecimal precio;

        // Getters
        public Long getIdVendedor() { return idVendedor; }
        public Long getIdProducto() { return idProducto; }
        public BigDecimal getPrecio() { return precio; }

        // Setters
        public void setIdVendedor(Long idVendedor) { this.idVendedor = idVendedor; }
        public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }
        public void setPrecio(BigDecimal precio) { this.precio = precio; }
    }
}