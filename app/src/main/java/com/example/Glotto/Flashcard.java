package com.example.Glotto;

public class Flashcard implements Flashcards {
    private final String foreignWord;
    private final String englishMeaning;
    private final String exampleEnglish;
    private final String exampleForeign;

    private final String audio;
    private double retentionScore;
    public Flashcard( String foreignWord, String englishMeaning,
                      String exampleEnglish, String exampleForeign, String audio,
                      double retentionScore) {
        this.foreignWord = foreignWord;
        this.englishMeaning = englishMeaning;
        this.exampleEnglish = exampleEnglish;
        this.exampleForeign = exampleForeign;
        this.audio = audio;
        this.retentionScore = retentionScore;
    }

    @Override
    public String getId() { return foreignWord; }
    @Override
    public String getForeignWord() { return foreignWord; }
    @Override
    public String getEnglishMeaning() { return englishMeaning; }
    @Override
    public String getExampleEnglish() { return exampleEnglish; }
    @Override
    public String getExampleForeign() { return exampleForeign; }
    @Override
    public String getAudioPath() { return audio; }
    @Override
    public double getRetentionScore() { return retentionScore; }
    @Override
    public void updateRetentionScore(double adjustment) {
        retentionScore += adjustment;
    }

}
