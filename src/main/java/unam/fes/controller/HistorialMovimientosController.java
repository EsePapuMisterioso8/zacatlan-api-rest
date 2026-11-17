package unam.fes.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import unam.fes.repository.HistorialMovimientosRepository;
import unam.fes.service.HistorialMovimientosService;


import java.util.List;

@RestController
@RequestMapping("/api/historial")
public class HistorialMovimientosController {

    private final HistorialMovimientosService historialMovimientosService;

    public HistorialMovimientosController(HistorialMovimientosService historialMovimientosService){
        this.historialMovimientosService = historialMovimientosService;
    }

    @GetMapping
    public ResponseEntity<?> listarHistorial(Authentication authentication){

        boolean esAdministrador = authentication.getAuthorities()
                .stream().anyMatch(autoridad -> autoridad.getAuthority().equals("ROLE_ADMINISTRADOR"));
        if(!esAdministrador){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<HistorialResponseDTO> historial  = historialMovimientosService.listarHistorial()
                .stream().map(movimiento ->  new HistorialResponseDTO(
                        movimiento.getId(),
                        movimiento.getAdministrador().getUsername(),
                        movimiento.getAccion(),
                        movimiento.getEntidad(),
                        movimiento.getIdRegistro(),
                        movimiento.getDescripcion(),
                        movimiento.getDatosAnteriores(),
                        movimiento.getDatosNuevos(),
                        movimiento.getFechaHora()
                )).toList();

        return ResponseEntity.ok(historial);


    }

    public record HistorialResponseDTO(

            Long IdMovimiento,
            String administrador,
            String accion,
            String entidad,
            Long idRegistro,
            String descripcion,
            String datosAnteriores,
            String datosNuevos,
            java.time.LocalDateTime fechaHora


    ){

    }



}
