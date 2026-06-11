package com.example.pets_backend.service.watermark;

import java.io.InputStream;
import org.springframework.stereotype.Service;

@Service
public class NoopVideoWatermarkService implements VideoWatermarkService {

    @Override
    public InputStream addWatermark(InputStream inputStream) {
        return inputStream;
    }
}
