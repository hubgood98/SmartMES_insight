package com.smartfactory.smartmes_insight.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development Server"),
                        new Server().url("https://api.smartmes.com").description("Production Server")
                ))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
    }

    private Info apiInfo() {
        return new Info()
                .title("🏭 SmartMES Insight API")
                .description("""
                    ## Smart Factory MES (Manufacturing Execution System) API
                    
                    이 API는 스마트 팩토리 MES 시스템의 핵심 기능들을 제공합니다.
                    
                    ### 주요 기능:
                    - 👥 **사용자 관리**: 로그인, 권한 관리 (ADMIN, MANAGER, OPERATOR)
                    - 🏭 **설비 관리**: 설비 등록, 상태 모니터링
                    - 📊 **센서 관리**: 센서 데이터 수집, 임계값 설정
                    - 🚨 **알림 시스템**: 실시간 이상 감지 및 알림
                    - 📝 **작업 지시**: 생산 계획 및 작업 관리
                    - 📈 **생산 실적**: 생산량, 불량률 분석
                    - 📋 **로그 추적**: 사용자 활동 감사 추적
                    
                    ### 인증 방식:
                    - JWT Bearer Token 사용
                    - 헤더: `Authorization: Bearer {token}`
                    
                    ### 개발 환경:
                    - Java 17 + Spring Boot 3.5.3
                    - MySQL 8.0 + JPA/Hibernate
                    - Spring Security + JWT
                    """)
                .version("v1.0.0")
                .contact(new Contact()
                        .name("SmartMES Development Team")
                        .email("dev@smartmes.com")
                        .url("https://github.com/smartfactory/smartmes-insight"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("JWT 토큰을 입력하세요 (Bearer 접두사 제외)");
    }
}
