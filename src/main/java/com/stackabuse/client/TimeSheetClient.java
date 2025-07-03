package com.stackabuse.client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

//3.13.217.11 prod
//3.23.112.255  test
@FeignClient(name = "gatnix-timesheet", url = "http://3.23.112.255:8082/api/v1/timesheet")
public interface TimeSheetClient {

    @PostMapping("/{orgId}/processTimeSheets")
    void callProcessTimesheetApi(@PathVariable("orgId") Long orgId, @RequestBody List<String> statuses);

}
