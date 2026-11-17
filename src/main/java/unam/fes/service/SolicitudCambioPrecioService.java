
package unam.fes.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unam.fes.entity.proven.ProductoVendedor;
import unam.fes.entity.solicitud.SolicitudCambioPrecio;
import unam.fes.repository.ProductoVendedorRepository;
import unam.fes.repository.SolicitudCambioPrecioRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SolicitudCambioPrecioService {

    private final SolicitudCambioPrecioRepository solicitudRepository;
    private final ProductoVendedorRepository productoVendedorRepository;
    private final HistorialMovimientosService historialService;

    public SolicitudCambioPrecioService(
            SolicitudCambioPrecioRepository solicitudRepository,
            ProductoVendedorRepository productoVendedorRepository,
            HistorialMovimientosService historialService) {

        this.solicitudRepository = solicitudRepository;
        this.productoVendedorRepository = productoVendedorRepository;
        this.historialService = historialService;
    }

    @Transactional
    public void solicitarCambioPrecio(
            Long idVendedor,
            Long idProductoVendedor,
            BigDecimal precioSolicitado) {

        if (precioSolicitado == null ||
                precioSolicitado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El precio solicitado debe ser mayor que cero.");
        }

        ProductoVendedor productoVendedor =
                productoVendedorRepository.findById(idProductoVendedor)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Producto del vendedor no encontrado."));

        if (!productoVendedor.getVendedor().getId().equals(idVendedor)) {
            throw new IllegalArgumentException(
                    "No puedes modificar productos de otro vendedor.");
        }

        if (!Boolean.TRUE.equals(productoVendedor.getVendedor().getActivo())) {
            throw new IllegalArgumentException(
                    "No puedes solicitar cambios porque tu cuenta está inactiva.");
        }

        if (!Boolean.TRUE.equals(productoVendedor.getActivo())
                || !Boolean.TRUE.equals(productoVendedor.getProducto().getActivo())) {
            throw new IllegalArgumentException(
                    "No puedes solicitar cambios para un producto inactivo.");
        }

        if (productoVendedor.getPrecio() == null) {
            throw new IllegalArgumentException(
                    "El producto no tiene un precio actual válido.");
        }

        if (precioSolicitado.compareTo(productoVendedor.getPrecio()) == 0) {
            throw new IllegalArgumentException(
                    "El nuevo precio debe ser diferente al actual.");
        }

        boolean tieneSolicitudPendiente =
                solicitudRepository.findByProductoVendedorVendedorIdAndEstado(
                        idVendedor,
                        SolicitudCambioPrecio.EstadoSolicitud.PENDIENTE
                ).stream().anyMatch(solicitud ->
                        solicitud.getProductoVendedor().getId()
                                .equals(idProductoVendedor));

        if (tieneSolicitudPendiente) {
            throw new IllegalArgumentException(
                    "Ya tienes una solicitud pendiente para este producto.");
        }

        SolicitudCambioPrecio solicitud = SolicitudCambioPrecio.builder()
                .productoVendedor(productoVendedor)
                .precioSolicitado(precioSolicitado)
                .estado(SolicitudCambioPrecio.EstadoSolicitud.PENDIENTE)
                .fechaSolicitud(LocalDateTime.now())
                .build();

        solicitudRepository.save(solicitud);
    }

    public List<SolicitudCambioPrecio> listarSolicitudesVendedor(Long idVendedor) {
        return solicitudRepository.findByProductoVendedorVendedorId(idVendedor);
    }

    public List<SolicitudCambioPrecio> listarSolicitudesPendientes() {
        return solicitudRepository.findByEstado(
                SolicitudCambioPrecio.EstadoSolicitud.PENDIENTE
        );
    }

    @Transactional
    public void autorizarSolicitud(
            Long idSolicitud,
            String observacion,
            String usernameAdministrador) {

        SolicitudCambioPrecio solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La solicitud no existe."));

        if (solicitud.getEstado() != SolicitudCambioPrecio.EstadoSolicitud.PENDIENTE) {
            throw new IllegalArgumentException(
                    "Esta solicitud ya fue atendida.");
        }

        ProductoVendedor productoVendedor = solicitud.getProductoVendedor();

        if (!Boolean.TRUE.equals(productoVendedor.getActivo())
                || !Boolean.TRUE.equals(productoVendedor.getProducto().getActivo())) {
            throw new IllegalArgumentException(
                    "No se puede autorizar el cambio porque el producto está inactivo.");
        }

        if (productoVendedor.getPrecio() == null) {
            throw new IllegalArgumentException(
                    "No se puede autorizar el cambio porque el producto no tiene un precio actual válido.");
        }

        BigDecimal precioAnterior = productoVendedor.getPrecio();
        BigDecimal precioNuevo = solicitud.getPrecioSolicitado();

        productoVendedor.setPrecio(precioNuevo);
        productoVendedorRepository.save(productoVendedor);

        solicitud.setEstado(SolicitudCambioPrecio.EstadoSolicitud.APLICADA);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        solicitud.setObservacionAdmin(observacion);

        solicitudRepository.save(solicitud);

        historialService.registrarMovimiento(
                usernameAdministrador,
                "AUTORIZACION",
                "CAMBIO_PRECIO",
                solicitud.getId(),
                "Se autorizó el cambio de precio de "
                        + productoVendedor.getProducto().getNombre(),
                "Precio anterior: $" + precioAnterior.toPlainString(),
                "Precio nuevo: $" + precioNuevo.toPlainString()
                        + ". Observación: "
                        + (observacion == null ? "Sin observación" : observacion)
        );
    }

    @Transactional
    public void rechazarSolicitud(
            Long idSolicitud,
            String observacion,
            String usernameAdministrador) {

        SolicitudCambioPrecio solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La solicitud no existe."));

        if (solicitud.getEstado() != SolicitudCambioPrecio.EstadoSolicitud.PENDIENTE) {
            throw new IllegalArgumentException(
                    "Esta solicitud ya fue atendida.");
        }

        ProductoVendedor productoVendedor = solicitud.getProductoVendedor();

        BigDecimal precioActual = productoVendedor.getPrecio();

        solicitud.setEstado(SolicitudCambioPrecio.EstadoSolicitud.RECHAZADA);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        solicitud.setObservacionAdmin(observacion);

        solicitudRepository.save(solicitud);

        historialService.registrarMovimiento(
                usernameAdministrador,
                "RECHAZO",
                "CAMBIO_PRECIO",
                solicitud.getId(),
                "Se rechazó el cambio de precio de "
                        + productoVendedor.getProducto().getNombre(),
                "Precio actual: $"
                        + (precioActual == null ? "No disponible" : precioActual.toPlainString()),
                "Precio sin cambios. Precio solicitado: $"
                        + solicitud.getPrecioSolicitado().toPlainString()
                        + ". Observación: "
                        + (observacion == null ? "Sin observación" : observacion)
        );
    }
}
