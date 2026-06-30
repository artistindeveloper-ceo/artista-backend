package com.artist_in.app.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChordTransposerTest {

    @Test
    void transposesSimpleChordUp() {
        assertEquals("A", ChordTransposer.transposeChordToken("G", 2));
    }

    @Test
    void transposesChordWithQualitySuffix() {
        assertEquals("Bm7", ChordTransposer.transposeChordToken("Am7", 2));
    }

    @Test
    void transposesSlashChord() {
        assertEquals("A/C#", ChordTransposer.transposeChordToken("G/B", 2));
    }

    @Test
    void wrapsAroundNegativeSemitones() {
        // C transposed down 2 semitones should wrap to A#
        assertEquals("A#", ChordTransposer.transposeChordToken("C", -2));
    }

    @Test
    void transposesFullLyricsLine() {
        String input = "[G]Twinkle twinkle [C]little star, [G]how I [D]wonder [G]what you are";
        String expected = "[A]Twinkle twinkle [D]little star, [A]how I [E]wonder [A]what you are";
        assertEquals(expected, ChordTransposer.transposeLyricsWithChords(input, 2));
    }

    @Test
    void zeroSemitonesReturnsInputUnchanged() {
        String input = "[G]Hello [C]world";
        assertEquals(input, ChordTransposer.transposeLyricsWithChords(input, 0));
    }

    @Test
    void transposeKeyHandlesMinorSuffix() {
        assertEquals("Bm", ChordTransposer.transposeKey("Am", 2));
    }

    @Test
    void transposeKeyHandlesMajorKey() {
        assertEquals("A", ChordTransposer.transposeKey("G", 2));
    }

    @Test
    void unrecognizedTokenIsReturnedUnchanged() {
        assertEquals("N.C.", ChordTransposer.transposeChordToken("N.C.", 3));
    }
}
