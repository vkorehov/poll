package com.poller.aop;

import graphql.ExecutionResult;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.SimpleInstrumentationContext;
import graphql.execution.instrumentation.parameters.InstrumentationExecuteOperationParameters;
import graphql.language.OperationDefinition;
import io.micronaut.context.annotation.Requires;
import io.micronaut.transaction.TransactionStatus;
import io.micronaut.transaction.jdbc.DataSourceTransactionManager;
import io.micronaut.transaction.support.DefaultTransactionDefinition;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

import static io.micronaut.transaction.TransactionDefinition.Propagation.REQUIRED;

@Singleton // this interceptor allows having single db tx for batched GraphQL mutations, this brings >3x..4x speed improvement
@Requires(beans = DataSourceTransactionManager.class)
public class GraphQLTransactionInstrumentation extends SimpleInstrumentation {
    public static final Logger LOG = LoggerFactory.getLogger(GraphQLTransactionInstrumentation.class);
    public static final String TRANSACTION_NAME = "graphQLTransaction";

    @Inject
    protected DataSourceTransactionManager tm;

    @Override
    public InstrumentationContext<ExecutionResult> beginExecuteOperation(InstrumentationExecuteOperationParameters parameters) {
        LOG.debug("Beginning transaction for GraphQL execution: {}", Thread.currentThread().getName());

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName(TRANSACTION_NAME);
        def.setPropagationBehavior(REQUIRED);
        def.setReadOnly(!OperationDefinition.Operation.MUTATION.equals(parameters.getExecutionContext().getOperationDefinition().getOperation()));
        TransactionStatus<Connection> status = tm.getTransaction(def);

        return SimpleInstrumentationContext.whenCompleted((t, e) -> {
            if (status.isRollbackOnly() || e != null) {
                tm.rollback(status);
                LOG.debug("Rolling back transaction for GraphQL execution: {}", Thread.currentThread().getName());
            } else {
                tm.commit(status);
                LOG.debug("Commit transaction for GraphQL execution: {}", Thread.currentThread().getName());
            }
        });
    }
}