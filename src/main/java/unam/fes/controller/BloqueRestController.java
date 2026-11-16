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

    //Metodo para obtener todos los bloques
    @GetMapping
    public ResponseEntity<List<Bloque>> listarBloques(){
        List<Bloque> bloques = bloqueService.listarTodos();
        if(bloques.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(bloques, HttpStatus.OK);
    }

    //Para crear un nuevo bloque
    @PostMapping
    public ResponseEntity<String> crear(@RequestBody Bloque bloque, java.security.Principal principal){
        try{
            //Trae el usuario que inició sesión
            String admin = principal.getName();

            bloqueService.crearBloque(bloque.getNombre(), bloque.getDescripcion(), admin);

            return new ResponseEntity<>("Bloque creado exitosamente", HttpStatus.CREATED);
        } catch (IllegalArgumentException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    //Para Activar o Desactivar un Bloque
    @PutMapping("/{id}/estado") //NOTA: a menos de que el bloque no tenga ningún producto, no se puede desactivar. Por el momento todos están activos
    public ResponseEntity<String> cambiarEstado(@PathVariable Long id, @RequestParam boolean activar, java.security.Principal principal){
        try{
            String admin = principal.getName();//verifica que sea el administrador
            bloqueService.cambiarEstado(id, activar, admin);

            String action = activar ? "activado" : "desactivado";
            return new ResponseEntity<>("El bloque fue " + action + " exitosamente.", HttpStatus.OK);
        } catch (IllegalArgumentException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
