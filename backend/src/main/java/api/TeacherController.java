package api;

import domain.*;
import interfaces.*;
import service.PinTokenService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    private final TeacherRepo teacherRepo;
    private final SchoolClassRepo classRepo;
    private final QuizTemplateRepo templateRepo;
    private final TemplateQuestionRepo tqRepo;
    private final QuizRunRepo runRepo;
    private final AttemptRepo attemptRepo;
    private final AnswerRepo answerRepo;
    private final FeedbackRepo feedbackRepo;
    private final PinTokenService pinTokenService;

    public TeacherController(
            TeacherRepo teacherRepo,
            SchoolClassRepo classRepo,
            QuizTemplateRepo templateRepo,
            TemplateQuestionRepo tqRepo,
            QuizRunRepo runRepo,
            AttemptRepo attemptRepo,
            AnswerRepo answerRepo,
            FeedbackRepo feedbackRepo,
            PinTokenService pinTokenService
    ) {
        this.teacherRepo = teacherRepo;
        this.classRepo = classRepo;
        this.templateRepo = templateRepo;
        this.tqRepo = tqRepo;
        this.runRepo = runRepo;
        this.attemptRepo = attemptRepo;
        this.answerRepo = answerRepo;
        this.feedbackRepo = feedbackRepo;
        this.pinTokenService = pinTokenService;
    }

    private long teacherId(Authentication auth) {
        return (long) auth.getPrincipal();
    }

    // ---------- Klassen ----------
    public record CreateClassReq(@NotBlank String name) {}

    @GetMapping("/classes")
    public List<SchoolClass> listClasses(Authentication auth) {
        return classRepo.findByOwnerId(teacherId(auth));
    }

    @PostMapping("/classes")
    public SchoolClass createClass(Authentication auth, @Valid @RequestBody CreateClassReq req) {
        Teacher t = teacherRepo.findById(teacherId(auth)).orElseThrow();
        return classRepo.save(new SchoolClass(t, req.name().trim()));
    }

    // ---------- Templates ----------
    public record TemplateQuestionReq(
            @Min(1) int pos,
            @NotBlank String text,
            @NotBlank String optionA,
            @NotBlank String optionB,
            @NotBlank String optionC,
            @NotBlank String optionD,
            @NotBlank String correctOption
    ) {}

    public record CreateTemplateReq(
            @NotBlank String title,
            List<TemplateQuestionReq> questions
    ) {}

    public record TemplateRes(long id, String title, int questionCount) {}

    @GetMapping("/templates")
    public List<TemplateRes> listTemplates(Authentication auth) {
        long tid = teacherId(auth);
        List<QuizTemplate> templates = templateRepo.findByOwnerId(tid);
        List<TemplateRes> res = new ArrayList<>();
        for (QuizTemplate t : templates) {
            int cnt = tqRepo.findByTemplateIdOrderByPosAsc(t.getId()).size();
            res.add(new TemplateRes(t.getId(), t.getTitle(), cnt));
        }
        return res;
    }

    @GetMapping("/templates/{templateId}")
    public Map<String, Object> getTemplate(Authentication auth, @PathVariable long templateId) {
        QuizTemplate t = templateRepo.findById(templateId).orElseThrow();
        if (!Objects.equals(t.getOwner().getId(), teacherId(auth))) throw new IllegalArgumentException("Not your template");

        List<TemplateQuestion> qs = tqRepo.findByTemplateIdOrderByPosAsc(templateId);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", t.getId());
        out.put("title", t.getTitle());
        out.put("questions", qs);
        return out;
    }

    @PostMapping("/templates")
    public TemplateRes createTemplate(Authentication auth, @Valid @RequestBody CreateTemplateReq req) {
        Teacher owner = teacherRepo.findById(teacherId(auth)).orElseThrow();
        QuizTemplate template = templateRepo.save(new QuizTemplate(owner, req.title().trim()));

        List<TemplateQuestionReq> qs = req.questions() == null ? List.of() : req.questions();
        List<TemplateQuestion> toSave = new ArrayList<>();

        for (TemplateQuestionReq q : qs) {
            AnswerOption corr = AnswerOption.valueOf(q.correctOption().trim().toUpperCase());
            toSave.add(new TemplateQuestion(
                    template,
                    q.pos(),
                    q.text().trim(),
                    q.optionA().trim(),
                    q.optionB().trim(),
                    q.optionC().trim(),
                    q.optionD().trim(),
                    corr
            ));
        }
        tqRepo.saveAll(toSave);

        return new TemplateRes(template.getId(), template.getTitle(), toSave.size());
    }

    // ---------- Template -> Klasse zuweisen => Run ----------
    public record AssignTemplateReq(
            long templateId,
            long classId,
            @Min(1) @Max(180) int durationMinutes
    ) {}

    @PostMapping("/runs/assign")
    public QuizRun assignTemplate(Authentication auth, @Valid @RequestBody AssignTemplateReq req) {
        long tid = teacherId(auth);

        QuizTemplate t = templateRepo.findById(req.templateId()).orElseThrow();
        if (!Objects.equals(t.getOwner().getId(), tid)) throw new IllegalArgumentException("Not your template");

        SchoolClass sc = classRepo.findById(req.classId()).orElseThrow();
        if (!Objects.equals(sc.getOwner().getId(), tid)) throw new IllegalArgumentException("Not your class");

        return runRepo.save(new QuizRun(sc, t, req.durationMinutes()));
    }

    @GetMapping("/classes/{classId}/runs")
    public List<Map<String,Object>> listRunsForClass(Authentication auth, @PathVariable long classId) {
        long tid = teacherId(auth);
        SchoolClass sc = classRepo.findById(classId).orElseThrow();
        if (!Objects.equals(sc.getOwner().getId(), tid)) throw new IllegalArgumentException("Not your class");

        List<QuizRun> runs = runRepo.findBySchoolClassIdOrderByIdDesc(classId);

        List<Map<String,Object>> out = new ArrayList<>();
        for (QuizRun r : runs) {
            // sicherstellen: nur eigene Templates (gehören ja dem Lehrer)
            if (!Objects.equals(r.getTemplate().getOwner().getId(), tid)) continue;

            Map<String,Object> row = new LinkedHashMap<>();
            row.put("id", r.getId());
            row.put("templateId", r.getTemplate().getId());
            row.put("title", r.getTemplate().getTitle());
            row.put("status", r.getStatus());
            row.put("durationMinutes", r.getDurationMinutes());
            row.put("joinPin", r.getJoinPin());
            row.put("startedAt", r.getStartedAt());
            row.put("endsAt", r.getEndsAt());
            row.put("finishedAt", r.getFinishedAt());
            out.add(row);
        }
        return out;
    }

    // ---------- Run starten/stoppen ----------
    public record StartRunReq(@Min(1) @Max(180) int durationMinutes) {}

    @PostMapping("/runs/{runId}/start")
    public Map<String,Object> startRun(Authentication auth, @PathVariable long runId, @Valid @RequestBody StartRunReq req) {
        long tid = teacherId(auth);
        QuizRun r = runRepo.findById(runId).orElseThrow();
        if (!Objects.equals(r.getSchoolClass().getOwner().getId(), tid)) throw new IllegalArgumentException("Not your run");

        // PIN erzeugen, Status ACTIVE, Zeiten setzen
        r.setDurationMinutes(req.durationMinutes());
        r.setJoinPin(pinTokenService.newPin(6));
        r.setStatus(RunStatus.ACTIVE);
        Instant now = Instant.now();
        r.setStartedAt(now);
        r.setEndsAt(now.plusSeconds((long) req.durationMinutes() * 60L));
        r.setFinishedAt(null);

        runRepo.save(r);

        return Map.of(
                "runId", r.getId(),
                "pin", r.getJoinPin(),
                "durationMinutes", r.getDurationMinutes(),
                "endsAt", r.getEndsAt(),
                "status", r.getStatus().name()
        );
    }

    @PostMapping("/runs/{runId}/finish")
    public Map<String,Object> finishRun(Authentication auth, @PathVariable long runId) {
        long tid = teacherId(auth);
        QuizRun r = runRepo.findById(runId).orElseThrow();
        if (!Objects.equals(r.getSchoolClass().getOwner().getId(), tid)) throw new IllegalArgumentException("Not your run");

        r.setStatus(RunStatus.FINISHED);
        r.setFinishedAt(Instant.now());
        runRepo.save(r);

        return Map.of("runId", r.getId(), "status", r.getStatus().name());
    }

    // „erneut starten“ => neuer Run (Historie bleibt)
    @PostMapping("/runs/{runId}/restart")
    public QuizRun restart(Authentication auth, @PathVariable long runId) {
        long tid = teacherId(auth);
        QuizRun old = runRepo.findById(runId).orElseThrow();
        if (!Objects.equals(old.getSchoolClass().getOwner().getId(), tid)) throw new IllegalArgumentException("Not your run");

        QuizRun fresh = new QuizRun(old.getSchoolClass(), old.getTemplate(), old.getDurationMinutes());
        return runRepo.save(fresh);
    }

    // ---------- Run Details (Fragen + Status) ----------
    @GetMapping("/runs/{runId}")
    public Map<String,Object> runDetail(Authentication auth, @PathVariable long runId) {
        long tid = teacherId(auth);
        QuizRun r = runRepo.findById(runId).orElseThrow();
        if (!Objects.equals(r.getSchoolClass().getOwner().getId(), tid)) throw new IllegalArgumentException("Not your run");

        List<TemplateQuestion> qs = tqRepo.findByTemplateIdOrderByPosAsc(r.getTemplate().getId());

        Map<String,Object> out = new LinkedHashMap<>();
        out.put("id", r.getId());
        out.put("classId", r.getSchoolClass().getId());
        out.put("templateId", r.getTemplate().getId());
        out.put("title", r.getTemplate().getTitle());
        out.put("status", r.getStatus().name());
        out.put("joinPin", r.getJoinPin());
        out.put("durationMinutes", r.getDurationMinutes());
        out.put("startedAt", r.getStartedAt());
        out.put("endsAt", r.getEndsAt());
        out.put("finishedAt", r.getFinishedAt());
        out.put("questions", qs);
        return out;
    }

    // ---------- Auswertung pro Frage + Chart Daten ----------
    public record QuestionStat(long templateQuestionId, int pos, String text, double correctPercent) {}

    @GetMapping("/runs/{runId}/stats")
    public Map<String,Object> runStats(Authentication auth, @PathVariable long runId) {
        long tid = teacherId(auth);
        QuizRun r = runRepo.findById(runId).orElseThrow();
        if (!Objects.equals(r.getSchoolClass().getOwner().getId(), tid)) throw new IllegalArgumentException("Not your run");

        List<Attempt> attempts = attemptRepo.findByRunId(runId);

        // nur finished Attempts zählen (sonst verfälscht)
        Set<Long> finishedAttemptIds = new HashSet<>();
        for (Attempt a : attempts) if (a.isFinished()) finishedAttemptIds.add(a.getId());

        List<TemplateQuestion> qs = tqRepo.findByTemplateIdOrderByPosAsc(r.getTemplate().getId());

        // Zähler pro Frage
        Map<Long, Integer> totalPerQ = new HashMap<>();
        Map<Long, Integer> correctPerQ = new HashMap<>();

        if (!finishedAttemptIds.isEmpty()) {
            // hol alle answers über run
            List<domain.Answer> answers = answerRepo.findByAttemptRunId(runId);
            for (domain.Answer ans : answers) {
                if (!finishedAttemptIds.contains(ans.getAttempt().getId())) continue;
                long tqId = ans.getTemplateQuestion().getId();
                totalPerQ.put(tqId, totalPerQ.getOrDefault(tqId, 0) + 1);
                if (ans.isCorrect()) correctPerQ.put(tqId, correctPerQ.getOrDefault(tqId, 0) + 1);
            }
        }

        List<QuestionStat> stats = new ArrayList<>();
        for (TemplateQuestion q : qs) {
            int total = totalPerQ.getOrDefault(q.getId(), 0);
            int corr = correctPerQ.getOrDefault(q.getId(), 0);
            double pct = total == 0 ? 0.0 : (corr * 100.0 / total);
            stats.add(new QuestionStat(q.getId(), q.getPos(), q.getText(), pct));
        }

        Map<String,Object> out = new LinkedHashMap<>();
        out.put("runId", r.getId());
        out.put("title", r.getTemplate().getTitle());
        out.put("status", r.getStatus().name());
        out.put("startedAt", r.getStartedAt());
        out.put("finishedAt", r.getFinishedAt());
        out.put("stats", stats);
        out.put("finishedAttempts", finishedAttemptIds.size());
        return out;
    }

    // ---------- Feedback Liste ----------
    @GetMapping("/runs/{runId}/feedback")
    public List<Map<String,Object>> feedback(Authentication auth, @PathVariable long runId) {
        long tid = teacherId(auth);
        QuizRun r = runRepo.findById(runId).orElseThrow();
        if (!Objects.equals(r.getSchoolClass().getOwner().getId(), tid)) throw new IllegalArgumentException("Not your run");

        var list = feedbackRepo.findByAttemptRunId(runId);
        List<Map<String,Object>> out = new ArrayList<>();
        for (var f : list) {
            out.add(Map.of(
                    "id", f.getId(),
                    "comment", f.getComment(),
                    "createdAt", f.getCreatedAt()
            ));
        }
        return out;
    }
}