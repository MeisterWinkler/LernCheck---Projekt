package com.example.quiz.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class QuizQuestion {
    private long id;
    private String text;
    private char correct; // A-D
    private int pos;
    private Map<Character, String> options = new LinkedHashMap<>();

    public QuizQuestion() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public char getCorrect() {
        return correct;
    }

    public void setCorrect(char correct) {
        this.correct = correct;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public Map<Character, String> getOptions() {
        return options;
    }

    public void setOptions(Map<Character, String> options) {
        this.options = options;
    }
}