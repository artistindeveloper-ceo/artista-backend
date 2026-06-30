package com.artist_in.app.dto.follow;

import com.artist_in.app.dto.user.UserSummaryResponse;
import com.artist_in.app.enums.FollowRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowRequestResponse {
    private Long id;
    private UserSummaryResponse requester;
    private UserSummaryResponse target;
    private FollowRequestStatus status;
    private Instant createdAt;
}
