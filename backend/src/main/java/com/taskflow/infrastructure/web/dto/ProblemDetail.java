package com.taskflow.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * RFC 7807 Problem Details response — khớp với schema ProblemDetail trong docs/api-spec.yaml.
 *
 * Spec reference: docs/api-spec.yaml → components/schemas/ProblemDetail
 * Rule: KHÔNG bao giờ trả stack trace trong production (domain-model.md § Invariant - sec rule).
 *
 * Dùng cho tất cả error response: 400, 401, 403, 404, 422, 500.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemDetail(
        String type,        // URI mô tả loại lỗi, vd: "https://taskflow.internal/problems/validation-error"
        String title,       // Tóm tắt ngắn loại lỗi
        int status,         // HTTP status code
        String detail,      // Mô tả cụ thể lỗi trong context hiện tại
        String instance,    // URI request gây ra lỗi (nullable)
        String trackingId,  // ID tra cứu log — chỉ có ở 500 (nullable)
        List<FieldViolation> violations  // Danh sách field validation errors — chỉ có ở 400 (nullable)
) {

    /**
     * Field-level validation error — dùng trong violations[] của 400.
     */
    public record FieldViolation(String field, String message) {}

    // ─── Factory methods cho từng loại lỗi ──────────────────────────────────

    /** 400 Bad Request — validation error với danh sách violations */
    public static ProblemDetail badRequest(String detail, String instance, List<FieldViolation> violations) {
        return new ProblemDetail(
                "https://taskflow.internal/problems/validation-error",
                "Validation Error",
                400,
                detail,
                instance,
                null,
                violations
        );
    }

    /** 400 Bad Request — lỗi đơn giản không có violations */
    public static ProblemDetail badRequest(String detail, String instance) {
        return badRequest(detail, instance, null);
    }

    /** 401 Unauthorized */
    public static ProblemDetail unauthorized(String instance) {
        return new ProblemDetail(
                "https://taskflow.internal/problems/unauthorized",
                "Unauthorized",
                401,
                "JWT token is missing or expired",
                instance,
                null,
                null
        );
    }

    /** 403 Forbidden */
    public static ProblemDetail forbidden(String detail, String instance) {
        return new ProblemDetail(
                "https://taskflow.internal/problems/forbidden",
                "Forbidden",
                403,
                detail,
                instance,
                null,
                null
        );
    }

    /** 404 Not Found */
    public static ProblemDetail notFound(String detail, String instance) {
        return new ProblemDetail(
                "https://taskflow.internal/problems/not-found",
                "Not Found",
                404,
                detail,
                instance,
                null,
                null
        );
    }

    /** 422 Unprocessable Entity — Business Rule Violation */
    public static ProblemDetail businessRuleViolation(String detail, String instance) {
        return new ProblemDetail(
                "https://taskflow.internal/problems/business-rule-violation",
                "Business Rule Violation",
                422,
                detail,
                instance,
                null,
                null
        );
    }

    /** 500 Internal Server Error — không lộ stack trace */
    public static ProblemDetail internalServerError(String trackingId, String instance) {
        return new ProblemDetail(
                "https://taskflow.internal/problems/internal-error",
                "Internal Server Error",
                500,
                "An unexpected error occurred. Please contact support with the trackingId.",
                instance,
                trackingId,
                null
        );
    }
}
