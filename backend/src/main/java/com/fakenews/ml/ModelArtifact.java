package com.fakenews.ml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

/**
 * ModelArtifact encapsulates the serialized state of the trained
 * TF-IDF + Logistic Regression model.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelArtifact {

    private String version;
    private String algorithm;
    private String datasetName;
    private String trainedAt;
    private double bias;
    private double fakeThreshold;
    private double genuineThreshold;
    private Map<String, Integer> vocabulary;
    private Map<String, Double> idf;
    private Map<String, Double> coefficients;
    private EvaluationMetrics metrics;

    public ModelArtifact() {
    }

    public ModelArtifact(String version, String algorithm, String datasetName, String trainedAt,
                         double bias, double fakeThreshold, double genuineThreshold,
                         Map<String, Integer> vocabulary, Map<String, Double> idf,
                         Map<String, Double> coefficients, EvaluationMetrics metrics) {
        this.version = version;
        this.algorithm = algorithm;
        this.datasetName = datasetName;
        this.trainedAt = trainedAt;
        this.bias = bias;
        this.fakeThreshold = fakeThreshold;
        this.genuineThreshold = genuineThreshold;
        this.vocabulary = vocabulary;
        this.idf = idf;
        this.coefficients = coefficients;
        this.metrics = metrics;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public String getTrainedAt() {
        return trainedAt;
    }

    public void setTrainedAt(String trainedAt) {
        this.trainedAt = trainedAt;
    }

    public double getBias() {
        return bias;
    }

    public void setBias(double bias) {
        this.bias = bias;
    }

    public double getFakeThreshold() {
        return fakeThreshold;
    }

    public void setFakeThreshold(double fakeThreshold) {
        this.fakeThreshold = fakeThreshold;
    }

    public double getGenuineThreshold() {
        return genuineThreshold;
    }

    public void setGenuineThreshold(double genuineThreshold) {
        this.genuineThreshold = genuineThreshold;
    }

    public Map<String, Integer> getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(Map<String, Integer> vocabulary) {
        this.vocabulary = vocabulary;
    }

    public Map<String, Double> getIdf() {
        return idf;
    }

    public void setIdf(Map<String, Double> idf) {
        this.idf = idf;
    }

    public Map<String, Double> getCoefficients() {
        return coefficients;
    }

    public void setCoefficients(Map<String, Double> coefficients) {
        this.coefficients = coefficients;
    }

    public EvaluationMetrics getMetrics() {
        return metrics;
    }

    public void setMetrics(EvaluationMetrics metrics) {
        this.metrics = metrics;
    }

    public static class EvaluationMetrics {
        private double accuracy;
        private double precision;
        private double recall;
        private double f1;
        private int totalSamples;
        private int trainSamples;
        private int testSamples;
        private ConfusionMatrix confusionMatrix;

        public EvaluationMetrics() {
        }

        public EvaluationMetrics(double accuracy, double precision, double recall, double f1,
                                 int totalSamples, int trainSamples, int testSamples,
                                 ConfusionMatrix confusionMatrix) {
            this.accuracy = accuracy;
            this.precision = precision;
            this.recall = recall;
            this.f1 = f1;
            this.totalSamples = totalSamples;
            this.trainSamples = trainSamples;
            this.testSamples = testSamples;
            this.confusionMatrix = confusionMatrix;
        }

        public double getAccuracy() {
            return accuracy;
        }

        public void setAccuracy(double accuracy) {
            this.accuracy = accuracy;
        }

        public double getPrecision() {
            return precision;
        }

        public void setPrecision(double precision) {
            this.precision = precision;
        }

        public double getRecall() {
            return recall;
        }

        public void setRecall(double recall) {
            this.recall = recall;
        }

        public double getF1() {
            return f1;
        }

        public void setF1(double f1) {
            this.f1 = f1;
        }

        public int getTotalSamples() {
            return totalSamples;
        }

        public void setTotalSamples(int totalSamples) {
            this.totalSamples = totalSamples;
        }

        public int getTrainSamples() {
            return trainSamples;
        }

        public void setTrainSamples(int trainSamples) {
            this.trainSamples = trainSamples;
        }

        public int getTestSamples() {
            return testSamples;
        }

        public void setTestSamples(int testSamples) {
            this.testSamples = testSamples;
        }

        public ConfusionMatrix getConfusionMatrix() {
            return confusionMatrix;
        }

        public void setConfusionMatrix(ConfusionMatrix confusionMatrix) {
            this.confusionMatrix = confusionMatrix;
        }
    }

    public static class ConfusionMatrix {
        private int truePositives;   // Predicted Fake, Actual Fake
        private int falsePositives;  // Predicted Fake, Actual Real
        private int trueNegatives;   // Predicted Real, Actual Real
        private int falseNegatives;  // Predicted Real, Actual Fake

        public ConfusionMatrix() {
        }

        public ConfusionMatrix(int truePositives, int falsePositives, int trueNegatives, int falseNegatives) {
            this.truePositives = truePositives;
            this.falsePositives = falsePositives;
            this.trueNegatives = trueNegatives;
            this.falseNegatives = falseNegatives;
        }

        public int getTruePositives() {
            return truePositives;
        }

        public void setTruePositives(int truePositives) {
            this.truePositives = truePositives;
        }

        public int getFalsePositives() {
            return falsePositives;
        }

        public void setFalsePositives(int falsePositives) {
            this.falsePositives = falsePositives;
        }

        public int getTrueNegatives() {
            return trueNegatives;
        }

        public void setTrueNegatives(int trueNegatives) {
            this.trueNegatives = trueNegatives;
        }

        public int getFalseNegatives() {
            return falseNegatives;
        }

        public void setFalseNegatives(int falseNegatives) {
            this.falseNegatives = falseNegatives;
        }
    }
}
