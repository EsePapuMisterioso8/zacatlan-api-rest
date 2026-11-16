
package unam.fes.entity.vendedor;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendedorDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String username;
    private String telefono;
    private String nombreBloque;
    private String estadoSolicitud;
    private Boolean activo;
}
