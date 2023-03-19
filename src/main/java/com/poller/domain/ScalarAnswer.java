package com.poller.domain;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.Transient;

import static io.micronaut.data.annotation.GeneratedValue.Type.AUTO;

@Introspected
public class ScalarAnswer {
    private final int scalar;
    private final String authorId;
    private final long scalarQuestionId;
    @Id
    @GeneratedValue(AUTO)
    private Long id;
    private boolean statsCalculated;

    @Transient
    private transient ScalarQuestion scalarQuestion; // set by DataFetcher if needed

    public ScalarAnswer(int scalar, long scalarQuestionId, String authorId) {
        this.scalar = scalar;
        this.authorId = authorId;
        this.scalarQuestionId = scalarQuestionId;
        // transient "thin" object by default
        this.scalarQuestion = new ScalarQuestion(null);
        this.scalarQuestion.setId(scalarQuestionId);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {// called by persistence layer!
        this.id = id;
    }

    public int getScalar() {
        return scalar;
    }

    public long getScalarQuestionId() {
        return scalarQuestionId;
    }

    public String getAuthorId() {
        return authorId;
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
    public ScalarQuestion getScalarQuestion() {
        return scalarQuestion;
    }

    @Transient
    public void setScalarQuestion(ScalarQuestion scalarQuestion) {
        this.scalarQuestion = scalarQuestion;
    }
}
