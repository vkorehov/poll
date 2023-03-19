package com.poller.domain;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;

import static io.micronaut.data.annotation.GeneratedValue.Type.AUTO;

@Introspected
public class ScalarQuestion {

    private final String question;
    @Id
    @GeneratedValue(AUTO)
    private Long id;


    public ScalarQuestion(String question) {
        this.question = question;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {// called by persistence layer!
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }
}
