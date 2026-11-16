
package unam.fes.service;

import jakarta.transaction.Transactional;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import unam.fes.entity.bloque.Bloque;
import unam.fes.entity.usuario.Usuario;
import unam.fes.entity.vendedor.Vendedor;
import unam.fes.repository.BloqueRepository;
import unam.fes.repository.UsuarioRepository;
import unam.fes.repository.VendedorRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
public class VendedorService {

    private final VendedorRepository vendedorRepository;
    private final BloqueRepository bloqueRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public VendedorService(
            VendedorRepository vendedorRepository,
            BloqueRepository bloqueRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.vendedorRepository = vendedorRepository;
        this.bloqueRepository = bloqueRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Registrar una solicitud de vendedor
    @Transactional
    public Vendedor registrarSolicitud(
            String nombre,
            String apellido,
            String telefono,
            Long idBloque,
            String username,
            String password) {

        // Validar datos personales
        nombre = validarNombre(nombre, "El nombre");
        apellido = validarNombre(apellido, "El apellido");

        // Validar teléfono
        if (telefono == null || !telefono.matches("[0-9]{10}")) {
            throw new IllegalArgumentException(
                    "El teléfono debe contener exactamente 10 dígitos");
        }

        // Comprobar que el teléfono no esté registrado
        if (vendedorRepository.existsByTelefono(telefono)) {
            throw new IllegalArgumentException(
                    "Ese número de teléfono ya está registrado"
            );
        }

        // Validar nombre de usuario
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de usuario es obligatorio");
        }

        username = username.trim();

        if (!username.matches("[a-zA-Z][a-zA-Z0-9._-]{2,49}")) {
            throw new IllegalArgumentException(
                    "El usuario debe tener entre 3 y 50 caracteres, comenzar con una letra y solo contener letras, números, puntos, guiones y guiones bajos");
        }

        if (usuarioRepository.existsByUsername(username)
                || vendedorRepository.existsByUsernameSolicitado(username)) {
            throw new IllegalArgumentException(
                    "Ese nombre de usuario ya está registrado o solicitado");
        }

        // Validar contraseña
        if (password == null || password.length() < 8
                || password.length() > 72
                || password.chars().anyMatch(Character::isWhitespace)) {

            throw new IllegalArgumentException(
                    "La contraseña debe tener entre 8 y 72 caracteres y no contener espacios");
        }

        // BCrypt admite un máximo de 72 bytes
        int bytesPassword = password.getBytes(
                StandardCharsets.UTF_8).length;

        if (bytesPassword > 72) {
            throw new IllegalArgumentException(
                    "La contraseña supera el límite permitido por el sistema");
        }

        // Validar bloque
        if (idBloque == null || idBloque <= 0) {
            throw new IllegalArgumentException(
                    "Debes seleccionar un bloque válido");
        }

        Bloque bloque = bloqueRepository.findById(idBloque)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El bloque no existe"));

        if (!Boolean.TRUE.equals(bloque.getActivo())) {
            throw new IllegalArgumentException(
                    "El bloque no está disponible");
        }

        // Comprobar nombre de usuario duplicado
        if (usuarioRepository.existsByUsername(username)
                || vendedorRepository.existsByUsernameSolicitado(username)) {

            throw new IllegalArgumentException(
                    "Ese nombre de usuario ya está registrado");
        }

        // Crear solicitud pendiente
        Vendedor vendedor = Vendedor.builder()
                .nombre(nombre)
                .apellido(apellido)
                .telefono(telefono)
                .bloque(bloque)
                .estadoSolicitud("PENDIENTE")
                .fechaSolicitud(LocalDateTime.now())
                .activo(false)
                .usuario(null)
                .usernameSolicitado(username)
                .passwordSolicitado(passwordEncoder.encode(password))
                .build();

        return vendedorRepository.save(vendedor);
    }

