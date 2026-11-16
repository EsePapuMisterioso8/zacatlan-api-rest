package unam.fes.entity.venta;

import jakarta.persistence.*;
import lombok.*;
import unam.fes.entity.detalle.DetalleVenta;
import unam.fes.entity.vendedor.Vendedor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ventas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vendedor", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Vendedor vendedor;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false , precision = 12, scale = 2)
    private BigDecimal total;

    @Column(nullable = false, length = 20)
    private String metodoPago;

    @Column(nullable = false,length = 20)
    private String estado;

    @OneToMany(mappedBy = "venta", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<DetalleVenta> detalles;
}
