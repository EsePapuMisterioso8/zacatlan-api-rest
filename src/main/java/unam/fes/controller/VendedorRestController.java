
package unam.fes.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import unam.fes.entity.vendedor.VendedorDTO;
import unam.fes.service.VendedorService;

import java.util.List;

@RestController
@RequestMapping("/api/vendedores")
public class VendedorRestController {

    private final VendedorService vendedorService;

    public VendedorRestController(VendedorService vendedorService) {
        this.vendedorService = vendedorService;
    }

    // Consultar solicitudes pendientes (solo administrador)
    @GetMapping("/pendientes")
    public ResponseEntity<?> listarSolicitudesPendientes() {
        return ResponseEntity.ok(
                vendedorService.listarSolicitudesPendientes()
        );
    }

    // Buscar vendedores aprobados por nombre o apellido
    @GetMapping("/buscar")
    public ResponseEntity<List<VendedorDTO>> buscarVendedores(
            @RequestParam String texto) {

        List<VendedorDTO> vendedores = vendedorService
                .buscarVendedoresAprobadosPorNombre(texto)
                .stream()
                .map(this::convertirADTO)
                .toList();

        return ResponseEntity.ok(vendedores);
    }

    // Consultar vendedores aprobados (solo administrador)
    @GetMapping
    public ResponseEntity<List<VendedorDTO>> listarVendedoresAprobados() {

        List<VendedorDTO> vendedores = vendedorService
                .listarVendedoresAprobados()
                .stream()
                .map(this::convertirADTO)
                .toList();

        return ResponseEntity.ok(vendedores);
    }

    // Convertir entidad a DTO
    private VendedorDTO convertirADTO(unam.fes.entity.vendedor.Vendedor vendedor) {
        return VendedorDTO.builder()
                .id(vendedor.getId())
                .nombre(vendedor.getNombre())
                .apellido(vendedor.getApellido())
                .username(
                        vendedor.getUsuario() != null
                                ? vendedor.getUsuario().getUsername()
                                : vendedor.getUsernameSolicitado()
                )
                .telefono(vendedor.getTelefono())
                .nombreBloque(
                        vendedor.getBloque() != null
                                ? vendedor.getBloque().getNombre()
                                : null
                )
                .estadoSolicitud(vendedor.getEstadoSolicitud())
                .activo(vendedor.getActivo())
                .build();
    }

    // Registrar solicitud
    @PostMapping("/registro")
    public ResponseEntity<String> registrar(
            @RequestBody RegistroRequestDTO request) {
        try {
            vendedorService.registrarSolicitud(
                    request.getNombre(),
                    request.getApellido(),
                    request.getTelefono(),
                    request.getIdBloque(),
                    request.getUsername(),
                    request.getPassword()
            );

            return new ResponseEntity<>(
                    "Solicitud registrada exitosamente. Esperando aprobación",
                    HttpStatus.CREATED
            );
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    e.getMessage(), HttpStatus.BAD_REQUEST
            );
        }
    }

    // Aprobar solicitud
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<String> aprobar(@PathVariable Long id) {
        try {
            vendedorService.aprobarSolicitud(id);

            return new ResponseEntity<>(
                    "Solicitud aprobada y usuario creado exitosamente",
                    HttpStatus.OK
            );
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    e.getMessage(), HttpStatus.BAD_REQUEST
            );
        }
    }

    // Rechazar solicitud
    @PutMapping("/{id}/rechazar")
    public ResponseEntity<String> rechazar(@PathVariable Long id) {
        try {
            vendedorService.rechazarSolicitud(id);

            return new ResponseEntity<>(
                    "Solicitud rechazada", HttpStatus.OK
            );
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    e.getMessage(), HttpStatus.BAD_REQUEST
            );
        }
    }

    // Activar o desactivar vendedor
    @PutMapping("/{id}/estado")
    public ResponseEntity<String> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activar) {
        try {
            if (activar) {
                vendedorService.activarVendedor(id);

                return new ResponseEntity<>(
                        "Vendedor activado exitosamente",
                        HttpStatus.OK
                );
            } else {
                vendedorService.desactivarVendedor(id);

                return new ResponseEntity<>(
                        "Vendedor desactivado exitosamente",
                        HttpStatus.OK
                );
            }
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    e.getMessage(), HttpStatus.BAD_REQUEST
            );
        }
    }

    // Modificar únicamente nombre, apellido y teléfono
    @PutMapping("/{id}")
    public ResponseEntity<String> modificarDatos(
            @PathVariable Long id,
            @RequestBody ModificarVendedorDTO request) {
        try {
            vendedorService.modificarDatosVendedor(
                    id,
                    request.getNombre(),
                    request.getApellido(),
                    request.getTelefono()
            );

            return new ResponseEntity<>(
                    "Datos del vendedor modificados exitosamente",
                    HttpStatus.OK
            );
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    e.getMessage(), HttpStatus.BAD_REQUEST
            );
        }
    }

    public static class RegistroRequestDTO {

        private String nombre;
        private String apellido;
        private String telefono;
        private Long idBloque;
        private String username;
        private String password;

        public String getNombre() { return nombre; }
        public String getApellido() { return apellido; }
        public String getTelefono() { return telefono; }
        public Long getIdBloque() { return idBloque; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public void setApellido(String apellido) {
            this.apellido = apellido;
        }

        public void setTelefono(String telefono) {
            this.telefono = telefono;
        }

        public void setIdBloque(Long idBloque) {
            this.idBloque = idBloque;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class ModificarVendedorDTO {

        private String nombre;
        private String apellido;
        private String telefono;

        public String getNombre() {
            return nombre;
        }

        public String getApellido() {
            return apellido;
        }

        public String getTelefono() {
            return telefono;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public void setApellido(String apellido) {
            this.apellido = apellido;
        }

        public void setTelefono(String telefono) {
            this.telefono = telefono;
        }
    }
}
