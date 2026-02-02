package com.example.quiz.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class QuizQuestion {
    public long id;
    public String text;
    public char correct; // A-D
    public int pos;
    public Map<Character, String> options = new LinkedHashMap<>();
}