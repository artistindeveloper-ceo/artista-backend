package com.artist_in.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A song placed into a jam session's setlist, in a given order. The leader can
 * reorder the setlist and jump between songs; each entry can also carry a
 * session-specific transpose offset override.
 */
@Entity
@Table(
        name = "jam_session_songs",
        indexes = {
                @Index(name = "idx_jss_session", columnList = "jam_session_id"),
                @Index(name = "idx_jss_song", columnList = "song_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JamSessionSong extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jam_session_id", nullable = false)
    private JamSession jamSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @Column(name = "position", nullable = false)
    private int position;

    /** Per-session override of transpose offset (semitones) for this song specifically. */
    @Column(name = "transpose_offset", nullable = false)
    @Builder.Default
    private int transposeOffset = 0;

    @Column(name = "performed", nullable = false)
    @Builder.Default
    private boolean performed = false;
}
