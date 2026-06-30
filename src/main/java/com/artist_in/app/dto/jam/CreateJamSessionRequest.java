package com.artist_in.app.dto.jam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateJamSessionRequest {

    @NotBlank(message = "Session name is required.")
    @Size(max = 150, message = "Session name must be at most 150 characters.")
    private String name;

    @Size(max = 500, message = "Description must be at most 500 characters.")
    private String description;

    private Instant scheduledStartAt;

    private Boolean isPrivate;

    /** Optional list of song IDs (from the leader's song library) to seed the initial setlist. */
    private java.util.List<Long> initialSongIds;
}
