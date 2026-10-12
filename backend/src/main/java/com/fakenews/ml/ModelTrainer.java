package com.fakenews.ml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

/**
 * ModelTrainer runs the end-to-end, reproducible training workflow:
 * 1. Reads the public labelled dataset (fake_or_real_news.csv).
 * 2. Cleans text and strips publisher/wire leakage tags using TextPreprocessor.
 * 3. Identifies and removes duplicate or near-duplicate articles.
 * 4. Performs a stratified 80/20 train/test split with a fixed random seed (42).
 * 5. Builds the TF-IDF feature vocabulary and smoothed IDF weights.
 * 6. Trains a Logistic Regression binary classifier using SGD with L2 regularization.
 * 7. Evaluates performance on the held-out test split (Accuracy, Precision, Recall, F1, Confusion Matrix).
 * 8. Serializes model.json and ml/reports/evaluation.json.
 */
public class ModelTrainer {

    private static final int RANDOM_SEED = 42;
    private static final double TRAIN_SPLIT_RATIO = 0.80;
    private static final int MAX_FEATURES = 6000;
    private static final int MIN_DOC_FREQ = 4;
    private static final double MAX_DOC_FREQ_RATIO = 0.70;
    private static final int EPOCHS = 35;
    private static final double LEARNING_RATE = 0.5;
    private static final double L2_REGULARIZATION = 1e-4;

    public static class Sample {
        String id;
        String title;
        String rawText;
        String cleanedText;
        int label; // 1 = FAKE, 0 = REAL
        List<String> tokens;

        public Sample(String id, String title, String rawText, int label) {
            this.id = id;
            this.title = title;
            this.rawText = rawText;
            this.label = label;
        }
    }

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("  FAKE NEWS CLASSIFIER - JAVA REPRODUCIBLE MODEL TRAINER");
        System.out.println("=================================================================");

        Path datasetPath = Paths.get("ml/data/fake_or_real_news.csv");
        if (!Files.exists(datasetPath)) {
            // Check fallback relative to backend directory
            datasetPath = Paths.get("../ml/data/fake_or_real_news.csv");
            if (!Files.exists(datasetPath)) {
                System.err.println("ERROR: Dataset file not found at ml/data/fake_or_real_news.csv or ../ml/data/fake_or_real_news.csv");
                System.exit(1);
            }
        }

