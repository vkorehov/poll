package com.poller.api;

import com.poller.dao.TextAnswerRepository;
import com.poller.dao.TextQuestionRepository;
import com.poller.domain.TextAnswer;
import com.poller.domain.TextQuestion;
import graphql.GraphQLException;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import jakarta.inject.Singleton;

import java.util.Optional;

@Singleton
public class TextAnswerDataFetcher implements DataFetcher<TextAnswer> {

    private final TextAnswerRepository textAnswerRepository;
    private final TextQuestionRepository textQuestionRepository;

    public TextAnswerDataFetcher(
            TextAnswerRepository textAnswerRepository,
            TextQuestionRepository textQuestionRepository) {
        this.textAnswerRepository = textAnswerRepository;
        this.textQuestionRepository = textQuestionRepository;
    }

    @Override
    public TextAnswer get(DataFetchingEnvironment env) {
        String text = env.getArgument("text");
        String authorId = env.getArgument("author");
        String textQuestionId = env.getArgument("textQuestion");

        TextAnswer textAnswer = textAnswerRepository.save(new TextAnswer(text, Long.parseLong(textQuestionId), authorId));
        if (env.getSelectionSet().getFields().stream().anyMatch(f -> f.getQualifiedName().equals("textQuestion/question"))) {
            // query and re-populate transient fields ONLY if asked for some field other than ID
            textAnswer.setTextQuestion(mustExist(this.textQuestionRepository.findById(textAnswer.getTextQuestionId()), textAnswer.getTextQuestionId()));
        }
        return textAnswer;
    }

    private TextQuestion mustExist(Optional<TextQuestion> optional, long id) {
        if (optional.isEmpty()) {
            throw new GraphQLException("Internal Error: database has no record for: " + id);
        }
        return optional.get();
    }

}