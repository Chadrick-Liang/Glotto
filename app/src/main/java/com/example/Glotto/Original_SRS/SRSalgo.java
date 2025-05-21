package com.example.Glotto.Orginal_SRS;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

public class SRSalgo extends AbstractSRSalgo {
    SRSalgo(int ps, int fs, int hs, double LRS) {
        super(ps, fs, hs, new BigDecimal(LRS));
    }

    public BigDecimal calcRetentionScore(BigDecimal rs, int q) {
        BigDecimal newRS;
        newRS = rs.add(BigDecimal.valueOf(0.1-(getHighestScore()-q) * (0.08+(getHighestScore()-q)*0.02)));
        if (newRS.compareTo(getLowestRS()) < 0) { // if newRS < lowestRS
            newRS = getLowestRS();
        }
        return newRS;
    }

    public BigDecimal intervalFunction(long prevInterval, BigDecimal rs, long delay) {
        BigDecimal result = new BigDecimal(0);
        if (prevInterval == 0) {
            result = new BigDecimal(1);
        } else if (prevInterval > 0) {
            result = rs.multiply(BigDecimal.valueOf(prevInterval + delay));
        }
        return result;
    }

    public int calcInterval(long prevInterval, BigDecimal rs, long delay) {
        MathContext mc = new MathContext(1, RoundingMode.UP);
        int result = intervalFunction(prevInterval, rs, delay).round(mc).intValue();
        if (result > getMaxInterval()) {
            result = getMaxInterval();
        }
        return result;
    }

    public Object[] update_RS_interval(long prevInterval, BigDecimal rs, int q, String stage, LocalDateTime LR, long delay) {
        Object[] result = new Object[3];
        Duration newInterval = Duration.ofMinutes(0);
        if (stage.equals("learning")) {
            if (q < getPassScore()) {
                newInterval = Duration.ofMinutes(1);
            } else {
                Duration sinceLR = Duration.between(LR, LocalDateTime.now());
                Duration minTime = Duration.ofMinutes(3);
                if (sinceLR.compareTo(minTime) < 0) { // if last review was < 3 mins ago
                    newInterval = Duration.ofMinutes(10);
                } else {
                    stage = "retaining";
                    rs = BigDecimal.valueOf(2.5);
                    newInterval = Duration.ofDays(calcInterval(0, rs, 0));
                }
            }
        } else if (stage.equals("retaining")) {
            if (q <= getFailScore()) {
                newInterval = Duration.ofDays(calcInterval(0, rs, 0));
            } else {
                switch(q) {
                    case 1: delay = (int) Math.floor(delay / 4.0);
                    case 2: delay = (int) Math.floor(delay / 2.0);
                    case 3: ;
                }
                rs = calcRetentionScore(rs, q);
                newInterval = Duration.ofDays(calcInterval(prevInterval, rs, delay));
            }
        }
        result[0] = rs;
        result[1] = newInterval;
        result[2] = stage;
        return result;
    }
}

