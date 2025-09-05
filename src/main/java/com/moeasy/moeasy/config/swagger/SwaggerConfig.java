package com.moeasy.moeasy.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(getInfo())
        .addSecurityItem(new SecurityRequirement().addList("jwtAuth"))
        .components(getComponents());
  }

  /**
   * 제목, 설명, 버전 등 API 정보
   */
  private Info getInfo() {
    return new Info()
        .title("Moeasy API")
        .version("v1.0.0")
        .description("Moeasy API 문서");
  }

  /**
   * 재사용이 가능한 보안 스키마, 스키마 정의 등
   */
  private Components getComponents() {
    return new Components()
        .addSecuritySchemes("jwtAuth", new SecurityScheme()
            .name("jwtAuth")
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT"));
  }
}
