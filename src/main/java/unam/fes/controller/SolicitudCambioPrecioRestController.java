package unam.fes.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unam.fes.entity.solicitud.SolicitudCambioPrecio;
import unam.fes.service.SolicitudCambioPrecioService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/solicitudes-precio")
public class SolicitudCambioPrecioRestController {

    private final SolicitudCambioPrecioService solicitudService;

    public SolicitudCambioPrecioRestController(SolicitudCambioPrecioService solicitudService) {
        this.solicitudService = solicitudService;
    }

    // --- MÉTODO TRADUCTOR (Entidad -> DTO Seguro) ---
    private SolicitudResponseDTO convertirADTO(SolicitudCambioPrecio s) {
        SolicitudResponseDTO dto = new SolicitudResponseDTO();
        dto.setIdSolicitud(s.getId());
        dto.setIdProductoVendedor(s.getProductoVendedor().getId());
        dto.setNombreProducto(s.getProductoVendedor().getProducto().getNombre());
        dto.setNombreVendedor(s.getProductoVendedor().getVendedor().getNombre() + " " + s.getProductoVendedor().getVendedor().getApellido());
        dto.setPrecioActual(s.getProductoVendedor().getPrecio());
        dto.setPrecioSolicitado(s.getPrecioSolicitado());
        dto.setEstado(s.getEstado().name());
        dto.setFechaSolicitud(s.getFechaSolicitud());
        dto.setFechaRespuesta(s.getFechaRespuesta());
        dto.setObservacionAdmin(s.getObservacionAdmin());
        return dto;
    }

    /* ========================================================
       ZONA DEL VENDEDOR
       ======================================================== */

    // 1. POST: Crear una solicitud de cambio de precio
    @PostMapping
    public ResponseEntity<?> crearSolicitud(@RequestBody NuevaSolicitudDTO request) {
        try {
            solicitudService.solicitarCambioPrecio(
                    request.getIdVendedor(),
                    request.getIdProductoVendedor(),
                    request.getPrecioSolicitado()
            );
            return new ResponseEntity<>("Solicitud enviada a revisión exitosamente.", HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 2. GET: Listar historial de solicitudes del vendedor
    @GetMapping("/vendedor/{idVendedor}")
    public ResponseEntity<List<SolicitudResponseDTO>> misSolicitudes(@PathVariable Long idVendedor) {
        List<SolicitudResponseDTO> respuesta = solicitudService.listarSolicitudesVendedor(idVendedor)
                .stream().map(this::convertirADTO).collect(Collectors.toList());
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    /* ========================================================
       ZONA DEL ADMINISTRADOR
       ======================================================== */

    // 3. GET: Listar todas las solicitudes pendientes
    @GetMapping("/pendientes")
    public ResponseEntity<List<SolicitudResponseDTO>> solicitudesPendientes() {
        List<SolicitudResponseDTO> respuesta = solicitudService.listarSolicitudesPendientes()
                .stream().map(this::convertirADTO).collect(Collectors.toList());
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    // 4. PUT: Autorizar solicitud (Y actualizar el precio automáticamente)
    @PutMapping("/{idSolicitud}/autorizar")
    public ResponseEntity<?> autorizar(@PathVariable Long idSolicitud, @RequestBody ResolucionDTO request) {
        try {
            solicitudService.autorizarSolicitud(idSolicitud, request.getObservacion());
            return new ResponseEntity<>("Solicitud AUTORIZADA y precio actualizado.", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 5. PUT: Rechazar solicitud
    @PutMapping("/{idSolicitud}/rechazar")
    public ResponseEntity<?> rechazar(@PathVariable Long idSolicitud, @RequestBody ResolucionDTO request) {
        try {
            solicitudService.rechazarSolicitud(idSolicitud, request.getObservacion());
            return new ResponseEntity<>("Solicitud RECHAZADA.", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /* ========================================================
       CLASES DTO INTERNAS
       ======================================================== */

    public static class NuevaSolicitudDTO {
        private Long idVendedor;
        private Long idProductoVendedor;
        private BigDecimal precioSolicitado;
        public Long getIdVendedor() { return idVendedor; } public void setIdVendedor(Long idVendedor) { this.idVendedor = idVendedor; }
        public Long getIdProductoVendedor() { return idProductoVendedor; } public void setIdProductoVendedor(Long idProductoVendedor) { this.idProductoVendedor = idProductoVendedor; }
        public BigDecimal getPrecioSolicitado() { return precioSolicitado; } public void setPrecioSolicitado(BigDecimal precioSolicitado) { this.precioSolicitado = precioSolicitado; }
    }

    public static class ResolucionDTO {
        private String observacion;
        public String getObservacion() { return observacion; } public void setObservacion(String observacion) { this.observacion = observacion; }
    }

    public static class SolicitudResponseDTO {
        private Long idSolicitud;
        private Long idProductoVendedor;
        private String nombreProducto;
        private String nombreVendedor;
        private BigDecimal precioActual;
        private BigDecimal precioSolicitado;
        private String estado;
        private LocalDateTime fechaSolicitud;
        private LocalDateTime fechaRespuesta;
        private String observacionAdmin;

        // Agrega los Getters y Setters de todos estos atributos aquí...
        public Long getIdSolicitud() { return idSolicitud; } public void setIdSolicitud(Long idSolicitud) { this.idSolicitud = idSolicitud; }
        public Long getIdProductoVendedor() { return idProductoVendedor; } public void setIdProductoVendedor(Long idProductoVendedor) { this.idProductoVendedor = idProductoVendedor; }
        public String getNombreProducto() { return nombreProducto; } public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
        public String getNombreVendedor() { return nombreVendedor; } public void setNombreVendedor(String nombreVendedor) { this.nombreVendedor = nombreVendedor; }
        public BigDecimal getPrecioActual() { return precioActual; } public void setPrecioActual(BigDecimal precioActual) { this.precioActual = precioActual; }
        public BigDecimal getPrecioSolicitado() { return precioSolicitado; } public void setPrecioSolicitado(BigDecimal precioSolicitado) { this.precioSolicitado = precioSolicitado; }
        public String getEstado() { return estado; } public void setEstado(String estado) { this.estado = estado; }
        public LocalDateTime getFechaSolicitud() { return fechaSolicitud; } public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
        public LocalDateTime getFechaRespuesta() { return fechaRespuesta; } public void setFechaRespuesta(LocalDateTime fechaRespuesta) { this.fechaRespuesta = fechaRespuesta; }
        public String getObservacionAdmin() { return observacionAdmin; } public void setObservacionAdmin(String observacionAdmin) { this.observacionAdmin = observacionAdmin; }
    }
}