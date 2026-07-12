package com.artist_in.app.serviceimpl;

import com.artist_in.app.service.SongService;
import com.artist_in.app.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.jam.CreateSongRequest;
import com.artist_in.app.dto.jam.SongResponse;
import com.artist_in.app.entity.Song;
import com.artist_in.app.entity.User;
import com.artist_in.app.exception.ForbiddenException;
import com.artist_in.app.exception.ResourceNotFoundException;
import com.artist_in.app.repository.SongRepository;
import com.artist_in.app.util.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SongServiceImpl implements SongService {

	private final SongRepository songRepository;
	private final UserService userService;

	@Override
	@Transactional
	public SongResponse createSong(Long ownerId, CreateSongRequest request) {
		User owner = userService.getUserOrThrow(ownerId);

		Song song = Song.builder().owner(owner).title(request.getTitle()).artist(request.getArtist())
				.originalKey(request.getOriginalKey()).bpm(request.getBpm()).timeSignature(request.getTimeSignature())
				.lyricsWithChords(request.getLyricsWithChords()).notes(request.getNotes())
				.isPublic(request.getIsPublic() == null || request.getIsPublic()).build();

		song = songRepository.save(song);
		return toResponse(song);
	}

	@Override
	@Transactional
	public SongResponse updateSong(Long songId, Long requesterId, CreateSongRequest request) {
		Song song = getSongOrThrow(songId);
		assertOwner(song, requesterId);

		song.setTitle(request.getTitle());
		song.setArtist(request.getArtist());
		song.setOriginalKey(request.getOriginalKey());
		song.setBpm(request.getBpm());
		song.setTimeSignature(request.getTimeSignature());
		song.setLyricsWithChords(request.getLyricsWithChords());
		song.setNotes(request.getNotes());
		if (request.getIsPublic() != null) {
			song.setPublic(request.getIsPublic());
		}

		song = songRepository.save(song);
		return toResponse(song);
	}

	@Override
	@Transactional
	public void deleteSong(Long songId, Long requesterId) {
		Song song = getSongOrThrow(songId);
		assertOwner(song, requesterId);
		songRepository.delete(song);
	}

	@Override
	@Transactional(readOnly = true)
	public SongResponse getSong(Long songId) {
		return toResponse(getSongOrThrow(songId));
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponse<SongResponse> getMySongs(Long ownerId, Pageable pageable) {
		User owner = userService.getUserOrThrow(ownerId);
		Page<Song> page = songRepository.findByOwner(owner, pageable);
		return PageResponse.from(page, this::toResponse);
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponse<SongResponse> getPublicSongs(Pageable pageable) {
		Page<Song> page = songRepository.findByIsPublicTrue(pageable);
		return PageResponse.from(page, this::toResponse);
	}

	public Song getSongOrThrow(Long songId) {
		return songRepository.findById(songId).orElseThrow(() -> ResourceNotFoundException.of("Song", songId));
	}

	private void assertOwner(Song song, Long requesterId) {
		if (!song.getOwner().getId().equals(requesterId)) {
			throw new ForbiddenException("You can only modify songs in your own library.");
		}
	}

	private SongResponse toResponse(Song song) {
		return SongResponse.builder().id(song.getId()).owner(UserMapper.toSummary(song.getOwner()))
				.title(song.getTitle()).artist(song.getArtist()).originalKey(song.getOriginalKey()).bpm(song.getBpm())
				.timeSignature(song.getTimeSignature()).lyricsWithChords(song.getLyricsWithChords())
				.notes(song.getNotes()).isPublic(song.isPublic()).build();
	}
}
