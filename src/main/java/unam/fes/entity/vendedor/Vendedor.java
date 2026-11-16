package unam.fes.entity.vendedor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import unam.fes.entity.bloque.Bloque;
import unam.fes.entity.proven.ProductoVendedor;
import unam.fes.entity.usuario.Usuario;
import unam.fes.entity.venta.Venta;


import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "vendedores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vendedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vendedor")
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_usuario",unique = true, nullable = true)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_bloque", nullable = false )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Bloque bloque;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(length = 10)
    private String telefono;

    @Column(nullable = false, length = 20)
    private String estadoSolicitud;

    @Column(nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column
    private LocalDateTime fechaAlta;

    @Column
    private LocalDateTime fechaDesactivacion;

    @Column(length = 255)
    private String motivoDesactivacion;

    @Column(nullable = false)
    private Boolean activo;

    @Column(length = 50, unique = true)
    private String usernameSolicitado;

    @JsonIgnore
    @Column(length = 255)
    private String passwordSolicitado;

    @JsonIgnore
    @OneToMany(mappedBy = "vendedor",
            fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ProductoVendedor> productos;

    @OneToMany(mappedBy = "vendedor",
            fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private List<Venta> ventas;
}
