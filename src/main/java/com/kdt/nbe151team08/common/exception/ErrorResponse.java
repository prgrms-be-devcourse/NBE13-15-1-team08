package com.kdt.nbe151team08.common.exception;

import java.util.Map;

public record ErrorResponse (String code, String message, Map<String, String> fieldErrors ){
}
