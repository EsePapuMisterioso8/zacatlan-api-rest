
package unam.fes.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unam.fes.entity.historial.HistorialMovimientos;
import unam.fes.entity.usuario.Usuario;
import unam.fes.repository.HistorialMovimientosRepository;
import unam.fes.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HistorialMovimientosService {

    private final HistorialMovimientosRepository historialRepository;
    private final UsuarioRepository usuarioRepository;

    public HistorialMovimientosService(
            HistorialMovimientosRepository historialRepository,
            UsuarioRepository usuarioRepository) {

        this.historialRepository = historialRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void registrarMovimiento(
            String usernameAdministrador,
            String accion,
            String entidad,
            Long idRegistro,
            String descripcion,
            String datosAnteriores,
            String datosNuevos) {

        Usuario administrador = usuarioRepository
                .findByUsername(usernameAdministrador)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró al administrador."));

        if (!"ADMINISTRADOR".equals(administrador.getRol())) {
            throw new IllegalArgumentException(
                    "El usuario no tiene permisos de administrador.");
        }

        HistorialMovimientos movimiento = HistorialMovimientos.builder()
                .administrador(administrador)
                .accion(accion)
                .entidad(entidad)
                .idRegistro(idRegistro)
                .descripcion(descripcion)
                .datosAnteriores(datosAnteriores)
                .datosNuevos(datosNuevos)
                .fechaHora(LocalDateTime.now())
                .build();

        historialRepository.save(movimiento);
    }

    public List<HistorialMovimientos> listarHistorial() {
        return historialRepository.findAllByOrderByFechaHoraDesc();
    }

    public List<HistorialMovimientos> listarHistorialAdministrador(
            Long idAdministrador) {

        return historialRepository
                .findByAdministradorIdOrderByFechaHoraDesc(idAdministrador);
    }
}
