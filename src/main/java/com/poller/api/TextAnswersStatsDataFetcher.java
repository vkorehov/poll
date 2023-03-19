package com.poller.api;

import com.poller.dao.TextQuestionRepository;
import com.poller.dao.stats.TextAnswerStatsRepository;
import com.poller.domain.TextQuestion;
import com.poller.domain.stats.TextAnswerStats;
import graphql.GraphQLException;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import jakarta.inject.Singleton;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Singleton
public class TextAnswersStatsDataFetcher implements DataFetcher<Stream<TextAnswerStats>> {

    private final TextAnswerStatsRepository textAnswerStatsRepository;
    private final TextQuestionRepository textQuestionRepository;

    public TextAnswersStatsDataFetcher(TextAnswerStatsRepository textAnswerStatsRepository,
                                       TextQuestionRepository textQuestionRepository) {
        this.textAnswerStatsRepository = textAnswerStatsRepository;
        this.textQuestionRepository = textQuestionRepository;
    }

    @Override
    public Stream<TextAnswerStats> get(DataFetchingEnvironment env) {
        return StreamSupport.stream(this.textAnswerStatsRepository.findAll().spliterator(), false).peek(s -> {
            if (env.getSelectionSet().getFields().stream().anyMatch(f -> f.getQualifiedName().equals("textQuestion/question"))) {
                // query and re-populate transient fields ONLY if asked for some field other than ID
                s.setTextQuestion(mustExist(this.textQuestionRepository.findById(s.getTextQuestionId()), s.getTextQuestionId()));
            }
        });
    }

    private TextQuestion mustExist(Optional<TextQuestion> optional, long id) {
        if (optional.isEmpty()) {
            throw new GraphQLException("Internal Error: database has no record for: " + id);
        }
        return optional.get();
    }
}
