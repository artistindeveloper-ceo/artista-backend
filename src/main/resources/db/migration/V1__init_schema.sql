-- JamConnect initial schema
-- PostgreSQL compatible

CREATE TABLE users (
    id                  BIGSERIAL    PRIMARY KEY,
    username            VARCHAR(30)  NOT NULL,
    email               VARCHAR(150) NOT NULL,
    password_hash       VARCHAR(255) NOT NULL,
    display_name        VARCHAR(100) NOT NULL,
    bio                 VARCHAR(500),
    profile_photo_url   VARCHAR(500),
    cover_photo_url     VARCHAR(500),
    location            VARCHAR(150),
    website_url         VARCHAR(500),
    primary_instrument  VARCHAR(30),
    genres              VARCHAR(300),
    role                VARCHAR(20)  NOT NULL DEFAULT 'USER',
    is_private          BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    is_email_verified   BOOLEAN      NOT NULL DEFAULT FALSE,
    last_login_at       TIMESTAMP(6),
    created_at          TIMESTAMP(6) NOT NULL,
    updated_at          TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email    UNIQUE (email)
);

CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_email    ON users (email);

CREATE TABLE user_instruments (
    user_id    BIGINT      NOT NULL,
    instrument VARCHAR(30) NOT NULL,
    CONSTRAINT fk_user_instruments_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE refresh_tokens (
    id         BIGSERIAL    PRIMARY KEY,
    token      VARCHAR(512) NOT NULL,
    user_id    BIGINT       NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_refresh_tokens_token UNIQUE (token),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_token ON refresh_tokens (token);
CREATE INDEX idx_refresh_tokens_user  ON refresh_tokens (user_id);

CREATE TABLE follows (
    id           BIGSERIAL    PRIMARY KEY,
    follower_id  BIGINT       NOT NULL,
    following_id BIGINT       NOT NULL,
    created_at   TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_follows_pair       UNIQUE (follower_id, following_id),
    CONSTRAINT fk_follows_follower   FOREIGN KEY (follower_id)  REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_follows_following  FOREIGN KEY (following_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_follows_follower  ON follows (follower_id);
CREATE INDEX idx_follows_following ON follows (following_id);

CREATE TABLE follow_requests (
    id           BIGSERIAL    PRIMARY KEY,
    requester_id BIGINT       NOT NULL,
    target_id    BIGINT       NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at   TIMESTAMP(6) NOT NULL,
    updated_at   TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_follow_requests_pair       UNIQUE (requester_id, target_id),
    CONSTRAINT fk_follow_requests_requester  FOREIGN KEY (requester_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_follow_requests_target     FOREIGN KEY (target_id)    REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_follow_requests_target    ON follow_requests (target_id);
CREATE INDEX idx_follow_requests_requester ON follow_requests (requester_id);

CREATE TABLE posts (
    id            BIGSERIAL    PRIMARY KEY,
    author_id     BIGINT       NOT NULL,
    caption       VARCHAR(2200),
    media_url     VARCHAR(500),
    thumbnail_url VARCHAR(500),
    media_type    VARCHAR(10)  NOT NULL DEFAULT 'NONE',
    like_count    BIGINT       NOT NULL DEFAULT 0,
    comment_count BIGINT       NOT NULL DEFAULT 0,
    is_archived   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP(6) NOT NULL,
    updated_at    TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_posts_author FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_posts_author     ON posts (author_id);
CREATE INDEX idx_posts_created_at ON posts (created_at);

CREATE TABLE post_likes (
    id         BIGSERIAL    PRIMARY KEY,
    post_id    BIGINT       NOT NULL,
    user_id    BIGINT       NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_post_likes_pair UNIQUE (post_id, user_id),
    CONSTRAINT fk_post_likes_post FOREIGN KEY (post_id)  REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_post_likes_user FOREIGN KEY (user_id)  REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_post_likes_post ON post_likes (post_id);
CREATE INDEX idx_post_likes_user ON post_likes (user_id);

CREATE TABLE post_comments (
    id                BIGSERIAL     PRIMARY KEY,
    post_id           BIGINT        NOT NULL,
    author_id         BIGINT        NOT NULL,
    parent_comment_id BIGINT,
    content           VARCHAR(1000) NOT NULL,
    is_deleted        BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP(6)  NOT NULL,
    updated_at        TIMESTAMP(6)  NOT NULL,
    CONSTRAINT fk_post_comments_post   FOREIGN KEY (post_id)           REFERENCES posts         (id) ON DELETE CASCADE,
    CONSTRAINT fk_post_comments_author FOREIGN KEY (author_id)         REFERENCES users         (id) ON DELETE CASCADE,
    CONSTRAINT fk_post_comments_parent FOREIGN KEY (parent_comment_id) REFERENCES post_comments (id) ON DELETE CASCADE
);

CREATE INDEX idx_post_comments_post   ON post_comments (post_id);
CREATE INDEX idx_post_comments_author ON post_comments (author_id);
CREATE INDEX idx_post_comments_parent ON post_comments (parent_comment_id);

CREATE TABLE conversations (
    id                   BIGSERIAL    PRIMARY KEY,
    user_a_id            BIGINT       NOT NULL,
    user_b_id            BIGINT       NOT NULL,
    last_message_at      TIMESTAMP(6),
    last_message_preview VARCHAR(200),
    created_at           TIMESTAMP(6) NOT NULL,
    updated_at           TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_conversations_pair   UNIQUE (user_a_id, user_b_id),
    CONSTRAINT fk_conversations_user_a FOREIGN KEY (user_a_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_conversations_user_b FOREIGN KEY (user_b_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_conversations_user_a ON conversations (user_a_id);
CREATE INDEX idx_conversations_user_b ON conversations (user_b_id);

CREATE TABLE chat_messages (
    id              BIGSERIAL    PRIMARY KEY,
    conversation_id BIGINT       NOT NULL,
    sender_id       BIGINT       NOT NULL,
    content         VARCHAR(4000) NOT NULL,
    attachment_url  VARCHAR(500),
    is_read         BOOLEAN      NOT NULL DEFAULT FALSE,
    read_at         TIMESTAMP(6),
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP(6) NOT NULL,
    updated_at      TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_chat_messages_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_messages_sender       FOREIGN KEY (sender_id)       REFERENCES users         (id) ON DELETE CASCADE
);

CREATE INDEX idx_chat_messages_conversation ON chat_messages (conversation_id);
CREATE INDEX idx_chat_messages_sender       ON chat_messages (sender_id);

CREATE TABLE songs (
    id                 BIGSERIAL    PRIMARY KEY,
    owner_id           BIGINT       NOT NULL,
    title              VARCHAR(200) NOT NULL,
    artist             VARCHAR(150),
    original_key       VARCHAR(10),
    bpm                INT,
    time_signature     VARCHAR(10),
    lyrics_with_chords TEXT         NOT NULL,
    notes              VARCHAR(1000),
    is_public          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP(6) NOT NULL,
    updated_at         TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_songs_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_songs_owner ON songs (owner_id);
CREATE INDEX idx_songs_title ON songs (title);

CREATE TABLE jam_sessions (
    id                       BIGSERIAL    PRIMARY KEY,
    name                     VARCHAR(150) NOT NULL,
    description              VARCHAR(500),
    leader_id                BIGINT       NOT NULL,
    status                   VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED',
    invite_code              VARCHAR(12)  NOT NULL,
    current_song_id          BIGINT,
    current_transpose_offset INT          NOT NULL DEFAULT 0,
    scheduled_start_at       TIMESTAMP(6),
    started_at               TIMESTAMP(6),
    ended_at                 TIMESTAMP(6),
    is_private               BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at               TIMESTAMP(6) NOT NULL,
    updated_at               TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_jam_sessions_invite_code UNIQUE (invite_code),
    CONSTRAINT fk_jam_sessions_leader FOREIGN KEY (leader_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_jam_sessions_leader      ON jam_sessions (leader_id);
CREATE INDEX idx_jam_sessions_status      ON jam_sessions (status);
CREATE INDEX idx_jam_sessions_invite_code ON jam_sessions (invite_code);

CREATE TABLE jam_session_songs (
    id               BIGSERIAL    PRIMARY KEY,
    jam_session_id   BIGINT       NOT NULL,
    song_id          BIGINT       NOT NULL,
    position         INT          NOT NULL,
    transpose_offset INT          NOT NULL DEFAULT 0,
    performed        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP(6) NOT NULL,
    updated_at       TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_jss_session FOREIGN KEY (jam_session_id) REFERENCES jam_sessions (id) ON DELETE CASCADE,
    CONSTRAINT fk_jss_song    FOREIGN KEY (song_id)        REFERENCES songs         (id) ON DELETE CASCADE
);

CREATE INDEX idx_jss_session ON jam_session_songs (jam_session_id);
CREATE INDEX idx_jss_song    ON jam_session_songs (song_id);

CREATE TABLE jam_session_participants (
    id             BIGSERIAL    PRIMARY KEY,
    jam_session_id BIGINT       NOT NULL,
    user_id        BIGINT       NOT NULL,
    role           VARCHAR(20)  NOT NULL DEFAULT 'MUSICIAN',
    joined_at      TIMESTAMP(6) NOT NULL,
    left_at        TIMESTAMP(6),
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_jam_participants_pair    UNIQUE (jam_session_id, user_id),
    CONSTRAINT fk_jam_participants_session FOREIGN KEY (jam_session_id) REFERENCES jam_sessions (id) ON DELETE CASCADE,
    CONSTRAINT fk_jam_participants_user    FOREIGN KEY (user_id)        REFERENCES users        (id) ON DELETE CASCADE
);

CREATE INDEX idx_jam_participants_session ON jam_session_participants (jam_session_id);
CREATE INDEX idx_jam_participants_user    ON jam_session_participants (user_id);

CREATE TABLE notifications (
    id           BIGSERIAL    PRIMARY KEY,
    recipient_id BIGINT       NOT NULL,
    actor_id     BIGINT,
    type         VARCHAR(40)  NOT NULL,
    reference_id BIGINT,
    message      VARCHAR(300),
    is_read      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP(6) NOT NULL,
    updated_at   TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_actor     FOREIGN KEY (actor_id)     REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX idx_notifications_recipient ON notifications (recipient_id);
CREATE INDEX idx_notifications_is_read   ON notifications (is_read);