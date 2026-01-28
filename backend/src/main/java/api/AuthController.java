// Summary: Registrierung/Login für Lehrkräfte (JWT).
package api;

import domain.Teacher;
import interfaces.TeacherRepo;
import security.JwtService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final TeacherRepo teacherRepo;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(TeacherRepo teacherRepo, JwtService jwtService) {
        this.teacherRepo = teacherRepo;
        this.jwtService = jwtService;
    }

    public record RegisterReq(@Email String email, @NotBlank String displayName, @NotBlank String password) {}
    public record LoginReq(@Email String email, @NotBlank String password) {}
    public record AuthRes(String token) {}

    @PostMapping("/register")
    public AuthRes register(@Valid @RequestBody RegisterReq req) {
        teacherRepo.findByEmail(req.email()).ifPresent(t -> { throw new IllegalArgumentException("Email exists"); });
        Teacher t = new Teacher(req.email(), req.displayName(), encoder.encode(req.password()));
        teacherRepo.save(t);
        return new AuthRes(jwtService.issueToken(t.getId(), t.getEmail()));
    }

    @PostMapping("/login")
    public AuthRes login(@Valid @RequestBody LoginReq req) {
        Teacher t = teacherRepo.findByEmail(req.email()).orElseThrow(() -> new IllegalArgumentException("Invalid login"));
        if (!encoder.matches(req.password(), t.getPasswordHash())) throw new IllegalArgumentException("Invalid login");
        return new AuthRes(jwtService.issueToken(t.getId(), t.getEmail()));
    }
}