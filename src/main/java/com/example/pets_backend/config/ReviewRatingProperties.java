package com.example.pets_backend.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "pets.review")
public class ReviewRatingProperties {

    private int lowScoreThreshold = 3;
    private List<String> lowScoreDimensions = new ArrayList<>(List.of("overall", "punctuality", "professional"));

    public int getLowScoreThreshold() {
        return lowScoreThreshold;
    }

    public void setLowScoreThreshold(int lowScoreThreshold) {
        this.lowScoreThreshold = lowScoreThreshold;
    }

    public List<String> getLowScoreDimensions() {
        return lowScoreDimensions;
    }

    public void setLowScoreDimensions(List<String> lowScoreDimensions) {
        this.lowScoreDimensions = lowScoreDimensions;
    }
}
