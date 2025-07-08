package com.stackabuse.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleOneTimeRequest {
    private String jobName;
    private String jobGroup;
    private String description;
    private Long orgId;
    private String payload;
    private long runAtTimestamp; // e.g., System.currentTimeMillis() + 60000
}
