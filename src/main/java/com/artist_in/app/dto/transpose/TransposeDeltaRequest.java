package com.artist_in.app.dto.transpose;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TransposeDeltaRequest {
    private int deltaSteps; // +1 ya -1
}