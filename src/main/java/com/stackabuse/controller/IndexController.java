package com.stackabuse.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.stackabuse.entity.SchedulerJobInfo;
import com.stackabuse.service.SchedulerJobService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

	@Autowired
	private SchedulerJobService schedulerJobService;
	@GetMapping("/index")
	public String index(Model model){
		List<SchedulerJobInfo> jobList = schedulerJobService.getAllJobList();
		model.addAttribute("jobs", jobList);
		return "index";
	}
	
}
