package com.fakenews.ml;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ModelPredictor Inference & Decision Rules Tests")
class ModelPredictorTest {

    private final ResourceLoader resourceLoader = new DefaultResourceLoader();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Should successfully load trained model.json from classpath")
    void testLoadClasspathModel() {
        ModelPredictor predictor = new ModelPredictor(resourceLoader, objectMapper);
        predictor.init();

        assertTrue(predictor.isLoaded(), "Model should be loaded from classpath:model.json");
        assertNotNull(predictor.getModelArtifact());
        assertEquals("1.0.0", predictor.getModelArtifact().getVersion());
        assertTrue(predictor.getModelArtifact().getVocabulary().size() > 1000);
    }

    @Test
    @DisplayName("Should return valid prediction, score, and feature contributions")
    void testPredictionPipeline() {
        ModelPredictor predictor = new ModelPredictor(resourceLoader, objectMapper);
        predictor.init();

        String headline = "Breaking news report";
        String body = "The president announced new international trade policies during an official press conference in the capital city.";

        PredictionResult result = predictor.predict(headline, body);

        assertNotNull(result);
        assertNotNull(result.getLabel());
        assertTrue(result.getScore() >= 0.0 && result.getScore() <= 1.0);
        assertNotNull(result.getExplanation());
        assertFalse(result.getLimitations().isEmpty());
        assertFalse(result.getVerificationActions().isEmpty());
    }

    @Test
    @DisplayName("Should correctly classify in Uncertain zone when score is between 0.40 and 0.60")
    void testUncertainZoneClassification() {
        ModelPredictor predictor = new ModelPredictor(resourceLoader, objectMapper);

        // Build a controlled synthetic artifact with zero weights and bias = 0.0 (sigmoid(0) = 0.50)
        ModelArtifact artifact = new ModelArtifact(
                "test-1.0",
                "TF-IDF + LR",
                "Synthetic",
                "2026-10-11T00:00:00Z",
                0.0, // bias 0.0 -> p = 0.50 (Uncertain)
                0.60,
                0.40,
                Map.of("neutral", 0),
                Map.of("neutral", 1.0),
                Map.of("neutral", 0.0),
                null
        );

        predictor.setModelArtifact(artifact);
        PredictionResult result = predictor.predict("Neutral headline", "neutral news content neutral");

        assertEquals("Uncertain / Needs Verification", result.getLabel());
        assertEquals(0.50, result.getScore(), 0.01);
    }

    @Test
    @DisplayName("Should throw ModelNotAvailableException if model is not loaded (never fake predictions)")
    void testMissingModelThrowsException() {
        ModelPredictor predictor = new ModelPredictor(resourceLoader, objectMapper);
        // Do not call init() -> model not loaded

        assertFalse(predictor.isLoaded());
        assertThrows(ModelNotAvailableException.class, () ->
                predictor.predict("Headline", "Sample body text")
        );
    }
}
