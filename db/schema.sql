CREATE DATABASE IF NOT EXISTS quizapp CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE quizapp;

-- Lehrer-Accounts
CREATE TABLE teachers (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          username VARCHAR(80) NOT NULL UNIQUE,
                          password_hash VARCHAR(255) NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Klassen
CREATE TABLE classes (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         name VARCHAR(50) NOT NULL UNIQUE
);

-- Quiz-Template (Vorlage) gehört einem Lehrer
CREATE TABLE quiz_templates (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                teacher_id BIGINT NOT NULL,
                                title VARCHAR(200) NOT NULL, -- Thema
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE
);

-- Fragen im Template
CREATE TABLE template_questions (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    template_id BIGINT NOT NULL,
                                    question_text TEXT NOT NULL,
                                    correct_option CHAR(1) NOT NULL, -- 'A','B','C','D'
                                    pos INT NOT NULL,
                                    FOREIGN KEY (template_id) REFERENCES quiz_templates(id) ON DELETE CASCADE
);

-- Antwortoptionen im Template
CREATE TABLE template_options (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                  question_id BIGINT NOT NULL,
                                  option_letter CHAR(1) NOT NULL, -- 'A','B','C','D'
                                  option_text TEXT NOT NULL,
                                  FOREIGN KEY (question_id) REFERENCES template_questions(id) ON DELETE CASCADE,
                                  UNIQUE(question_id, option_letter)
);

-- Ein Quiz ist eine "Instanz" eines Templates für eine konkrete Klasse
CREATE TABLE class_quizzes (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               teacher_id BIGINT NOT NULL,
                               class_id BIGINT NOT NULL,
                               template_id BIGINT NOT NULL,
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               status ENUM('NOT_STARTED','RUNNING','ENDED') NOT NULL DEFAULT 'NOT_STARTED',
                               invite_code VARCHAR(12) DEFAULT NULL,
                               duration_seconds INT DEFAULT NULL,  -- vom Lehrer gesetzt
                               started_at DATETIME DEFAULT NULL,
                               ends_at DATETIME DEFAULT NULL,
                               ended_at DATETIME DEFAULT NULL,

                               FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE,
                               FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
                               FOREIGN KEY (template_id) REFERENCES quiz_templates(id) ON DELETE CASCADE
);

-- Schüler (einfach: Nickname pro Versuch)
CREATE TABLE students (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          display_name VARCHAR(120) NOT NULL
);

-- Quiz-Teilnahme/Abgabe
CREATE TABLE quiz_attempts (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               quiz_id BIGINT NOT NULL,
                               student_id BIGINT NOT NULL,
                               submitted_at DATETIME NOT NULL,
                               feedback TEXT NULL,
                               FOREIGN KEY (quiz_id) REFERENCES class_quizzes(id) ON DELETE CASCADE,
                               FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
                               UNIQUE (quiz_id, student_id)
);

-- Antworten (pro Frage genau eine Antwort)
CREATE TABLE attempt_answers (
                                 id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                 attempt_id BIGINT NOT NULL,
                                 question_id BIGINT NOT NULL,         -- template_questions.id
                                 chosen_option CHAR(1) NOT NULL,      -- 'A','B','C','D'
                                 FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(id) ON DELETE CASCADE,
                                 FOREIGN KEY (question_id) REFERENCES template_questions(id) ON DELETE CASCADE,
                                 UNIQUE(attempt_id, question_id)
);

CREATE INDEX idx_class_quizzes_invite ON class_quizzes(invite_code);
CREATE INDEX idx_attempt_answers_q ON attempt_answers(question_id);