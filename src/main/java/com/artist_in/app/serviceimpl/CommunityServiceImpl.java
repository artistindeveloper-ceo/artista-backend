package com.artist_in.app.serviceImpl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.artist_in.app.dto.user.DiscoverUserDto;
import com.artist_in.app.entity.User;
import com.artist_in.app.enums.FollowRequestStatus;
import com.artist_in.app.repository.CommunityRepository;
import com.artist_in.app.repository.FollowRequestRepository;
import com.artist_in.app.repository.UserRepository;
import com.artist_in.app.service.CommunityService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {
    private final CommunityRepository discoverRepository;
    private final FollowRequestRepository followRequestRepository; // ✅ NEW
    private final UserRepository userRepository; // ✅ NEW

    @Override
    public Page<DiscoverUserDto> getDiscoverUsers(Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        User currentUser = userRepository.findById(currentUserId).orElseThrow(); // ✅ NEW

        return discoverRepository.findDiscoverUsers(currentUserId, pageable).map(user -> {
            boolean hasPending = followRequestRepository // ✅ NEW
                    .findByRequesterAndTargetAndStatus(currentUser, user, FollowRequestStatus.PENDING).isPresent();
            return DiscoverUserDto.from(user, hasPending);
        });
    }
}