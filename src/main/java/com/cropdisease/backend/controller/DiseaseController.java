package com.cropdisease.backend.controller;

import com.cropdisease.backend.model.PredictionHistory;
import com.cropdisease.backend.model.User;
import com.cropdisease.backend.repository.PredictionHistoryRepository;
import com.cropdisease.backend.repository.UserRepository;
import com.cropdisease.backend.security.services.UserDetailsImpl;
import com.cropdisease.backend.service.MLPredictionResponse;
import com.cropdisease.backend.service.MLServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class DiseaseController {

    @Autowired
    private MLServiceClient mlServiceClient;

    @Autowired
    private PredictionHistoryRepository historyRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "cropType", required = false) String cropType) {
        
        try {
            // Get Current User
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Optional<User> userOptional = userRepository.findById(userDetails.getId());
            
            if (!userOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }

            // Call Python ML API
            MLPredictionResponse mlResponse = mlServiceClient.predictDisease(file, cropType);

            // Save to History
            PredictionHistory history = new PredictionHistory();
            history.setUser(userOptional.get());
            history.setCropType(cropType != null ? cropType : "Unknown");
            history.setDiseaseName(mlResponse.getDiseaseName());
            history.setConfidenceScore(mlResponse.getConfidenceScore());
            history.setSuggestedTreatment(mlResponse.getSuggestedTreatment());
            history.setImageUrl(file.getOriginalFilename()); // Should be a real URL in production (e.g. S3)

            historyRepository.save(history);

            return ResponseEntity.ok(history);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing image: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory() {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<PredictionHistory> history = historyRepository.findByUserIdOrderByCreatedAtDesc(userDetails.getId());
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching history: " + e.getMessage());
        }
    }
}
