package com.artist_in.app.dto.jam;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeCurrentSongRequest {

    /** The id of the JamSessionSong (setlist entry) to switch to. */
    @NotNull(message = "jamSessionSongId is required.")
    private Long jamSessionSongId;

    /** Optional transpose offset override (semitones) to apply when switching to this song. */
    private Integer transposeOffset;
}
