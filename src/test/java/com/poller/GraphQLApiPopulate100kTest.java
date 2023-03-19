package com.poller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poller.stats.StatsService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@MicronautTest(transactional = false,rollback = false)
public class GraphQLApiPopulate100kTest {
    private static final Logger LOG = LoggerFactory.getLogger(GraphQLApiPopulate100kTest.class);
    @Client("/")
    @Inject
    HttpClient client;

    @Inject
    ObjectMapper mapper;
    @Inject
    StatsService statsService;

    @Test
    void test100kAnswers() throws JsonProcessingException, InterruptedException, ExecutionException {
        long start = System.currentTimeMillis();
        final List<String> scalarQuestions = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            scalarQuestions.add(addScalarQuestion(i));
        }
        LOG.info("Scalar Question IDs = {}", scalarQuestions);
        final String textQuestionId = addTextQuestion();
        LOG.info("Text Question ID = {}", textQuestionId);
        // Populate answers and randomize questions with fixed seed in order to make test reproducable
        Random rand = new Random(0);
        final AtomicInteger count = new AtomicInteger();
        IntStream.range(0, 10000).parallel().forEach(i -> {
                    populateTenAnswers(rand, scalarQuestions, textQuestionId, "an apple is very good");
                    int v = count.incrementAndGet() * 10;
                    if (v % 1000 == 0) {
                        long finish = System.currentTimeMillis();
                        long timeElapsed = finish - start;
                        float rate = (float) v / (float) (timeElapsed / 1000);
                        LOG.info("Processed: {} Rate: {}", v, rate);
                    }
                }
        );
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        LOG.info("Total seconds elapsed: {}", timeElapsed / 1000);
        LOG.info("Waiting for stats to finish calculating");
        statsService.calculateTextStats();
        statsService.calculateScalarStats();
        LOG.info("All Stats Are calculated!");
    }

    private String addScalarQuestion(int i) throws JsonProcessingException {
        String insertQuestion = String.format("""
                mutation {
                   addScalarQuestion(question: "Some question number %d") {
                     id
                   }
                }
                """, i);
        HttpRequest<String> insertQuestionRequest = HttpRequest.POST("/graphql",
                mapper.writeValueAsString(Map.of("query", insertQuestion)));

        HttpResponse<Map> rsp = this.client.toBlocking().exchange(insertQuestionRequest, Map.class);
        Assertions.assertEquals(200, rsp.status().getCode());
        Assertions.assertNotNull(rsp.body());
        Map data = (Map) rsp.body().get("data");
        Map questionMap = (Map) data.get("addScalarQuestion");
        String questionId = (String) questionMap.get("id");
        Assertions.assertNotNull(questionId);
        return questionId;
    }

    private String addTextQuestion() throws JsonProcessingException {
        String insertQuestion = """
                mutation {
                   addTextQuestion(question: "Some vague text question") {
                      id
                   }
                }
                """;
        HttpRequest<String> insertQuestionRequest = HttpRequest.POST("/graphql",
                mapper.writeValueAsString(Map.of("query", insertQuestion)));

        HttpResponse<Map> rsp = this.client.toBlocking().exchange(insertQuestionRequest, Map.class);
        Assertions.assertEquals(200, rsp.status().getCode());
        Assertions.assertNotNull(rsp.body());
        Map data = (Map) rsp.body().get("data");
        Map questionMap = (Map) data.get("addTextQuestion");
        String textQuestionId = (String) questionMap.get("id");
        Assertions.assertNotNull(textQuestionId);
        return textQuestionId;
    }

    private void populateTenAnswers(Random rand, List<String> scalarQuestionIds, String textQuestionId, String text) {
        String answerQuestion = String.format("""
                        mutation {
                          a1:scalarAnswer(scalarQuestion: "%s", author: "vasja", scalar: %d) {
                            id
                          }
                          a2:scalarAnswer(scalarQuestion: "%s", author: "vasja", scalar: %d) {
                            id
                          }
                          a3:scalarAnswer(scalarQuestion: "%s", author: "vasja", scalar: %d) {
                            id
                          }
                          a4:scalarAnswer(scalarQuestion: "%s", author: "vasja", scalar: %d) {
                            id
                          }
                          a5:scalarAnswer(scalarQuestion: "%s", author: "vasja", scalar: %d) {
                            id
                          }
                          a6:scalarAnswer(scalarQuestion: "%s", author: "vasja", scalar: %d) {
                            id
                          }
                          a7:scalarAnswer(scalarQuestion: "%s", author: "vasja", scalar: %d) {
                            id
                          }
                          a8:scalarAnswer(scalarQuestion: "%s", author: "vasja", scalar: %d) {
                            id
                          }
                          a9:scalarAnswer(scalarQuestion: "%s", author: "vasja", scalar: %d) {
                            id
                          }
                          a10:textAnswer(textQuestion: "%s", author: "vasja", text: "%s") {
                            id
                          }
                        }
                        """,
                scalarQuestionIds.get(0), rand.nextInt(0, Application.MAX_SCALAR + 1),
                scalarQuestionIds.get(1), rand.nextInt(0, Application.MAX_SCALAR + 1),
                scalarQuestionIds.get(2), rand.nextInt(0, Application.MAX_SCALAR + 1),
                scalarQuestionIds.get(3), rand.nextInt(0, Application.MAX_SCALAR + 1),
                scalarQuestionIds.get(4), rand.nextInt(0, Application.MAX_SCALAR + 1),
                scalarQuestionIds.get(5), rand.nextInt(0, Application.MAX_SCALAR + 1),
                scalarQuestionIds.get(6), rand.nextInt(0, Application.MAX_SCALAR + 1),
                scalarQuestionIds.get(7), rand.nextInt(0, Application.MAX_SCALAR + 1),
                scalarQuestionIds.get(8), rand.nextInt(0, Application.MAX_SCALAR + 1),
                textQuestionId, text
        );
        HttpRequest<String> answerQuestionRequest = null;
        try {
            answerQuestionRequest = HttpRequest.POST("/graphql",
                    mapper.writeValueAsString(Map.of("query", answerQuestion)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        HttpResponse<Map> answerRsp = this.client.toBlocking().exchange(answerQuestionRequest, Map.class);
        Assertions.assertEquals(200, answerRsp.status().getCode());
        Assertions.assertNotNull(answerRsp.body());
        Map answerData = (Map) answerRsp.body().get("data");
        IntStream.range(1, 11).forEach(i -> {
            Map a = (Map) answerData.get("a" + i);
            Assertions.assertNotNull(a.get("id"));
        });
    }
}