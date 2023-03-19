package com.poller;

import com.poller.aop.GraphQLTransactionInstrumentation;
import com.poller.api.*;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.core.io.ResourceResolver;
import jakarta.inject.Singleton;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Factory //
public class GraphQLFactory {

    @Bean
    @Singleton
    public GraphQL graphQL(GraphQLTransactionInstrumentation graphQLTransactionInstrumentation,
                           ResourceResolver resourceResolver,
                           WordsStatsDataFetcher wordsStatsDataFetcher,
                           ScalarAnswersStatsDataFetcher scalarAnswersStatsDataFetcher,
                           ScalarAnswersStatsPerAnswerDataFetcher scalarAnswersStatsPerAnswerDataFetcher,
                           TextAnswersStatsDataFetcher textAnswersStatsDataFetcher,
                           ScalarAnswerDataFetcher scalarAnswerDataFetcher,
                           TextAnswerDataFetcher textAnswerDataFetcher,
                           AddScalarQuestionDataFetcher addScalarQuestionDataFetcher,
                           AddTextQuestionDataFetcher addTextQuestionDataFetcher) { //

        SchemaParser schemaParser = new SchemaParser();
        SchemaGenerator schemaGenerator = new SchemaGenerator();

        // Parse the schema.
        TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();
        typeRegistry.merge(schemaParser.parse(new BufferedReader(new InputStreamReader(
                resourceResolver.getResourceAsStream("classpath:schema.graphqls").get()))));

        // Create the runtime wiring.
        RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
                .type("Query", typeWiring -> typeWiring
                        .dataFetcher("wordsStats", wordsStatsDataFetcher))
                .type("Query", typeWiring -> typeWiring
                        .dataFetcher("scalarAnswersStats", scalarAnswersStatsDataFetcher))
                .type("Query", typeWiring -> typeWiring
                        .dataFetcher("scalarAnswersStatsPerAnswer", scalarAnswersStatsPerAnswerDataFetcher))
                .type("Query", typeWiring -> typeWiring
                        .dataFetcher("textAnswersStats", textAnswersStatsDataFetcher))
                .type("Mutation", typeWiring -> typeWiring
                        .dataFetcher("scalarAnswer", scalarAnswerDataFetcher))
                .type("Mutation", typeWiring -> typeWiring
                        .dataFetcher("textAnswer", textAnswerDataFetcher))
                .type("Mutation", typeWiring -> typeWiring
                        .dataFetcher("addScalarQuestion", addScalarQuestionDataFetcher))
                .type("Mutation", typeWiring -> typeWiring
                        .dataFetcher("addTextQuestion", addTextQuestionDataFetcher))
                .build();

        // Create the executable schema.
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);

        // Return the GraphQL bean.
        return GraphQL.newGraphQL(graphQLSchema).instrumentation(graphQLTransactionInstrumentation).build();
    }
}
