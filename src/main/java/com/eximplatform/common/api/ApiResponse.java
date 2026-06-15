package com.eximplatform.common.api;

import java.time.Instant;

/**
 * Standard response envelope: { success, data, error, timestamp }.
 */
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private ApiError error;
    private Instant timestamp = Instant.now();

    public ApiResponse() {
    }

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.data = data;
        return r;
    }

    public static <T> ApiResponse<T> fail(ApiError error) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.error = error;
        return r;
    }

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public ApiError getError() { return error; }
    public Instant getTimestamp() { return timestamp; }
}
