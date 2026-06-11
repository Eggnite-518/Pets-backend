package com.example.pets_backend.service.video;

/**
 * A single step in the fulfillment video processing chain.
 */
public interface VideoProcessingStep {

    /**
     * Executes one processing step.
     *
     * @param context video processing context
     */
    void execute(VideoProcessingContext context);
}
