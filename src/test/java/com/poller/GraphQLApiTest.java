package com.poller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poller.dao.ScalarAnswerRepository;
import com.poller.dao.ScalarQuestionRepository;
import com.poller.dao.TextAnswerRepository;
import com.poller.dao.TextQuestionRepository;
import com.poller.dao.stats.ScalarAnswerStatsPerAnswerRepository;
import com.poller.dao.stats.ScalarAnswerStatsRepository;
import com.poller.dao.stats.TextAnswerStatsRepository;
import com.poller.dao.stats.TextAnswerWordRepository;
import com.poller.domain.stats.ScalarAnswerStats;
import com.poller.domain.stats.ScalarAnswerStatsPerAnswer;
import com.poller.domain.stats.TextAnswerStats;
import com.poller.domain.stats.TextAnswerWord;
import com.poller.stats.StatsService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.transaction.Transactional;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@MicronautTest(transactional = false, rollback = false)
class GraphQLApiTest {
    @Client("/")
    @Inject
    HttpClient client;
    @Inject
    ObjectMapper mapper;
    @Inject
    StatsService statsService;
    @Inject
    ScalarAnswerStatsRepository scalarAnswerStatsRepository;
    @Inject
    ScalarAnswerStatsPerAnswerRepository scalarAnswerStatsPerAnswerRepository;
    @Inject
    TextAnswerStatsRepository textAnswerStatsRepository;
    @Inject
    TextAnswerWordRepository textAnswerWordRepository;
    @Inject
    TextAnswerRepository textAnswerRepository;
    @Inject
    ScalarAnswerRepository scalarAnswerRepository;
    @Inject
    ScalarQuestionRepository scalarQuestionRepository;
    @Inject
    TextQuestionRepository textQuestionRepository;

    @Test
    void testAnswerScalarQuestionWithAllFieldsReturned() throws JsonProcessingException {
        String insertQuestion = """
                mutation {
                   addScalarQuestion(question: "how are you?") {
                     id,
                     question
                   }
                }
                """;
        HttpRequest<String> insertQuestionRequest = HttpRequest.POST("/graphql",
                mapper.writeValueAsString(Map.of("query", insertQuestion)));

        HttpResponse<Map> rsp = this.client.toBlocking().exchange(insertQuestionRequest, Map.class);
        Assertions.assertEquals(200, rsp.status().getCode());
        Assertions.assertNotNull(rsp.body());
        Map data = (Map) rsp.body().get("data");
        Map questionMap = (Map) data.get("addScalarQuestion");
        String questionId = (String) questionMap.get("id");
        Assertions.assertNotNull(questionId);
        Assertions.assertEquals("how are you?", questionMap.get("question"));

        String answerQuestion = String.format("""
                mutation {
                  scalarAnswer(scalarQuestion: "%s", author: "vasja", scalar: 3) {
                    id,
                    scalar,
                    author,
                    scalarQuestion {
                      id,
                      question
                    }
                  }
                }
                """, questionId);
        HttpRequest<String> answerQuestionRequest = HttpRequest.POST("/graphql",
                mapper.writeValueAsString(Map.of("query", answerQuestion)));

        HttpResponse<Map> answerRsp = this.client.toBlocking().exchange(answerQuestionRequest, Map.class);
        Assertions.assertEquals(200, answerRsp.status().getCode());
        Assertions.assertNotNull(answerRsp.body());
        Map answerData = (Map) answerRsp.body().get("data");
        Map answer = (Map) answerData.get("scalarAnswer");

        Assertions.assertEquals(3, answer.get("scalar"));
        Assertions.assertEquals("vasja", answer.get("author"));
        Map originalQuestion = (Map) answer.get("scalarQuestion");
        Assertions.assertNotNull(originalQuestion);
        Assertions.assertEquals(questionId, originalQuestion.get("id"));
        Assertions.assertEquals("how are you?", originalQuestion.get("question"));

        statsService.calculateScalarStats(); // ensure stats are calculated after this point

        ScalarAnswerStats stats = scalarAnswerStatsRepository.findByScalarQuestionId(Long.parseLong(questionId));
        Assertions.assertEquals(1, stats.getCount());
        Assertions.assertEquals(3, stats.getAverage());
        Map<Integer, ScalarAnswerStatsPerAnswer> statsPerAnswers =
                scalarAnswerStatsPerAnswerRepository.findByScalarQuestionId(Long.parseLong(questionId)).stream()
                        .collect(Collectors.toMap(ScalarAnswerStatsPerAnswer::getAnswer, Function.identity()));
        IntStream.range(0, Application.MAX_SCALAR + 1).forEach(i -> {
            if (i == 3) {
                Assertions.assertEquals(1, statsPerAnswers.get(i).getCount());
            } else {
                Assertions.assertEquals(0, statsPerAnswers.get(i).getCount());
            }
        });
        // cleanup
        scalarAnswerStatsRepository.delete(stats);
        scalarAnswerStatsPerAnswerRepository.deleteAll(statsPerAnswers.values());
        scalarAnswerRepository.deleteAll(
                scalarAnswerRepository.findTop500ByStatsCalculatedAndScalarQuestionId(true, Long.parseLong(questionId)));
        scalarQuestionRepository.deleteById(Long.parseLong(questionId));
    }

