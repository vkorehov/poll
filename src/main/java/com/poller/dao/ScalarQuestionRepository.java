package com.poller.dao;

import com.poller.domain.ScalarQuestion;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.repository.CrudRepository;

import static io.micronaut.data.model.query.builder.sql.Dialect.MYSQL;

@JdbcRepository(dialect = MYSQL)
public interface ScalarQuestionRepository extends CrudRepository<ScalarQuestion, Long> {
}
