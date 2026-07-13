package com.artist_in.app.transpose;

/**
 * Plain result carrier for a transpose calculation — just the two computed
 * values, nothing about sessions, events, or persistence.
 */
public record TransposeResult(String transposedKey, String transposedLyricsWithChords) {

}
