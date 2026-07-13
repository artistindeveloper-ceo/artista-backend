package com.artist_in.app.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.artist_in.app.enums.InstrumentType;
import com.artist_in.app.enums.Role;
import com.artist_in.app.instument.entity.InstrumentReview;
import com.artist_in.app.instument.entity.UserInstrument;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users", uniqueConstraints = { @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
		@UniqueConstraint(name = "uk_users_email", columnNames = "email") }, indexes = {
				@Index(name = "idx_users_username", columnList = "username"),
				@Index(name = "idx_users_email", columnList = "email") })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 50)
	private String username;

	@Column(nullable = false, unique = true, length = 255)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(name = "display_name", nullable = false, length = 100)
	private String displayName;

	@Column(name = "bio", columnDefinition = "TEXT", length = 500)
	private String bio;

	@Column(name = "profile_photo_url")
	private String profilePhotoUrl;

	@Column(name = "cover_photo_url")
	private String coverPhotoUrl;

	@Column(name = "location", length = 150)
	private String location;

	@Column(name = "website_url")
	private String websiteUrl;

	@Enumerated(EnumType.STRING)
	@Column(name = "primary_instrument", length = 30)
	private InstrumentType primaryInstrument;

	@ElementCollection(targetClass = InstrumentType.class, fetch = FetchType.LAZY)
	@CollectionTable(name = "user_instruments", joinColumns = @JoinColumn(name = "user_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "instrument", length = 30)
	@Fetch(FetchMode.SUBSELECT)
	@Builder.Default
	private Set<InstrumentType> instruments = new HashSet<>();

	@Column(name = "genres", length = 300)
	private String genres;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	@Builder.Default
	private Role role = Role.USER;

	/**
	 * If true, follow requests must be approved before the requester can see
	 * private content and is added as a follower.
	 */
	@Column(name = "is_private", nullable = false)
	@Builder.Default
	private boolean isPrivate = false;

	@Column(name = "is_active", nullable = false, columnDefinition = "boolean default true")
	@Builder.Default
	private boolean isActive = true;

	@Column(name = "is_email_verified", nullable = false)
	@Builder.Default
	private boolean isEmailVerified = false;

	@Column(name = "last_login_at")
	private java.time.Instant lastLoginAt;

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<UserInstrument> userInstruments = new ArrayList<>();

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	private List<InstrumentReview> reviews = new ArrayList<>();
}
