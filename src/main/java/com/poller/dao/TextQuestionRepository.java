package com.poller.dao;

import com.poller.domain.TextQuestion;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.repository.CrudRepository;

import static io.micronaut.data.model.query.builder.sql.Dialect.MYSQL;

@JdbcRepository(dialect = MYSQL)
public interface TextQuestionRepository extends CrudRepository<TextQuestion, Long> {
}
