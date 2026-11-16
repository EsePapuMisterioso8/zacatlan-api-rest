package unam.fes.entity.venta;

import java.math.BigDecimal;

public class InventarioResponseDTO {
    private Long idProductoVendedor;
    private String nombreProducto;
    private String descripcionProducto;
    private BigDecimal precio;
    private Boolean activo;

    // Getters
    public Long getIdProductoVendedor() { return idProductoVendedor; }
    public String getNombreProducto() { return nombreProducto; }
    public String getDescripcionProducto() { return descripcionProducto; }
    public BigDecimal getPrecio() { return precio; }
    public Boolean getActivo() { return activo; }

    // Setters
    public void setIdProductoVendedor(Long idProductoVendedor) { this.idProductoVendedor = idProductoVendedor; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public void setDescripcionProducto(String descripcionProducto) { this.descripcionProducto = descripcionProducto; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
