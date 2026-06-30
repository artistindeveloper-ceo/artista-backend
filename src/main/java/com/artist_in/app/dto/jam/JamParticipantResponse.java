package com.artist_in.app.dto.jam;

import com.artist_in.app.dto.user.UserSummaryResponse;
import com.artist_in.app.enums.ParticipantRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JamParticipantResponse {
    private Long id;
    private UserSummaryResponse user;
    private ParticipantRole role;
    private Instant joinedAt;
    private boolean isActive;
}
