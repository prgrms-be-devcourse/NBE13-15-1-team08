package com.kdt.nbe151team08.menu.dto;

import jakarta.validation.constraints.NotNull;

public record MenuAvailabilityRequest (@NotNull Boolean available){
}
