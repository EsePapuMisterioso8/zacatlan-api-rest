
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

    public SolicitudCambioPrecioService(
            SolicitudCambioPrecioRepository solicitudRepository,
            ProductoVendedorRepository productoVendedorRepository) {

        this.solicitudRepository = solicitudRepository;
        this.productoVendedorRepository = productoVendedorRepository;
    }

    // Registrar una solicitud de cambio de precio
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

    // Consultar las solicitudes de un vendedor
    public List<SolicitudCambioPrecio> listarSolicitudesVendedor(Long idVendedor) {
        return solicitudRepository.findByProductoVendedorVendedorId(idVendedor);
    }

    // Consultar todas las solicitudes pendientes
    public List<SolicitudCambioPrecio> listarSolicitudesPendientes() {
        return solicitudRepository.findByEstado(
                SolicitudCambioPrecio.EstadoSolicitud.PENDIENTE
        );
    }

    // Autorizar una solicitud y aplicar el nuevo precio
    @Transactional
    public void autorizarSolicitud(Long idSolicitud, String observacion) {

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

        // Actualizar automáticamente el precio
        productoVendedor.setPrecio(solicitud.getPrecioSolicitado());

        productoVendedorRepository.save(productoVendedor);

        // Marcar la solicitud como aplicada
        solicitud.setEstado(SolicitudCambioPrecio.EstadoSolicitud.APLICADA);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        solicitud.setObservacionAdmin(observacion);

        solicitudRepository.save(solicitud);
    }

    // Rechazar una solicitud
    @Transactional
    public void rechazarSolicitud(Long idSolicitud, String observacion) {

        SolicitudCambioPrecio solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La solicitud no existe."));

        if (solicitud.getEstado() != SolicitudCambioPrecio.EstadoSolicitud.PENDIENTE) {
            throw new IllegalArgumentException(
                    "Esta solicitud ya fue atendida.");
        }

        solicitud.setEstado(SolicitudCambioPrecio.EstadoSolicitud.RECHAZADA);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        solicitud.setObservacionAdmin(observacion);

        solicitudRepository.save(solicitud);
    }
}
