package com.poller.api;

import com.poller.Application;
import com.poller.dao.ScalarAnswerRepository;
import com.poller.dao.ScalarQuestionRepository;
import com.poller.domain.ScalarAnswer;
import com.poller.domain.ScalarQuestion;
import graphql.GraphQLException;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import jakarta.inject.Singleton;

import java.util.Optional;

@Singleton
public class ScalarAnswerDataFetcher implements DataFetcher<ScalarAnswer> {

    private final ScalarAnswerRepository scalarAnswerRepository;
    private final ScalarQuestionRepository scalarQuestionRepository;

    public ScalarAnswerDataFetcher(ScalarAnswerRepository scalarAnswerRepository,
                                   ScalarQuestionRepository scalarQuestionRepository) {
        this.scalarAnswerRepository = scalarAnswerRepository;
        this.scalarQuestionRepository = scalarQuestionRepository;
    }

    @Override
    public ScalarAnswer get(DataFetchingEnvironment env) {
        Integer scalar = env.getArgument("scalar");
        if (scalar < 0 || scalar > Application.MAX_SCALAR) {
            throw new GraphQLException("Invalid value for scalar");
        }
        String authorId = env.getArgument("author");
        String scalarQuestionId = env.getArgument("scalarQuestion");
        ScalarAnswer scalarAnswer = scalarAnswerRepository.save(new ScalarAnswer(scalar, Long.parseLong(scalarQuestionId), authorId));
        if (env.getSelectionSet().getFields().stream().anyMatch(f -> f.getQualifiedName().equals("scalarQuestion/question"))) {
            // query and re-populate transient fields ONLY if asked for some field other than ID
            scalarAnswer.setScalarQuestion(
                    mustExist(this.scalarQuestionRepository.findById(scalarAnswer.getScalarQuestionId()), scalarAnswer.getScalarQuestionId()));
        }
        return scalarAnswer;
    }

    private ScalarQuestion mustExist(Optional<ScalarQuestion> optional, long id) {
        if (optional.isEmpty()) {
            throw new GraphQLException("Internal Error: database has no record for: " + id);
        }
        return optional.get();
    }

}