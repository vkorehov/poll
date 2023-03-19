package com.poller.domain.stats;

import com.poller.domain.TextQuestion;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.Transient;

import static io.micronaut.data.annotation.GeneratedValue.Type.AUTO;

@Introspected
public class TextAnswerWord {
    private final long textQuestionId;
    private final String word;
    @Id
    @GeneratedValue(AUTO)
    private Long id;
    private long count;
    @Transient
    private transient TextQuestion textQuestion; // set by DataFetcher if needed

    public TextAnswerWord(long textQuestionId, String word, long count) {
        this.textQuestionId = textQuestionId;
        this.word = word;
        this.count = count;

        // "thin" transient object by default
        this.textQuestion = new TextQuestion(null);
        this.textQuestion.setId(textQuestionId);
    }

    public String getWord() {
        return word;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getTextQuestionId() {
        return textQuestionId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
