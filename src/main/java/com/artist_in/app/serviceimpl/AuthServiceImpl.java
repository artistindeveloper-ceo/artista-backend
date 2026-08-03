package com.artist_in.app.serviceimpl;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artist_in.app.dto.auth.AuthResponse;
import com.artist_in.app.dto.auth.CompleteGoogleRegistrationRequest;
import com.artist_in.app.dto.auth.GoogleAuthResponse;
import com.artist_in.app.dto.auth.GoogleLoginRequest;
import com.artist_in.app.dto.auth.LoginRequest;
import com.artist_in.app.dto.auth.RegisterDeviceRequest;
import com.artist_in.app.dto.auth.RegisterRequest;
import com.artist_in.app.dto.business.BusinessCreateRequest;
import com.artist_in.app.entity.LoginDevice;
import com.artist_in.app.entity.Profile;
import com.artist_in.app.entity.User;
import com.artist_in.app.entity.Professional.ProfileCategory;
import com.artist_in.app.enums.AccountType;
import com.artist_in.app.enums.Role;
import com.artist_in.app.exception.ConflictException;
import com.artist_in.app.exception.UnauthorizedException;
import com.artist_in.app.repository.LoginDeviceRepository;
import com.artist_in.app.repository.ProfileCategoryRepository;
import com.artist_in.app.repository.ProfileRepository;
import com.artist_in.app.repository.UserRepository;
import com.artist_in.app.security.JwtTokenProvider;
import com.artist_in.app.security.UserPrincipal;
import com.artist_in.app.service.AuthService;
import com.artist_in.app.service.BusinessService;
import com.artist_in.app.util.UserMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final LoginDeviceRepository loginDeviceRepository;
	private final ProfileRepository profileRepository;
	private final ProfileCategoryRepository professionalCategoryRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final AuthenticationManager authenticationManager;
	private final BusinessService businessService;

	// application.properties me:
	// google.oauth.client-id=<your-web-client-id>.apps.googleusercontent.com
	// Ye WEB client ID hai (Firebase/Google Cloud Console me "Web application"
	// type) —
	// google_sign_in Flutter package Android par bhi is hi client ID ko idToken ka
	// audience banata hai, Android client ID ka nahi.
	@Value("${google.oauth.client-id}")
	private String googleClientId;

	@Override
	@Transactional
	public AuthResponse register(RegisterRequest request) {
		log.info("Register attempt: username={}", request.getUsername());

		validateAccountTypeFields(request);

		if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
			log.warn("Registration failed - username already taken: username={}", request.getUsername());
			throw new ConflictException("Username '" + request.getUsername() + "' is already taken.");
		}
		if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
			log.warn("Registration failed - email already in use: username={}", request.getUsername());
			throw new ConflictException("An account with this email already exists.");
		}

		User user = User.builder().username(request.getUsername()).email(request.getEmail())
				.passwordHash(passwordEncoder.encode(request.getPassword())).displayName(request.getDisplayName())
				.accountType(request.getAccountType()).role(Role.USER).isActive(true).build();
		user = userRepository.save(user);
		log.info("User registered successfully: userId={}, username={}", user.getId(), user.getUsername());

		switch (request.getAccountType()) {
		case INDIVIDUAL -> createIndividualProfile(user, request.getProfessionalType());
		case BUSINESS ->
			createBusinessForOwner(user, request.getBusinessName(), request.getBusinessType(), request.getCityId());
		}

		UserPrincipal principal = new UserPrincipal(user);
		// register pe device info nahi aata (koi deviceId/type request me nahi),
		// isliye yahan session row nahi banate — user turant login karega app se.
		return buildAuthResponse(principal, user, null, null, null);
	}

	private void validateAccountTypeFields(RegisterRequest request) {
		boolean hasProfessionalType = request.getProfessionalType() != null && !request.getProfessionalType().isBlank();
		boolean hasBusinessFields = (request.getBusinessName() != null && !request.getBusinessName().isBlank())
				|| (request.getBusinessType() != null && !request.getBusinessType().isBlank())
				|| request.getCityId() != null;

		if (request.getAccountType() == AccountType.INDIVIDUAL && hasBusinessFields) {
			throw new IllegalArgumentException(
					"accountType is INDIVIDUAL but business fields (businessName/businessType/cityId) were sent");
		}
		if (request.getAccountType() == AccountType.BUSINESS && hasProfessionalType) {
			throw new IllegalArgumentException("accountType is BUSINESS but professionalType was sent");
		}
		if (request.getAccountType() == AccountType.INDIVIDUAL && !hasProfessionalType) {
			throw new IllegalArgumentException("professionalType is required for an individual account");
		}
		if (request.getAccountType() == AccountType.BUSINESS
				&& (request.getBusinessName() == null || request.getBusinessName().isBlank()
						|| request.getBusinessType() == null || request.getBusinessType().isBlank())) {
			throw new IllegalArgumentException("businessName and businessType are required for a business account");
		}
	}

	// professionalTypeCode = request.getProfessionalType() — MUSICIAN,
	// PHOTOGRAPHER, etc. (ProfessionalCategory.code se match hona chahiye)
	private void createIndividualProfile(User user, String professionalTypeCode) {
		ProfileCategory category = resolveProfessionalCategory(professionalTypeCode);

		Profile profile = new Profile();
		profile.setUser(user);
		profile.setProfileCategory(category);
		profile.setAvailable(true);
		profile.setAvgRating(0.0);
		profile.setRatingCount(0);
		profileRepository.save(profile);
		log.info("Default profile created: userId={}, category={}", user.getId(), category.getCode());
	}

	private ProfileCategory resolveProfessionalCategory(String code) {
		return professionalCategoryRepository.findByCodeIgnoreCaseAndIsActiveTrue(code)
				.orElseThrow(() -> new IllegalArgumentException("Invalid professionalType: " + code));
	}

	private void createBusinessForOwner(User user, String businessName, String businessTypeCode, Long cityId) {
		// businessTypeCode BusinessService ke andar BusinessCategory se resolve hoga
		// aur Business.businessCategory pe set hoga.
		BusinessCreateRequest businessReq = BusinessCreateRequest.builder().name(businessName)
				.businessCategoryCode(businessTypeCode).cityId(cityId).build();
		businessService.create(user.getId(), businessReq);
		log.info("Business created: userId={}, businessType={}", user.getId(), businessTypeCode);
	}

	@Override
	@Transactional
	public GoogleAuthResponse loginWithGoogle(GoogleLoginRequest request, String ip) {
		log.info("Google login attempt");

		GoogleIdToken.Payload payload = verifyGoogleIdToken(request.getIdToken());
		String email = payload.getEmail();
		String name = (String) payload.get("name");
		String googleSub = payload.getSubject();

		if (email == null || email.isBlank()) {
			log.warn("Google login failed - token had no email");
			throw new UnauthorizedException("Google account has no email associated.");
		}

		var existingUser = userRepository.findByEmailIgnoreCase(email);

		// User exists AND already completed account-type setup -> seedha login
		if (existingUser.isPresent() && existingUser.get().getAccountType() != null) {
			User user = existingUser.get();
			UserPrincipal principal = new UserPrincipal(user);
			log.info("Google login successful: userId={}", user.getId());

			AuthResponse authResponse = buildAuthResponse(principal, user, request.getDeviceId(),
					request.getDeviceType(), request.getFcmToken(), request.getDeviceModel(), request.getDeviceOs(),
					ip);

			return GoogleAuthResponse.builder().status(GoogleAuthResponse.GoogleAuthStatus.LOGIN_SUCCESS)
					.auth(authResponse).build();
		}

		// Naya user, ya purana bare user jiska accountType set nahi -> signup required
		log.info("Google signup required: email={}", email);
		String signupToken = jwtTokenProvider.generateSignupToken(email, googleSub, name);

		return GoogleAuthResponse.builder().status(GoogleAuthResponse.GoogleAuthStatus.SIGNUP_REQUIRED)
				.signupToken(signupToken).email(email).name(name).build();
	}

	@Override
	@Transactional
	public AuthResponse completeGoogleRegistration(CompleteGoogleRegistrationRequest request, String ip) {
		log.info("Completing Google registration");

		JwtTokenProvider.SignupTokenClaims claims = jwtTokenProvider.parseSignupToken(request.getSignupToken());
		String email = claims.email();
		String name = claims.name();

		validateAccountTypeFieldsForGoogle(request);

		User user = userRepository.findByEmailIgnoreCase(email).orElse(null);

		if (user == null) {
			String username = generateUniqueUsernameFromEmail(email);
			user = User.builder().username(username).email(email)
					.passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
					.displayName(name != null && !name.isBlank() ? name : username)
					.accountType(request.getAccountType()).role(Role.USER).isActive(true).isEmailVerified(true).build();
			user = userRepository.save(user);
			log.info("New Google user created: userId={}, username={}", user.getId(), username);
		} else {
			if (user.getAccountType() != null) {
				log.warn("Registration already completed for email={}", email);
				throw new ConflictException("Account setup is already complete for this email.");
			}
			user.setAccountType(request.getAccountType());
			user = userRepository.save(user);
			log.info("Existing bare Google user completed setup: userId={}", user.getId());
		}

		switch (request.getAccountType()) {
		case INDIVIDUAL -> createIndividualProfileIfMissing(user, request.getProfessionalType());
		case BUSINESS ->
			createBusinessForOwner(user, request.getBusinessName(), request.getBusinessType(), request.getCityId());
		}

		UserPrincipal principal = new UserPrincipal(user);
		return buildAuthResponse(principal, user, request.getDeviceId(), request.getDeviceType(), request.getFcmToken(),
				request.getDeviceModel(), request.getDeviceOs(), ip);
	}

	private void validateAccountTypeFieldsForGoogle(CompleteGoogleRegistrationRequest request) {
		boolean hasProfessionalType = request.getProfessionalType() != null && !request.getProfessionalType().isBlank();

		if (request.getAccountType() == AccountType.INDIVIDUAL && !hasProfessionalType) {
			throw new IllegalArgumentException("professionalType is required for an individual account");
		}
		if (request.getAccountType() == AccountType.BUSINESS
				&& (request.getBusinessName() == null || request.getBusinessName().isBlank()
						|| request.getBusinessType() == null || request.getBusinessType().isBlank())) {
			throw new IllegalArgumentException("businessName and businessType are required for a business account");
		}
	}

	private void createIndividualProfileIfMissing(User user, String professionalTypeCode) {
		if (profileRepository.existsByUser_Id(user.getId())) {
			return;
		}
		createIndividualProfile(user, professionalTypeCode);
	}

	private GoogleIdToken.Payload verifyGoogleIdToken(String idTokenString) {
		try {
			GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
					GsonFactory.getDefaultInstance()).setAudience(Collections.singletonList(googleClientId)).build();

			GoogleIdToken idToken = verifier.verify(idTokenString);
			if (idToken == null) {
				log.warn("Google login failed - idToken verification returned null (invalid/expired token)");
				throw new UnauthorizedException("Invalid or expired Google token.");
			}
			return idToken.getPayload();
		} catch (UnauthorizedException e) {
			throw e;
		} catch (Exception e) {
			log.error("Google idToken verification error", e);
			throw new UnauthorizedException("Could not verify Google token.");
		}
	}

	private String generateUniqueUsernameFromEmail(String email) {
		String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "");
		if (base.isBlank()) {
			base = "user";
		}
		String candidate = base;
		int suffix = 1;
		while (userRepository.existsByUsernameIgnoreCase(candidate)) {
			candidate = base + suffix++;
		}
		return candidate;
	}

	private AuthResponse buildAuthResponse(UserPrincipal principal, User user, String deviceId, String deviceType,
			String fcmToken, String deviceModel, String deviceOs, String deviceIp) {
		String accessToken = jwtTokenProvider.generateAccessToken(principal);
		String refreshTokenValue = jwtTokenProvider.generateRefreshTokenValue();
		Instant expiry = Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs());

		if (deviceId != null) {
			LoginDevice device = loginDeviceRepository.findByUser_IdAndDeviceId(user.getId(), deviceId)
					.orElse(new LoginDevice());

			device.setUser(user);
			device.setDeviceId(deviceId);
			device.setDeviceType(deviceType);
			if (fcmToken != null)
				device.setFcmToken(fcmToken);
			if (deviceModel != null)
				device.setDeviceModel(deviceModel);
			if (deviceOs != null)
				device.setDeviceOs(deviceOs);
			if (deviceIp != null)
				device.setDeviceIp(deviceIp);
			device.setRefreshToken(refreshTokenValue);
			device.setRefreshTokenExpiry(expiry);
			device.setLastLoginAt(Instant.now());
			device.setActive(true);

			loginDeviceRepository.save(device);
		}

		return AuthResponse.builder().accessToken(accessToken).refreshToken(refreshTokenValue)
				.accessTokenExpiresInMs(jwtTokenProvider.getAccessTokenExpirationMs()).user(UserMapper.toSummary(user))
				.build();
	}

	@Override
	@Transactional
	public AuthResponse refresh(String refreshTokenValue) {
		log.debug("Refresh token request received");

		LoginDevice device = loginDeviceRepository.findByRefreshTokenAndIsActiveTrue(refreshTokenValue)
				.orElseThrow(() -> {
					log.warn("Refresh failed - token not found or inactive");
					return new UnauthorizedException("Invalid refresh token.");
				});

		if (device.getRefreshTokenExpiry() != null && device.getRefreshTokenExpiry().isBefore(Instant.now())) {
			log.warn("Refresh failed - token expired: userId={}", device.getUser().getId());
			device.setActive(false);
			device.setRefreshToken(null);
			loginDeviceRepository.save(device);
			throw new UnauthorizedException("Refresh token has expired. Please log in again.");
		}

		User user = device.getUser();
		log.info("Token refreshed successfully: userId={}, deviceId={}", user.getId(), device.getDeviceId());

		UserPrincipal principal = new UserPrincipal(user);
		// same device row reuse hogi — naya refreshToken issue karke isi row me update
		return buildAuthResponse(principal, user, device.getDeviceId(), device.getDeviceType(), device.getFcmToken());
	}

	@Override
	@Transactional
	public void logout(String refreshTokenValue) {
		log.debug("Logout request received");

		int updated = loginDeviceRepository.deactivateByRefreshToken(refreshTokenValue);

		if (updated > 0) {
			log.info("Logout successful");
		} else {
			log.warn("Logout attempted with unknown/invalid refresh token");
		}
	}

	private AuthResponse buildAuthResponse(UserPrincipal principal, User user, String deviceId, String deviceType,
			String fcmToken) {
		String accessToken = jwtTokenProvider.generateAccessToken(principal);
		String refreshTokenValue = jwtTokenProvider.generateRefreshTokenValue();
		Instant expiry = Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs());

		// deviceId null ho sakta hai register() se aane par — us case me row skip
		if (deviceId != null) {
			LoginDevice device = loginDeviceRepository.findByUser_IdAndDeviceId(user.getId(), deviceId)
					.orElse(new LoginDevice());

			device.setUser(user);
			device.setDeviceId(deviceId);
			device.setDeviceType(deviceType);
			if (fcmToken != null) {
				device.setFcmToken(fcmToken);
			}
			device.setRefreshToken(refreshTokenValue);
			device.setRefreshTokenExpiry(expiry);
			device.setLastLoginAt(Instant.now());
			device.setActive(true);

			loginDeviceRepository.save(device);
			log.debug("Device session saved: userId={}, deviceId={}", user.getId(), deviceId);
		}

		log.debug("Auth tokens generated for userId={}", user.getId());

		return AuthResponse.builder().accessToken(accessToken).refreshToken(refreshTokenValue)
				.accessTokenExpiresInMs(jwtTokenProvider.getAccessTokenExpirationMs()).user(UserMapper.toSummary(user))
				.build();
	}

	@Override
	@Transactional
	public void registerDevice(Long userId, RegisterDeviceRequest request) {
		LoginDevice device = loginDeviceRepository.findByUser_IdAndDeviceId(userId, request.getDeviceId()).orElseThrow(
				() -> new UnauthorizedException("No active session found for this device. Please log in again."));

		device.setFcmToken(request.getFcmToken());
		loginDeviceRepository.save(device);
		log.info("FCM token updated: userId={}, deviceId={}", userId, request.getDeviceId());
	}

	@Override
	@Transactional
	public AuthResponse login(LoginRequest request, String ip) {
		log.info("Login attempt: usernameOrEmail={}", request.getUsernameOrEmail());
		try {
			var authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword()));
			UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
			User user = principal.getUser();

			log.info("Login successful: userId={}", user.getId());
			return buildAuthResponse(principal, user, request.getDeviceId(), request.getDeviceType(),
					request.getFcmToken(), request.getDeviceModel(), request.getDeviceOs(), ip);
		} catch (Exception ex) {
			log.warn("Login failed: usernameOrEmail={}, reason={}", request.getUsernameOrEmail(), ex.getMessage());
			throw ex;
		}
	}

}