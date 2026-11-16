package unam.fes.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unam.fes.service.VendedorService;

@RestController
@RequestMapping("/api/vendedores")
public class VendedorRestController {
    private final VendedorService vendedorService;

    public VendedorRestController(VendedorService vendedorService) {
        this.vendedorService = vendedorService;
    }

    //Registrar solicitud
    @PostMapping("/registro")
    public ResponseEntity<String> registrar(@RequestBody RegistroRequestDTO request){
        try {
            vendedorService.registrarSolicitud(
                    request.getNombre(),
                    request.getApellido(),
                    request.getTelefono(),
                    request.getIdBloque(),
                    request.getUsername(),
                    request.getPassword()
            );
            return new ResponseEntity<>("Solicitud registrada exitosamente. Esperando aprobación", HttpStatus.CREATED);
        } catch (IllegalArgumentException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    //Para aprobar la solicitud (solo para el admin)
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<String> aprobar(@PathVariable Long id){
        try {
            vendedorService.aprobarSolicitud(id);
            return new ResponseEntity<>("Solicitud aprobada y usuario creado exitosamente", HttpStatus.OK);
        } catch (IllegalArgumentException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    //Para rechazar la solicitud
    @PutMapping("/{id}/rechazar")
    public ResponseEntity<String> rechazar(@PathVariable Long id){
        try {
            vendedorService.rechazarSolicitud(id);
            return new ResponseEntity<>("Solicitud rechazada", HttpStatus.OK);
        } catch (IllegalArgumentException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    //Para activar o desactivar vendedor
    @PutMapping("/{id}/estado")
    public ResponseEntity<String> cambiarEstado(@PathVariable Long id, @RequestParam boolean activar){
        try {
            if (activar){
                vendedorService.activarVendedor(id);
                return new ResponseEntity<>("Vendedor activado exitosamente", HttpStatus.OK);
            } else {
                vendedorService.desactivarVendedor(id);
                return new ResponseEntity<>("Vendedor desactivado exitosamente", HttpStatus.OK);
            }
        } catch (IllegalArgumentException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    public static class RegistroRequestDTO {
        private String nombre;
        private String apellido;
        private String telefono;
        private Long idBloque;
        private String username;
        private String password;

        // Getters
        public String getNombre() { return nombre; }
        public String getApellido() { return apellido; }
        public String getTelefono() { return telefono; }
        public Long getIdBloque() { return idBloque; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }

        // Setters
        public void setNombre(String nombre) { this.nombre = nombre; }
        public void setApellido(String apellido) { this.apellido = apellido; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
        public void setIdBloque(Long idBloque) { this.idBloque = idBloque; }
        public void setUsername(String username) { this.username = username; }
        public void setPassword(String password) { this.password = password; }
    }
}
