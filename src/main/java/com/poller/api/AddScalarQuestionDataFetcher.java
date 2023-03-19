package com.poller.api;

import com.poller.dao.ScalarQuestionRepository;
import com.poller.domain.ScalarQuestion;
import com.poller.stats.StatsService;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import jakarta.inject.Singleton;

@Singleton
public class AddScalarQuestionDataFetcher implements DataFetcher<ScalarQuestion> {

    private final StatsService statsService;
    private final ScalarQuestionRepository scalarQuestionRepository;

    public AddScalarQuestionDataFetcher(StatsService statsService,
                                        ScalarQuestionRepository scalarQuestionRepository) {
        this.statsService = statsService;
        this.scalarQuestionRepository = scalarQuestionRepository;
    }

    @Override
    public ScalarQuestion get(DataFetchingEnvironment env) {
        String question = env.getArgument("question");
        ScalarQuestion scalarQuestion = scalarQuestionRepository.save(new ScalarQuestion(question));
        statsService.newScalarQuestion(scalarQuestion.getId());
        return scalarQuestion;
    }
}