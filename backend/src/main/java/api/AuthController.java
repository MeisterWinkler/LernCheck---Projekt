package api;

import domain.Teacher;
import interfaces.TeacherRepo;
import security.JwtService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String COOKIE_NAME = "LC_TOKEN";

    private final TeacherRepo teacherRepo;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(TeacherRepo teacherRepo, JwtService jwtService) {
        this.teacherRepo = teacherRepo;
        this.jwtService = jwtService;
    }

    public record RegisterReq(@NotBlank String username, @NotBlank String displayName, @NotBlank String password) {}
    public record LoginReq(@NotBlank String username, @NotBlank String password) {}
    public record AuthRes(String token) {}

    @PostMapping("/register")
    public AuthRes register(@Valid @RequestBody RegisterReq req, HttpServletResponse response) {
        teacherRepo.findByUsername(req.username()).ifPresent(t -> {
            throw new IllegalArgumentException("Username exists");
        });

        Teacher t = new Teacher(req.username().trim(), req.displayName().trim(), encoder.encode(req.password()));
        teacherRepo.save(t);

        String token = jwtService.issueToken(t.getId(), t.getUsername());
        setJwtCookie(response, token);
        return new AuthRes(token);
    }

    @PostMapping("/login")
    public AuthRes login(@Valid @RequestBody LoginReq req, HttpServletResponse response) {
        Teacher t = teacherRepo.findByUsername(req.username().trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid login"));

        if (!encoder.matches(req.password(), t.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid login");
        }

        String token = jwtService.issueToken(t.getId(), t.getUsername());
        setJwtCookie(response, token);
        return new AuthRes(token);
    }

    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private void setJwtCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 8);
        response.addCookie(cookie);
    }
}