package com.example.memoria.utils;
import java.util.Calendar;
import java.util.Date;
public class SpacedRepetitionAlgo {
    //Class chứa kết quả tính toán
    public static class SRSResult {
        public double newEaseFactor;
        public int newInterval;
        public int newRepetitions;
        public Date nextReviewDate;

        public SRSResult(double newEaseFactor, int newInterval, int newRepetitions, Date nextReviewDate) {
            this.newEaseFactor = newEaseFactor;
            this.newInterval = newInterval;
            this.newRepetitions = newRepetitions;
            this.nextReviewDate = nextReviewDate;
        }
        public static SRSResult calculateNextReview(boolean isCorrect, int prevInterval, double prevEaseFactor, int prevRepetitions)
        {
            double newEaseFactor = prevEaseFactor;
            int newInterval;
            int newRepetitions;

            if(isCorrect){
                int quality = 4;
                newRepetitions = prevRepetitions + 1;
                newEaseFactor = prevEaseFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
                if (newEaseFactor < 1.3) {
                    newEaseFactor = 1.3;
                }
                if (newRepetitions == 1) {
                    newInterval = 1;
                } else if (newRepetitions == 2) {
                    newInterval = 6;
                } else {
                    newInterval = (int) Math.round(prevInterval * prevEaseFactor);
                }
            }
            else {
                newRepetitions = 0;
                newInterval = 1;

                newEaseFactor = prevEaseFactor - 0.2;
                if (newEaseFactor < 1.3) newEaseFactor = 1.3;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_YEAR, newInterval);
            Date nextReviewDate = calendar.getTime();

            return new SRSResult(newEaseFactor, newInterval, newRepetitions, nextReviewDate);

        }
    }
}
