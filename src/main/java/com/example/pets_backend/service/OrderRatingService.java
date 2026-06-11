package com.example.pets_backend.service;

import com.example.pets_backend.dto.req.SubmitRatingReqDTO;
import com.example.pets_backend.dto.resp.OrderRatingDetailRespDTO;

public interface OrderRatingService {

    void submitRating(Long orderId, SubmitRatingReqDTO reqDTO);

    OrderRatingDetailRespDTO getRating(Long orderId);
}
