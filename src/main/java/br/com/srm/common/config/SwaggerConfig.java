package br.com.srm.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura o bean OpenAPI para personalizar o Swagger UI com título, versão e descrição.
 *
 * Sem esse bean o Swagger exibiria metadados genéricos. Com ele, a tela em
 * /swagger-ui.html mostra o nome e a versão corretos do serviço.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SRM Credit Engine API")
                        .version("1.0.0")
                        .description("Plataforma de Cessão de Crédito Multimoedas — SRM Asset. " +
                                "Endpoints para simulação, liquidação de recebíveis (FIDC), " +
                                "gestão de câmbio e extrato de transações."));
    }
}
