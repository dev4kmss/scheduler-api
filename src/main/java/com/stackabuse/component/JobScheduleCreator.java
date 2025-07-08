package com.stackabuse.component;

import java.text.ParseException;
import java.util.Date;

import org.quartz.*;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JobScheduleCreator {

    /**
     * Create a JobDetail using Quartz's recommended JobBuilder API.
     */
    public JobDetail createJob(
            Class<? extends QuartzJobBean> jobClass,
            boolean isDurable,
            ApplicationContext context,
            String jobName,
            String jobGroup,
            Long orgId,
            String payload
    ) {
        if (!StringUtils.hasText(payload)) {
            payload = "{}";
        }

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("orgId", orgId);
        jobDataMap.put("payload", payload);
        jobDataMap.put("jobName", jobName);

        return JobBuilder.newJob(jobClass)
                .withIdentity(jobName, jobGroup)
                .usingJobData(jobDataMap)
                .storeDurably(isDurable)
                .build();
    }

    /**
     * Create a CronTrigger using Spring's CronTriggerFactoryBean.
     */
    public CronTrigger createCronTrigger(String triggerName, Date startTime, String cronExpression, int misFireInstruction) {
        CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
        factoryBean.setName(triggerName);
        factoryBean.setStartTime(startTime);
        factoryBean.setCronExpression(cronExpression);
        factoryBean.setMisfireInstruction(misFireInstruction);
        try {
            factoryBean.afterPropertiesSet();
        } catch (ParseException e) {
            log.error("Invalid cron expression: {}", cronExpression, e);
        }
        return factoryBean.getObject();
    }

    /**
     * Create a SimpleTrigger using Spring's SimpleTriggerFactoryBean.
     */
    public SimpleTrigger createSimpleTrigger(String triggerName, Date startTime, Long repeatTime, int misFireInstruction) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setName(triggerName);
        factoryBean.setStartTime(startTime);
//        factoryBean.setRepeatInterval(repeatTime);
        factoryBean.setRepeatInterval(repeatTime != null ? repeatTime : 0L);
        factoryBean.setRepeatCount(0); //
        factoryBean.setMisfireInstruction(misFireInstruction);
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }
}
