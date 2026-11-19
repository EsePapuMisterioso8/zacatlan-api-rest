
package unam.fes.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import unam.fes.entity.detalle.DetalleVenta;
import unam.fes.entity.venta.Venta;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ReportePdfService {

    private static final PDType1Font NORMAL =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    private static final PDType1Font NEGRITA =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private static final Color AZUL =
            new Color(35, 65, 95);

    private static final Color GRIS =
            new Color(100, 100, 100);

    private static final Color CLARO =
            new Color(235, 240, 245);

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // =====================================================
    // REPORTE DIARIO
    // =====================================================

    public byte[] generarReporteDiario(
            LocalDate fecha,
            List<Venta> ventas,
            BigDecimal totalDia,
            Map<String, BigDecimal> ventasPorVendedor
    ) throws IOException {

        try (PDDocument documento = new PDDocument();
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {

            if (fecha == null) {
                fecha = LocalDate.now();
            }

            if (ventas == null) {
                ventas = new ArrayList<>();
            }

            if (totalDia == null) {
                totalDia = BigDecimal.ZERO;
            }

            if (ventasPorVendedor == null) {
                ventasPorVendedor = Map.of();
            }

            Lienzo pdf = new Lienzo(
                    documento,
                    "ZACATLAN - REPORTE DIARIO DE VENTAS"
            );

            pdf.texto("ZACATLAN", 45, 24, NEGRITA, AZUL);
            pdf.texto("REPORTE DIARIO DE VENTAS", 45, 13, NEGRITA, GRIS);

            pdf.y -= 8;
            pdf.linea();

            pdf.texto("RESUMEN DEL DIA", 45, 12, NEGRITA, AZUL);

            pdf.texto(
                    "Fecha: " + fecha.format(FORMATO_FECHA),
                    45, 10, NORMAL, Color.BLACK
            );

            pdf.texto(
                    "Cantidad de ventas: " + ventas.size(),
                    45, 10, NORMAL, Color.BLACK
            );

            pdf.recuadroTotal(
                    "TOTAL DEL DIA",
                    formatoMoneda(totalDia)
            );

            pdf.espacio(55);

            pdf.texto("VENTAS POR VENDEDOR", 45, 12, NEGRITA, AZUL);
            pdf.linea();

            if (ventasPorVendedor.isEmpty()) {

                pdf.texto(
                        "No hay ventas registradas para esta fecha.",
                        50, 10, NORMAL, GRIS
                );

            } else {

                for (Map.Entry<String, BigDecimal> entrada :
                        ventasPorVendedor.entrySet()) {

                    pdf.espacio(20);

                    pdf.texto(
                            entrada.getKey(),
                            50, 10, NORMAL, Color.BLACK
                    );

                    pdf.textoDerecha(
                            formatoMoneda(entrada.getValue()),
                            560, 10, NEGRITA, Color.BLACK
                    );
                }
            }

            pdf.espacio(60);

            pdf.y -= 8;
            pdf.texto("DETALLE DE VENTAS", 45, 14, NEGRITA, AZUL);
            pdf.linea();

            if (ventas.isEmpty()) {

                pdf.texto(
                        "No hay ventas registradas para esta fecha.",
                        50, 10, NORMAL, GRIS
                );

            } else {

                for (Venta venta : ventas) {
                    generarDetalleVenta(pdf, venta);
                }
            }

            pdf.espacio(35);
            pdf.linea();

            pdf.texto(
                    "Zacatlan - Sistema de administracion de ventas",
                    45, 8, NORMAL, GRIS
            );

            pdf.textoDerecha(
                    "Reporte generado: " +
                            LocalDate.now().format(FORMATO_FECHA),
                    560, 8, NORMAL, GRIS
            );

            pdf.cerrar();
            documento.save(salida);

            return salida.toByteArray();
        }
    }

    // =====================================================
    // REPORTE POR RANGO DE FECHAS
    // =====================================================

    public byte[] generarReportePorRango(
            LocalDate fechaInicio,
            LocalDate fechaFin,
            List<Venta> ventas,
            BigDecimal totalRango,
            Map<String, BigDecimal> ventasPorVendedor
    ) throws IOException {

        try (PDDocument documento = new PDDocument();
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {

            if (fechaInicio == null || fechaFin == null) {
                throw new IllegalArgumentException(
                        "Debes indicar la fecha de inicio y la fecha de fin."
                );
            }

            if (fechaInicio.isAfter(fechaFin)) {
                throw new IllegalArgumentException(
                        "La fecha de inicio no puede ser posterior a la fecha de fin."
                );
            }

            if (ventas == null) {
                ventas = new ArrayList<>();
            }

            if (totalRango == null) {
                totalRango = BigDecimal.ZERO;
            }

            if (ventasPorVendedor == null) {
                ventasPorVendedor = Map.of();
            }

            Lienzo pdf = new Lienzo(
                    documento,
                    "ZACATLAN - REPORTE POR RANGO DE FECHAS"
            );

            // ENCABEZADO

            pdf.texto("ZACATLAN", 45, 24, NEGRITA, AZUL);

            pdf.texto(
                    "REPORTE DE VENTAS POR RANGO",
                    45, 13, NEGRITA, GRIS
            );

            pdf.y -= 8;
            pdf.linea();

            // RESUMEN

            pdf.texto("RESUMEN DEL PERIODO", 45, 12, NEGRITA, AZUL);

            pdf.texto(
                    "Fecha de inicio: " + fechaInicio.format(FORMATO_FECHA),
                    45, 10, NORMAL, Color.BLACK
            );

            pdf.texto(
                    "Fecha de fin: " + fechaFin.format(FORMATO_FECHA),
                    45, 10, NORMAL, Color.BLACK
            );

            pdf.texto(
                    "Cantidad de ventas: " + ventas.size(),
                    45, 10, NORMAL, Color.BLACK
            );

            pdf.recuadroTotal(
                    "TOTAL DEL PERIODO",
                    formatoMoneda(totalRango)
            );

            // VENTAS POR VENDEDOR

            pdf.espacio(55);

            pdf.texto("VENTAS POR VENDEDOR", 45, 12, NEGRITA, AZUL);
            pdf.linea();

            if (ventasPorVendedor.isEmpty()) {

                pdf.texto(
                        "No hay ventas registradas en este periodo.",
                        50, 10, NORMAL, GRIS
                );

            } else {

                for (Map.Entry<String, BigDecimal> entrada :
                        ventasPorVendedor.entrySet()) {

                    pdf.espacio(20);

                    pdf.texto(
                            entrada.getKey(),
                            50, 10, NORMAL, Color.BLACK
                    );

                    pdf.textoDerecha(
                            formatoMoneda(entrada.getValue()),
                            560, 10, NEGRITA, Color.BLACK
                    );
                }
            }

            // DETALLE DE VENTAS

            pdf.espacio(60);

            pdf.y -= 8;
            pdf.texto("DETALLE DE VENTAS", 45, 14, NEGRITA, AZUL);
            pdf.linea();

            if (ventas.isEmpty()) {

                pdf.texto(
                        "No hay ventas registradas en este periodo.",
                        50, 10, NORMAL, GRIS
                );

            } else {

                for (Venta venta : ventas) {
                    generarDetalleVenta(pdf, venta);
                }
            }

            // PIE DE PAGINA

            pdf.espacio(35);
            pdf.linea();

            pdf.texto(
                    "Zacatlan - Sistema de administracion de ventas",
                    45, 8, NORMAL, GRIS
            );

            pdf.textoDerecha(
                    "Reporte generado: " +
                            LocalDate.now().format(FORMATO_FECHA),
                    560, 8, NORMAL, GRIS
            );

            pdf.cerrar();
            documento.save(salida);

            return salida.toByteArray();
        }
    }

    // =====================================================
    // DETALLE DE UNA VENTA
    // =====================================================

    private void generarDetalleVenta(
            Lienzo pdf,
            Venta venta
    ) throws IOException {

        if (venta == null) {
            return;
        }

        pdf.espacio(125);

        pdf.recuadroEncabezado(
                "VENTA #" + venta.getId()
        );

        String vendedor = "No disponible";

        if (venta.getVendedor() != null) {

            String nombre = venta.getVendedor().getNombre();
            String apellido = venta.getVendedor().getApellido();

            vendedor = valorTexto(nombre) + " " + valorTexto(apellido);
        }

        pdf.texto(
                "Vendedor: " + vendedor,
                50, 9, NORMAL, Color.BLACK
        );

        String fechaHora = "No disponible";

        if (venta.getFechaHora() != null) {
            fechaHora = venta.getFechaHora().format(FORMATO_FECHA_HORA);
        }

        pdf.texto(
                "Fecha: " + fechaHora,
                50, 9, NORMAL, Color.BLACK
        );

        pdf.texto(
                "Pago: " + valorTexto(venta.getMetodoPago()) +
                        "     Estado: " + valorTexto(venta.getEstado()),
                50, 9, NORMAL, Color.BLACK
        );

        // ENCABEZADOS DE LA TABLA

        pdf.espacio(40);
        pdf.fondoTabla();

        pdf.texto("Producto", 50, 8, NEGRITA, AZUL);
        pdf.texto("Cant.", 345, 8, NEGRITA, AZUL);
        pdf.texto("Precio", 400, 8, NEGRITA, AZUL);
        pdf.texto("Subtotal", 490, 8, NEGRITA, AZUL);

        pdf.y -= 13;
        pdf.linea();

        // PRODUCTOS DE LA VENTA

        List<DetalleVenta> detalles = venta.getDetalles();

        if (detalles != null) {

            for (DetalleVenta detalle : detalles) {

                if (detalle == null) {
                    continue;
                }

                String nombre = "Producto no disponible";

                if (detalle.getProductoVendedor() != null
                        && detalle.getProductoVendedor().getProducto() != null) {

                    nombre = valorTexto(
                            detalle.getProductoVendedor()
                                    .getProducto()
                                    .getNombre()
                    );
                }

                List<String> lineas = envolver(nombre, 42);

                float altura = Math.max(16, lineas.size() * 10);

                pdf.espacio(altura + 5);

                float inicioY = pdf.y;

                for (String linea : lineas) {

                    pdf.textoEnY(
                            linea,
                            50,
                            pdf.y,
                            8,
                            NORMAL,
                            Color.BLACK
                    );

                    pdf.y -= 10;
                }

                pdf.textoEnY(
                        String.valueOf(detalle.getCantidad()),
                        345,
                        inicioY,
                        8,
                        NORMAL,
                        Color.BLACK
                );

                pdf.textoEnY(
                        formatoMoneda(detalle.getPrecioUnitario()),
                        400,
                        inicioY,
                        8,
                        NORMAL,
                        Color.BLACK
                );

                pdf.textoEnY(
                        formatoMoneda(detalle.getSubTotal()),
                        490,
                        inicioY,
                        8,
                        NEGRITA,
                        Color.BLACK
                );

                pdf.y = inicioY - altura - 3;
                pdf.lineaSuave();
            }
        }

        // TOTAL DE LA VENTA

        pdf.espacio(30);

        pdf.textoDerecha(
                "TOTAL DE LA VENTA: " +
                        formatoMoneda(venta.getTotal()),
                560, 11, NEGRITA, AZUL
        );

        pdf.y -= 15;
        pdf.linea();
    }

    // =====================================================
    // FORMATO MONETARIO
    // =====================================================

    private String formatoMoneda(BigDecimal cantidad) {

        if (cantidad == null) {
            cantidad = BigDecimal.ZERO;
        }

        DecimalFormatSymbols simbolos =
                new DecimalFormatSymbols(Locale.US);

        DecimalFormat formato =
                new DecimalFormat("#,##0.00", simbolos);

        return "$" + formato.format(cantidad);
    }

    // =====================================================
    // EVITAR VALORES DE TEXTO NULOS
    // =====================================================

    private String valorTexto(String texto) {

        if (texto == null || texto.isBlank()) {
            return "No disponible";
        }

        return texto;
    }

    // =====================================================
    // DIVIDIR NOMBRES LARGOS EN VARIAS LINEAS
    // =====================================================

    private List<String> envolver(String texto, int limite) {

        List<String> resultado = new ArrayList<>();

        if (texto == null || texto.isBlank()) {
            resultado.add("Producto no disponible");
            return resultado;
        }

        StringBuilder linea = new StringBuilder();

        for (String palabra : texto.split("\\s+")) {

            if (linea.length() + palabra.length() + 1 > limite
                    && !linea.isEmpty()) {

                resultado.add(linea.toString());
                linea = new StringBuilder();
            }

            if (!linea.isEmpty()) {
                linea.append(" ");
            }

            linea.append(palabra);
        }

        if (!linea.isEmpty()) {
            resultado.add(linea.toString());
        }

        return resultado;
    }

    // =====================================================
    // CONVERTIR TEXTO A CARACTERES COMPATIBLES CON HELVETICA
    // =====================================================

    private String limpiarTexto(String texto) {

        if (texto == null) {
            return "";
        }

        String normalizado = Normalizer.normalize(
                texto,
                Normalizer.Form.NFD
        );

        normalizado = normalizado.replaceAll("\\p{M}", "");

        return normalizado
                .replace("€", "EUR")
                .replace("—", "-")
                .replace("–", "-")
                .replace("“", "\"")
                .replace("”", "\"")
                .replace("’", "'")
                .replace("‘", "'");
    }

    // =====================================================
    // CONTROL DEL DOCUMENTO PDF
    // =====================================================

    private class Lienzo {

        private final PDDocument documento;
        private final String encabezadoPaginas;
        private PDPageContentStream contenido;
        private float y;

        private Lienzo(
                PDDocument documento,
                String encabezadoPaginas
        ) throws IOException {

            this.documento = documento;
            this.encabezadoPaginas = encabezadoPaginas;

            nuevaPagina();
        }

        private void nuevaPagina() throws IOException {

            if (contenido != null) {
                contenido.close();
            }

            PDPage pagina = new PDPage(PDRectangle.LETTER);
            documento.addPage(pagina);

            contenido = new PDPageContentStream(documento, pagina);

            y = 745;

            if (documento.getNumberOfPages() > 1) {

                escribir(
                        encabezadoPaginas,
                        45,
                        y,
                        11,
                        NEGRITA,
                        AZUL
                );

                y -= 25;

                linea();
            }
        }

        private void espacio(float necesario) throws IOException {

            if (y - necesario < 55) {
                nuevaPagina();
            }
        }

        private void texto(
                String texto,
                float x,
                int tamano,
                PDType1Font fuente,
                Color color
        ) throws IOException {

            espacio(tamano + 7);

            escribir(texto, x, y, tamano, fuente, color);

            y -= tamano + 7;
        }

        private void textoEnY(
                String texto,
                float x,
                float posicionY,
                int tamano,
                PDType1Font fuente,
                Color color
        ) throws IOException {

            escribir(texto, x, posicionY, tamano, fuente, color);
        }

        private void textoDerecha(
                String texto,
                float derecha,
                int tamano,
                PDType1Font fuente,
                Color color
        ) throws IOException {

            texto = limpiarTexto(texto);

            float ancho = fuente.getStringWidth(texto)
                    / 1000 * tamano;

            texto(texto, derecha - ancho, tamano, fuente, color);
        }

        private void escribir(
                String texto,
                float x,
                float posicionY,
                int tamano,
                PDType1Font fuente,
                Color color
        ) throws IOException {

            texto = limpiarTexto(texto);

            contenido.beginText();
            contenido.setFont(fuente, tamano);
            contenido.setNonStrokingColor(color);
            contenido.newLineAtOffset(x, posicionY);
            contenido.showText(texto);
            contenido.endText();
        }

        private void linea() throws IOException {

            contenido.setStrokingColor(CLARO);
            contenido.setLineWidth(1);

            contenido.moveTo(45, y);
            contenido.lineTo(567, y);
            contenido.stroke();

            y -= 15;
        }

        private void lineaSuave() throws IOException {

            contenido.setStrokingColor(CLARO);
            contenido.setLineWidth(0.5f);

            contenido.moveTo(50, y);
            contenido.lineTo(560, y);
            contenido.stroke();
        }

        private void fondoTabla() throws IOException {

            contenido.setNonStrokingColor(CLARO);
            contenido.addRect(45, y - 5, 522, 18);
            contenido.fill();

            y -= 2;
        }

        private void recuadroEncabezado(String titulo)
                throws IOException {

            espacio(30);

            contenido.setNonStrokingColor(CLARO);
            contenido.addRect(45, y - 5, 522, 20);
            contenido.fill();

            texto(titulo, 50, 10, NEGRITA, AZUL);

            y -= 5;
        }

        private void recuadroTotal(
                String titulo,
                String cantidad
        ) throws IOException {

            espacio(55);

            contenido.setNonStrokingColor(AZUL);
            contenido.addRect(45, y - 35, 522, 42);
            contenido.fill();

            escribir(
                    titulo,
                    60,
                    y - 17,
                    10,
                    NEGRITA,
                    Color.WHITE
            );

            cantidad = limpiarTexto(cantidad);

            float ancho = NEGRITA.getStringWidth(cantidad)
                    / 1000 * 16;

            escribir(
                    cantidad,
                    550 - ancho,
                    y - 23,
                    16,
                    NEGRITA,
                    Color.WHITE
            );

            y -= 55;
        }

        private void cerrar() throws IOException {

            if (contenido != null) {
                contenido.close();
                contenido = null;
            }
        }
    }
}
