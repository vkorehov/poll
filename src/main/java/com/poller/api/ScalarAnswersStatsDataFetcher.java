package com.poller.api;

import com.poller.dao.ScalarQuestionRepository;
import com.poller.dao.stats.ScalarAnswerStatsRepository;
import com.poller.domain.ScalarQuestion;
import com.poller.domain.stats.ScalarAnswerStats;
import graphql.GraphQLException;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import jakarta.inject.Singleton;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Singleton
public class ScalarAnswersStatsDataFetcher implements DataFetcher<Stream<ScalarAnswerStats>> {

    private final ScalarAnswerStatsRepository scalarAnswerStatsRepository;
    private final ScalarQuestionRepository scalarQuestionRepository;

    public ScalarAnswersStatsDataFetcher(ScalarAnswerStatsRepository scalarAnswerStatsRepository,
                                         ScalarQuestionRepository scalarQuestionRepository) {
        this.scalarAnswerStatsRepository = scalarAnswerStatsRepository;
        this.scalarQuestionRepository = scalarQuestionRepository;
    }

    @Override
    public Stream<ScalarAnswerStats> get(DataFetchingEnvironment env) {
        return StreamSupport.stream(this.scalarAnswerStatsRepository.findAll().spliterator(), false).peek(s -> {
            if (env.getSelectionSet().getFields().stream().anyMatch(f -> f.getQualifiedName().equals("scalarQuestion/question"))) {
                // query and re-populate transient fields ONLY if asked for some field other than ID
                s.setScalarQuestion(
                        mustExist(this.scalarQuestionRepository.findById(s.getScalarQuestionId()), s.getScalarQuestionId()));
            }
        });
    }

    private ScalarQuestion mustExist(Optional<ScalarQuestion> optional, long id) {
        if (optional.isEmpty()) {
            throw new GraphQLException("Internal Error: database has no record for: " + id);
        }
        return optional.get();
    }

}
