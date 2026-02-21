package com.diiexe.pcsalessystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;

    // nếu để trống, backend tự sinh từ name
    private String slug;

    private Boolean isActive;
}
