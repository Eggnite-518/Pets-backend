package com.example.pets_backend.controller;

import com.example.pets_backend.dto.req.PetOwnerReqDTO;
import com.example.pets_backend.dto.resp.PetOwnerRespDTO;
import com.example.pets_backend.frameworks.convention.result.Result;
import com.example.pets_backend.frameworks.web.Results;
import com.example.pets_backend.service.PetOwnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/pet-owner")
@RequiredArgsConstructor
public class MePetOwnerController {

    private final PetOwnerService petOwnerService;

    @PostMapping
    public Result<PetOwnerRespDTO> create(@RequestBody PetOwnerReqDTO reqDTO) {
        return Results.success(petOwnerService.create(reqDTO));
    }

    @GetMapping
    public Result<PetOwnerRespDTO> detail() {
        return Results.success(petOwnerService.detail());
    }

    @PutMapping
    public Result<PetOwnerRespDTO> update(@RequestBody PetOwnerReqDTO reqDTO) {
        return Results.success(petOwnerService.update(reqDTO));
    }

    @DeleteMapping
    public Result<Void> delete() {
        petOwnerService.delete();
        return Results.success();
    }
}

