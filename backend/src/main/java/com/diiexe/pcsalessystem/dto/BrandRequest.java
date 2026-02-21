package com.diiexe.pcsalessystem.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BrandRequest {
    @NotBlank(message = "Tên thương hiệu không được để trống")
    private String name;

    // nếu để trống, backend tự sinh từ name
    private String slug;

    private Boolean isActive;
}
