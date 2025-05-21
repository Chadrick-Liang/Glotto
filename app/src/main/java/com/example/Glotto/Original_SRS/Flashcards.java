package com.example.Glotto.Orginal_SRS;

import java.math.BigDecimal;

public interface Flashcards {
    String getWordID();
    String getForeignWord();
    String getEnglishMeaning();
    String getExampleEnglish();
    String getExampleForeign();
    BigDecimal getRetentionScore();
    void update_RS_interval(int q, long delay);
}
