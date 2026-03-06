package com.fincore.usermgmt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemInfoController {

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("timestamp", LocalDateTime.now().toString());
        info.put("version", "1.0.0");
        info.put("build", "24e5038-simplified-jwt");
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("status", "running");
        info.put("message", "If you see build=24e5038, the new code is deployed");
        return ResponseEntity.ok(info);
    }
}
