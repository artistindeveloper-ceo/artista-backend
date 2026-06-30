package com.artist_in.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A song in a user's personal library. Stores lyrics with inline chord
 * annotations using a ChordPro-like syntax, e.g.:
 *   "[G]Twinkle twinkle [C]little star, [G]how I [D]wonder [G]what you are"
 * The original key is stored so the frontend (or jam session) can compute
 * transposition offsets when the leader changes key.
 */
@Entity
@Table(name = "songs", indexes = {
        @Index(name = "idx_songs_owner", columnList = "owner_id"),
        @Index(name = "idx_songs_title", columnList = "title")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Song extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 150)
    private String artist;

    /** Musical key the lyricsWithChords were originally written in, e.g. "G", "Am", "F#". */
    @Column(name = "original_key", length = 10)
    private String originalKey;

    @Column(name = "bpm")
    private Integer bpm;

    @Column(name = "time_signature", length = 10)
    private String timeSignature;

    @Column(name = "lyrics_with_chords", columnDefinition = "TEXT")
    private String lyricsWithChords;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private boolean isPublic = true;
}
