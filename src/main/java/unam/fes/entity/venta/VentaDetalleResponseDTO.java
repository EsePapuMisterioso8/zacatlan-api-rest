
package unam.fes.entity.venta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaDetalleResponseDTO {

    private Long idVenta;
    private String nombreVendedor;
    private LocalDateTime fechaHora;
    private BigDecimal total;
    private String metodoPago;
    private String estado;

    @JsonIgnore
    private Long idVendedor;

    private List<ItemDetalleDTO> detalles;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemDetalleDTO {

        private String nombreProducto;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subTotal;
    }
}
