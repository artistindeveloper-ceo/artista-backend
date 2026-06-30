package com.artist_in.app.dto.jam;

import com.artist_in.app.dto.user.UserSummaryResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongResponse {
    private Long id;
    private UserSummaryResponse owner;
    private String title;
    private String artist;
    private String originalKey;
    private Integer bpm;
    private String timeSignature;
    private String lyricsWithChords;
    private String notes;
    private boolean isPublic;
}
