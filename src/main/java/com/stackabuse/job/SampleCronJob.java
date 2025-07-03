package com.stackabuse.job;


import com.stackabuse.client.GatnixClient;
import com.stackabuse.client.TimeSheetClient;
import com.stackabuse.entity.SchedulerJobInfo;
import com.stackabuse.repository.SchedulerRepository;
import com.stackabuse.service.SchedulerJobService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@DisallowConcurrentExecution
@Component
public class SampleCronJob extends QuartzJobBean {

    @Autowired
    private  SchedulerJobService scheduleJobService;

    @Autowired
    private SchedulerRepository schedulerRepository;

    @Autowired
    private GatnixClient gatnixClient;

    @Autowired
    private TimeSheetClient timeSheetClient;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        Long orgId = (Long) context.getJobDetail().getJobDataMap().get("orgId");
        Long messageLimit = (Long) context.getJobDetail().getJobDataMap().get("messageLimit");
        String jobName = (String) context.getJobDetail().getJobDataMap().get("jobName");
        List<String> statuses = (List<String>) context.getJobDetail().getJobDataMap().get("statuses");
//        log.info("SampleCronJob Start with OrgId::{}", orgId);
//        log.info("SampleCronJob Start with messageLimit::{}", messageLimit);
//        log.info("SampleCronJob Start with statuses::{}", statuses);
        // Call the other application API using Feign client
        log.info("Scheduler API triggered for organization: {}, message limit: {}, job name: '{}', and status list: {}", orgId, messageLimit, jobName, statuses);
        List<String> targetStatuses = Arrays.asList("whatsapp", "sms", "email");
        if (!Collections.disjoint(statuses, targetStatuses)) {
            timeSheetClient.callProcessTimesheetApi(orgId,statuses);
        }else{
            gatnixClient.callProcessSubmissionApi(orgId, messageLimit,jobName,statuses);
        }
//        try {
//            getJobDetails(jobName);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        gatnixClient.callProcessSubmissionApi(orgId, messageLimit,statuses);

        log.info("SampleCronJob End................");
    }

//    public  void getJobDetails(String jobName) throws Exception {
//        SchedulerJobInfo schedulerJobInfo= schedulerRepository.findByJobName(jobName);
//        schedulerJobInfo.setCronExpression(removeDayOfMonthAndMonthIfPresent(schedulerJobInfo.getCronExpression()));
//        log.info("Updated schedulerJobInfo:{}",schedulerJobInfo);
//        scheduleJobService.saveOrupdate(schedulerJobInfo);
//    }
//
//
//    public String removeDayOfMonthAndMonthIfPresent(String cronExpression) {
//        log.info("cronExpression has to update:{}",cronExpression);
//        String[] fields = cronExpression.split(" ");
//        // Check if day of month and month are present
//        boolean hasDayOfMonth = !fields[2].equals("?");
//        boolean hasMonth = !fields[4].equals("*");
//        StringBuilder modifiedExpression = new StringBuilder();
//        // Modify only if necessary
//        if (!hasDayOfMonth && !hasMonth) {
//            return cronExpression;  // No modification needed, return original expression
//        } else {
//            for (int i = 0; i < fields.length; i++) {
//                if (i != 2 && i != 4) {
//                    modifiedExpression.append(fields[i]).append(" ");
//                }
//            }
//        }
//        String modifiedCron = modifiedExpression.toString().trim();
//        log.info("Edited cron expression: {}", modifiedCron);
//        return modifiedCron; // Return the modified cron expression
//    }

}
