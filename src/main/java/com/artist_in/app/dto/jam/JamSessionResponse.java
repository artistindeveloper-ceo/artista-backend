package com.artist_in.app.dto.jam;

import com.artist_in.app.dto.user.UserSummaryResponse;
import com.artist_in.app.enums.JamSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JamSessionResponse {
    private Long id;
    private String name;
    private String description;
    private UserSummaryResponse leader;
    private JamSessionStatus status;
    private String inviteCode;
    private Long currentSongId;
    private int currentTransposeOffset;
    private Instant scheduledStartAt;
    private Instant startedAt;
    private Instant endedAt;
    private boolean isPrivate;
    private long activeParticipantCount;
    private List<JamSessionSongResponse> setlist;
}
