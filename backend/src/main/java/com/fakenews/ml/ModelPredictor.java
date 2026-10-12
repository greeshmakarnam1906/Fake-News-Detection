package com.fakenews.ml;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
public class ModelPredictor {

    private static final Logger logger = LoggerFactory.getLogger(ModelPredictor.class);

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    @Value("${app.ml.model-location:classpath:model.json}")
    private String modelLocation = "classpath:model.json";

    private ModelArtifact modelArtifact;
    private boolean loaded = false;
    private String loadError = null;

    private double[] idfArray;
    private double[] weightsArray;
    private int vocabSize = 0;

    private static final List<String> STANDARD_LIMITATIONS = List.of(
            "Model scores reflect statistical resemblance to training data style, not real-time journalistic fact-checking.",
            "Satirical, ironic, or parody content can share stylistic markers with fabricated news.",
            "Breaking news events and newly emerged terminology may experience distribution shift.",
            "Factual accuracy of specific numbers, dates, and quotes cannot be determined by text style alone."
    );

    private static final List<String> VERIFICATION_ACTIONS = List.of(
            "Verify claims against established primary sources and official wire reports.",
            "Check publication date and ensure quotes or photos are not taken out of historical context.",
            "Consult verified fact-checking databases (e.g., PolitiFact, Snopes, FactCheck.org).",
            "Evaluate domain reputability and search for corroborating reporting by independent outlets."
    );

    public ModelPredictor(ResourceLoader resourceLoader, ObjectMapper objectMapper) {
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        loadModel();
    }

    public synchronized void loadModel() {
        try {
            logger.info("Loading ML model artifact from location: {}", modelLocation);
            Resource resource = resourceLoader.getResource(modelLocation);

            if (!resource.exists()) {
                this.loaded = false;
                this.loadError = "Model file does not exist at " + modelLocation;
                logger.warn("Model artifact not found: {}", loadError);
                return;
            }

            try (InputStream in = resource.getInputStream()) {
                this.modelArtifact = objectMapper.readValue(in, ModelArtifact.class);
            }

            if (modelArtifact.getVocabulary() == null || modelArtifact.getCoefficients() == null) {
                this.loaded = false;
                this.loadError = "Model artifact is malformed (missing vocabulary or coefficients)";
                logger.error("Malformed model artifact: {}", loadError);
                return;
            }

            // Build fast indexing arrays
            this.vocabSize = modelArtifact.getVocabulary().size();
            this.idfArray = new double[vocabSize];
            this.weightsArray = new double[vocabSize];

            for (Map.Entry<String, Integer> entry : modelArtifact.getVocabulary().entrySet()) {
                String term = entry.getKey();
                int idx = entry.getValue();
                Double idfVal = modelArtifact.getIdf() != null ? modelArtifact.getIdf().get(term) : 1.0;
                Double wVal = modelArtifact.getCoefficients().get(term);

                this.idfArray[idx] = (idfVal != null) ? idfVal : 1.0;
                this.weightsArray[idx] = (wVal != null) ? wVal : 0.0;
            }

            this.loaded = true;
            this.loadError = null;
            logger.info("Successfully loaded ML model version: {} with {} vocabulary features.",
                    modelArtifact.getVersion(), vocabSize);

        } catch (Exception e) {
            this.loaded = false;
            this.loadError = "Failed to load model: " + e.getMessage();
            logger.error("Exception occurred while loading model: {}", e.getMessage(), e);
        }
    }

    public boolean isLoaded() {
        return loaded;
    }

    public String getLoadError() {
        return loadError;
    }

    public ModelArtifact getModelArtifact() {
        return modelArtifact;
    }

    /**
     * Injects a ModelArtifact manually (useful for unit tests without classpath file).
     */
    public synchronized void setModelArtifact(ModelArtifact artifact) {
        this.modelArtifact = artifact;
        if (artifact == null || artifact.getVocabulary() == null) {
            this.loaded = false;
            this.loadError = "Null or invalid artifact";
            return;
        }

        this.vocabSize = artifact.getVocabulary().size();
        this.idfArray = new double[vocabSize];
        this.weightsArray = new double[vocabSize];

        for (Map.Entry<String, Integer> entry : artifact.getVocabulary().entrySet()) {
            String term = entry.getKey();
            int idx = entry.getValue();
            Double idfVal = artifact.getIdf() != null ? artifact.getIdf().get(term) : 1.0;
            Double wVal = artifact.getCoefficients().get(term);

            this.idfArray[idx] = (idfVal != null) ? idfVal : 1.0;
            this.weightsArray[idx] = (wVal != null) ? wVal : 0.0;
        }

        this.loaded = true;
        this.loadError = null;
    }

