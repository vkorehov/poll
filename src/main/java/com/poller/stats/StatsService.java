package com.poller.stats;

import com.poller.Application;
import com.poller.dao.ScalarAnswerRepository;
import com.poller.dao.ScalarQuestionRepository;
import com.poller.dao.TextAnswerRepository;
import com.poller.dao.TextQuestionRepository;
import com.poller.dao.stats.ScalarAnswerStatsPerAnswerRepository;
import com.poller.dao.stats.ScalarAnswerStatsRepository;
import com.poller.dao.stats.TextAnswerStatsRepository;
import com.poller.dao.stats.TextAnswerWordRepository;
import com.poller.domain.ScalarAnswer;
import com.poller.domain.ScalarQuestion;
import com.poller.domain.TextAnswer;
import com.poller.domain.TextQuestion;
import com.poller.domain.stats.ScalarAnswerStats;
import com.poller.domain.stats.ScalarAnswerStatsPerAnswer;
import com.poller.domain.stats.TextAnswerStats;
import com.poller.domain.stats.TextAnswerWord;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.Transactional;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Singleton
public class StatsService {
    private static final Logger LOG = LoggerFactory.getLogger(StatsService.class);

    private final ScalarAnswerStatsPerAnswerRepository scalarAnswerStatsPerAnswerRepository;
    private final ScalarAnswerStatsRepository scalarAnswerStatsRepository;
    private final TextAnswerStatsRepository textAnswerStatsRepository;
    private final TextAnswerWordRepository textAnswerWordRepository;
    private final TextQuestionRepository textQuestionRepository;
    private final TextAnswerRepository textAnswerRepository;
    private final ScalarQuestionRepository scalarQuestionRepository;
    private final ScalarAnswerRepository scalarAnswerRepository;

    public StatsService(
            TextAnswerStatsRepository textAnswerStatsRepository,
            ScalarAnswerStatsPerAnswerRepository scalarAnswerStatsPerAnswerRepository,
            ScalarAnswerStatsRepository scalarAnswerStatsRepository,
            TextAnswerWordRepository textAnswerWordRepository,
            TextQuestionRepository textQuestionRepository,
            TextAnswerRepository textAnswerRepository,
            ScalarQuestionRepository scalarQuestionRepository,
            ScalarAnswerRepository scalarAnswerRepository) {
        this.textAnswerStatsRepository = textAnswerStatsRepository;
        this.scalarAnswerStatsRepository = scalarAnswerStatsRepository;
        this.scalarAnswerStatsPerAnswerRepository = scalarAnswerStatsPerAnswerRepository;
        this.textAnswerWordRepository = textAnswerWordRepository;
        this.textQuestionRepository = textQuestionRepository;
        this.textAnswerRepository = textAnswerRepository;
        this.scalarQuestionRepository = scalarQuestionRepository;
        this.scalarAnswerRepository = scalarAnswerRepository;
    }

    public void calculateScalarStats() {
        boolean hasAnswers;
        do {
            hasAnswers = this.doCalculateScalarStats();
        } while (hasAnswers);
    }

    public void calculateTextStats() {
        boolean hasAnswers;
        do {
            hasAnswers = this.doCalculateTextStats();
        } while (hasAnswers);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    protected boolean doCalculateScalarStats() {
        boolean hasAnswers = false;
        for (ScalarQuestion q : scalarQuestionRepository.findAll()) {
            Map<Integer, ScalarAnswerStatsPerAnswer> statsPerAnswers =
                    scalarAnswerStatsPerAnswerRepository.findByScalarQuestionId(q.getId()).stream()
                            .collect(Collectors.toMap(ScalarAnswerStatsPerAnswer::getAnswer, Function.identity()));
            ScalarAnswerStats stats = scalarAnswerStatsRepository.findByScalarQuestionId(q.getId());
            List<ScalarAnswer> answers = scalarAnswerRepository.findTop500ByStatsCalculatedAndScalarQuestionId(false, q.getId());
            if (answers.size() == 0) {
                continue;
            }
            LOG.info("Scalar stats found {} unprocessed answers", answers.size());
            hasAnswers = true;
            List<Long> answerIds = answers.stream().map(ScalarAnswer::getId).toList();
            for (ScalarAnswer a : answers) {
                calculateScalarAnswerStats(
                        stats,
                        statsPerAnswers.get(a.getScalar()),
                        a.getScalar());
            }
            scalarAnswerStatsRepository.update(stats);
            scalarAnswerStatsPerAnswerRepository.updateAll(statsPerAnswers.values());
            scalarAnswerRepository.markAsCalculated(answerIds);
        }
        return hasAnswers;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    protected boolean doCalculateTextStats() {
        boolean hasAnswers = false;
        for (TextQuestion q : textQuestionRepository.findAll()) {
            TextAnswerStats stats = textAnswerStatsRepository.findByTextQuestionId(q.getId());
            List<TextAnswer> answers = textAnswerRepository.findTop500ByStatsCalculatedAndTextQuestionId(false, q.getId());
            if (answers.size() == 0) {
                continue;
            }
            LOG.info("Text stats found {} unprocessed answers", answers.size());
            hasAnswers = true;
            List<Long> answerIds = answers.stream().map(TextAnswer::getId).toList();
            Map<String, Integer> wordCounts = new HashMap<>();
            for (TextAnswer a : answers) {
                calculateTextAnswerStats(stats, wordCounts, a.getText());
            }
            textAnswerStatsRepository.update(stats);
            for (Map.Entry<String, Integer> wc : wordCounts.entrySet()) {
                Optional<TextAnswerWord> existingWord = this.textAnswerWordRepository
                        .findByTextQuestionIdAndWord(q.getId(), wc.getKey());
                if (existingWord.isPresent()) {
                    TextAnswerWord word = existingWord.get();
                    word.setCount(word.getCount() + wc.getValue());
                    this.textAnswerWordRepository.update(word);
                } else {
                    TextAnswerWord word = new TextAnswerWord(q.getId(), wc.getKey(), wc.getValue());
                    this.textAnswerWordRepository.save(word);
                }
            }
            textAnswerRepository.markAsCalculated(answerIds);
        }
        return hasAnswers;
    }

    public void newScalarQuestion(long questionId) {
        // initialize statistics with zeroes
        IntStream.range(0, Application.MAX_SCALAR + 1).forEach(s ->
                this.scalarAnswerStatsPerAnswerRepository.save(new ScalarAnswerStatsPerAnswer(
                        questionId, s, 0)));
        this.scalarAnswerStatsRepository.save(new ScalarAnswerStats(questionId, 0, 0, 0));
    }

    public void newTextQuestion(long questionId) {
        this.textAnswerStatsRepository.save(new TextAnswerStats(questionId, 0));
    }

    private void calculateScalarAnswerStats(
            ScalarAnswerStats stats,
            ScalarAnswerStatsPerAnswer statsPerAnswer,
            int answer) {
        statsPerAnswer.setCount(statsPerAnswer.getCount() + 1);
        stats.setCount(stats.getCount() + 1);
        stats.setAverage((stats.getSum() + answer) / stats.getCount()); // standard moving average
        stats.setSum(stats.getSum() + answer);
    }

    private void calculateTextAnswerStats(
            TextAnswerStats stats,
            Map<String, Integer> wordCounts,
            String text) {
        stats.setCount(stats.getCount() + 1);
        Arrays.stream(text.split("[ \t\n]"))
                .filter(w -> !Application.STOPWORDS.contains(w))
                .map(String::toLowerCase).forEach(w -> wordCounts.merge(w, 1, Integer::sum));
    }
}