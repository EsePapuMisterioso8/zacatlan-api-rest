package unam.fes.entity.proven;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import unam.fes.entity.detalle.DetalleVenta;
import unam.fes.entity.producto.Producto;
import unam.fes.entity.vendedor.Vendedor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "productos_vendedor",
        uniqueConstraints = {
            @UniqueConstraint(
                    columnNames = {"id_vendedor","id_producto"}
            )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoVendedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto_vendedor")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vendedor" , nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Vendedor vendedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto" , nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Producto producto;

    @Column(nullable = false , precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private Boolean activo;


    @OneToMany(mappedBy = "productoVendedor", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<DetalleVenta> detalles;
}
