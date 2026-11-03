package com.kdt.nbe151team08.menu.dto;

import jakarta.validation.constraints.*;

public record MenuRequest(
        @NotNull @Min(1) Integer categoryId,
        @NotBlank @Size(max = 100) String name,
        @NotNull @Min(1) @Max(1000000) Integer price,
        @NotNull @Size(max = 300) String description
) {
    public MenuRequest {
        if (name != null) name = name.strip();
        if (description != null) description = description.strip();
    }
}
