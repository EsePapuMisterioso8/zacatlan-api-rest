package unam.fes.entity.usuario;

import jakarta.persistence.*;
import lombok.*;
import unam.fes.entity.historial.HistorialMovimientos;
import unam.fes.entity.vendedor.Vendedor;


import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false , length = 255)
    private String password;

    @Column(nullable = false, length = 20)
    private String rol;

    @Column(nullable = false)
    private Boolean activo;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    @Column
    private LocalDateTime fechaUltimaSesion;

    @OneToOne(mappedBy = "usuario")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Vendedor vendedor;

    @OneToMany(mappedBy = "administrador",  fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<HistorialMovimientos> movimientos;
}
