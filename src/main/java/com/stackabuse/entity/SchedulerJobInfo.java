package com.stackabuse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Entity
@Table(name = "scheduler_job_info")
public class SchedulerJobInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobName;
    private String jobGroup;
    private String jobStatus;
    private String description;
    private String cronExpression;
    private Boolean cronJob;
    private Long orgId;

    @Column(name = "run_at_timestamp")
    private Long runAtTimestamp;

    @Lob
    private String payload;
}
