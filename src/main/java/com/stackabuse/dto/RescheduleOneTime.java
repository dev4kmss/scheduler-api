package com.stackabuse.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RescheduleOneTime {
    private String jobName;
    private long runAtTimestamp;
}
