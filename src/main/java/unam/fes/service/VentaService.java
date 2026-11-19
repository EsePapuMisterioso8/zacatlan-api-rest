
package unam.fes.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import unam.fes.entity.detalle.DetalleVenta;
import unam.fes.entity.proven.ProductoVendedor;
import unam.fes.entity.vendedor.Vendedor;
import unam.fes.entity.venta.Venta;
import unam.fes.entity.venta.VentaRequestDTO;
import unam.fes.entity.venta.VentaDetalleResponseDTO;

import unam.fes.repository.DetalleVentaRepository;
import unam.fes.repository.ProductoVendedorRepository;
import unam.fes.repository.VendedorRepository;
import unam.fes.repository.VentaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ProductoVendedorRepository productoVendedorRepository;
    private final VendedorRepository vendedorRepository;

    public VentaService(
            VentaRepository ventaRepository,
            DetalleVentaRepository detalleVentaRepository,
            ProductoVendedorRepository productoVendedorRepository,
            VendedorRepository vendedorRepository) {

        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.productoVendedorRepository = productoVendedorRepository;
        this.vendedorRepository = vendedorRepository;
    }

    @Transactional
    public Venta registrarVenta(Long idVendedor, List<VentaRequestDTO.ItemVenta> items) {

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Debes seleccionar al menos un producto.");
        }

        Vendedor vendedor = vendedorRepository.findById(idVendedor)
                .orElseThrow(() -> new IllegalArgumentException("El vendedor no existe."));

        if (!Boolean.TRUE.equals(vendedor.getActivo())) {
            throw new IllegalArgumentException("El vendedor está inactivo.");
        }

        Venta venta = Venta.builder()
                .vendedor(vendedor)
                .fechaHora(LocalDateTime.now())
                .total(BigDecimal.ZERO)
                .metodoPago("EFECTIVO")
                .estado("COMPLETADA")
                .detalles(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (VentaRequestDTO.ItemVenta item : items) {

            Long idProductoVendedor = item.getIdProductoVendedor();
            Integer cantidad = item.getCantidad();

            if (cantidad == null || cantidad <= 0) {
                throw new IllegalArgumentException("Las cantidades deben ser mayores que cero.");
            }

            ProductoVendedor productoVendedor = productoVendedorRepository.findById(idProductoVendedor)
                    .orElseThrow(() -> new IllegalArgumentException("Uno de los productos no existe."));

            if (!productoVendedor.getVendedor().getId().equals(idVendedor)) {
                throw new IllegalArgumentException("No puedes vender productos de otro vendedor.");
            }

            if (!Boolean.TRUE.equals(productoVendedor.getActivo())
                    || !Boolean.TRUE.equals(productoVendedor.getProducto().getActivo())) {
                throw new IllegalArgumentException("Uno de los productos está inactivo.");
            }

            BigDecimal precioFinal = item.getPrecioVenta() != null
                    ? item.getPrecioVenta()
                    : productoVendedor.getPrecio();

            if (precioFinal == null || precioFinal.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "El precio de venta debe ser mayor que cero.");
            }

            BigDecimal subtotal = precioFinal.multiply(BigDecimal.valueOf(cantidad));

            DetalleVenta detalle = DetalleVenta.builder()
                    .venta(venta)
                    .productoVendedor(productoVendedor)
                    .cantidad(cantidad)
                    .precioUnitario(precioFinal)
                    .subTotal(subtotal)
                    .build();

            venta.getDetalles().add(detalle);
            total = total.add(subtotal);
        }

        venta.setTotal(total);

        Venta ventaGuardada = ventaRepository.save(venta);
        detalleVentaRepository.saveAll(ventaGuardada.getDetalles());

        return ventaGuardada;
    }

    // Ventas de un vendedor
    public List<Venta> listarVentasVendedor(Long idVendedor) {
        return ventaRepository.findByVendedorIdOrderByFechaHoraDesc(idVendedor);
    }

    // Todas las ventas
    public List<Venta> listarTodasLasVentas() {
        return ventaRepository.findAllByOrderByFechaHoraDesc();
    }

    // Obtiene los nombres de los productos de una venta.
    @Transactional(readOnly = true)
    public List<String> obtenerNombresProductos(Long idVenta) {

        List<DetalleVenta> detalles = detalleVentaRepository.findByVentaId(idVenta);

        return detalles.stream()
                .map(detalle -> detalle.getProductoVendedor()
                        .getProducto()
                        .getNombre())
                .distinct()
                .toList();
    }

    // Detalle de una venta
    @Transactional(readOnly = true)
    public VentaDetalleResponseDTO obtenerDetalleVenta(Long idVenta) {

        Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new IllegalArgumentException("La venta no existe."));

        List<DetalleVenta> detalles = detalleVentaRepository.findByVentaId(idVenta);

        List<VentaDetalleResponseDTO.ItemDetalleDTO> items = detalles.stream()
                .map(detalle -> VentaDetalleResponseDTO.ItemDetalleDTO.builder()
                        .nombreProducto(detalle.getProductoVendedor().getProducto().getNombre())
                        .cantidad(detalle.getCantidad())
                        .precioUnitario(detalle.getPrecioUnitario())
                        .subTotal(detalle.getSubTotal())
                        .build())
                .toList();

        return VentaDetalleResponseDTO.builder()
                .idVenta(venta.getId())
                .nombreVendedor(venta.getVendedor().getNombre() + " " + venta.getVendedor().getApellido())
                .fechaHora(venta.getFechaHora())
                .total(venta.getTotal())
                .metodoPago(venta.getMetodoPago())
                .estado(venta.getEstado())
                .idVendedor(venta.getVendedor().getId())
                .detalles(items)
                .build();
    }

    // Ventas de un día
    public List<Venta> listarVentasPorDia(LocalDate fecha) {

        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.plusDays(1).atStartOfDay();

        return ventaRepository
                .findByFechaHoraGreaterThanEqualAndFechaHoraLessThanOrderByFechaHoraAsc(
                        inicio,
                        fin
                );
    }

    // Ventas de un vendedor en un día específico
    public List<Venta> listarVentasVendedorPorDia(
            Long idVendedor,
            LocalDate fecha) {

        return listarVentasPorDia(fecha)
                .stream()
                .filter(venta -> venta.getVendedor().getId().equals(idVendedor))
                .toList();
    }

    // Totales por vendedor en un día
    public Map<String, BigDecimal> calcularVentasPorVendedor(LocalDate fecha) {

        List<Venta> ventas = listarVentasPorDia(fecha);

        Map<String, BigDecimal> totales = new LinkedHashMap<>();

        for (Venta venta : ventas) {
            Vendedor vendedor = venta.getVendedor();
            String nombre = vendedor.getNombre() + " " + vendedor.getApellido();

            totales.merge(nombre, venta.getTotal(), BigDecimal::add);
        }

        return totales;
    }

    // Ventas por rango de fechas
    public List<Venta> listarVentasPorRango(LocalDate fechaInicio, LocalDate fechaFin) {

        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.plusDays(1).atStartOfDay();

        return ventaRepository
                .findByFechaHoraGreaterThanEqualAndFechaHoraLessThanOrderByFechaHoraAsc(
                        inicio,
                        fin
                );
    }

    // Totales por vendedor dentro de un rango de fechas
    public Map<String, BigDecimal> calcularVentasPorVendedorEnRango(
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        List<Venta> ventas = listarVentasPorRango(fechaInicio, fechaFin);

        Map<String, BigDecimal> totales = new LinkedHashMap<>();

        for (Venta venta : ventas) {

            Vendedor vendedor = venta.getVendedor();
            String nombre = vendedor.getNombre() + " " + vendedor.getApellido();

            totales.merge(nombre, venta.getTotal(), BigDecimal::add);
        }

        return totales;
    }

    // Ventas de un vendedor dentro de un rango de fechas
    public List<Venta> listarVentasPorVendedorYRango(
            Long idVendedor,
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.plusDays(1).atStartOfDay();

        return ventaRepository
                .findByVendedorIdAndFechaHoraGreaterThanEqualAndFechaHoraLessThanOrderByFechaHoraDesc(
                        idVendedor,
                        inicio,
                        fin
                );
    }
}
