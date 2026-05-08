package com.lcb.framework.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("LCB 管理系统 API")
                .version("1.0.0")
                .description("LCB 管理系统脚手架接口文档")
                .license(new License().name("Apache 2.0")));
    }
}
