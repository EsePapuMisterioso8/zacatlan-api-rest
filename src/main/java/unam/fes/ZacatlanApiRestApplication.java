package unam.fes;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(security = {@SecurityRequirement(name = "basicAuth")})//Esto es temporal
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic")//Esto es temporal

@SpringBootApplication
public class ZacatlanApiRestApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZacatlanApiRestApplication.class, args);
	}

}
