package com.poller.dao.stats;

import com.poller.domain.stats.TextAnswerWord;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

import static io.micronaut.data.model.query.builder.sql.Dialect.MYSQL;

@JdbcRepository(dialect = MYSQL)
public interface TextAnswerWordRepository extends CrudRepository<TextAnswerWord, Long> {
    Optional<TextAnswerWord> findByTextQuestionIdAndWord(long textQuestionId, String word);

    List<TextAnswerWord> findByTextQuestionId(long textQuestionId);
}
