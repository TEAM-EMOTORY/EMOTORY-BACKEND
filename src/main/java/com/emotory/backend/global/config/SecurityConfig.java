package com.emotory.backend.global.config;

import jakarta.servlet.SessionTrackingMode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Set;

@Configuration
public class SecurityConfig {
    // 브라우저가 관리자 세션을 유지할 때 사용할 쿠키 설정
    @Bean
    public ServletContextInitializer adminSessionCookie(
            @Value("${admin.cookie-secure:${ADMIN_COOKIE_SECURE:true}}") boolean secure) {
        return context -> {
            var cookie = context.getSessionCookieConfig();
            cookie.setName("EMOTORY_ADMIN_SESSION");
            cookie.setHttpOnly(true); // 자바스크립트에서 세션 쿠키를 읽지 못하게 한다.
            cookie.setSecure(secure); // 기본적으로 HTTPS에서만 전송한다. 로컬 HTTP에서만 false 설정.
            cookie.setPath("/"); // 서버의 모든 경로에서 같은 세션 쿠키를 사용한다.
            cookie.setAttribute("SameSite", "Lax"); // 다른 사이트에서 보낸 요청의 쿠키 전송을 제한한다.

            // 세션 ID를 URL에 넣지 않고 쿠키로만 전달한다.
            context.setSessionTrackingModes(Set.of(SessionTrackingMode.COOKIE));
        };
    }

    // 인증된 관리자 정보를 서버 세션에 저장하고 다음 요청에서 다시 읽는다.
    @Bean
    public SecurityContextRepository adminSecurityContexts() {
        return new HttpSessionSecurityContextRepository();
    }

    // 위조된 변경 요청을 막기 위한 CSRF 토큰을 서버 세션에 보관한다.
    @Bean
    public CsrfTokenRepository adminCsrfTokens() {
        return new HttpSessionCsrfTokenRepository();
    }

    @Bean
    @Order(1) // 공개 API 설정보다 먼저 검사한다.
    public SecurityFilterChain adminSecurity(
            HttpSecurity http,
            @Qualifier("corsConfigurationSource") CorsConfigurationSource cors,
            SecurityContextRepository contexts,
            CsrfTokenRepository csrf) throws Exception {
        // 이 경로에 해당하는 요청에만 관리자 보안 정책을 적용한다.
        http.securityMatcher("/admin/**", "/authoring/**", "/interactive-stories/**")
                // CorsConfig에서 허용한 프론트 주소의 요청을 처리한다.
                .cors(config -> config.configurationSource(cors))
                // POST·PUT·PATCH·DELETE에는 올바른 CSRF 토큰이 필요하다.
                .csrf(config -> config.csrfTokenRepository(csrf))
                // 요청마다 서버 세션에서 관리자 인증 정보를 읽는다.
                .securityContext(config -> config.securityContextRepository(contexts))
                // 인증 전 요청을 저장했다가 자동으로 다시 실행하지 않는다.
                .requestCache(config -> config.disable())

                // 기본 로그인 화면·HTTP Basic·로그아웃 대신 직접 만든 관리자 API를 사용한다.
                .formLogin(config -> config.disable())
                .httpBasic(config -> config.disable())
                .logout(config -> config.disable())

                .authorizeHttpRequests(auth -> auth
                        // 토큰 발급·비밀번호 확인·인증 상태 확인은 인증 전에도 가능하다.
                        // permitAll이어도 변경 요청의 CSRF 검사는 그대로 수행한다.
                        .requestMatchers("/admin/csrf", "/admin/session").permitAll()
                        // 이 보안 설정이 담당하는 나머지 요청은 관리자만 허용한다.
                        .anyRequest().hasRole("ADMIN"))
                .exceptionHandling(config -> config
                        // 인증이 없으면 로그인 화면 이동 대신 JSON으로 401을 반환한다.
                        .authenticationEntryPoint((request, response, error) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"status\":401,\"message\":\"관리자 인증이 필요합니다.\"}");
                        })
                        // 권한이 부족하거나 CSRF 검증에 실패하면 JSON으로 403을 반환한다.
                        .accessDeniedHandler((request, response, error) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"status\":403,\"message\":\"권한 또는 CSRF 토큰을 확인하세요.\"}");
                        }));
        return http.build();
    }

    @Bean
    @Order(2) // 관리자 경로에 해당하지 않는 나머지 요청을 처리한다.
    public SecurityFilterChain publicSecurity(
            HttpSecurity http,
            @Qualifier("corsConfigurationSource") CorsConfigurationSource cors) throws Exception {
        http.cors(config -> config.configurationSource(cors))
                // 기존 공개 API는 관리자 세션을 요구하지 않으며 CSRF 정책도 유지한다.
                .csrf(config -> config.disable())
                .formLogin(config -> config.disable())
                .httpBasic(config -> config.disable())
                .logout(config -> config.disable())
                // 동화 목록·플레이·이미지·S3·Swagger 등 나머지 경로는 기존처럼 공개한다.
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
