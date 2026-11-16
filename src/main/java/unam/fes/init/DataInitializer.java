package unam.fes.init;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import unam.fes.entity.usuario.Usuario;
import unam.fes.repository.UsuarioRepository;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.password:}")
    private String adminPaswword;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder){
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }



    @Override
    public void run(String... args) throws Exception {

        if (usuarioRepository.existsByUsername("admin")) {

            Usuario administrador = usuarioRepository
                    .findByUsername("admin")
                    .orElseThrow();

            boolean coincide = passwordEncoder.matches(
                    adminPaswword,
                    administrador.getPassword()
            );

            System.out.println("El admin ya existe.");
            System.out.println("¿La contraseña coincide? " + coincide);

            return;
        }

        if(adminPaswword == null || adminPaswword.isBlank()){
            throw new IllegalStateException(
              "Debes de configurar ADMIN_INITIAL_PASSWORD"
            );
        }

        Usuario administrador = Usuario.builder()
                .username("admin")
                .password(passwordEncoder.encode(adminPaswword))
                .rol("ADMINISTRADOR")//sera?
                .activo(true)
                .fechaRegistro(LocalDateTime.now())
                .build();

        usuarioRepository.save(administrador);
        Usuario guardado = usuarioRepository
                .findByUsername("admin")
                .orElseThrow();

        System.out.println("¿Coincide la contraseña? " +
                passwordEncoder.matches(adminPaswword, guardado.getPassword()));
    }
}
