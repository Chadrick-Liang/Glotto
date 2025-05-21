package com.example.Glotto;

public interface Flashcards {

    String getId();
    String getForeignWord();
    String getEnglishMeaning();
    String getExampleEnglish();
    String getExampleForeign();
    double getRetentionScore();
    String getAudioPath();
    void updateRetentionScore(double adjustment);
}
