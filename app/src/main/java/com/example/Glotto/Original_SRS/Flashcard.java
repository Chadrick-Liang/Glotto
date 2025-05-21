package com.example.Glotto.Orginal_SRS;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

public class Flashcard implements Flashcards {
    private final String wordID;
    private final String foreignWord;
    private final String englishMeaning;
    private final String exampleEnglish;
    private final String exampleForeign;
    private BigDecimal retentionScore;
    private LocalDateTime lastReviewed;
    private LocalDateTime nextReview;
    private Duration interval;
    private String stage;
    private SRSalgo srsAlgo = new SRSalgo(3,1,3, 1.3);

    public Flashcard( String wordID, String foreignWord, String englishMeaning,
                      String exampleEnglish, String exampleForeign,
                      BigDecimal retentionScore, LocalDateTime lastReviewed,
                      LocalDateTime nextReview, Duration interval, String stage) {
        this.wordID = wordID;
        this.foreignWord = foreignWord;
        this.englishMeaning = englishMeaning;
        this.exampleEnglish = exampleEnglish;
        this.exampleForeign = exampleForeign;
        this.retentionScore = new BigDecimal(0);
        this.lastReviewed = lastReviewed;
        this.nextReview = nextReview;
        this.interval = interval;
        this.stage = stage;
    }

    @Override
    public String getWordID() { return wordID; }
    @Override
    public String getForeignWord() { return foreignWord; }
    @Override
    public String getEnglishMeaning() { return englishMeaning; }
    @Override
    public String getExampleEnglish() { return exampleEnglish; }
    @Override
    public String getExampleForeign() { return exampleForeign; }
    @Override
    public BigDecimal getRetentionScore() { return retentionScore; }

    public LocalDateTime getLastReviewed() { return lastReviewed; }

    public LocalDateTime getNextReview() { return nextReview; }

    public Duration getInterval() { return interval; }

    public String getStage() { return stage; }

    public void setSrsAlgo(SRSalgo algo) { this.srsAlgo = algo; }

    public long calcDelay() {
        return Duration.between(nextReview, LocalDateTime.now()).toDays();
    }
    @Override
    public void update_RS_interval(int q, long delay) {
        long interval_long = interval.toDays();
        Object[] RS_Interval_Stage = srsAlgo.update_RS_interval(interval_long, retentionScore, q, stage, lastReviewed, delay);
        this.retentionScore = (BigDecimal) RS_Interval_Stage[0];
        this.interval = (Duration) RS_Interval_Stage[1];
        this.stage = (String) RS_Interval_Stage[2];
    }

    public void updateReviewDate() {
        lastReviewed = LocalDateTime.now();
        nextReview = lastReviewed.plus(interval);
    }

}
