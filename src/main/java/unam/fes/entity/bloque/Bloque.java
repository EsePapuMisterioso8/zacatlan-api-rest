package unam.fes.entity.bloque;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import unam.fes.entity.producto.Producto;
import unam.fes.entity.vendedor.Vendedor;

import java.util.List;

@Entity
@Table(name = "bloques")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bloque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bloque")
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false)
    private Boolean activo;

    @JsonIgnore
    @OneToMany(mappedBy = "bloque",
            fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Vendedor> vendedores;

    @JsonIgnore
    @OneToMany(mappedBy = "bloque",
            fetch = FetchType.LAZY
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Producto> productos;

}
