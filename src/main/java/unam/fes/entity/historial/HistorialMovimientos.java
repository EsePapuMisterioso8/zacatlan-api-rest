package unam.fes.entity.historial;

import jakarta.persistence.*;
import lombok.*;
import unam.fes.entity.usuario.Usuario;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_movimientos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialMovimientos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_administrador", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario administrador;

    @Column(nullable = false,length = 50)
    private String accion;

    @Column(nullable = false, length = 50)
    private String entidad;

    @Column(name = "id_registro")
    private  Long idRegistro;

    @Column(nullable = false, length = 500)
    private String descripcion;


    @Column(name = "datos_anteriores", columnDefinition = "TEXT")
    private String datosAnteriores;


    @Column(name = "datos_nuevos" ,columnDefinition = "TEXT")
    private String datosNuevos;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

}


