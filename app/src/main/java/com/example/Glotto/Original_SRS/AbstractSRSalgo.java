package com.example.Glotto.Orginal_SRS;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public abstract class AbstractSRSalgo {
    int passScore;
    int failScore;
    int highestScore;
    BigDecimal lowestRS;
    int maxInterval;

    AbstractSRSalgo(int ps, int fs, int hs, BigDecimal LRS) {
        passScore = ps;
        failScore = fs;
        highestScore = hs;
        lowestRS = LRS;
    }

    public int getPassScore() {
        return passScore;
    }

    public int getFailScore() {
        return failScore;
    }

    public int getHighestScore() {
        return highestScore;
    }

    public BigDecimal getLowestRS() {
        return lowestRS;
    }

    public int getMaxInterval() {
        return maxInterval;
    }

    public void setPassScore(int passScore) {
        this.passScore = passScore;
    }

    public void setFailScore(int failScore) {
        this.failScore = failScore;
    }

    public void setHighestScore(int highestScore) {
        this.highestScore = highestScore;
    }

    public void setLowestRS(BigDecimal lowestRS) {
        this.lowestRS = lowestRS;
    }

    public void setMaxInterval(int maxInterval) {
        this.maxInterval = maxInterval;
    }

    public abstract BigDecimal calcRetentionScore(BigDecimal rs, int q);
    public abstract BigDecimal intervalFunction(long prevInterval, BigDecimal rs, long delay);
    public abstract int calcInterval(long prevInterval, BigDecimal rs, long delay);
    public abstract Object[] update_RS_interval(long prevInterval, BigDecimal rs, int q, String stage, LocalDateTime LR, long delay);
}

