package com.stackabuse.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
//3.13.217.11 prod
//3.23.112.255  test
@FeignClient(name = "gatnix-client", url = "http://3.13.217.11:8080/api/v1")

public interface GatnixClient {
    @PostMapping("/{orgId}/submissions/{messageLimit}/{jobName}/processSubmission")
    void callProcessSubmissionApi(@PathVariable("orgId") Long orgId, @PathVariable("messageLimit") Long messageLimit,@PathVariable("jobName") String jobName, @RequestBody List<String> statuses);

}
