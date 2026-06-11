package com.example.pets_backend.controller;

import com.example.pets_backend.dto.req.PetArchiveReqDTO;
import com.example.pets_backend.dto.resp.PetArchiveRespDTO;
import com.example.pets_backend.frameworks.convention.result.Result;
import com.example.pets_backend.frameworks.web.Results;
import com.example.pets_backend.service.PetArchiveService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pet-archives")
@RequiredArgsConstructor
public class PetArchiveController {

    private final PetArchiveService petArchiveService;

    @PostMapping
    public Result<PetArchiveRespDTO> create(@RequestBody PetArchiveReqDTO reqDTO) {
        return Results.success(petArchiveService.create(reqDTO));
    }

    @GetMapping("/{petId}")
    public Result<PetArchiveRespDTO> detail(@PathVariable Long petId) {
        return Results.success(petArchiveService.detail(petId));
    }

    @GetMapping
    public Result<List<PetArchiveRespDTO>> list(@RequestParam(required = false) String petName,
                                                @RequestParam(required = false) Integer petType) {
        return Results.success(petArchiveService.list(petName, petType));
    }

    @PutMapping("/{petId}")
    public Result<PetArchiveRespDTO> update(@PathVariable Long petId,
                                            @RequestBody PetArchiveReqDTO reqDTO) {
        return Results.success(petArchiveService.update(petId, reqDTO));
    }

    @DeleteMapping("/{petId}")
    public Result<Void> delete(@PathVariable Long petId) {
        petArchiveService.delete(petId);
        return Results.success();
    }
}



