package com.artist_in.app.service;

import org.springframework.data.domain.Pageable;

import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.jam.CreateSongRequest;
import com.artist_in.app.dto.jam.SongResponse;


public interface SongService {

    SongResponse createSong(Long ownerId, CreateSongRequest request);

    SongResponse updateSong(Long songId, Long requesterId, CreateSongRequest request);

    void deleteSong(Long songId, Long requesterId);

    SongResponse getSong(Long songId);

    PageResponse<SongResponse> getMySongs(Long ownerId, Pageable pageable);

    PageResponse<SongResponse> getPublicSongs(Pageable pageable);


}