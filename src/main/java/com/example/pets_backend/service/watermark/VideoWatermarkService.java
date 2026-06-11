package com.example.pets_backend.service.watermark;

import java.io.InputStream;

public interface VideoWatermarkService {

    InputStream addWatermark(InputStream inputStream);
}
