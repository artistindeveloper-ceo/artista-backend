package com.artist_in.app.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Transposes chords written in ChordPro-like inline syntax, e.g.:
 *   "[G]Twinkle twinkle [C]little star, [G]how I [D]wonder [G]what you are"
 * Chords are matched inside square brackets and shifted by a number of
 * semitones, preserving the chord quality suffix (m, 7, maj7, sus4, dim, aug,
 * add9, slash chords like "G/B", etc).
 */
public final class ChordTransposer {

    private ChordTransposer() {
    }

    // Prefer sharps for the default scale; flats are normalized to their sharp equivalent.
    private static final String[] SHARP_SCALE = {
            "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };

    private static final java.util.Map<String, Integer> NOTE_TO_INDEX = new java.util.HashMap<>();

    static {
        String[] names = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        for (int i = 0; i < names.length; i++) {
            NOTE_TO_INDEX.put(names[i], i);
        }
        // Flat equivalents
        NOTE_TO_INDEX.put("Db", 1);
        NOTE_TO_INDEX.put("Eb", 3);
        NOTE_TO_INDEX.put("Gb", 6);
        NOTE_TO_INDEX.put("Ab", 8);
        NOTE_TO_INDEX.put("Bb", 10);
        NOTE_TO_INDEX.put("Cb", 11);
        NOTE_TO_INDEX.put("Fb", 4);
    }

    // Matches a root note (A-G) optionally followed by # or b, then any trailing
    // chord-quality text (m, 7, maj7, sus2, dim, aug, add9, etc), optionally
    // followed by a slash and a bass note (also transposed).
    private static final Pattern CHORD_PATTERN = Pattern.compile(
            "^([A-G])(#|b)?([^/\\s]*)(?:/([A-G])(#|b)?)?$"
    );

    private static final Pattern BRACKETED_CHORD = Pattern.compile("\\[([^\\[\\]]+)]");

    /**
     * Transpose every chord token found inside square brackets in the given
     * lyrics+chords text by the given number of semitones. Non-chord bracketed
     * tokens are left unchanged (best-effort: if it doesn't look like a chord,
     * we don't touch it).
     */
    public static String transposeLyricsWithChords(String lyricsWithChords, int semitones) {
        if (lyricsWithChords == null || lyricsWithChords.isEmpty() || semitones == 0) {
            return lyricsWithChords;
        }
        Matcher matcher = BRACKETED_CHORD.matcher(lyricsWithChords);
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;
        while (matcher.find()) {
            result.append(lyricsWithChords, lastEnd, matcher.start());
            String token = matcher.group(1);
            String transposed = transposeChordToken(token, semitones);
            result.append('[').append(transposed).append(']');
            lastEnd = matcher.end();
        }
        result.append(lyricsWithChords.substring(lastEnd));
        return result.toString();
    }

    /**
     * Transpose a single chord symbol, e.g. "Am7" or "G/B". Returns the input
     * unchanged if it doesn't parse as a recognizable chord.
     */
    public static String transposeChordToken(String token, int semitones) {
        if (token == null || token.isBlank()) {
            return token;
        }
        Matcher m = CHORD_PATTERN.matcher(token.trim());
        if (!m.matches()) {
            return token;
        }

        String root = m.group(1);
        String accidental = m.group(2) == null ? "" : m.group(2);
        String quality = m.group(3) == null ? "" : m.group(3);
        String bassRoot = m.group(4);
        String bassAccidental = m.group(5) == null ? "" : m.group(5);

        String transposedRoot = transposeNote(root + accidental, semitones);
        if (transposedRoot == null) {
            return token;
        }

        StringBuilder sb = new StringBuilder(transposedRoot).append(quality);

        if (bassRoot != null) {
            String transposedBass = transposeNote(bassRoot + bassAccidental, semitones);
            if (transposedBass != null) {
                sb.append('/').append(transposedBass);
            } else {
                sb.append('/').append(bassRoot).append(bassAccidental);
            }
        }

        return sb.toString();
    }

    /** Transpose a single key string (e.g. "G", "Am", "F#") used as a song's display key. */
    public static String transposeKey(String key, int semitones) {
        if (key == null || key.isBlank() || semitones == 0) {
            return key;
        }
        // Allow a trailing "m" for minor keys, e.g. "Am" -> root "A", suffix "m"
        String trimmed = key.trim();
        String suffix = "";
        String rootPart = trimmed;
        if (trimmed.length() > 1 && trimmed.endsWith("m") && !trimmed.endsWith("dim")) {
            rootPart = trimmed.substring(0, trimmed.length() - 1);
            suffix = "m";
        }
        String transposed = transposeNote(rootPart, semitones);
        return transposed == null ? key : transposed + suffix;
    }

    private static String transposeNote(String note, int semitones) {
        Integer index = NOTE_TO_INDEX.get(note);
        if (index == null) {
            return null;
        }
        int newIndex = Math.floorMod(index + semitones, 12);
        return SHARP_SCALE[newIndex];
    }
}
