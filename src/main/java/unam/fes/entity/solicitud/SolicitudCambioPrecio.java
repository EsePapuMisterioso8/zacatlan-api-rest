
package unam.fes.entity.solicitud;

import jakarta.persistence.*;
import lombok.*;
import unam.fes.entity.proven.ProductoVendedor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_cambio_precio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudCambioPrecio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto_vendedor", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ProductoVendedor productoVendedor;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioSolicitado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado;

    @Column(nullable = false)
    private LocalDateTime fechaSolicitud;

    private LocalDateTime fechaRespuesta;

    @Column(length = 255)
    private String observacionAdmin;

    public enum EstadoSolicitud {
        PENDIENTE,
        AUTORIZADA,
        RECHAZADA,
        APLICADA
    }
}
