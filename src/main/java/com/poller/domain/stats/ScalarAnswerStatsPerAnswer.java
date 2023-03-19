package com.poller.domain.stats;

import com.poller.domain.ScalarQuestion;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.Transient;

import static io.micronaut.data.annotation.GeneratedValue.Type.AUTO;

@Introspected
public class ScalarAnswerStatsPerAnswer {
    private final long scalarQuestionId;
    private final int answer;
    @Id
    @GeneratedValue(AUTO)
    private Long id;
    private long count;

    @Transient
    private transient ScalarQuestion scalarQuestion; // set by DataFetcher if needed

    public ScalarAnswerStatsPerAnswer(long scalarQuestionId, int answer, long count) {
        this.scalarQuestionId = scalarQuestionId;
        this.answer = answer;
        this.count = count;

        // transient "thin" object by default
        this.scalarQuestion = new ScalarQuestion(null);
        this.scalarQuestion.setId(scalarQuestionId);
    }

    public int getAnswer() {
        return answer;
    }

    public long getScalarQuestionId() {
        return scalarQuestionId;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
