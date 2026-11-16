package unam.fes.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unam.fes.entity.venta.Venta;
import unam.fes.service.ReportePdfService;
import unam.fes.service.VentaService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
public class ReportePdfRestController {

    private final ReportePdfService reportePdfService;
    private final VentaService ventaService;

    public ReportePdfRestController(ReportePdfService reportePdfService, VentaService ventaService) {
        this.reportePdfService = reportePdfService;
        this.ventaService = ventaService;
    }

    @GetMapping("/diario")
    public ResponseEntity<byte[]> descargarReporteDiario(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        try {
            // Si el cliente no manda fecha, usamos la de hoy por defecto
            if (fecha == null) {
                fecha = LocalDate.now();
            }

            // 1. Recopilar la información usando el VentaService
            List<Venta> ventasDelDia = ventaService.listarVentasPorDia(fecha);
            Map<String, BigDecimal> ventasPorVendedor = ventaService.calcularVentasPorVendedor(fecha);

            // 2. Calcular el gran total del día sumando todas las ventas
            BigDecimal totalDia = ventasDelDia.stream()
                    .map(Venta::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 3. Generar los bytes del documento PDF
            byte[] documentoPdf = reportePdfService.generarReporteDiario(
                    fecha, ventasDelDia, totalDia, ventasPorVendedor
            );

            // 4. Configurar los encabezados para forzar la descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            // Esto le da el nombre dinámico al archivo cuando se descarga
            headers.setContentDispositionFormData("attachment", "Reporte_Ventas_" + fecha + ".pdf");

            return new ResponseEntity<>(documentoPdf, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}