        try {
            System.out.println("1. Loading dataset from: " + datasetPath.toAbsolutePath());
            List<Sample> rawSamples = loadDataset(datasetPath);
            System.out.println("   Loaded " + rawSamples.size() + " raw records from CSV.");

            System.out.println("2. Deduplicating and preprocessing samples...");
            List<Sample> distinctSamples = preprocessAndDeduplicate(rawSamples);
            System.out.println("   Remaining distinct, valid samples: " + distinctSamples.size());

            System.out.println("3. Performing stratified 80/20 train/test split (seed=" + RANDOM_SEED + ")...");
            SplitResult split = stratifiedSplit(distinctSamples, TRAIN_SPLIT_RATIO, RANDOM_SEED);
            System.out.println("   Train set size: " + split.trainSet.size() + " ("
                    + countLabel(split.trainSet, 1) + " Fake, " + countLabel(split.trainSet, 0) + " Genuine)");
            System.out.println("   Test set size:  " + split.testSet.size() + " ("
                    + countLabel(split.testSet, 1) + " Fake, " + countLabel(split.testSet, 0) + " Genuine)");

            System.out.println("4. Building TF-IDF vocabulary (maxFeatures=" + MAX_FEATURES + ")...");
            TfidfModel tfidf = buildTfidf(split.trainSet);
            System.out.println("   Selected vocabulary size: " + tfidf.vocabulary.size() + " terms.");

            System.out.println("5. Training Logistic Regression with L2 regularization (epochs=" + EPOCHS + ")...");
            LogisticRegressionModel lr = trainLogisticRegression(split.trainSet, tfidf);

            System.out.println("6. Evaluating on held-out test split...");
            ModelArtifact.EvaluationMetrics eval = evaluateModel(split.testSet, tfidf, lr, distinctSamples.size());

            System.out.println("-----------------------------------------------------------------");
            System.out.printf("   Accuracy:   %.4f (%.2f%%)%n", eval.getAccuracy(), eval.getAccuracy() * 100);
            System.out.printf("   Precision:  %.4f%n", eval.getPrecision());
            System.out.printf("   Recall:     %.4f%n", eval.getRecall());
            System.out.printf("   F1-Score:   %.4f%n", eval.getF1());
            System.out.println("   Confusion Matrix: ");
            System.out.println("     True Positives (Fake->Fake):     " + eval.getConfusionMatrix().getTruePositives());
            System.out.println("     False Positives (Real->Fake):    " + eval.getConfusionMatrix().getFalsePositives());
            System.out.println("     True Negatives (Real->Real):     " + eval.getConfusionMatrix().getTrueNegatives());
            System.out.println("     False Negatives (Fake->Real):    " + eval.getConfusionMatrix().getFalseNegatives());
            System.out.println("-----------------------------------------------------------------");

            System.out.println("7. Exporting model artifacts and evaluation report...");
            exportArtifacts(tfidf, lr, eval);

            System.out.println("=================================================================");
            System.out.println("  TRAINING COMPLETED SUCCESSFULLY!");
            System.out.println("=================================================================");

        } catch (Exception e) {
            System.err.println("Training failed with exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static List<Sample> loadDataset(Path csvPath) throws Exception {
        List<Sample> samples = new ArrayList<>();
        try (Reader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(csvPath.toFile()), StandardCharsets.UTF_8));
             CSVReader csvReader = new CSVReaderBuilder(reader).build()) {

            String[] header = csvReader.readNext();
            if (header == null) {
                throw new IllegalStateException("CSV file is empty: " + csvPath);
            }

            // Identify column positions: usually [, title, text, label]
            int titleCol = -1;
            int textCol = -1;
            int labelCol = -1;

            for (int i = 0; i < header.length; i++) {
                String h = header[i].trim().toLowerCase(Locale.ROOT);
                if (h.contains("title")) titleCol = i;
                else if (h.contains("text")) textCol = i;
                else if (h.contains("label")) labelCol = i;
            }

            if (textCol == -1 || labelCol == -1) {
                // Fallback positional indexing for McIntire dataset: 0=id, 1=title, 2=text, 3=label
                titleCol = 1;
                textCol = 2;
                labelCol = 3;
            }

            String[] row;
            int rowIdx = 0;
            while ((row = csvReader.readNext()) != null) {
                rowIdx++;
                if (row.length <= Math.max(textCol, labelCol)) {
                    continue;
                }

                String id = row.length > 0 ? row[0] : String.valueOf(rowIdx);
                String title = (titleCol >= 0 && titleCol < row.length) ? row[titleCol] : "";
                String text = row[textCol];
                String rawLabel = row[labelCol].trim().toUpperCase(Locale.ROOT);

                int label;
                if ("FAKE".equals(rawLabel) || "1".equals(rawLabel)) {
                    label = 1;
                } else if ("REAL".equals(rawLabel) || "GENUINE".equals(rawLabel) || "0".equals(rawLabel)) {
                    label = 0;
                } else {
                    continue; // Skip unrecognized label
                }

                samples.add(new Sample(id, title, text, label));
            }
        }
        return samples;
    }

    private static List<Sample> preprocessAndDeduplicate(List<Sample> rawSamples) {
        Set<String> seenFingerprints = new HashSet<>();
        List<Sample> cleanList = new ArrayList<>();

        for (Sample s : rawSamples) {
            String combined = (s.title != null ? s.title : "") + " " + (s.rawText != null ? s.rawText : "");
            String cleaned = TextPreprocessor.cleanText(combined);
            if (cleaned.length() < 30) {
                continue; // Skip articles with fewer than 30 characters of meaningful content
            }

            // Deduplication fingerprint using first 200 characters of cleaned text
            String fingerprint = cleaned.substring(0, Math.min(cleaned.length(), 200));
            if (seenFingerprints.contains(fingerprint)) {
                continue;
            }
            seenFingerprints.add(fingerprint);

            s.cleanedText = cleaned;
            s.tokens = TextPreprocessor.tokenize(combined);
            if (s.tokens.size() >= 5) {
                cleanList.add(s);
            }
        }
        return cleanList;
    }

    private static class SplitResult {
        List<Sample> trainSet;
        List<Sample> testSet;
        SplitResult(List<Sample> train, List<Sample> test) {
            this.trainSet = train;
            this.testSet = test;
        }
    }

    private static SplitResult stratifiedSplit(List<Sample> samples, double trainRatio, int seed) {
        List<Sample> fakeList = new ArrayList<>();
        List<Sample> realList = new ArrayList<>();

        for (Sample s : samples) {
            if (s.label == 1) fakeList.add(s);
            else realList.add(s);
        }

        Collections.shuffle(fakeList, new Random(seed));
        Collections.shuffle(realList, new Random(seed));

        int trainFakeCount = (int) Math.round(fakeList.size() * trainRatio);
        int trainRealCount = (int) Math.round(realList.size() * trainRatio);

        List<Sample> train = new ArrayList<>();
        train.addAll(fakeList.subList(0, trainFakeCount));
        train.addAll(realList.subList(0, trainRealCount));
        Collections.shuffle(train, new Random(seed));

        List<Sample> test = new ArrayList<>();
        test.addAll(fakeList.subList(trainFakeCount, fakeList.size()));
        test.addAll(realList.subList(trainRealCount, realList.size()));
        Collections.shuffle(test, new Random(seed));

        return new SplitResult(train, test);
    }

    private static int countLabel(List<Sample> samples, int targetLabel) {
        int count = 0;
        for (Sample s : samples) {
            if (s.label == targetLabel) count++;
        }
        return count;
    }

    private static class TfidfModel {
        Map<String, Integer> vocabulary = new LinkedHashMap<>();
        Map<String, Double> idf = new LinkedHashMap<>();
        double[] idfArray;
    }

    private static TfidfModel buildTfidf(List<Sample> trainSamples) {
        Map<String, Integer> docFreq = new HashMap<>();
        int nDocs = trainSamples.size();

        for (Sample s : trainSamples) {
            Set<String> uniqueTokens = new HashSet<>(s.tokens);
            for (String token : uniqueTokens) {
                docFreq.merge(token, 1, Integer::sum);
            }
        }

        // Filter vocabulary based on frequency thresholds
        int maxDocFreq = (int) Math.floor(nDocs * MAX_DOC_FREQ_RATIO);
        List<Map.Entry<String, Integer>> candidateTerms = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : docFreq.entrySet()) {
            if (entry.getValue() >= MIN_DOC_FREQ && entry.getValue() <= maxDocFreq) {
                candidateTerms.add(entry);
            }
        }

        // Sort by doc frequency descending and select top features
        candidateTerms.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        int vocabLimit = Math.min(candidateTerms.size(), MAX_FEATURES);

        TfidfModel model = new TfidfModel();
        model.idfArray = new double[vocabLimit];

        for (int i = 0; i < vocabLimit; i++) {
            String term = candidateTerms.get(i).getKey();
            int df = candidateTerms.get(i).getValue();

            // Smooth IDF calculation: ln((1 + N) / (1 + df)) + 1.0
            double idfVal = Math.log((1.0 + nDocs) / (1.0 + df)) + 1.0;

            model.vocabulary.put(term, i);
            model.idf.put(term, idfVal);
            model.idfArray[i] = idfVal;
        }

        return model;
    }

    /**
     * Converts a document's tokens into an L2-normalized sparse TF-IDF vector.
     */
    public static Map<Integer, Double> vectorize(List<String> tokens, TfidfModel tfidf) {
        Map<Integer, Double> termCounts = new HashMap<>();
        for (String t : tokens) {
            Integer idx = tfidf.vocabulary.get(t);
            if (idx != null) {
                termCounts.merge(idx, 1.0, Double::sum);
            }
        }

        if (termCounts.isEmpty()) {
            return Collections.emptyMap();
        }

        // Sublinear TF-IDF: (1 + ln(count)) * idf
        Map<Integer, Double> tfidfVector = new HashMap<>();
        double sumSq = 0.0;

        for (Map.Entry<Integer, Double> entry : termCounts.entrySet()) {
            int idx = entry.getKey();
            double count = entry.getValue();
            double tf = 1.0 + Math.log(count);
            double val = tf * tfidf.idfArray[idx];
            tfidfVector.put(idx, val);
            sumSq += val * val;
        }

        // L2 normalization
        double norm = Math.sqrt(sumSq);
        if (norm > 0) {
            for (Map.Entry<Integer, Double> entry : tfidfVector.entrySet()) {
                entry.setValue(entry.getValue() / norm);
            }
        }

        return tfidfVector;
    }

    private static class LogisticRegressionModel {
        double[] weights;
        double bias;
    }

    private static LogisticRegressionModel trainLogisticRegression(List<Sample> trainSamples, TfidfModel tfidf) {
        int vocabSize = tfidf.vocabulary.size();
        double[] weights = new double[vocabSize];
        double bias = 0.0;

        // Pre-vectorize training samples for fast training
        List<Map<Integer, Double>> vectorizedDocs = new ArrayList<>(trainSamples.size());
        int[] labels = new int[trainSamples.size()];

        for (int i = 0; i < trainSamples.size(); i++) {
            Sample s = trainSamples.get(i);
            vectorizedDocs.add(vectorize(s.tokens, tfidf));
            labels[i] = s.label;
        }

        Random rng = new Random(RANDOM_SEED);
        List<Integer> indices = new ArrayList<>(trainSamples.size());
        for (int i = 0; i < trainSamples.size(); i++) indices.add(i);

        double lr = LEARNING_RATE;

        for (int epoch = 0; epoch < EPOCHS; epoch++) {
            Collections.shuffle(indices, rng);
            double epochLoss = 0.0;

            for (int idx : indices) {
                Map<Integer, Double> x = vectorizedDocs.get(idx);
                int y = labels[idx];

                // Linear combination: z = w.x + b
                double z = bias;
                for (Map.Entry<Integer, Double> feat : x.entrySet()) {
                    z += weights[feat.getKey()] * feat.getValue();
                }

                // Sigmoid
                double p = sigmoid(z);
                double error = p - y;

                epochLoss += (y == 1 ? -Math.log(Math.max(p, 1e-15)) : -Math.log(Math.max(1.0 - p, 1e-15)));

                // Update weights with L2 regularization
                for (Map.Entry<Integer, Double> feat : x.entrySet()) {
                    int featIdx = feat.getKey();
                    double featVal = feat.getValue();
                    double grad = error * featVal + L2_REGULARIZATION * weights[featIdx];
                    weights[featIdx] -= lr * grad;
                }

                // Update bias
                bias -= lr * error;
            }

            // Learning rate decay
            lr = LEARNING_RATE / (1.0 + 0.05 * epoch);
        }

        LogisticRegressionModel model = new LogisticRegressionModel();
        model.weights = weights;
        model.bias = bias;
        return model;
    }

    public static double sigmoid(double z) {
        if (z < -35.0) return 0.0;
        if (z > 35.0) return 1.0;
        return 1.0 / (1.0 + Math.exp(-z));
    }

    private static ModelArtifact.EvaluationMetrics evaluateModel(List<Sample> testSamples,
                                                                 TfidfModel tfidf,
                                                                 LogisticRegressionModel lr,
                                                                 int totalDistinct) {
        int tp = 0; // Predicted Fake (1), Actual Fake (1)
        int fp = 0; // Predicted Fake (1), Actual Real (0)
        int tn = 0; // Predicted Real (0), Actual Real (0)
        int fn = 0; // Predicted Real (0), Actual Fake (1)

        for (Sample s : testSamples) {
            Map<Integer, Double> x = vectorize(s.tokens, tfidf);
            double z = lr.bias;
            for (Map.Entry<Integer, Double> feat : x.entrySet()) {
                z += lr.weights[feat.getKey()] * feat.getValue();
            }
            double prob = sigmoid(z);
            int pred = (prob >= 0.50) ? 1 : 0;

            if (pred == 1 && s.label == 1) tp++;
            else if (pred == 1 && s.label == 0) fp++;
            else if (pred == 0 && s.label == 0) tn++;
            else if (pred == 0 && s.label == 1) fn++;
        }

        int total = tp + fp + tn + fn;
        double accuracy = total > 0 ? (double) (tp + tn) / total : 0.0;
        double precision = (tp + fp) > 0 ? (double) tp / (tp + fp) : 0.0;
        double recall = (tp + fn) > 0 ? (double) tp / (tp + fn) : 0.0;
        double f1 = (precision + recall) > 0 ? 2.0 * (precision * recall) / (precision + recall) : 0.0;

        ModelArtifact.ConfusionMatrix matrix = new ModelArtifact.ConfusionMatrix(tp, fp, tn, fn);
        return new ModelArtifact.EvaluationMetrics(
                accuracy, precision, recall, f1,
                totalDistinct, totalDistinct - testSamples.size(), testSamples.size(), matrix
        );
    }

    private static void exportArtifacts(TfidfModel tfidf,
                                        LogisticRegressionModel lr,
                                        ModelArtifact.EvaluationMetrics metrics) throws Exception {
        Map<String, Double> coefficients = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : tfidf.vocabulary.entrySet()) {
            coefficients.put(entry.getKey(), lr.weights[entry.getValue()]);
        }

        ModelArtifact artifact = new ModelArtifact(
                "1.0.0",
                "TF-IDF + L2-Regularized Logistic Regression (Java)",
                "George McIntire Fake or Real News Dataset",
                Instant.now().toString(),
                lr.bias,
                0.60, // fake threshold
                0.40, // genuine threshold
                tfidf.vocabulary,
                tfidf.idf,
                coefficients,
                metrics
        );

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // 1. Write ml/models/model.json
        Path mlModelsDir = Paths.get("ml/models");
        Files.createDirectories(mlModelsDir);
        Path mlModelFile = mlModelsDir.resolve("model.json");
        mapper.writeValue(mlModelFile.toFile(), artifact);
        System.out.println("   Exported: " + mlModelFile.toAbsolutePath());

        // 2. Write backend/src/main/resources/model.json
        Path backendResDir = Paths.get("backend/src/main/resources");
        if (!Files.exists(backendResDir)) {
            backendResDir = Paths.get("src/main/resources");
        }
        Files.createDirectories(backendResDir);
        Path backendModelFile = backendResDir.resolve("model.json");
        mapper.writeValue(backendModelFile.toFile(), artifact);
        System.out.println("   Exported: " + backendModelFile.toAbsolutePath());

        // 3. Write ml/reports/evaluation.json
        Path mlReportsDir = Paths.get("ml/reports");
        Files.createDirectories(mlReportsDir);
        Path reportFile = mlReportsDir.resolve("evaluation.json");
        mapper.writeValue(reportFile.toFile(), metrics);
        System.out.println("   Exported: " + reportFile.toAbsolutePath());
    }
}
