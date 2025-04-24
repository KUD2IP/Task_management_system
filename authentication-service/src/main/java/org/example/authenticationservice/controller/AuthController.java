package org.example.authenticationservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.authenticationservice.model.dto.request.LoginRequest;
import org.example.authenticationservice.model.dto.request.RegistrationRequest;
import org.example.authenticationservice.model.dto.request.TokenRequest;
import org.example.authenticationservice.model.dto.response.AuthenticationResponse;
import org.example.authenticationservice.model.dto.response.UserResponse;
import org.example.authenticationservice.security.jwt.JwtService;
import org.example.authenticationservice.service.AuthenticationService;
import org.example.authenticationservice.service.UserServiceImpl;
import org.example.authenticationservice.service.contract.UserService;
import org.example.authenticationservice.service.contract.VerificationCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final JwtService jwtService;
    private final UserServiceImpl userServiceImpl;
    private final VerificationCodeService verificationCodeService;

    public AuthController(AuthenticationService authenticationService,
                          UserService userService,
                          JwtService jwtService, UserServiceImpl userServiceImpl, VerificationCodeService verificationCodeService) {
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.jwtService = jwtService;
        this.userServiceImpl = userServiceImpl;
        this.verificationCodeService = verificationCodeService;
    }

    /**
     * Регистрация нового пользователя.
     *
     * @param request данные для регистрации
     * @return ответ о результате регистрации
     */
    @PostMapping("/registration")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest request) {

        log.info("Registration request: {}", request);
        authenticationService.register(request);

        return ResponseEntity.ok("Code sent on email: " + request.getEmail());
    }

    @PostMapping("/new-code")
    public ResponseEntity<String> newCode(@RequestParam String email) {
        verificationCodeService.sendCode(email);
        return ResponseEntity.ok("Code sent on email: " + email);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String email, @RequestParam String code) {
        verificationCodeService.verify(email, code);
        return ResponseEntity.ok("Account verified!");
    }

    /**
     * Аутентификация пользователя.
     *
     * @param request данные для входа
     * @return токен доступа
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody LoginRequest request) {
        log.info("Login request: {}", request.getEmail());

        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    /**
     * Обновление токена доступа.
     *
     * @param request HTTP-запрос
     * @param response HTTP-ответ
     * @return новый токен доступа
     */
    @PostMapping("/refresh_token")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {

        log.info("Refresh token request");

        return authenticationService.refreshToken(request, response);
    }

    /**
     * Валидация токена доступа.
     *
     * @param tokenRequest запрос с токеном
     * @return true, если токен валиден, иначе false
     */
    @PostMapping("/validate-token")
    public ResponseEntity<Boolean> validateToken(@RequestBody TokenRequest tokenRequest) {
        log.info("Validate token request");
        try {
            String username = jwtService.extractUsername(tokenRequest.getToken());

            if (username == null) {
                log.warn("Token validation failed: username not found");
                return ResponseEntity.ok(false);
            }

            UserDetails userDetails = userService.loadUserByUsername(username);
            boolean isValid = jwtService.isAccessValid(tokenRequest.getToken(), userDetails);
            log.info("Token is valid: {}", isValid);
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage(), e);
            return ResponseEntity.ok(false);
        }
    }

    /**
     * Выдача роли исполнителю.
     *
     * @param userId идентификатор пользователя
     * @return ответ о результате выдачи роли
     */
    @PostMapping("/executor/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> assignExecutor(
            @PathVariable Long userId) {

        authenticationService.assignRoleToUser(userId);
        return ResponseEntity.ok("Executor assigned successfully");
    }

    /**
     * Поиск пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return пользователь
     */
    @GetMapping("/executor/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getExecutor(
            @PathVariable Long userId) {

        return ResponseEntity.ok(userServiceImpl.getUserById(userId));
    }
}
