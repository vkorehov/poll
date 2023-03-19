package com.poller.dao.stats;

import com.poller.domain.stats.ScalarAnswerStats;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.repository.CrudRepository;

import static io.micronaut.data.model.query.builder.sql.Dialect.MYSQL;

@JdbcRepository(dialect = MYSQL)
public interface ScalarAnswerStatsRepository extends CrudRepository<ScalarAnswerStats, Long> {
    ScalarAnswerStats findByScalarQuestionId(long scalarQuestionId);
}
