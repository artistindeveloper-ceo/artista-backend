package com.artist_in.app.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artist_in.app.entity.User;
import com.artist_in.app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
		User user = userRepository.findByUsernameIgnoreCase(usernameOrEmail)
				.or(() -> userRepository.findByEmailIgnoreCase(usernameOrEmail)).orElseThrow(
						() -> new UsernameNotFoundException("No user found with username/email: " + usernameOrEmail));
		return new UserPrincipal(user);
	}

	@Transactional(readOnly = true)
	public UserDetails loadUserById(Long id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new UsernameNotFoundException("No user found with id: " + id));
		return new UserPrincipal(user);
	}
}
