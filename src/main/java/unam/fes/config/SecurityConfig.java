package unam.fes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. APAGAR CSRF: Esta es la instrucción que soluciona el error 403 en las peticiones POST
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // 2. Swagger UI público para poder hacer pruebas visuales
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // 3. Puerta pública para el formulario de registro de vendedores
                        .requestMatchers(HttpMethod.POST, "/api/vendedores/registro").permitAll()

                        // 4. Zona estricta del Administrador (Crear bloques/productos y ver reportes)
                        .requestMatchers(HttpMethod.POST, "/api/bloques", "/api/productos").hasAnyAuthority("ADMINISTRADOR", "ROLE_ADMINISTRADOR")
                        .requestMatchers("/api/admin/**").hasAnyAuthority("ADMINISTRADOR", "ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/bloques", "/api/productos").hasAnyAuthority("ADMINISTRADOR", "ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/bloques/**", "/api/productos/**", "/api/vendedores/**").hasAnyAuthority("ADMINISTRADOR", "ROLE_ADMINISTRADOR").requestMatchers(HttpMethod.GET, "/api/reportes/**").hasAnyAuthority("ADMINISTRADOR", "ROLE_ADMINISTRADOR").requestMatchers(HttpMethod.GET, "/api/solicitudes-precio/pendientes").hasAnyAuthority("ADMINISTRADOR", "ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/solicitudes-precio/*/autorizar").hasAnyAuthority("ADMINISTRADOR", "ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/solicitudes-precio/*/rechazar").hasAnyAuthority("ADMINISTRADOR", "ROLE_ADMINISTRADOR")

                        // 5. Zona estricta de Vendedores (Registrar ventas)
                        .requestMatchers(HttpMethod.POST, "/api/ventas").hasAnyAuthority("VENDEDOR", "ROLE_VENDEDOR").requestMatchers(HttpMethod.POST, "/api/ventas", "/api/inventario").hasAnyAuthority("VENDEDOR", "ROLE_VENDEDOR")
                        .requestMatchers(HttpMethod.GET, "/api/inventario/**").hasAnyAuthority("VENDEDOR", "ROLE_VENDEDOR").requestMatchers(HttpMethod.POST, "/api/solicitudes-precio").hasAnyAuthority("VENDEDOR", "ROLE_VENDEDOR")
                        .requestMatchers(HttpMethod.GET, "/api/solicitudes-precio/vendedor/**").hasAnyAuthority("VENDEDOR", "ROLE_VENDEDOR")

                        // 6. Cualquier otra petición requiere haber iniciado sesión (GET a catálogos)
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }
}