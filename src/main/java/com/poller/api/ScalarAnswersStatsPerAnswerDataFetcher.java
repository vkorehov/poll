package com.poller.api;

import com.poller.dao.ScalarQuestionRepository;
import com.poller.dao.stats.ScalarAnswerStatsPerAnswerRepository;
import com.poller.domain.ScalarQuestion;
import com.poller.domain.stats.ScalarAnswerStatsPerAnswer;
import graphql.GraphQLException;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import jakarta.inject.Singleton;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Singleton
public class ScalarAnswersStatsPerAnswerDataFetcher implements DataFetcher<Stream<ScalarAnswerStatsPerAnswer>> {

    private final ScalarAnswerStatsPerAnswerRepository scalarAnswerStatsPerAnswerRepository;
    private final ScalarQuestionRepository scalarQuestionRepository;

    public ScalarAnswersStatsPerAnswerDataFetcher(
            ScalarAnswerStatsPerAnswerRepository scalarAnswerStatsPerAnswerRepository,
            ScalarQuestionRepository scalarQuestionRepository) {
        this.scalarAnswerStatsPerAnswerRepository = scalarAnswerStatsPerAnswerRepository;
        this.scalarQuestionRepository = scalarQuestionRepository;
    }

    @Override
    public Stream<ScalarAnswerStatsPerAnswer> get(DataFetchingEnvironment env) {
        return StreamSupport.stream(this.scalarAnswerStatsPerAnswerRepository.findAll().spliterator(), false).peek(s -> {
            if (env.getSelectionSet().getFields().stream().anyMatch(f -> f.getQualifiedName().equals("scalarQuestion/question"))) {
                // query and re-populate transient fields ONLY if asked for some field other than ID
                s.setScalarQuestion(mustExist(this.scalarQuestionRepository.findById(s.getScalarQuestionId()), s.getScalarQuestionId()));
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
