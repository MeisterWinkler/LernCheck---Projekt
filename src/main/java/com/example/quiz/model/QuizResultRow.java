package com.example.quiz.model;

public class QuizResultRow {
    private long questionId;
    private int pos;
    private String questionText;
    private double percentCorrect; // 0..100

    public QuizResultRow() {
    }

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public double getPercentCorrect() {
        return percentCorrect;
    }

    public void setPercentCorrect(double percentCorrect) {
        this.percentCorrect = percentCorrect;
    }
}