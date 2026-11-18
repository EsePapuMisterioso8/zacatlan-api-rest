
package unam.fes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // Swagger público
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Registro público de vendedores
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/vendedores/registro"
                        ).permitAll()
                        // Consulta pública de bloques para el registro de vendedores
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/bloques"
                        ).permitAll()
                        // ADMINISTRADOR: administración general
                        .requestMatchers("/api/admin/**")
                        .hasAnyAuthority("ADMINISTRADOR", "ROLE_ADMINISTRADOR")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/bloques",
                                "/api/productos"
                        ).hasAnyAuthority("ADMINISTRADOR", "ROLE_ADMINISTRADOR")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/bloques/**",
                                "/api/productos/**",
                                "/api/vendedores/**"
                        ).hasAnyAuthority("ADMINISTRADOR", "ROLE_ADMINISTRADOR")

                        // ADMINISTRADOR: consultar bitácora
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/historial"
                        ).hasAnyAuthority("ADMINISTRADOR", "ROLE_ADMINISTRADOR")

                        // ADMINISTRADOR: reportes y ventas
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/reportes/**",
                                "/api/ventas",
                                "/api/ventas/dia",
                                "/api/ventas/totales",
                                "/api/ventas/rango",
                                "/api/ventas/vendedor/*",
                                "/api/ventas/vendedor/*/rango"
                        ).hasAnyAuthority("ADMINISTRADOR", "ROLE_ADMINISTRADOR")

                        // ADMINISTRADOR: solicitudes de cambio de precio
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/solicitudes-precio/pendientes"
                        ).hasAnyAuthority("ADMINISTRADOR", "ROLE_ADMINISTRADOR")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/solicitudes-precio/*/autorizar",
                                "/api/solicitudes-precio/*/rechazar"
                        ).hasAnyAuthority("ADMINISTRADOR", "ROLE_ADMINISTRADOR")

                        // ADMINISTRADOR: consultar solicitudes pendientes de vendedores
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/vendedores/pendientes"
                        ).hasAnyAuthority("ADMINISTRADOR", "ROLE_ADMINISTRADOR")

                        // VENDEDOR: registrar y consultar sus ventas
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/ventas"
                        ).hasAnyAuthority("VENDEDOR", "ROLE_VENDEDOR")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/ventas/vendedor"
                        ).hasAnyAuthority("VENDEDOR", "ROLE_VENDEDOR")

                        // VENDEDOR: inventario
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/inventario"
                        ).hasAnyAuthority("VENDEDOR", "ROLE_VENDEDOR")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/inventario/**"
                        ).hasAnyAuthority("VENDEDOR", "ROLE_VENDEDOR")

                        // VENDEDOR: solicitudes de cambio de precio
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/solicitudes-precio"
                        ).hasAnyAuthority("VENDEDOR", "ROLE_VENDEDOR")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/solicitudes-precio/vendedor",
                                "/api/solicitudes-precio/vendedor/**"
                        ).hasAnyAuthority("VENDEDOR", "ROLE_VENDEDOR")

                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )

                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