    public PredictionResult predict(String headline, String text) {
        if (!loaded) {
            throw new ModelNotAvailableException("ML Model is unavailable: " + (loadError != null ? loadError : "Model not loaded"));
        }

        String fullText = (headline != null ? headline + " " : "") + (text != null ? text : "");
        int charCount = fullText.trim().length();

        List<String> tokens = TextPreprocessor.tokenize(fullText);
        int wordCount = tokens.size();

        Map<Integer, Double> termCounts = new HashMap<>();
        Map<Integer, String> termLookup = new HashMap<>();

        for (String t : tokens) {
            Integer idx = modelArtifact.getVocabulary().get(t);
            if (idx != null) {
                termCounts.merge(idx, 1.0, Double::sum);
                termLookup.put(idx, t);
            }
        }

        double z = modelArtifact.getBias();
        List<PredictionResult.FeatureContribution> allContributions = new ArrayList<>();

        if (!termCounts.isEmpty()) {
            // Compute sublinear TF-IDF vector: (1 + ln(count)) * idf
            Map<Integer, Double> tfidfVector = new HashMap<>();
            double sumSq = 0.0;

            for (Map.Entry<Integer, Double> entry : termCounts.entrySet()) {
                int idx = entry.getKey();
                double count = entry.getValue();
                double tf = 1.0 + Math.log(count);
                double val = tf * idfArray[idx];
                tfidfVector.put(idx, val);
                sumSq += val * val;
            }

            // L2 normalize
            double norm = Math.sqrt(sumSq);
            if (norm > 0) {
                for (Map.Entry<Integer, Double> entry : tfidfVector.entrySet()) {
                    entry.setValue(entry.getValue() / norm);
                }
            }

            // Dot product with model weights
            for (Map.Entry<Integer, Double> entry : tfidfVector.entrySet()) {
                int idx = entry.getKey();
                double x_i = entry.getValue();
                double w_i = weightsArray[idx];
                double contribution = w_i * x_i;

                z += contribution;
                allContributions.add(new PredictionResult.FeatureContribution(
                        termLookup.get(idx),
                        round(w_i, 4),
                        round(contribution, 4)
                ));
            }
        }

        // Model score via sigmoid
        double score = sigmoid(z);
        double roundedScore = round(score, 4);

        // Decision rule:
        // Uncertain zone: 0.40 <= score <= 0.60
        // Likely Fake: score > 0.60
        // Likely Genuine: score < 0.40
        String label;
        String explanation;

        double fakeThreshold = modelArtifact.getFakeThreshold() > 0 ? modelArtifact.getFakeThreshold() : 0.60;
        double genuineThreshold = modelArtifact.getGenuineThreshold() > 0 ? modelArtifact.getGenuineThreshold() : 0.40;

        if (roundedScore >= fakeThreshold) {
            label = "Likely Fake";
            explanation = String.format("The model detected lexical and stylistic patterns frequently found in unverified or fabricated stories (Model Score: %.2f).", roundedScore);
        } else if (roundedScore <= genuineThreshold) {
            label = "Likely Genuine";
            explanation = String.format("The content exhibits structural and stylistic characteristics consistent with verified news sources in the training corpus (Model Score: %.2f).", roundedScore);
        } else {
            label = "Uncertain / Needs Verification";
            explanation = String.format("The content falls within the uncertain range (0.40 - 0.60) with mixed linguistic indicators (Model Score: %.2f). Manual verification recommended.", roundedScore);
        }

        // Extract Top 5 words pushing towards Fake (positive contribution)
        List<PredictionResult.FeatureContribution> topFake = allContributions.stream()
                .filter(fc -> fc.getContribution() > 0.0001)
                .sorted((a, b) -> Double.compare(b.getContribution(), a.getContribution()))
                .limit(5)
                .toList();

        // Extract Top 5 words pushing towards Genuine (negative contribution, largest magnitude)
        List<PredictionResult.FeatureContribution> topGenuine = allContributions.stream()
                .filter(fc -> fc.getContribution() < -0.0001)
                .sorted(Comparator.comparingDouble(PredictionResult.FeatureContribution::getContribution))
                .limit(5)
                .toList();

        return new PredictionResult(
                label,
                roundedScore,
                modelArtifact.getVersion(),
                explanation,
                topFake,
                topGenuine,
                STANDARD_LIMITATIONS,
                VERIFICATION_ACTIONS,
                charCount,
                wordCount
        );
    }

    private double sigmoid(double z) {
        if (z < -35.0) return 0.0;
        if (z > 35.0) return 1.0;
        return 1.0 / (1.0 + Math.exp(-z));
    }

    private double round(double val, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(val * factor) / factor;
    }
}
