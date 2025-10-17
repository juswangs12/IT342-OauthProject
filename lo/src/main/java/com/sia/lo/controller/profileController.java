package com.sia.lo.controller;

import com.sia.lo.entity.user;
import com.sia.lo.repository.authProviderRepository;
import com.sia.lo.repository.userRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class profileController {

    private final userRepository userRepo;
    private final authProviderRepository authRepo;

    public profileController(userRepository userRepo, authProviderRepository authRepo) {
        this.userRepo = userRepo; this.authRepo = authRepo;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        Long userId = extractUserId(principal);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        user user = userRepo.findById(userId).orElseThrow();
        Map<String, Object> body = Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "displayName", user.getDisplayName(),
                "avatarUrl", user.getAvatarUrl(),
                "bio", user.getBio(),
                "createdAt", user.getCreatedAt(),
                "updatedAt", user.getUpdatedAt()
        );
        return ResponseEntity.ok(body);
    }

    @PostMapping("/profile")
    public ResponseEntity<?> updateProfile(Authentication authentication, @RequestBody Map<String,String> payload) {
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        Long userId = extractUserId(principal);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        user user = userRepo.findById(userId).orElseThrow();
        String displayName = payload.get("displayName");
        String bio = payload.get("bio");
        if (displayName != null) user.setDisplayName(displayName);
        if (bio != null) user.setBio(bio);
        user.setUpdatedAt(Instant.now());
        userRepo.save(user);

        return ResponseEntity.ok(Map.of("status","ok"));
    }

    private Long extractUserId(OAuth2User principal) {
        Object idObj = principal.getAttribute("id");
        if (idObj == null) {
            // fallback if stored differently
            Object userId = principal.getAttributes().get("id");
            if (userId == null) return null;
            return Long.valueOf(String.valueOf(userId));
        }
        return Long.valueOf(String.valueOf(idObj));
    }
}

