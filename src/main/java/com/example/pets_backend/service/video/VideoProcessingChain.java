package com.example.pets_backend.service.video;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Executes fulfillment video processing steps in order.
 */
@Service
@RequiredArgsConstructor
public class VideoProcessingChain {

    private final List<VideoProcessingStep> videoProcessingSteps;

    /**
     * Runs all configured processing steps.
     *
     * @param context video processing context
     */
    public void process(VideoProcessingContext context) {
        videoProcessingSteps.forEach(step -> step.execute(context));
    }
}
