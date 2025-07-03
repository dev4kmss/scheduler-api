$(function() {

$(document).ready(function() {

        // Function to populate jobs data in the table
        function populateJobsTable(jobs) {
            // Your code to populate the table with the jobs data goes here
              var tableBody = $("#jobTableBody");
                tableBody.empty(); // Clear the existing table rows before populating the new data


        $.each(jobs, function(index, job) {
            var row = $("<tr>").attr("data-id", job.jobId);
            row.append($("<td>").text(job.jobId));
            row.append($("<td>").attr("id", "name_" + job.jobId).text(job.jobName));
            row.append($("<td>").attr("id", "group_" + job.jobId).text(job.jobGroup));
            row.append($("<td>").attr("id", "cron_" + job.jobId).text(job.cronExpression));
            row.append($("<td>").attr("id", "status_" + job.jobId).text(job.jobStatus));
            row.append($("<td>").attr("id", "desc_" + job.jobId).text(job.desc));
            row.append($("<td>").attr("id", "orgId_" + job.jobId).text(job.orgId));

            var operationGroup = $("<div>").addClass("btn-group text-center").attr("role", "group").attr("data-id", job.jobId);
            operationGroup.append($("<button>").addClass("btn btn-default btnRun").text("Run Once"));
            operationGroup.append($("<button>").addClass("btn btn-default btnPause").text("Pause"));
            operationGroup.append($("<button>").addClass("btn btn-default btnResume").text("Resume"));
            operationGroup.append($("<button>").addClass("btn btn-default btnEdit").text("Edit"));
            operationGroup.append($("<button>").addClass("btn btn-warning btnDelete").text("Delete"));
            row.append($("<td>").css("text-align", "center").append(operationGroup));

            tableBody.append(row);
        });
        }

        function getAllJobs() {
            $.ajax({
                url: "/api/getAllJobs",
                type: "GET",
                success: function(data) {
                    populateJobsTable(data);
                },
                error: function() {
                    alert("Error fetching jobs data.");
                }
            });
        }

        // Call the function to get all jobs and populate the table on page load
        getAllJobs();

    });

	//run job once
    $(document).on("click", ".btnRun", function() {
    	var jobId = $(this).parent().data("id");
    	console.log(jobId);
        $.ajax({
            url: "/api/runJob?t=" + new Date().getTime(),
            type: "POST",
            data: {
                "jobName": $("#name_"+jobId).text(),
                "jobGroup": $("#group_"+jobId).text()
            },
            success: function(res) {
                if (res.valid) {
                	alert("run success!");  
                } else {
                	alert(res.msg); 
                }
            }
        });
    });
    
    //pause job
$(document).on("click", ".btnPause", function() {
    	var jobId = $(this).parent().data("id");
        $.ajax({
            url: "/api/pauseJob?t=" + new Date().getTime(),
            type: "POST",
            data: {
                "jobName": $("#name_"+jobId).text(),
                "jobGroup": $("#group_"+jobId).text()
            },
            success: function(res) {
                if (res.valid) {
                	alert("pause success!");
                	location.reload();
                } else {
                	alert(res.msg); 
                }
            }
        });
    });
    
    //resume job
    $(document).on("click", ".btnResume", function() {
    	var jobId = $(this).parent().data("id");
        $.ajax({
            url: "/api/resumeJob?t=" + new Date().getTime(),
            type: "POST",
            data: {
                "jobName": $("#name_"+jobId).text(),
                "jobGroup": $("#group_"+jobId).text()
            },
            success: function(res) {
                if (res.valid) {
                	alert("resume success!");
                	location.reload();
                } else {
                	alert(res.msg); 
                }
            }
        });
    });
    
    //delete job
    $(document).on("click", ".btnDelete", function() {
    	var jobId = $(this).parent().data("id");
        $.ajax({
            url: "/api/deleteJob?t=" + new Date().getTime(),
            type: "POST",
            data: {
                "jobName": $("#name_"+jobId).text(),
                "jobGroup": $("#group_"+jobId).text()
            },
            success: function(res) {
                if (res.valid) {
                	alert("delete success!");
                	location.reload();
                } else {
                	alert(res.msg); 
                }
            }
        });
    });
	
	// update cron expression
	$(document).on("click", ".btnEdit", function() {
    			$("#myModalLabel").html("cron edit");
    			var jobId = $(this).parent().data("id");
    			$("#jobId").val(jobId);
    			$("#edit_name").val($("#name_"+jobId).text());
    			$("#edit_group").val($("#group_"+jobId).text());
    			$("#edit_cron").val($("#cron_"+jobId).text());
    			$("#edit_status").val($("#status_"+jobId).text());
    			$("#edit_desc").val($("#desc_"+jobId).text());
    			$("#edit_orgId").val($("#orgId_"+jobId).text());
    			
    			$('#edit_name').attr("readonly","readonly"); 
    			$('#edit_group').attr("readonly","readonly");
    			$('#edit_desc').attr("readonly","readonly");
    			
    			$("#myModal").modal("show");
    });



    $(document).on("click", "#save", function() {
	    	$.ajax({
	            url: "/api/saveOrUpdate?t=" + new Date().getTime(),
	            type: "POST",
	            data:  $('#mainForm').serialize(),
	            success: function(res) {
	            	if (res.valid) {
	                	alert("success!");
	                	location.reload();
	                } else {
	                	alert(res.msg); 
	                }
	            }
	        });
    });


    // create
    $(document).on("click", "#createBtn", function() {
    			$("#myModalLabel").html("Create Job");
    			$("#jobId").val("");
    			$("#edit_name").val("");
    			$("#edit_group").val("");
    			$("#edit_cron").val("");
    			$("#edit_status").val("NORMAL");
    			$("#edit_desc").val("");
    			
    			$('#edit_name').removeAttr("readonly");
    			$('#edit_group').removeAttr("readonly");
    			$('#edit_desc').removeAttr("readonly");
    			
    			$("#myModal").modal("show");
    });
    
    
});