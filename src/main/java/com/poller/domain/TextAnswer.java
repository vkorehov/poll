package com.poller.domain;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.Transient;

import static io.micronaut.data.annotation.GeneratedValue.Type.AUTO;

@Introspected
public class TextAnswer {
    private final String text;
    private final String authorId;
    private final long textQuestionId;
    @Id
    @GeneratedValue(AUTO)
    private Long id;
    private boolean statsCalculated;

    @Transient
    private transient TextQuestion textQuestion; // set by DataFetcher if needed

    public TextAnswer(String text, long textQuestionId, String authorId) {
        this.text = text;
        this.authorId = authorId;
        this.textQuestionId = textQuestionId;
        // transient "thin" object by default
        this.textQuestion = new TextQuestion(null);
        this.textQuestion.setId(textQuestionId);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {// called by persistence layer!
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public String getAuthorId() {
        return authorId;
    }

    public long getTextQuestionId() {
        return textQuestionId;
    }

    public boolean isStatsCalculated() {
        return statsCalculated;
    }

    public void setStatsCalculated(boolean statsCalculated) {
        this.statsCalculated = statsCalculated;
    }

    @Transient
    public String getAuthor() {// transient field for GraphQL API
        return String.valueOf(authorId);
    }

    @Transient
    public TextQuestion getTextQuestion() {
        return textQuestion;
    }

    @Transient
    public void setTextQuestion(TextQuestion textQuestion) {
        this.textQuestion = textQuestion;
    }
}
