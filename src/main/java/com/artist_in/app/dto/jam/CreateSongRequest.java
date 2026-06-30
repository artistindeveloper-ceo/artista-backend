package com.artist_in.app.dto.jam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateSongRequest {

    @NotBlank(message = "Title is required.")
    @Size(max = 200, message = "Title must be at most 200 characters.")
    private String title;

    @Size(max = 150, message = "Artist must be at most 150 characters.")
    private String artist;

    @Size(max = 10, message = "Original key must be at most 10 characters, e.g. 'G', 'Am', 'F#'.")
    private String originalKey;

    private Integer bpm;

    @Size(max = 10)
    private String timeSignature;

    @NotBlank(message = "Lyrics with chords are required.")
    private String lyricsWithChords;

    @Size(max = 1000, message = "Notes must be at most 1000 characters.")
    private String notes;

    private Boolean isPublic;
}
