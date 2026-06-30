package com.artist_in.app.dto.jam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JamSessionSongResponse {
    private Long id;
    private Long songId;
    private String title;
    private String artist;
    private String originalKey;
    private int position;
    private int transposeOffset;
    private boolean performed;
}
