package com.stackabuse.controller;

import java.util.List;

import com.stackabuse.repository.SchedulerRepository;
import org.quartz.SchedulerException;
import org.quartz.SchedulerMetaData;
import org.springframework.web.bind.annotation.*;

import com.stackabuse.entity.Message;
import com.stackabuse.entity.SchedulerJobInfo;
import com.stackabuse.service.SchedulerJobService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class JobController {

	private final SchedulerJobService scheduleJobService;

	private final SchedulerRepository schedulerRepository;

	@RequestMapping(value = "/saveOrUpdate", method = { RequestMethod.GET, RequestMethod.POST })
	public Object saveOrUpdate(SchedulerJobInfo job) {
		log.info("params, job = {}", job);
		Message message = Message.failure();
		try {
			scheduleJobService.saveOrupdate(job);
			message = Message.success();
		} catch (Exception e) {
			message.setMsg(e.getMessage());
			log.error("updateCron ex:", e);
		}
		return message;
	}

	@RequestMapping(value ="/jobName/cronExpression", method = { RequestMethod.GET, RequestMethod.POST })
	public Object updateCronExpression(@RequestParam(value="jobName") String jobName,
									   @RequestParam(value="cronExpression") String cronExpression) {
		log.info("updateCronExpression triggred for = {}", jobName);
		Message message = Message.failure();
		try {
			scheduleJobService.updateSchedulerCronExpression(jobName,cronExpression);
			message = Message.success();
		} catch (Exception e) {
			message.setMsg(e.getMessage());
			log.error("update cronExpression ex:", e);
		}
		return message;
    }


	@RequestMapping(value = "/update", method = { RequestMethod.GET, RequestMethod.PUT })
//	@PutMapping("/update")
	public Object update(@RequestParam(value="jobName") String jobName,
						 @RequestParam(value="cronExpression") String cronExpression,
						 @RequestParam(value ="messageLimit") Long messageLimit,
						 @RequestParam(value = "statuses") List<String> statuses) {
		SchedulerJobInfo existingSchedulerJobInfo = schedulerRepository.findByJobName(jobName);
		existingSchedulerJobInfo.setCronExpression(cronExpression);
		existingSchedulerJobInfo.setMessageLimit(messageLimit);
		existingSchedulerJobInfo.setStatuses(statuses);
		log.info("params, job = {}", jobName);
		Message message = Message.failure();
		try {
			scheduleJobService.updateScheduleJob(existingSchedulerJobInfo);
			message = Message.success();
		} catch (Exception e) {
			message.setMsg(e.getMessage());
			log.error("updateCron ex:", e);
		}
		return message;
	}

	@RequestMapping("/metaData")
	public Object metaData() throws SchedulerException {
		SchedulerMetaData metaData = scheduleJobService.getMetaData();
		return metaData;
	}

	@RequestMapping("/getAllJobs")
	public Object getAllJobs() throws SchedulerException {
		List<SchedulerJobInfo> jobList = scheduleJobService.getAllJobList();
		return jobList;
	}

	@RequestMapping(value = "/runJob", method = { RequestMethod.GET, RequestMethod.POST })
	public Object runJob(SchedulerJobInfo job) {
		log.info("params, job = {}", job);
		Message message = Message.failure();
		try {
			scheduleJobService.startJobNow(job);
			message = Message.success();
		} catch (Exception e) {
			message.setMsg(e.getMessage());
			log.error("runJob ex:", e);
		}
		return message;
	}

	@RequestMapping(value = "/pauseJob", method = { RequestMethod.GET, RequestMethod.POST })
	public Object pauseJob(SchedulerJobInfo job) {
		log.info("params, job = {}", job);
		Message message = Message.failure();
		try {
			scheduleJobService.pauseJob(job);
			message = Message.success();
		} catch (Exception e) {
			message.setMsg(e.getMessage());
			log.error("pauseJob ex:", e);
		}
		return message;
	}

	@RequestMapping(value = "/resumeJob", method = { RequestMethod.GET, RequestMethod.POST })
	public Object resumeJob(SchedulerJobInfo job) {
		log.info("params, job = {}", job);
		Message message = Message.failure();
		try {
			scheduleJobService.resumeJob(job);
			message = Message.success();
		} catch (Exception e) {
			message.setMsg(e.getMessage());
			log.error("resumeJob ex:", e);
		}
		return message;
	}

	@RequestMapping(value = "/deleteJob/{jobName}", method = { RequestMethod.GET, RequestMethod.DELETE })
	public Object deleteJob(@PathVariable String jobName) {
		log.info("params, job = {}", jobName);
		Message message = Message.failure();
		try {
			scheduleJobService.deleteJob(jobName);
			message = Message.success();
		} catch (Exception e) {
			message.setMsg(e.getMessage());
			log.error("deleteJob ex:", e);
		}
		return message;
	}

//	@RequestMapping(value = "/jobName/{jobName}/cronExpression/{cronExpression}", method = { RequestMethod.GET, RequestMethod.DELETE })
//	public Object updateCronExpression(@PathVariable String jobName,@PathVariable String cronExpression) {
//		log.info("updateCronExpression triggred with = {}", jobName);
//
//		Message message = Message.failure();
//		try {
//			scheduleJobService.deleteJob(jobName);
//			message = Message.success();
//		} catch (Exception e) {
//			message.setMsg(e.getMessage());
//			log.error("deleteJob ex:", e);
//		}
//		return message;
//	}

}
