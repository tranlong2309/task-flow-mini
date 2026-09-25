package com.taskflow.infrastructure.web.dto;

import java.util.List;

/**
 * DTO bọc danh sách Work Order phân trang — khớp với schema PagedWorkOrderResponse trong docs/api-spec.yaml.
 */
public record PagedWorkOrderResponse(
        List<WorkOrderSummary> items,
        long totalCount,
        int page,
        int pageSize,
        boolean hasNext
) {
    public static PagedWorkOrderResponse from(PagedResponse<?> pagedData, List<WorkOrderSummary> mappedItems) {
        return new PagedWorkOrderResponse(
                mappedItems,
                pagedData.totalElements(),
                pagedData.page(),
                pagedData.size(),
                pagedData.page() < pagedData.totalPages() - 1
        );
    }
}
