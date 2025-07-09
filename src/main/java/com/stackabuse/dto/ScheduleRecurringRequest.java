package com.stackabuse.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleRecurringRequest {
    private String jobName;
    private String jobGroup;
    private String description;
    private Long orgId;
    private String payload;
    private String cronExpression;
    private Long startTime;  // e.g., System.currentTimeMillis()
    private Long endTime;    // Optional
}
