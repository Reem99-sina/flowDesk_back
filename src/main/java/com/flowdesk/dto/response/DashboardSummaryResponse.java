package com.flowdesk.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardSummaryResponse {

    private Long totalTasks;
    private Long completedTasks;
    private Long pendingTasks;
    private Long todaysTasks;
    private Long overdueTasks;
}