    // Validar textos personales
    private String validarTexto(
            String valor,
            String campo,
            int longitudMaxima) {

        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    campo + " es obligatorio");
        }

        valor = valor.trim();

        if (valor.length() > longitudMaxima) {
            throw new IllegalArgumentException(
                    campo + " no puede superar los "
                            + longitudMaxima + " caracteres");
        }

        return valor;
    }

    // Aprobar solicitud de vendedor
    @Transactional
    public Vendedor aprobarSolicitud(Long idVendedor) {

        Vendedor vendedor = vendedorRepository.findById(idVendedor)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La solicitud no existe"));

        if (!"PENDIENTE".equals(vendedor.getEstadoSolicitud())) {
            throw new IllegalArgumentException(
                    "Esta solicitud ya fue procesada");
        }

        String username = vendedor.getUsernameSolicitado();
        String passwordCifrada = vendedor.getPasswordSolicitado();

        if (username == null || username.isBlank()
                || passwordCifrada == null || passwordCifrada.isBlank()) {

            throw new IllegalArgumentException(
                    "La solicitud no contiene credenciales válidas");
        }

        if (usuarioRepository.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "El nombre de usuario ya está registrado");
        }

        // Crear cuenta del vendedor
        Usuario usuario = Usuario.builder()
                .username(username)
                .password(passwordCifrada)
                .rol("VENDEDOR")
                .activo(true)
                .fechaRegistro(LocalDateTime.now())
                .build();

        usuarioRepository.save(usuario);

        vendedor.setUsuario(usuario);
        vendedor.setEstadoSolicitud("APROBADA");
        vendedor.setActivo(true);
        vendedor.setFechaAlta(LocalDateTime.now());

        // Eliminar credenciales temporales
        vendedor.setUsernameSolicitado(null);
        vendedor.setPasswordSolicitado(null);

        return vendedorRepository.save(vendedor);
    }

    // Rechazar solicitud de vendedor
    @Transactional
    public Vendedor rechazarSolicitud(Long idVendedor) {

        Vendedor vendedor = vendedorRepository.findById(idVendedor)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La solicitud no existe"));

        if (!"PENDIENTE".equals(vendedor.getEstadoSolicitud())) {
            throw new IllegalArgumentException(
                    "Esta solicitud ya fue procesada");
        }

        vendedor.setEstadoSolicitud("RECHAZADA");
        vendedor.setActivo(false);

        // Eliminar credenciales temporales
        vendedor.setUsernameSolicitado(null);
        vendedor.setPasswordSolicitado(null);

        return vendedorRepository.save(vendedor);
    }

    // Desactivar vendedor
    @Transactional
    public void desactivarVendedor(Long idVendedor) {

        Vendedor vendedor = vendedorRepository.findById(idVendedor)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró el vendedor"));

        if (!"APROBADA".equals(vendedor.getEstadoSolicitud())) {
            throw new IllegalArgumentException(
                    "Solo se pueden desactivar vendedores aprobados");
        }

        if (!Boolean.TRUE.equals(vendedor.getActivo())) {
            throw new IllegalArgumentException(
                    "El vendedor ya está desactivado");
        }

        vendedor.setActivo(false);
        vendedor.setFechaDesactivacion(LocalDateTime.now());

        vendedor.getUsuario().setActivo(false);
        usuarioRepository.save(vendedor.getUsuario());

        vendedorRepository.save(vendedor);
    }

    // Activar vendedor
    @Transactional
    public void activarVendedor(Long idVendedor) {

        Vendedor vendedor = vendedorRepository.findById(idVendedor)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró el vendedor"));

        if (!"APROBADA".equals(vendedor.getEstadoSolicitud())) {
            throw new IllegalArgumentException(
                    "Solo se pueden activar vendedores aprobados");
        }

        if (Boolean.TRUE.equals(vendedor.getActivo())) {
            throw new IllegalArgumentException(
                    "El vendedor ya está activo");
        }

        vendedor.setActivo(true);
        vendedor.setFechaDesactivacion(null);
        vendedor.setMotivoDesactivacion(null);

        vendedor.getUsuario().setActivo(true);
        usuarioRepository.save(vendedor.getUsuario());

        vendedorRepository.save(vendedor);
    }

    // Validar nombres y apellidos
    private String validarNombre(String valor, String campo) {

        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    campo + " es obligatorio");
        }

        valor = valor.trim();

        if (valor.length() > 100) {
            throw new IllegalArgumentException(
                    campo + " no puede superar los 100 caracteres");
        }

        if (!valor.matches(
                "[\\p{L}]+(?:[ '\\u002D][\\p{L}]+)*")) {

            throw new IllegalArgumentException(
                    campo + " solo puede contener letras, espacios, guiones y apóstrofes");
        }

        return valor;
    }
}
