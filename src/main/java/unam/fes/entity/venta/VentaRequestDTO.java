package unam.fes.entity.venta;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class VentaRequestDTO {

    private Long idVendedor;
    private List<ItemVenta> items;

    // Getters y Setters
    public Long getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(Long idVendedor) {
        this.idVendedor = idVendedor;
    }

    public List<ItemVenta> getItems() {
        return items;
    }

    public void setItems(List<ItemVenta> items) {
        this.items = items;
    }

    /* --- CLASE AUXILIAR PARA EL CARRITO --- */
    public static class ItemVenta {
        private Long idProductoVendedor;
        private Integer cantidad;
        private BigDecimal precioVenta; // El precio negociado (opcional)

        // Getters y Setters
        public Long getIdProductoVendedor() {
            return idProductoVendedor;
        }

        public void setIdProductoVendedor(Long idProductoVendedor) {
            this.idProductoVendedor = idProductoVendedor;
        }

        public Integer getCantidad() {
            return cantidad;
        }

        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }

        public BigDecimal getPrecioVenta() {
            return precioVenta;
        }

        public void setPrecioVenta(BigDecimal precioVenta) {
            this.precioVenta = precioVenta;
        }
    }
}
