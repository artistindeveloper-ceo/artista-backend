package com.artist_in.app.dto.user;

import java.time.Instant;

import com.artist_in.app.entity.User;
import com.artist_in.app.enums.InstrumentType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiscoverUserDto {
	private Long id;
	private String username;
	private String displayName;
	private String bio;
	private String profilePhotoUrl;
	private String location;
	private InstrumentType primaryInstrument;
	private String genres;
	private Instant joinedAt;

	public static DiscoverUserDto from(User user) {
		return DiscoverUserDto.builder().id(user.getId()).username(user.getUsername())
				.displayName(user.getDisplayName()).bio(user.getBio()).profilePhotoUrl(user.getProfilePhotoUrl())
				.location(user.getLocation()).primaryInstrument(user.getPrimaryInstrument()).genres(user.getGenres())
				.joinedAt(user.getCreatedAt()) // from BaseEntity
				.build();
	}
}
