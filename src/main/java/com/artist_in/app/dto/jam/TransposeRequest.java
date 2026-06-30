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
public class TransposeRequest {

    /** Number of semitones to transpose relative to the song's original key. Can be negative. */
    @NotNull(message = "transposeOffset is required.")
    private Integer transposeOffset;
}