    @Test
    void testAnswerScalarQuestionWithIdOnlyReturned() throws JsonProcessingException {
        String insertQuestion = """
                mutation {
                   addScalarQuestion(question: "how are you?") {
                     id,
                     question
                   }
                }
                """;
        HttpRequest<String> insertQuestionRequest = HttpRequest.POST("/graphql",
                mapper.writeValueAsString(Map.of("query", insertQuestion)));

        HttpResponse<Map> rsp = this.client.toBlocking().exchange(insertQuestionRequest, Map.class);
        Assertions.assertEquals(200, rsp.status().getCode());
        Assertions.assertNotNull(rsp.body());
        Map data = (Map) rsp.body().get("data");
        Map questionMap = (Map) data.get("addScalarQuestion");
        String questionId = (String) questionMap.get("id");
        Assertions.assertNotNull(questionId);
        Assertions.assertEquals("how are you?", questionMap.get("question"));

        String answerQuestion = String.format("""
                mutation {
                   scalarAnswer(scalarQuestion: "%s", author: "vasja", scalar: 3) {
                    id,
                    scalar,
                    author,
                    scalarQuestion {
                      id
                    }
                   }
                }
                """, questionId);
        HttpRequest<String> answerQuestionRequest = HttpRequest.POST("/graphql",
                mapper.writeValueAsString(Map.of("query", answerQuestion)));

        HttpResponse<Map> answerRsp = this.client.toBlocking().exchange(answerQuestionRequest, Map.class);
        Assertions.assertEquals(200, answerRsp.status().getCode());
        Assertions.assertNotNull(answerRsp.body());
        Map answerData = (Map) answerRsp.body().get("data");
        Map answer = (Map) answerData.get("scalarAnswer");

        Assertions.assertEquals(3, answer.get("scalar"));
        Assertions.assertEquals("vasja", answer.get("author"));
        Map originalQuestion = (Map) answer.get("scalarQuestion");
        Assertions.assertNotNull(originalQuestion);
        Assertions.assertEquals(questionId, originalQuestion.get("id"));
        Assertions.assertNull(originalQuestion.get("question"));

        statsService.calculateScalarStats(); // ensure stats are calculated after this point

        ScalarAnswerStats stats = scalarAnswerStatsRepository.findByScalarQuestionId(Long.parseLong(questionId));
        Assertions.assertEquals(1, stats.getCount());
        Assertions.assertEquals(3, stats.getAverage());
        Map<Integer, ScalarAnswerStatsPerAnswer> statsPerAnswers =
                scalarAnswerStatsPerAnswerRepository.findByScalarQuestionId(Long.parseLong(questionId)).stream()
                        .collect(Collectors.toMap(ScalarAnswerStatsPerAnswer::getAnswer, Function.identity()));
        IntStream.range(0, Application.MAX_SCALAR + 1).forEach(i -> {
            if (i == 3) {
                Assertions.assertEquals(1, statsPerAnswers.get(i).getCount());
            } else {
                Assertions.assertEquals(0, statsPerAnswers.get(i).getCount());
            }
        });
        // cleanup
        scalarAnswerStatsRepository.delete(stats);
        scalarAnswerStatsPerAnswerRepository.deleteAll(statsPerAnswers.values());
        scalarAnswerRepository.deleteAll(
                scalarAnswerRepository.findTop500ByStatsCalculatedAndScalarQuestionId(true, Long.parseLong(questionId)));
        scalarQuestionRepository.deleteById(Long.parseLong(questionId));
    }

