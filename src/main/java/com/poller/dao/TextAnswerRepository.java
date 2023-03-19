package com.poller.dao;

import com.poller.domain.TextAnswer;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

import static io.micronaut.data.model.query.builder.sql.Dialect.MYSQL;

@JdbcRepository(dialect = MYSQL)
public interface TextAnswerRepository extends CrudRepository<TextAnswer, Long> {
    @Query("UPDATE text_answer SET stats_calculated = true WHERE id IN(:ids)")
    void markAsCalculated(List<Long> ids);

    List<TextAnswer> findTop500ByStatsCalculatedAndTextQuestionId(boolean statsCalculated, long textQuestionId);
}
