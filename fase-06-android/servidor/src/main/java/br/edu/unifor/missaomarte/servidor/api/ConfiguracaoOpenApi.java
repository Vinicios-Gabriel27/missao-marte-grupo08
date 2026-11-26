package br.edu.unifor.missaomarte.servidor.api;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Documentação interativa da API em /swagger-ui.html. */
@Configuration
public class ConfiguracaoOpenApi {
    @Bean
    OpenAPI documentacao() {
        return new OpenAPI()
                .info(new Info().title("Missão Marte — Ranking Global").version("1.0")
                        .description("Cadastro, login (JWT), registro de partidas, histórico e ranking."))
                .components(new Components().addSecuritySchemes("bearer",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
