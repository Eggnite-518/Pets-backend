package com.example.pets_backend.service.video;

import com.example.pets_backend.config.VideoUploadProperties;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Transcodes video to standard MP4 and burns a visible watermark.
 */
@Component
@Order(10)
@RequiredArgsConstructor
public class FfmpegWatermarkTranscodeStep implements VideoProcessingStep {

    private static final int WATERMARK_FONT_SIZE = 24;
    private static final String WATERMARK_X = "w-tw-w*0.03";
    private static final String WATERMARK_Y = "h-th-h*0.03";

    private final VideoUploadProperties videoUploadProperties;

    @Override
    public void execute(VideoProcessingContext context) {
        Path outputFile = buildOutputFile(context.getSourceFile());
        List<String> command = buildCommand(context, outputFile);
        runCommand(command);
        context.setProcessedFile(outputFile);
    }

    private Path buildOutputFile(Path sourceFile) {
        String filename = UUID.randomUUID() + "." + videoUploadProperties.getTargetExtension();
        return sourceFile.getParent().resolve(filename);
    }

    private List<String> buildCommand(VideoProcessingContext context, Path outputFile) {
        List<String> command = new ArrayList<>();
        command.add(videoUploadProperties.getFfmpegPath());
        command.add("-y");
        command.add("-i");
        command.add(context.getSourceFile().toString());
        command.add("-vf");
        command.add(buildWatermarkFilter(context.getWatermarkText()));
        command.add("-c:v");
        command.add("libx264");
        command.add("-preset");
        command.add("veryfast");
        command.add("-c:a");
        command.add("aac");
        command.add("-movflags");
        command.add("+faststart");
        command.add(outputFile.toString());
        return command;
    }

    private String buildWatermarkFilter(String watermarkText) {
        String escapedText = escapeDrawText(watermarkText);
        String fontPart = buildFontPart();
        return "drawtext=" + fontPart + "text='" + escapedText + "':fontcolor=white:fontsize="
                + WATERMARK_FONT_SIZE + ":box=1:boxcolor=black@0.45:x=" + WATERMARK_X
                + ":y=" + WATERMARK_Y;
    }

    private String buildFontPart() {
        String fontPath = videoUploadProperties.getWatermarkFontPath();
        if (fontPath == null || fontPath.isBlank()) {
            return "";
        }
        return "fontfile='" + escapeDrawText(fontPath) + "':";
    }

    private String escapeDrawText(String value) {
        return value.replace("\\", "\\\\")
                .replace(":", "\\:")
                .replace("'", "\\'");
    }

    private void runCommand(List<String> command) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new ClientException(buildFailureMessage(output), BaseErrorCode.VIDEO_TRANSCODE_ERROR);
            }
        } catch (IOException ex) {
            String ffmpegPath = videoUploadProperties.getFfmpegPath();
            throw new ClientException(
                    "视频转码失败，请确认服务器已安装 ffmpeg（当前路径：" + ffmpegPath + "）",
                    BaseErrorCode.VIDEO_TRANSCODE_ERROR);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ClientException(BaseErrorCode.VIDEO_TRANSCODE_ERROR);
        }
    }

    private String buildFailureMessage(String output) {
        if (output == null || output.isBlank()) {
            return BaseErrorCode.VIDEO_TRANSCODE_ERROR.message();
        }
        String normalized = output.replaceAll("\\s+", " ").trim();
        if (normalized.contains("No such filter: 'drawtext'")
                || normalized.contains("Filter not found")) {
            return "视频转码失败：当前 ffmpeg 未启用 drawtext 水印滤镜，请安装 ffmpeg-full 或配置 PETS_FFMPEG_PATH";
        }
        return normalized.length() > 500 ? normalized.substring(0, 500) : normalized;
    }
}
