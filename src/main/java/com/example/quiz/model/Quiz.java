package com.example.quiz.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Quiz {
    public long quizId;        // class_quizzes.id
    public long templateId;    // quiz_templates.id
    public String title;

    public String status;      // NOT_STARTED/RUNNING/ENDED
    public String inviteCode;
    public Integer durationSeconds;
    public LocalDateTime startedAt;
    public LocalDateTime endsAt;
    public LocalDateTime endedAt;

    public List<QuizQuestion> questions = new ArrayList<>();
}