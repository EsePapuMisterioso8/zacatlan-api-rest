
package unam.fes.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unam.fes.entity.detalle.DetalleVenta;
import unam.fes.entity.proven.ProductoVendedor;
import unam.fes.entity.vendedor.Vendedor;
import unam.fes.entity.venta.Venta;
import unam.fes.entity.venta.VentaRequestDTO;
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
                .estado("COMPLETADA") // <- Como la creamos completada, ya no se puede modificar
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

            if (!Boolean.TRUE.equals(productoVendedor.getActivo()) || !Boolean.TRUE.equals(productoVendedor.getProducto().getActivo())) {
                throw new IllegalArgumentException("Uno de los productos está inactivo.");
            }

            // LÓGICA DE REGATEO:
            // Si el vendedor mandó un precio personalizado válido, lo usamos. Si no, tomamos el precio base de su inventario.
            BigDecimal precioFinal = (item.getPrecioVenta() != null && item.getPrecioVenta().compareTo(BigDecimal.ZERO) > 0)
                    ? item.getPrecioVenta()
                    : productoVendedor.getPrecio();

            BigDecimal subtotal = precioFinal.multiply(BigDecimal.valueOf(cantidad));

            DetalleVenta detalle = DetalleVenta.builder()
                    .venta(venta)
                    .productoVendedor(productoVendedor)
                    .cantidad(cantidad)
                    .precioUnitario(precioFinal) // Guardamos el precio exacto al que se vendió
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

    /*@Transactional
    public Venta registrarVenta(
            Long idVendedor,
            Map<Long, Integer> productosCantidad) {

        if (productosCantidad == null || productosCantidad.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debes seleccionar al menos un producto.");
        }

        Vendedor vendedor = vendedorRepository.findById(idVendedor)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El vendedor no existe."));

        if (!Boolean.TRUE.equals(vendedor.getActivo())) {
            throw new IllegalArgumentException(
                    "El vendedor está inactivo.");
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

        for (Map.Entry<Long, Integer> entrada : productosCantidad.entrySet()) {

            Long idProductoVendedor = entrada.getKey();
            Integer cantidad = entrada.getValue();

            if (cantidad == null || cantidad <= 0) {
                throw new IllegalArgumentException(
                        "Las cantidades deben ser mayores que cero.");
            }

            ProductoVendedor productoVendedor =
                    productoVendedorRepository.findById(idProductoVendedor)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Uno de los productos no existe."));

            if (!productoVendedor.getVendedor().getId().equals(idVendedor)) {
                throw new IllegalArgumentException(
                        "No puedes vender productos de otro vendedor.");
            }

            if (!Boolean.TRUE.equals(productoVendedor.getActivo())
                    || !Boolean.TRUE.equals(
                    productoVendedor.getProducto().getActivo())) {
                throw new IllegalArgumentException(
                        "Uno de los productos está inactivo.");
            }

            BigDecimal precio = productoVendedor.getPrecio();

            if (precio == null
                    || precio.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "El producto tiene un precio inválido.");
            }

            BigDecimal subtotal = precio.multiply(
                    BigDecimal.valueOf(cantidad));

            DetalleVenta detalle = DetalleVenta.builder()
                    .venta(venta)
                    .productoVendedor(productoVendedor)
                    .cantidad(cantidad)
                    .precioUnitario(precio)
                    .subTotal(subtotal)
                    .build();

            venta.getDetalles().add(detalle);
            total = total.add(subtotal);
        }

        venta.setTotal(total);

        Venta ventaGuardada = ventaRepository.save(venta);

        detalleVentaRepository.saveAll(ventaGuardada.getDetalles());

        return ventaGuardada;
    }*/

    public List<Venta> listarVentasVendedor(Long idVendedor) {
        return ventaRepository.findByVendedorIdOrderByFechaHoraDesc(idVendedor);
    }

    public List<Venta> listarTodasLasVentas() {
        return ventaRepository.findAllByOrderByFechaHoraDesc();
    }

    public List<Venta> listarVentasPorDia(LocalDate fecha) {

        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.plusDays(1).atStartOfDay();

        return ventaRepository
                .findByFechaHoraGreaterThanEqualAndFechaHoraLessThanOrderByFechaHoraAsc(
                        inicio,
                        fin
                );
    }

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

    //Metodo para consultar ventas por rango de fecha
    public List<Venta> listarVentasPorRango(LocalDate fechaInicio, LocalDate fechaFin) {
        // El inicio es a las 00:00:00 del primer día
        LocalDateTime inicio = fechaInicio.atStartOfDay();

        // El fin es a las 00:00:00 del día SIGUIENTE a la fecha de corte,
        // para incluir todo el día final completo.
        LocalDateTime fin = fechaFin.plusDays(1).atStartOfDay();

        return ventaRepository
                .findByFechaHoraGreaterThanEqualAndFechaHoraLessThanOrderByFechaHoraAsc(
                        inicio,
                        fin
                );
    }
}
