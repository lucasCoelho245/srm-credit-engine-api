package br.com.srm.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
