package com.example.pets_backend.dto.resp;

public record PetArchiveRespDTO(
                Long petId,
                Long ownerId,
                String petName,
                Integer petType,
                String petTypeDesc,
                String defaultReq,
                String image,
                PetProfileTagsRespDTO profileTags) {

}
