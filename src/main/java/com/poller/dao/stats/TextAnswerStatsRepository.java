package com.poller.dao.stats;

import com.poller.domain.stats.TextAnswerStats;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.repository.CrudRepository;

import static io.micronaut.data.model.query.builder.sql.Dialect.MYSQL;

@JdbcRepository(dialect = MYSQL)
public interface TextAnswerStatsRepository extends CrudRepository<TextAnswerStats, Long> {
    TextAnswerStats findByTextQuestionId(long textQuestionId);
}
