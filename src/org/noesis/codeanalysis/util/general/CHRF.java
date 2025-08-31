package org.noesis.codeanalysis.util.general;

import java.util.HashMap;
import java.util.Map;

public class CHRF {

    private final int maxOrder;
    private final double beta;

    public CHRF() {
        this.maxOrder = 6;
        this.beta = 2.0d;
    }

    public CHRF(int maxOrder, double beta) {
        this.maxOrder = maxOrder;
        this.beta = beta;
    }

    public static class Score {
        public final double precision;
        public final double recall;
        public final double fscore;

        public Score(double precision, double recall, double fscore) {
            this.precision = precision;
            this.recall = recall;
            this.fscore = fscore;
        }

        @Override
        public String toString() {
            return String.format("chrF2 = %.2f (P: %.2f, R: %.2f)", fscore * 100, precision * 100, recall * 100);
        }
    }

    public Score compute(String reference, String hypothesis) {
        double totalPrecision = 0.0;
        double totalRecall = 0.0;

        for (int n = 1; n <= maxOrder; n++) {
            Map<String, Integer> refNgrams = getCharNgrams(reference, n);
            Map<String, Integer> hypNgrams = getCharNgrams(hypothesis, n);

            int overlap = 0;
            int refTotal = 0;
            int hypTotal = 0;

            for (String ngram : refNgrams.keySet()) {
                refTotal += refNgrams.get(ngram);
                if (hypNgrams.containsKey(ngram)) {
                    overlap += Math.min(refNgrams.get(ngram), hypNgrams.get(ngram));
                }
            }

            for (int val : hypNgrams.values()) {
                hypTotal += val;
            }

            double precision = hypTotal == 0 ? 0 : (double) overlap / hypTotal;
            double recall = refTotal == 0 ? 0 : (double) overlap / refTotal;

            totalPrecision += precision;
            totalRecall += recall;
        }

        double avgPrecision = totalPrecision / maxOrder;
        double avgRecall = totalRecall / maxOrder;
        double beta2 = beta * beta;

        double fscore = (avgPrecision + avgRecall == 0)
                ? 0.0
                : (1 + beta2) * avgPrecision * avgRecall / (beta2 * avgPrecision + avgRecall);

        return new Score(avgPrecision, avgRecall, fscore);
    }

    private Map<String, Integer> getCharNgrams(String text, int n) {
        Map<String, Integer> ngramCounts = new HashMap<>();
        for (int i = 0; i <= text.length() - n; i++) {
            String ngram = text.substring(i, i + n);
            ngramCounts.put(ngram, ngramCounts.getOrDefault(ngram, 0) + 1);
        }
        return ngramCounts;
    }

    // Example usage
    public static void main(String[] args) {
        CHRF chrf = new CHRF(6, 2.0); // max char order = 6, beta = 2
        String reference = "val foo = listOf(1, 2, 3)";
        String hypothesis = "val foo = listOf(1, 2)";
        Score score = chrf.compute(reference, hypothesis);
        System.out.println(score);
    }
}


