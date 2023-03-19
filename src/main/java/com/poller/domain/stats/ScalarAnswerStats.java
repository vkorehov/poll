package com.poller.domain.stats;

import com.poller.domain.ScalarQuestion;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.Transient;

@Introspected
public class ScalarAnswerStats {
    @Id
    private final long scalarQuestionId;
    private long count;
    private long average;
    private long sum;

    @Transient
    private transient ScalarQuestion scalarQuestion; // set by DataFetcher if needed

    public ScalarAnswerStats(long scalarQuestionId, long count, long sum, long average) {
        this.scalarQuestionId = scalarQuestionId;
        this.count = count;
        this.sum = sum;
        this.average = average;

        // transient "thin" object by default
        this.scalarQuestion = new ScalarQuestion(null);
        this.scalarQuestion.setId(scalarQuestionId);
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

    public long getAverage() {
        return average;
    }

    public void setAverage(long average) {
        this.average = average;
    }

    public long getSum() {
        return sum;
    }

    public void setSum(long sum) {
        this.sum = sum;
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
