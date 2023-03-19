package com.poller.dao.stats;

import com.poller.domain.stats.ScalarAnswerStatsPerAnswer;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

import static io.micronaut.data.model.query.builder.sql.Dialect.MYSQL;

@JdbcRepository(dialect = MYSQL)
public interface ScalarAnswerStatsPerAnswerRepository extends CrudRepository<ScalarAnswerStatsPerAnswer, Long> {
    List<ScalarAnswerStatsPerAnswer> findByScalarQuestionId(long scalarQuestionId);
}
