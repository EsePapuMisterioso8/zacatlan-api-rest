
package unam.fes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unam.fes.entity.solicitud.SolicitudCambioPrecio;

import java.util.List;

public interface SolicitudCambioPrecioRepository
        extends JpaRepository<SolicitudCambioPrecio, Long> {

    // Consultar las solicitudes de un vendedor
    List<SolicitudCambioPrecio>
    findByProductoVendedorVendedorId(Long idVendedor);

    // Consultar las solicitudes pendientes
    List<SolicitudCambioPrecio>
    findByEstado(SolicitudCambioPrecio.EstadoSolicitud estado);

    // Consultar las solicitudes pendientes de un vendedor
    List<SolicitudCambioPrecio>
    findByProductoVendedorVendedorIdAndEstado(
            Long idVendedor,
            SolicitudCambioPrecio.EstadoSolicitud estado
    );
}
