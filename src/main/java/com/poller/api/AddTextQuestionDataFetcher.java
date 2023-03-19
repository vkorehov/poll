package com.poller.api;

import com.poller.dao.TextQuestionRepository;
import com.poller.domain.TextQuestion;
import com.poller.stats.StatsService;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import jakarta.inject.Singleton;

@Singleton
public class AddTextQuestionDataFetcher implements DataFetcher<TextQuestion> {

    private final StatsService statsService;
    private final TextQuestionRepository textQuestionRepository;

    public AddTextQuestionDataFetcher(StatsService statsService, TextQuestionRepository textQuestionRepository) {
        this.statsService = statsService;
        this.textQuestionRepository = textQuestionRepository;
    }

    @Override
    public TextQuestion get(DataFetchingEnvironment env) {
        String question = env.getArgument("question");
        TextQuestion textQuestion = textQuestionRepository.save(new TextQuestion(question));
        statsService.newTextQuestion(textQuestion.getId());
        return textQuestion;
    }
}