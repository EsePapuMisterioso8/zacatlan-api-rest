
package unam.fes.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import unam.fes.entity.venta.Venta;
import unam.fes.entity.venta.VentaRequestDTO;
import unam.fes.entity.venta.VentaResponseDTO;
import unam.fes.entity.venta.VentaDetalleResponseDTO;
import unam.fes.entity.vendedor.Vendedor;
import unam.fes.repository.VendedorRepository;
import unam.fes.service.VentaService;
import unam.fes.service.ReportePdfService;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ventas")
public class VentaRestController {

    private final VentaService ventaService;
    private final VendedorRepository vendedorRepository;
    private final ReportePdfService reportePdfService;

    public VentaRestController(
            VentaService ventaService,
            VendedorRepository vendedorRepository,
            ReportePdfService reportePdfService) {
        this.ventaService = ventaService;
        this.vendedorRepository = vendedorRepository;
        this.reportePdfService = reportePdfService;
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

    // 1. Registrar una nueva venta
    @PostMapping
    public ResponseEntity<?> registrarVenta(
            @RequestBody VentaRequestDTO request,
            Principal principal) {

        try {
            Vendedor vendedor = vendedorRepository
                    .findByUsuario_Username(principal.getName())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "No se encontró un vendedor asociado al usuario."
                            ));

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

    // 2. Listar todas las ventas
    @GetMapping
    public ResponseEntity<List<VentaResponseDTO>> listarTodas() {

        List<VentaResponseDTO> respuesta =
                ventaService.listarTodasLasVentas()
                        .stream()
                        .map(this::convertirADTO)
                        .collect(Collectors.toList());

        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    // 3. Listar ventas del vendedor autenticado
    @GetMapping("/vendedor")
    public ResponseEntity<?> listarPorVendedor(Principal principal) {

        try {
            Vendedor vendedor = vendedorRepository
                    .findByUsuario_Username(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No se encontró al vendedor autenticado"
                    ));

            List<VentaResponseDTO> respuesta =
                    ventaService.listarVentasVendedor(vendedor.getId())
                            .stream()
                            .map(this::convertirADTO)
                            .collect(Collectors.toList());

            return new ResponseEntity<>(respuesta, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. Consultar una venta por ID con sus detalles
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            VentaDetalleResponseDTO detalle =
                    ventaService.obtenerDetalleVenta(id);

            boolean esAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a ->
                            a.getAuthority().equals("ADMINISTRADOR")
                                    || a.getAuthority().equals("ROLE_ADMINISTRADOR")
                    );

            if (!esAdmin) {
                Vendedor vendedor = vendedorRepository
                        .findByUsuario_Username(authentication.getName())
                        .orElse(null);

                if (vendedor == null
                        || !vendedor.getId().equals(detalle.getIdVendedor())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("No tienes permiso para consultar esta venta.");
                }
            }

            return ResponseEntity.ok(detalle);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    // 5. Listar ventas de un día específico
    @GetMapping("/dia")
    public ResponseEntity<?> listarPorDia(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {

        if (fecha.isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest()
                    .body("No se pueden consultar ventas de fechas futuras.");
        }

        List<VentaResponseDTO> respuesta =
                ventaService.listarVentasPorDia(fecha)
                        .stream()
                        .map(this::convertirADTO)
                        .collect(Collectors.toList());

        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    // 6. Obtener el total vendido por cada vendedor en un día
    @GetMapping("/totales")
    public ResponseEntity<?> totalesPorVendedor(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {

        if (fecha.isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest()
                    .body("No se pueden consultar totales de fechas futuras.");
        }

        return new ResponseEntity<>(
                ventaService.calcularVentasPorVendedor(fecha),
                HttpStatus.OK
        );
    }

    // 7. Listar ventas por rango de fechas
    @GetMapping("/rango")
    public ResponseEntity<?> listarPorRango(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaInicio,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaFin) {

        if (fechaInicio.isAfter(fechaFin)) {
            return ResponseEntity.badRequest()
                    .body("La fecha de inicio no puede ser posterior a la fecha final.");
        }

        if (fechaFin.isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest()
                    .body("No se pueden consultar rangos que incluyan fechas futuras.");
        }

        try {
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

    // 8. Listar ventas de un vendedor específico
    @GetMapping("/vendedor/{idVendedor}")
    public ResponseEntity<?> listarPorVendedorId(
            @PathVariable Long idVendedor) {

        if (!vendedorRepository.existsById(idVendedor)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("El vendedor no existe.");
        }

        List<VentaResponseDTO> respuesta =
                ventaService.listarVentasVendedor(idVendedor)
                        .stream()
                        .map(this::convertirADTO)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(respuesta);
    }

    // 9. Listar ventas de un vendedor dentro de un rango de fechas
    @GetMapping("/vendedor/{idVendedor}/rango")
    public ResponseEntity<?> listarPorVendedorYRango(
            @PathVariable Long idVendedor,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaInicio,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaFin) {

        if (!vendedorRepository.existsById(idVendedor)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("El vendedor no existe.");
        }

        if (fechaInicio.isAfter(fechaFin)) {
            return ResponseEntity.badRequest()
                    .body("La fecha de inicio no puede ser posterior a la fecha final.");
        }

        if (fechaFin.isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest()
                    .body("No se pueden consultar rangos que incluyan fechas futuras.");
        }

        List<VentaResponseDTO> respuesta =
                ventaService.listarVentasPorVendedorYRango(
                                idVendedor,
                                fechaInicio,
                                fechaFin
                        )
                        .stream()
                        .map(this::convertirADTO)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(respuesta);
    }

    // 10. Generar PDF del reporte diario (solo administrador)
    @GetMapping("/reporte/pdf")
    @Transactional(readOnly = true)
    public ResponseEntity<?> generarReportePdf(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,
            Authentication authentication) {

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ADMINISTRADOR")
                                || a.getAuthority().equals("ROLE_ADMINISTRADOR")
                );

        if (!esAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes permiso para generar reportes.");
        }

        if (fecha.isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest()
                    .body("No se pueden generar reportes de fechas futuras.");
        }

        try {
            List<Venta> ventas = ventaService.listarVentasPorDia(fecha);

            BigDecimal totalDia = ventas.stream()
                    .map(Venta::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, BigDecimal> ventasPorVendedor =
                    ventaService.calcularVentasPorVendedor(fecha);

            byte[] pdf = reportePdfService.generarReporteDiario(
                    fecha,
                    ventas,
                    totalDia,
                    ventasPorVendedor
            );

            String nombreArchivo = "reporte-ventas-" + fecha + ".pdf";

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + nombreArchivo + "\""
                    )
                    .body(pdf);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ocurrió un error al generar el reporte PDF.");
        }
    }
}
