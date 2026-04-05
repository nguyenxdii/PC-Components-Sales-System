package com.diiexe.pcsalessystem.controller;

import com.diiexe.pcsalessystem.dto.BuildPCRequest;
import com.diiexe.pcsalessystem.dto.ProductResponse;
import com.diiexe.pcsalessystem.service.BuildPCService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/build-pc")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BuildPCController {

    private final BuildPCService buildPCService;

    @PostMapping("/suggest")
    public ResponseEntity<List<ProductResponse>> suggestBuild(@RequestBody BuildPCRequest request) {
        return ResponseEntity.ok(buildPCService.suggestBuild(request));
    }
    
    @PostMapping("/save")
    public ResponseEntity<String> saveBuild(@RequestBody Map<String, Object> payload) {
        // In a real app, get userId from SecurityContext
        Long userId = Long.valueOf(payload.get("userId").toString());
        String name = (String) payload.get("name");
        @SuppressWarnings("unchecked")
        Map<String, String> items = (Map<String, String>) payload.get("items");
        
        buildPCService.saveBuild(userId, name, items);
        return ResponseEntity.ok("Build saved successfully");
    }

    @GetMapping("/my-builds/{userId}")
    public ResponseEntity<List<com.diiexe.pcsalessystem.entity.BuildPC>> getMyBuilds(@PathVariable Long userId) {
        return ResponseEntity.ok(buildPCService.getUserBuilds(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBuild(@PathVariable Long id) {
        buildPCService.deleteBuild(id);
        return ResponseEntity.noContent().build();
    }
}
