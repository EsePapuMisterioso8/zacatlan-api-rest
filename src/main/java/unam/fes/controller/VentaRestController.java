package unam.fes.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unam.fes.entity.venta.Venta;
import unam.fes.entity.venta.VentaRequestDTO;
import unam.fes.entity.venta.VentaResponseDTO;
import unam.fes.service.VentaService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ventas")
public class VentaRestController {
    private final VentaService ventaService;

    public VentaRestController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    // --- MÉTODO TRADUCTOR (Entidad -> DTO) ---
    private VentaResponseDTO convertirADTO(Venta venta) {
        VentaResponseDTO dto = new VentaResponseDTO();
        dto.setIdVenta(venta.getId());
        // Extraemos solo el nombre del vendedor para evitar bucles de seguridad
        dto.setNombreVendedor(venta.getVendedor().getNombre() + " " + venta.getVendedor().getApellido());
        dto.setFechaHora(venta.getFechaHora());
        dto.setTotal(venta.getTotal());
        dto.setMetodoPago(venta.getMetodoPago());
        return dto;
    }

    // 1. POST: Registrar una nueva venta
    @PostMapping
    public ResponseEntity<?> registrarVenta(@RequestBody VentaRequestDTO request) {
        try {
            Venta nuevaVenta = ventaService.registrarVenta(
                    request.getIdVendedor(),
                    request.getItems()
            );
            return new ResponseEntity<>(convertirADTO(nuevaVenta), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 2. GET: Listar todas las ventas
    @GetMapping
    public ResponseEntity<List<VentaResponseDTO>> listarTodas() {
        List<VentaResponseDTO> respuesta = ventaService.listarTodasLasVentas()
                .stream().map(this::convertirADTO).collect(Collectors.toList());
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    // 3. GET: Listar ventas de un vendedor específico
    @GetMapping("/vendedor/{idVendedor}")
    public ResponseEntity<List<VentaResponseDTO>> listarPorVendedor(@PathVariable Long idVendedor) {
        List<VentaResponseDTO> respuesta = ventaService.listarVentasVendedor(idVendedor)
                .stream().map(this::convertirADTO).collect(Collectors.toList());
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    // 4. GET: Listar ventas de un día en específico
    @GetMapping("/dia")
    public ResponseEntity<List<VentaResponseDTO>> listarPorDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        List<VentaResponseDTO> respuesta = ventaService.listarVentasPorDia(fecha)
                .stream().map(this::convertirADTO).collect(Collectors.toList());
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    // 5. GET: Obtener el total de dinero vendido por cada vendedor en un día
    @GetMapping("/totales")
    public ResponseEntity<?> totalesPorVendedor(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return new ResponseEntity<>(ventaService.calcularVentasPorVendedor(fecha), HttpStatus.OK);
    }

    // 6. GET: Listar ventas por rango de fechas
    @GetMapping("/rango")
    public ResponseEntity<?> listarPorRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        try {
            if (fechaInicio.isAfter(fechaFin)) {
                return new ResponseEntity<>("La fecha de inicio no puede ser posterior a la fecha final.", HttpStatus.BAD_REQUEST);
            }
            List<VentaResponseDTO> respuesta = ventaService.listarVentasPorRango(fechaInicio, fechaFin)
                    .stream().map(this::convertirADTO).collect(Collectors.toList());
            return new ResponseEntity<>(respuesta, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Ocurrió un error interno al consultar las ventas.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    /*private final VentaService ventaService;

    public VentaRestController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    //Registrar una nueva venta
    @PostMapping
    public ResponseEntity<?> registrarVenta(@RequestBody VentaRequestDTO requestDTO){
        try {
            Venta nuevaVenta = ventaService.registrarVenta(
                    requestDTO.getIdVendedor(),
                    requestDTO.getProductosCantidad()
            );
            return new ResponseEntity<>(nuevaVenta, HttpStatus.CREATED);
        } catch (IllegalArgumentException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    //Listar todas las ventas del sistema
    @GetMapping
    public ResponseEntity<List<Venta>> listarTodas(){
        return new ResponseEntity<>(ventaService.listarTodasLasVentas(), HttpStatus.OK);
    }

    //Listar las ventas de un vendedor específico
    @GetMapping("/vendedor/{idVendedor}")
    public ResponseEntity<List<Venta>> listarPorVendedor(@PathVariable Long idVendedor){
        return new ResponseEntity<>(ventaService.listarVentasVendedor(idVendedor), HttpStatus.OK);
    }

    //Listar ventas de un día específico
    @RequestMapping("/dia")
    public ResponseEntity<List<Venta>> listarPorDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha){
        return new ResponseEntity<>(ventaService.listarVentasPorDia(fecha), HttpStatus.OK);
    }

    //Obtener el total de dinero vendido por cada vendedor en un día
    @GetMapping("/totales")
    public ResponseEntity<Map<String, BigDecimal>> totalesPorVendedor(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return new ResponseEntity<>(ventaService.calcularVentasPorVendedor(fecha), HttpStatus.OK);
    }

    //Consultar ventas por rango de fecha
    @GetMapping("/rango")
    public ResponseEntity<?> listarPorRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        try {
            // Validación: Si el usuario pone las fechas al revés
            if (fechaInicio.isAfter(fechaFin)) {
                return new ResponseEntity<>("La fecha de inicio no puede ser posterior a la fecha final.", HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(ventaService.listarVentasPorRango(fechaInicio, fechaFin), HttpStatus.OK);

        } catch (Exception e) {
            // Si algo explota en la base de datos o al procesar
            return new ResponseEntity<>("Ocurrió un error interno al consultar las ventas.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/
}
