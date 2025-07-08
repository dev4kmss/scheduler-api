package com.stackabuse.service;

import java.util.Date;
import java.util.List;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stackabuse.component.JobScheduleCreator;
import com.stackabuse.entity.SchedulerJobInfo;
import com.stackabuse.job.GenericHttpJob;
import com.stackabuse.repository.SchedulerRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
@Transactional
@Service
public class SchedulerJobService {

	@Autowired
	private SchedulerFactoryBean schedulerFactoryBean;

	@Autowired
	private SchedulerRepository schedulerRepository;

	@Autowired
	private ApplicationContext context;

	@Autowired
	private JobScheduleCreator scheduleCreator;

	/**
	 * Get scheduler metadata info.
	 */
	public SchedulerMetaData getMetaData() throws SchedulerException {
		return schedulerFactoryBean.getScheduler().getMetaData();
	}

	/**
	 * Return all stored job definitions.
	 */
	public List<SchedulerJobInfo> getAllJobList() {
		return schedulerRepository.findAll();
	}

	/**
	 * Delete a job.
	 */
	public boolean deleteJob(String jobName) {
		try {
			SchedulerJobInfo jobInfo = schedulerRepository.findByJobName(jobName);
			schedulerRepository.delete(jobInfo);
			return schedulerFactoryBean.getScheduler().deleteJob(new JobKey(jobName, jobInfo.getJobGroup()));
		} catch (SchedulerException e) {
			log.error("Failed to delete job - {}", jobName, e);
			return false;
		}
	}

	/**
	 * Pause a job.
	 */
	public boolean pauseJob(SchedulerJobInfo jobInfo) {
		try {
			SchedulerJobInfo storedJob = schedulerRepository.findByJobName(jobInfo.getJobName());
			storedJob.setJobStatus("PAUSED");
			schedulerRepository.save(storedJob);
			schedulerFactoryBean.getScheduler().pauseJob(new JobKey(jobInfo.getJobName(), jobInfo.getJobGroup()));
			return true;
		} catch (SchedulerException e) {
			log.error("Failed to pause job - {}", jobInfo.getJobName(), e);
			return false;
		}
	}

	/**
	 * Resume a job.
	 */
	public boolean resumeJob(SchedulerJobInfo jobInfo) {
		try {
			SchedulerJobInfo storedJob = schedulerRepository.findByJobName(jobInfo.getJobName());
			storedJob.setJobStatus("RESUMED");
			schedulerRepository.save(storedJob);
			schedulerFactoryBean.getScheduler().resumeJob(new JobKey(jobInfo.getJobName(), jobInfo.getJobGroup()));
			return true;
		} catch (SchedulerException e) {
			log.error("Failed to resume job - {}", jobInfo.getJobName(), e);
			return false;
		}
	}

	/**
	 * Start job immediately.
	 */
	public boolean startJobNow(SchedulerJobInfo jobInfo) {
		try {
			SchedulerJobInfo storedJob = schedulerRepository.findByJobName(jobInfo.getJobName());
			storedJob.setJobStatus("SCHEDULED & STARTED");
			schedulerRepository.save(storedJob);
			schedulerFactoryBean.getScheduler().triggerJob(new JobKey(jobInfo.getJobName(), jobInfo.getJobGroup()));
			return true;
		} catch (SchedulerException e) {
			log.error("Failed to start job - {}", jobInfo.getJobName(), e);
			return false;
		}
	}

	/**
	 * Create or update a job definition.
	 */
	public void saveOrupdate(SchedulerJobInfo scheduleJob) {
		if (scheduleJob.getId() == null) {
			scheduleNewJob(scheduleJob);
		} else {
			updateScheduleJob(scheduleJob);
		}
		log.info(">>>>> jobName = [{}] created or updated.", scheduleJob.getJobName());
	}

	/**
	 * Schedule a new job.
	 */
	private void scheduleNewJob(SchedulerJobInfo jobInfo) {
		try {
			Scheduler scheduler = schedulerFactoryBean.getScheduler();

			// ✅ Ensure payload is always valid here too
			if (!StringUtils.hasText(jobInfo.getPayload())) {
				jobInfo.setPayload("{}");
			}

			JobDetail jobDetail = scheduleCreator.createJob(
					GenericHttpJob.class,
					false,
					context,
					jobInfo.getJobName(),
					jobInfo.getJobGroup(),
					jobInfo.getOrgId(),
					jobInfo.getPayload()
			);

			Trigger trigger = scheduleCreator.createCronTrigger(
					jobInfo.getJobName(),
					new Date(),
					jobInfo.getCronExpression(),
					SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW
			);

			scheduler.scheduleJob(jobDetail, trigger);

			jobInfo.setJobStatus("SCHEDULED");
			schedulerRepository.save(jobInfo);
		} catch (Exception e) {
			log.error("Error scheduling job", e);
		}
	}

	public void rescheduleOneTimeJob(String jobName, long newRunAtTimestamp) {
		try {
			SchedulerJobInfo existingJob = schedulerRepository.findByJobName(jobName);

			if (existingJob == null) {
				throw new RuntimeException("No job found with name: " + jobName);
			}

			// Delete from Quartz
			schedulerFactoryBean.getScheduler().deleteJob(new JobKey(jobName, existingJob.getJobGroup()));

			// Update run time in DB
			existingJob.setRunAtTimestamp(newRunAtTimestamp);
			schedulerRepository.delete(existingJob); // Optional: remove old before recreate

			// Re-schedule with updated time
			scheduleOneTimeJob(existingJob);

		} catch (SchedulerException e) {
			throw new RuntimeException("Failed to reschedule job: " + jobName, e);
		}
	}


	/**
	 * Update an existing job's cron expression.
	 */
	public void updateScheduleJob(SchedulerJobInfo jobInfo) {
		Trigger newTrigger = scheduleCreator.createCronTrigger(
				jobInfo.getJobName(),
				new Date(),
				jobInfo.getCronExpression(),
				SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW
		);

		try {
			schedulerFactoryBean.getScheduler().rescheduleJob(
					TriggerKey.triggerKey(jobInfo.getJobName()),
					newTrigger
			);
			jobInfo.setJobStatus("UPDATED & SCHEDULED");
			schedulerRepository.save(jobInfo);
		} catch (SchedulerException e) {
			log.error("Error updating job", e);
		}
	}

	/**
	 * Schedule a job to fire exactly once at the specified time.
	 */
	@Transactional
	public void scheduleOneTimeJob(SchedulerJobInfo jobInfo) {
		Scheduler scheduler = schedulerFactoryBean.getScheduler();

		// Validate
		if (jobInfo.getRunAtTimestamp() == null) {
			throw new IllegalArgumentException("runAtTimestamp is required.");
		}

		// Ensure payload is always valid
		if (!StringUtils.hasText(jobInfo.getPayload())) {
			jobInfo.setPayload("{}");
		}

		JobDetail jobDetail = scheduleCreator.createJob(
				GenericHttpJob.class,
				false,
				context,
				jobInfo.getJobName(),
				jobInfo.getJobGroup(),
				jobInfo.getOrgId(),
				jobInfo.getPayload()
		);

		Trigger trigger = scheduleCreator.createSimpleTrigger(
				jobInfo.getJobName(),
				new Date(jobInfo.getRunAtTimestamp()),
				0L,
				SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW
		);

		try {
			scheduler.scheduleJob(jobDetail, trigger);
			jobInfo.setJobStatus("SCHEDULED");
			schedulerRepository.save(jobInfo);
		} catch (SchedulerException e) {
			throw new RuntimeException("Error scheduling job", e);
		}
	}

}
