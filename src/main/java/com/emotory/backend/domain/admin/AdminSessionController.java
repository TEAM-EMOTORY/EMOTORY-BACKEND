package com.emotory.backend.domain.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.*;
import com.emotory.backend.global.exception.ErrorResponse;
import java.util.List;
import java.nio.charset.StandardCharsets;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/admin")
@Tag(name = "관리자 인증", description = "회원가입 없는 관리자 비밀번호 확인 및 세션 관리")
public class AdminSessionController {
    private static final int MAX_PASSWORD_BYTES = 72;
    private static final int SESSION_TIMEOUT_SECONDS = 30 * 60;
    private static final String ADMIN_ROLE = "ROLE_ADMIN";
    private static final String BCRYPT_HASH_PATTERN = "\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}";

    private final SecurityContextRepository securityContexts;
    private final CsrfTokenRepository csrfTokens;
    private final String passwordHash;
    private final BCryptPasswordEncoder passwords = new BCryptPasswordEncoder();

    public AdminSessionController(SecurityContextRepository securityContexts, CsrfTokenRepository csrfTokens,
                                  @Value("${admin.password-hash:${ADMIN_PASSWORD_HASH:}}") String passwordHash,
                                  @Value("${admin.password:${ADMIN_PASSWORD:}}") String password) {
        this.securityContexts = securityContexts;
        this.csrfTokens = csrfTokens;
        this.passwordHash = resolvePasswordHash(passwordHash, password);
    }

    public record PasswordRequest(@NotBlank @Size(max = 72) String password) {}
    public record Status(boolean authenticated) {}
    public record CsrfResponse(String headerName, String token) {}

    @GetMapping("/csrf")
    @Operation(summary = "관리자 CSRF 토큰 발급", description = "쿠키를 유지하고 반환된 headerName과 token을 변경 요청에 전달합니다. 인증 후 다시 발급받아야 합니다.")
    public CsrfResponse csrf(CsrfToken token, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        return new CsrfResponse(token.getHeaderName(), token.getToken());
    }

    @PostMapping("/session")
    @Operation(summary = "관리자 인증", description = "CSRF 토큰과 비밀번호를 확인해 30분 유휴 만료 세션을 발급합니다. 성공 204, 비밀번호 오류 401, 설정 누락 503입니다.")
    public ResponseEntity<?> authenticate(@Valid @RequestBody PasswordRequest body,
                                         HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        // 설정 확인 → 비밀번호 확인 → 세션 생성 순서로 처리한다.
        if (!isPasswordConfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ErrorResponse(503, "관리자 인증이 설정되지 않았습니다."));
        }
        if (isPasswordTooLong(body.password())) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(400, "관리자 비밀번호는 UTF-8 기준 72바이트 이하여야 합니다."));
        }
        if (!passwords.matches(body.password(), passwordHash)) {
            return ResponseEntity.status(401)
                    .body(new ErrorResponse(401, "관리자 비밀번호를 확인하세요."));
        }

        createAdminSession(request, response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/session")
    @Operation(summary = "관리자 인증 상태 확인")
    public Status status(Authentication authentication, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        return new Status(hasAdminRole(authentication));
    }

    @DeleteMapping("/session")
    @Operation(summary = "관리자 로그아웃", description = "CSRF 토큰을 확인하고 서버 세션을 무효화합니다.")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        invalidateAdminSession(request, response);
        response.setHeader("Cache-Control", "no-store");
        return ResponseEntity.noContent().build();
    }

    // 기존 해시가 있으면 우선 사용하고, 원문 비밀번호는 시작할 때 한 번만 변환한다.
    private String resolvePasswordHash(String configuredHash, String password) {
        if (!configuredHash.isBlank()) {
            return configuredHash;
        }
        if (password.isBlank() || isPasswordTooLong(password)) {
            return "";
        }
        return passwords.encode(password);
    }

    private boolean isPasswordConfigured() {
        return passwordHash.matches(BCRYPT_HASH_PATTERN);
    }

    private boolean isPasswordTooLong(String password) {
        return password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES;
    }

    private boolean hasAdminRole(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> ADMIN_ROLE.equals(authority.getAuthority()));
    }

    private void createAdminSession(HttpServletRequest request, HttpServletResponse response) {
        // 기존 세션 ID를 교체해 인증 전 세션을 그대로 사용하는 것을 막는다.
        var session = request.getSession();
        request.changeSessionId();
        session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);

        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated("admin", null,
                List.of(new SimpleGrantedAuthority(ADMIN_ROLE))));
        SecurityContextHolder.setContext(context);

        // 인증 전 CSRF 토큰은 폐기하고, 관리자 인증 정보를 세션에 저장한다.
        csrfTokens.saveToken(null, request, response);
        securityContexts.saveContext(context, request, response);
    }

    private void invalidateAdminSession(HttpServletRequest request, HttpServletResponse response) {
        csrfTokens.saveToken(null, request, response);
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
    }
}
