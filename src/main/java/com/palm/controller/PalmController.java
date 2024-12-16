package com.palm.controller;

import com.palm.service.PalmService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/palm")
public class PalmController {
    @Autowired
    private PalmService palmService;

    @PostMapping(value = "/store", consumes = "application/octet-stream")
    public ResponseEntity<Map<String, String>> storePalmData(@RequestParam String school_id, @RequestBody byte[] palm_binary) {
        String palmId = palmService.storePalmData(school_id, palm_binary);
        if (palmId != null) {
            Map<String, String> response = new HashMap<>();
            response.put("palm_id", palmId);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to store palm data"));
        }
    }

    @PostMapping(value = "/validate", consumes = "application/octet-stream")
    public ResponseEntity<Map<String, String>> validatePalmData(@RequestParam String school_id, @RequestBody byte[] palm_binary) {
        String palmId = palmService.validatePalmData(school_id, palm_binary);
        if (palmId != null) {
            return ResponseEntity.ok(Map.of("palm_id", palmId));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Palm not found"));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deletePalmData(@RequestParam String palm_id, @RequestParam String school_id) {
        boolean deleted = palmService.deletePalmData(palm_id, school_id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Palm data deleted successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Palm data not found"));
        }
    }
}