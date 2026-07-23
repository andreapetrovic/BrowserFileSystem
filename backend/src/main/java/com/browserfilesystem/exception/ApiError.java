package com.browserfilesystem.exception;

import java.time.Instant;

/** Consistent error payload returned for validation, domain, and unexpected API failures. */
public record ApiError(Instant timestamp, int status, String error, String message) {
}
