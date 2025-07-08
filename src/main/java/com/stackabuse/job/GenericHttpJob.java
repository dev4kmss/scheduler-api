package com.stackabuse.job;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.http.*;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class GenericHttpJob extends QuartzJobBean {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("GenericHttpJob started");

        String payloadJson = (String) context.getMergedJobDataMap().get("payload");
        Long orgId = (Long) context.getMergedJobDataMap().get("orgId");
        String jobName = (String) context.getMergedJobDataMap().get("jobName");

        try {
            JsonNode root = objectMapper.readTree(payloadJson);

            if (root.has("requests") && root.path("requests").isArray()) {
                // Multiple requests
                for (JsonNode req : root.path("requests")) {
                    processRequest(req);
                }
            } else {
                // Single request
                processRequest(root);
            }

            log.info("GenericHttpJob completed for orgId={} jobName={}", orgId, jobName);
        } catch (Exception e) {
            log.error("Error in GenericHttpJob: {}", e.getMessage(), e);
        }
    }

    private void processRequest(JsonNode req) throws Exception {
        String url = req.path("url").asText();
        String methodString = req.path("method").asText("GET").toUpperCase();
        JsonNode body = req.path("body");
        JsonNode headersNode = req.path("headers");

        // Validate and resolve HTTP method
        HttpMethod httpMethod;
        try {
            httpMethod = HttpMethod.valueOf(methodString);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid HTTP method: " + methodString);
        }

        HttpHeaders headers = new HttpHeaders();
        if (headersNode != null && headersNode.isObject()) {
            headersNode.fields().forEachRemaining(e -> headers.add(e.getKey(), e.getValue().asText()));
        }

        // ✅ Ensure application/json Content-Type if not explicitly set
        if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }

        HttpEntity<String> entity;
        if (!body.isMissingNode() && !body.isNull()) {
            String bodyString = objectMapper.writeValueAsString(body);
            entity = new HttpEntity<>(bodyString, headers);
        } else {
            entity = new HttpEntity<>(headers);
        }

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                httpMethod,
                entity,
                String.class
        );

        log.info("Request to {} returned status {} and body: {}", url, response.getStatusCode(), response.getBody());
    }
}
