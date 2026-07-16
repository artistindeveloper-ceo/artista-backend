package com.artist_in.app.serviceimpl;

import lombok.extern.slf4j.Slf4j;
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
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final CommunityRepository discoverRepository;
    private final FollowRequestRepository followRequestRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<DiscoverUserDto> getDiscoverUsers(Long currentUserId, int page, int size) {

        log.info("Fetching discover users. UserId={}, Page={}, Size={}",
                currentUserId, page, size);

        Pageable pageable = PageRequest.of(page, size);

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow();

        Page<DiscoverUserDto> discoverUsers = discoverRepository
                .findDiscoverUsers(currentUserId, pageable)
                .map(user -> {
                    boolean hasPending = followRequestRepository
                            .findByRequesterAndTargetAndStatus(
                                    currentUser,
                                    user,
                                    FollowRequestStatus.PENDING)
                            .isPresent();

                    return DiscoverUserDto.from(user, hasPending);
                });

        log.info("Successfully fetched discover users. UserId={}, ReturnedRecords={}, TotalRecords={}",
                currentUserId,
                discoverUsers.getNumberOfElements(),
                discoverUsers.getTotalElements());

        return discoverUsers;
    }
}