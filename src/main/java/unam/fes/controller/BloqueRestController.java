
package unam.fes.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import unam.fes.entity.bloque.Bloque;
import unam.fes.service.BloqueService;

import java.util.List;

@RestController
@RequestMapping("/api/bloques")
public class BloqueRestController {

    @Autowired
    private BloqueService bloqueService;

    // Obtener todos los bloques
    @GetMapping
    public ResponseEntity<List<Bloque>> listarBloques() {

        List<Bloque> bloques = bloqueService.listarTodos();

        if (bloques.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(bloques, HttpStatus.OK);
    }

    // Crear un nuevo bloque
    @PostMapping
    public ResponseEntity<String> crear(
            @RequestBody Bloque bloque,
            java.security.Principal principal) {

        try {
            String admin = principal.getName();

            bloqueService.crearBloque(
                    bloque.getNombre(),
                    bloque.getDescripcion(),
                    admin
            );

            return new ResponseEntity<>(
                    "Bloque creado exitosamente",
                    HttpStatus.CREATED
            );

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    // Modificar nombre y descripción de un bloque
    @PutMapping("/{id}")
    public ResponseEntity<String> modificarBloque(
            @PathVariable Long id,
            @RequestBody Bloque bloque,
            java.security.Principal principal) {

        try {
            String admin = principal.getName();

            bloqueService.modificarBloque(
                    id,
                    bloque.getNombre(),
                    bloque.getDescripcion(),
                    admin
            );

            return ResponseEntity.ok(
                    "Bloque modificado exitosamente."
            );

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    // Activar o desactivar un bloque
    @PutMapping("/{id}/estado")
    public ResponseEntity<String> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activar,
            java.security.Principal principal) {

        try {
            String admin = principal.getName();

            bloqueService.cambiarEstado(id, activar, admin);

            String action = activar ? "activado" : "desactivado";

            return new ResponseEntity<>(
                    "El bloque fue " + action + " exitosamente.",
                    HttpStatus.OK
            );

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}
