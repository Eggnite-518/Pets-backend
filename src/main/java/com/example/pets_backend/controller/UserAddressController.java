package com.example.pets_backend.controller;

import com.example.pets_backend.dto.req.UserAddressReqDTO;
import com.example.pets_backend.dto.resp.UserAddressRespDTO;
import com.example.pets_backend.frameworks.convention.result.Result;
import com.example.pets_backend.frameworks.web.Results;
import com.example.pets_backend.dto.req.AddressFamilySopReqDTO;
import com.example.pets_backend.dto.resp.AddressFamilySopRespDTO;
import com.example.pets_backend.service.AddressFamilySopService;
import com.example.pets_backend.service.UserAddressService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user-addresses")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService userAddressService;
    private final AddressFamilySopService addressFamilySopService;

    @PostMapping
    public Result<UserAddressRespDTO> create(@RequestBody UserAddressReqDTO reqDTO) {
        return Results.success(userAddressService.create(reqDTO));
    }

    @GetMapping("/{addressId}")
    public Result<UserAddressRespDTO> detail(@PathVariable Long addressId) {
        return Results.success(userAddressService.detail(addressId));
    }

    @GetMapping
    public Result<List<UserAddressRespDTO>> list() {
        return Results.success(userAddressService.list());
    }

    @PutMapping("/{addressId}")
    public Result<UserAddressRespDTO> update(@PathVariable Long addressId,
                                             @RequestBody UserAddressReqDTO reqDTO) {
        return Results.success(userAddressService.update(addressId, reqDTO));
    }

    @PutMapping("/{addressId}/default")
    public Result<UserAddressRespDTO> switchDefault(@PathVariable Long addressId) {
        return Results.success(userAddressService.switchDefault(addressId));
    }

    @DeleteMapping("/{addressId}")
    public Result<Void> delete(@PathVariable Long addressId) {
        userAddressService.delete(addressId);
        return Results.success();
    }

    @GetMapping("/{addressId}/family-sop")
    public Result<AddressFamilySopRespDTO> getFamilySop(@PathVariable Long addressId) {
        return Results.success(addressFamilySopService.getFamilySop(addressId));
    }

    @PutMapping("/{addressId}/family-sop")
    public Result<AddressFamilySopRespDTO> saveFamilySop(@PathVariable Long addressId,
            @RequestBody AddressFamilySopReqDTO reqDTO) {
        return Results.success(addressFamilySopService.saveFamilySop(addressId, reqDTO));
    }
}