    @Test
    void testAnswerTextQuestionWithAllFieldsReturned() throws JsonProcessingException {
        String insertQuestion = """
                mutation {
                   addTextQuestion(question: "how are you?") {
                     id,
                     question
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
        String questionId = (String) questionMap.get("id");
        Assertions.assertNotNull(questionId);
        Assertions.assertEquals("how are you?", questionMap.get("question"));

        String answerQuestion = String.format("""
                mutation {
                   textAnswer(textQuestion: "%s", author: "vasja", text: "I have to say something") {
                    id,
                    text,
                    author,
                    textQuestion {
                      id,
                      question
                    }
                   }
                }
                """, questionId);
        HttpRequest<String> answerQuestionRequest = HttpRequest.POST("/graphql",
                mapper.writeValueAsString(Map.of("query", answerQuestion)));

        HttpResponse<Map> answerRsp = this.client.toBlocking().exchange(answerQuestionRequest, Map.class);
        Assertions.assertEquals(200, answerRsp.status().getCode());
        Assertions.assertNotNull(answerRsp.body());
        Map answerData = (Map) answerRsp.body().get("data");
        Map answer = (Map) answerData.get("textAnswer");

        Assertions.assertEquals("I have to say something", answer.get("text"));
        Assertions.assertEquals("vasja", answer.get("author"));
        Map originalQuestion = (Map) answer.get("textQuestion");
        Assertions.assertNotNull(originalQuestion);
        Assertions.assertEquals(questionId, originalQuestion.get("id"));
        Assertions.assertEquals("how are you?", originalQuestion.get("question"));

        statsService.calculateTextStats(); // ensure stats are calculated after this point

        TextAnswerStats stats = textAnswerStatsRepository.findByTextQuestionId(Long.parseLong(questionId));
        Assertions.assertEquals(1, stats.getCount());

        Map<String, TextAnswerWord> words =
                textAnswerWordRepository.findByTextQuestionId(Long.parseLong(questionId)).stream()
                        .collect(Collectors.toMap(TextAnswerWord::getWord, Function.identity()));
        Assertions.assertEquals(5, words.size());
        Assertions.assertEquals(1, words.get("i").getCount());
        Assertions.assertEquals(1, words.get("have").getCount());
        Assertions.assertEquals(1, words.get("to").getCount());
        Assertions.assertEquals(1, words.get("say").getCount());
        Assertions.assertEquals(1, words.get("something").getCount());

        // cleanup
        textAnswerStatsRepository.delete(stats);
        textAnswerWordRepository.deleteAll(textAnswerWordRepository.findByTextQuestionId(Long.parseLong(questionId)));
        textAnswerRepository.deleteAll(
                textAnswerRepository.findTop500ByStatsCalculatedAndTextQuestionId(true, Long.parseLong(questionId)));
        textQuestionRepository.deleteById(Long.parseLong(questionId));
    }

    @Test
    void testAnswerTextQuestionWithIdOnlyReturned() throws JsonProcessingException {
        String insertQuestion = """
                mutation {
                   addTextQuestion(question: "how are you?") {
                     id,
                     question
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
        String questionId = (String) questionMap.get("id");
        Assertions.assertNotNull(questionId);
        Assertions.assertEquals("how are you?", questionMap.get("question"));

        String answerQuestion = String.format("""
                mutation {
                   textAnswer(textQuestion: "%s", author: "vasja", text: "I have to say something") {
                    id,
                    text,
                    author,
                    textQuestion {
                      id
                    }
                   }
                }
                """, questionId);
        HttpRequest<String> answerQuestionRequest = HttpRequest.POST("/graphql",
                mapper.writeValueAsString(Map.of("query", answerQuestion)));

        HttpResponse<Map> answerRsp = this.client.toBlocking().exchange(answerQuestionRequest, Map.class);
        Assertions.assertEquals(200, answerRsp.status().getCode());
        Assertions.assertNotNull(answerRsp.body());
        Map answerData = (Map) answerRsp.body().get("data");
        Map answer = (Map) answerData.get("textAnswer");

        Assertions.assertEquals("I have to say something", answer.get("text"));
        Assertions.assertEquals("vasja", answer.get("author"));
        Map originalQuestion = (Map) answer.get("textQuestion");
        Assertions.assertNotNull(originalQuestion);
        Assertions.assertEquals(questionId, originalQuestion.get("id"));
        Assertions.assertNull(originalQuestion.get("question"));

        statsService.calculateTextStats();

        TextAnswerStats stats = textAnswerStatsRepository.findByTextQuestionId(Long.parseLong(questionId));
        Assertions.assertEquals(1, stats.getCount());

        Map<String, TextAnswerWord> words =
                textAnswerWordRepository.findByTextQuestionId(Long.parseLong(questionId)).stream()
                        .collect(Collectors.toMap(TextAnswerWord::getWord, Function.identity()));
        Assertions.assertEquals(5, words.size());
        Assertions.assertEquals(1, words.get("i").getCount());
        Assertions.assertEquals(1, words.get("have").getCount());
        Assertions.assertEquals(1, words.get("to").getCount());
        Assertions.assertEquals(1, words.get("say").getCount());
        Assertions.assertEquals(1, words.get("something").getCount());

        // cleanup
        textAnswerStatsRepository.delete(stats);
        textAnswerWordRepository.deleteAll(textAnswerWordRepository.findByTextQuestionId(Long.parseLong(questionId)));
        textAnswerRepository.deleteAll(
                textAnswerRepository.findTop500ByStatsCalculatedAndTextQuestionId(true, Long.parseLong(questionId)));
        textQuestionRepository.deleteById(Long.parseLong(questionId));
    }

}