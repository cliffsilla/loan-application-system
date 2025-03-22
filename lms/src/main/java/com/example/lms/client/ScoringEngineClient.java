package com.example.lms.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "scoring-engine", url = "${scoring.engine.url}")
public interface ScoringEngineClient {

    @PostMapping("/score")
    Map<String, Object> getScore(@RequestBody Map<String, Object> request);
}
