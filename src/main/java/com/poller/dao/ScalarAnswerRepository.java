package com.poller.dao;

import com.poller.domain.ScalarAnswer;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

import static io.micronaut.data.model.query.builder.sql.Dialect.MYSQL;

@JdbcRepository(dialect = MYSQL)
public interface ScalarAnswerRepository extends CrudRepository<ScalarAnswer, Long> {
    @Query("UPDATE scalar_answer SET stats_calculated = true WHERE id IN(:ids)")
        // (2)
    void markAsCalculated(List<Long> ids);

    List<ScalarAnswer> findTop500ByStatsCalculatedAndScalarQuestionId(boolean statsCalculated, long scalarQuestionId);
}
