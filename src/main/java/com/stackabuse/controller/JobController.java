package com.stackabuse.controller;

import com.stackabuse.dto.RescheduleOneTime;
import com.stackabuse.dto.ScheduleOneTimeRequest;
import com.stackabuse.dto.ScheduleRecurringRequest;
import com.stackabuse.entity.Message;
import com.stackabuse.entity.SchedulerJobInfo;
import com.stackabuse.repository.SchedulerRepository;
import com.stackabuse.service.SchedulerJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.quartz.SchedulerMetaData;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class JobController {

	private final SchedulerJobService scheduleJobService;
	private final SchedulerRepository schedulerRepository;

	@PostMapping("/saveOrUpdate")
	public Object saveOrUpdate(@RequestBody SchedulerJobInfo job) {
		Message message = Message.failure();
		try {
			scheduleJobService.saveOrupdate(job);
			message = Message.success();
		} catch (Exception e) {
			message.setMsg(e.getMessage());
			log.error("Error saving job", e);
		}
		return message;
	}

	@PostMapping("/rescheduleOneTime")
	public Object rescheduleOneTime(@RequestBody RescheduleOneTime request) {
		Message message = Message.failure();
		try {
			scheduleJobService.rescheduleOneTimeJob(request.getJobName(), request.getRunAtTimestamp());
			message = Message.success();
		} catch (Exception e) {
			message.setMsg(e.getMessage());
			log.error("Error rescheduling one-time job", e);
		}
		return message;
	}

	@PostMapping("/scheduleOneTime")
	public Object scheduleOneTime(@RequestBody SchedulerJobInfo job) {
		Message message = Message.failure();
		try {
			scheduleJobService.scheduleOneTimeJob(job);
			message = Message.success();
		} catch (Exception e) {
			message.setMsg(e.getMessage());
			log.error("Error scheduling one-time job", e);
		}
		return message;
	}

	@GetMapping("/metaData")
	public Object metaData() throws SchedulerException {
		SchedulerMetaData metaData = scheduleJobService.getMetaData();
		return metaData;
	}

	@GetMapping("/getAllJobs")
	public Object getAllJobs() {
		return scheduleJobService.getAllJobList();
	}

	@PostMapping("/runJob")
	public Object runJob(@RequestBody SchedulerJobInfo job) {
		Message message = Message.failure();
		if (scheduleJobService.startJobNow(job)) {
			message = Message.success();
		}
		return message;
	}

	@PostMapping("/pauseJob")
	public Object pauseJob(@RequestBody SchedulerJobInfo job) {
		Message message = Message.failure();
		if (scheduleJobService.pauseJob(job)) {
			message = Message.success();
		}
		return message;
	}

	@PostMapping("/resumeJob")
	public Object resumeJob(@RequestBody SchedulerJobInfo job) {
		Message message = Message.failure();
		if (scheduleJobService.resumeJob(job)) {
			message = Message.success();
		}
		return message;
	}

	@DeleteMapping("/deleteJob/{jobName}")
	public Object deleteJob(@PathVariable String jobName) {
		Message message = Message.failure();
		if (scheduleJobService.deleteJob(jobName)) {
			message = Message.success();
		}
		return message;
	}


	@PostMapping("/scheduleRecurringBetween")
	public Object scheduleRecurringBetween(@RequestBody ScheduleRecurringRequest request) {
		Message message = Message.failure();
		try {
			SchedulerJobInfo job = new SchedulerJobInfo();
			job.setJobName(request.getJobName());
			job.setJobGroup(request.getJobGroup());
			job.setDescription(request.getDescription());
			job.setOrgId(request.getOrgId());
			job.setPayload(request.getPayload());
			job.setCronExpression(request.getCronExpression());
			job.setCronJob(true);
			job.setStartTime(request.getStartTime());
			job.setEndTime(request.getEndTime());

			scheduleJobService.scheduleRecurringJobBetween(job);
			message = Message.success();
		} catch (Exception e) {
			message.setMsg(e.getMessage());
			log.error("Error scheduling recurring job", e);
		}
		return message;
	}

	@GetMapping("/health")
	public String health() {
		System.out.println("Health check API was called.");
		return "I am alive!";
	}
}
