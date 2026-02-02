package com.example.quiz.model;

public class Klass {
    private long id;
    private String name;

    public Klass() {
    }

    public Klass(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}