package com.artist_in.app.controller;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import com.artist_in.app.serviceimpl.PresenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/presence")
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;

    // Single user — e.g. opening a chat screen or profile.
    @GetMapping("/{userId}")
    public ResponseEntity<PresenceStatus> getStatus(@PathVariable Long userId) {
        return ResponseEntity.ok(toStatus(userId));
    }

    // Bulk lookup — e.g. a conversations list or followers list showing a green
    // dot next to each user. Usage: /api/v1/presence?userIds=1,2,3
    @GetMapping
    public ResponseEntity<List<PresenceStatus>> getStatuses(@RequestParam List<Long> userIds) {
        return ResponseEntity.ok(userIds.stream().map(this::toStatus).collect(Collectors.toList()));
    }

    private PresenceStatus toStatus(Long userId) {
        boolean online = presenceService.isOnline(userId);
        Instant lastSeenAt = presenceService.getLastSeenAt(userId);
        return PresenceStatus.builder().userId(userId).online(online).lastSeenAt(lastSeenAt).build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class PresenceStatus {
        private Long userId;
        private boolean online;
        private Instant lastSeenAt;
    }
}