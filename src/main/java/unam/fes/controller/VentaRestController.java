
package unam.fes.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import unam.fes.entity.venta.Venta;
import unam.fes.entity.venta.VentaRequestDTO;
import unam.fes.entity.venta.VentaResponseDTO;
import unam.fes.entity.vendedor.Vendedor;
import unam.fes.repository.VendedorRepository;
import unam.fes.service.VentaService;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ventas")
public class VentaRestController {

    private final VentaService ventaService;
    private final VendedorRepository vendedorRepository;

    public VentaRestController(
            VentaService ventaService,
            VendedorRepository vendedorRepository) {
        this.ventaService = ventaService;
        this.vendedorRepository = vendedorRepository;
    }

    // Entidad -> DTO
    private VentaResponseDTO convertirADTO(Venta venta) {
        VentaResponseDTO dto = new VentaResponseDTO();

        dto.setIdVenta(venta.getId());
        dto.setNombreVendedor(
                venta.getVendedor().getNombre() + " "
                        + venta.getVendedor().getApellido()
        );
        dto.setFechaHora(venta.getFechaHora());
        dto.setTotal(venta.getTotal());
        dto.setMetodoPago(venta.getMetodoPago());

        return dto;
    }

    // 1. POST: Registrar una nueva venta
    @PostMapping
    public ResponseEntity<?> registrarVenta(
            @RequestBody VentaRequestDTO request,
            Principal principal) {

        try {
            // Obtener al vendedor mediante el usuario autenticado
            Vendedor vendedor = vendedorRepository
                    .findByUsuario_Username(principal.getName())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "No se encontró un vendedor asociado al usuario."
                            ));

            // Registrar la venta con el vendedor autenticado
            Venta nuevaVenta = ventaService.registrarVenta(
                    vendedor.getId(),
                    request.getItems()
            );

            return new ResponseEntity<>(
                    convertirADTO(nuevaVenta),
                    HttpStatus.CREATED
            );

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    // 2. GET: Listar todas las ventas
    @GetMapping
    public ResponseEntity<List<VentaResponseDTO>> listarTodas() {

        List<VentaResponseDTO> respuesta =
                ventaService.listarTodasLasVentas()
                        .stream()
                        .map(this::convertirADTO)
                        .collect(Collectors.toList());

        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    // 3. GET: Listar ventas de un vendedor específico
    @GetMapping("/vendedor/{idVendedor}")
    public ResponseEntity<List<VentaResponseDTO>> listarPorVendedor(
            @PathVariable Long idVendedor) {

        List<VentaResponseDTO> respuesta =
                ventaService.listarVentasVendedor(idVendedor)
                        .stream()
                        .map(this::convertirADTO)
                        .collect(Collectors.toList());

        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    // 4. GET: Listar ventas de un día específico
    @GetMapping("/dia")
    public ResponseEntity<List<VentaResponseDTO>> listarPorDia(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {

        List<VentaResponseDTO> respuesta =
                ventaService.listarVentasPorDia(fecha)
                        .stream()
                        .map(this::convertirADTO)
                        .collect(Collectors.toList());

        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    // 5. GET: Obtener el total vendido por cada vendedor en un día
    @GetMapping("/totales")
    public ResponseEntity<?> totalesPorVendedor(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {

        return new ResponseEntity<>(
                ventaService.calcularVentasPorVendedor(fecha),
                HttpStatus.OK
        );
    }

    // 6. GET: Listar ventas por rango de fechas
    @GetMapping("/rango")
    public ResponseEntity<?> listarPorRango(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaInicio,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaFin) {

        try {
            if (fechaInicio.isAfter(fechaFin)) {
                return new ResponseEntity<>(
                        "La fecha de inicio no puede ser posterior a la fecha final.",
                        HttpStatus.BAD_REQUEST
                );
            }

            List<VentaResponseDTO> respuesta =
                    ventaService.listarVentasPorRango(fechaInicio, fechaFin)
                            .stream()
                            .map(this::convertirADTO)
                            .collect(Collectors.toList());

            return new ResponseEntity<>(respuesta, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Ocurrió un error interno al consultar las ventas.",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
