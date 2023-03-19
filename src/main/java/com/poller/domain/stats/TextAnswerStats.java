package com.poller.domain.stats;

import com.poller.domain.TextQuestion;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.Transient;

@Introspected
public class TextAnswerStats {
    @Id
    private final long textQuestionId;
    private long count;

    @Transient
    private transient TextQuestion textQuestion; // set by DataFetcher if needed

    public TextAnswerStats(long textQuestionId, long count) {
        this.textQuestionId = textQuestionId;
        this.count = count;
        // "thin" transient object by default
        this.textQuestion = new TextQuestion(null);
        this.textQuestion.setId(textQuestionId);
    }

    public long getTextQuestionId() {
        return textQuestionId;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
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
