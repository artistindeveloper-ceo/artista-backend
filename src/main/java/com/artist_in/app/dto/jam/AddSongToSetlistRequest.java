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
public class AddSongToSetlistRequest {

    @NotNull(message = "songId is required.")
    private Long songId;

    /** Optional explicit position; if omitted, the song is appended to the end. */
    private Integer position;
}
