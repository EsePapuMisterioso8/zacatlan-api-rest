
package unam.fes.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import unam.fes.entity.bloque.Bloque;
import unam.fes.repository.BloqueRepository;
import unam.fes.repository.ProductoRepository;
import unam.fes.repository.VendedorRepository;

import java.util.List;

@Service
public class BloqueService {

    private final BloqueRepository bloqueRepository;
    private final VendedorRepository vendedorRepository;
    private final ProductoRepository productoRepository;
    private final HistorialMovimientosService historialService;

    public BloqueService(
            BloqueRepository bloqueRepository,
            VendedorRepository vendedorRepository,
            ProductoRepository productoRepository,
            HistorialMovimientosService historialService) {

        this.bloqueRepository = bloqueRepository;
        this.vendedorRepository = vendedorRepository;
        this.productoRepository = productoRepository;
        this.historialService = historialService;
    }

    // Listar todos los bloques
    public List<Bloque> listarTodos() {
        return bloqueRepository.findAll();
    }

    // Crear un bloque nuevo
    @Transactional
    public void crearBloque(
            String nombre,
            String descripcion,
            String usernameAdministrador) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del bloque es obligatorio.");
        }

        final String nombreValidado = nombre.trim();

        if (nombreValidado.length() > 100) {
            throw new IllegalArgumentException(
                    "El nombre no puede superar los 100 caracteres.");
        }

        String descripcionValidada = null;

        if (descripcion != null && !descripcion.isBlank()) {
            descripcionValidada = descripcion.trim();

            if (descripcionValidada.length() > 255) {
                throw new IllegalArgumentException(
                        "La descripción no puede superar los 255 caracteres.");
            }
        }

        boolean duplicado = bloqueRepository.findAll().stream()
                .anyMatch(b -> b.getNombre() != null
                        && b.getNombre().equalsIgnoreCase(nombreValidado));

        if (duplicado) {
            throw new IllegalArgumentException(
                    "Ya existe un bloque con ese nombre.");
        }

        Bloque bloque = Bloque.builder()
                .nombre(nombreValidado)
                .descripcion(descripcionValidada)
                .activo(true)
                .build();

        Bloque guardado = bloqueRepository.save(bloque);

        historialService.registrarMovimiento(
                usernameAdministrador,
                "CREAR",
                "BLOQUE",
                guardado.getId(),
                "Se creó el bloque " + guardado.getNombre(),
                null,
                "Nombre: " + guardado.getNombre()
                        + ", Descripción: " + guardado.getDescripcion()
                        + ", Activo: true"
        );
    }

    // Modificar nombre y descripción de un bloque
    @Transactional
    public void modificarBloque(
            Long idBloque,
            String nombre,
            String descripcion,
            String usernameAdministrador) {

        Bloque bloque = bloqueRepository.findById(idBloque)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El bloque no existe."));

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del bloque es obligatorio.");
        }

        String nombreValidado = nombre.trim();

        if (nombreValidado.length() > 100) {
            throw new IllegalArgumentException(
                    "El nombre no puede superar los 100 caracteres.");
        }

        String descripcionValidada = null;

        if (descripcion != null && !descripcion.isBlank()) {
            descripcionValidada = descripcion.trim();

            if (descripcionValidada.length() > 255) {
                throw new IllegalArgumentException(
                        "La descripción no puede superar los 255 caracteres.");
            }
        }

        boolean duplicado = bloqueRepository.findAll().stream()
                .anyMatch(b -> !b.getId().equals(idBloque)
                        && b.getNombre() != null
                        && b.getNombre().equalsIgnoreCase(nombreValidado));

        if (duplicado) {
            throw new IllegalArgumentException(
                    "Ya existe otro bloque con ese nombre.");
        }

        String datosAnteriores =
                "Nombre: " + bloque.getNombre()
                        + ", Descripción: " + bloque.getDescripcion();

        bloque.setNombre(nombreValidado);
        bloque.setDescripcion(descripcionValidada);

        bloqueRepository.save(bloque);

        String datosNuevos =
                "Nombre: " + bloque.getNombre()
                        + ", Descripción: " + bloque.getDescripcion();

        historialService.registrarMovimiento(
                usernameAdministrador,
                "MODIFICAR",
                "BLOQUE",
                bloque.getId(),
                "Se modificó el bloque " + bloque.getNombre(),
                datosAnteriores,
                datosNuevos
        );
    }

    // Activar o desactivar un bloque
    @Transactional
    public void cambiarEstado(
            Long idBloque,
            boolean activar,
            String usernameAdministrador) {

        Bloque bloque = bloqueRepository.findById(idBloque)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El bloque no existe."));

        boolean estadoAnterior =
                Boolean.TRUE.equals(bloque.getActivo());

        if (estadoAnterior == activar) {
            throw new IllegalArgumentException(
                    activar
                            ? "El bloque ya está activo."
                            : "El bloque ya está desactivado."
            );
        }

        // No permitir desactivar bloques con vendedores o productos
        if (!activar) {

            if (vendedorRepository.existsByBloqueId(idBloque)) {
                throw new IllegalArgumentException(
                        "No se puede desactivar el bloque porque tiene vendedores asociados.");
            }

            if (productoRepository.existsByBloqueId(idBloque)) {
                throw new IllegalArgumentException(
                        "No se puede desactivar el bloque porque tiene productos asociados.");
            }
        }

        bloque.setActivo(activar);
        bloqueRepository.save(bloque);

        historialService.registrarMovimiento(
                usernameAdministrador,
                activar ? "ACTIVAR" : "DESACTIVAR",
                "BLOQUE",
                idBloque,
                (activar ? "Se activó" : "Se desactivó")
                        + " el bloque " + bloque.getNombre(),
                "Activo: " + estadoAnterior,
                "Activo: " + activar
        );
    }
}
