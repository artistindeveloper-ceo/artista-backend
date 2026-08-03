package com.artist_in.app.serviceimpl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.artist_in.app.entity.Profile;
import com.artist_in.app.entity.ProfileRating;
import com.artist_in.app.entity.User;
import com.artist_in.app.entity.Professional.ProfileCategory;
import com.artist_in.app.entity.location.City;
import com.artist_in.app.repository.ProfileCategoryRepository;
import com.artist_in.app.repository.ProfileRatingRepository;
import com.artist_in.app.repository.ProfileRepository;
import com.artist_in.app.repository.UserRepository;
import com.artist_in.app.repository.location.CityRepository;
import com.artist_in.app.service.ProfileService;
import com.artist_in.app.validator.ProfileValidator;
import com.artist_in.app.validator.ProfileValidatorFactory;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class ProfileServiceImpl implements ProfileService {
	private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

	private final ProfileRepository profileRepository;
	private final UserRepository userRepository;
	private final CityRepository cityRepository;
	private final ProfileValidatorFactory validatorFactory;
	private final ProfileRatingRepository ratingRepository;
	private final ProfileCategoryRepository profileCategoryRepository;

	public ProfileServiceImpl(ProfileRepository profileRepository, UserRepository userRepository,
			CityRepository cityRepository, ProfileValidatorFactory validatorFactory,
			ProfileRatingRepository ratingRepository, ProfileCategoryRepository profileCategoryRepository) {
		this.profileRepository = profileRepository;
		this.userRepository = userRepository;
		this.cityRepository = cityRepository;
		this.validatorFactory = validatorFactory;
		this.ratingRepository = ratingRepository;
		this.profileCategoryRepository = profileCategoryRepository;
	}

	@Transactional
	public Profile createOrUpdateProfile(Long userId, String professionalType, Long cityId, String mobileNumber,
			Map<String, Object> details) {

		log.info("Creating/updating profile: userId={}, type={}, cityId={}", userId, professionalType, cityId);

		User user = userRepository.findById(userId).orElseThrow(() -> {
			log.warn("Profile save failed - user not found: userId={}", userId);
			return new EntityNotFoundException("User not found with id: " + userId);
		});

		City city = cityRepository.findById(cityId).orElseThrow(() -> {
			log.warn("Profile save failed - invalid cityId: {}", cityId);
			return new EntityNotFoundException("City not found with id: " + cityId);
		});

		ProfileCategory category = profileCategoryRepository.findByCodeIgnoreCaseAndIsActiveTrue(professionalType)
				.orElseThrow(() -> {
					log.warn("Profile save failed - invalid professionalType: {}", professionalType);
					return new IllegalArgumentException("Invalid professionalType: " + professionalType);
				});

		ProfileValidator validator = validatorFactory.getValidator(professionalType);
		validator.validate(details);

		user.setCity(city);
		if (mobileNumber != null && !mobileNumber.isBlank()) {
			user.setMobileNumber(mobileNumber.trim());
		}

		Profile profile = profileRepository.findById(userId).orElseGet(Profile::new);

		profile.setUser(user);
		profile.setProfileCategory(category);
		profile.setDetails(details);

		Profile saved = profileRepository.save(profile);

		log.info("Profile saved successfully: userId={}, professionalType={}", userId, category.getCode());

		return saved;
	}

	public Profile getProfileById(Long userId) {
		return profileRepository.findById(userId).orElse(null);
	}

	// NOTE: still accepts a city NAME string here (e.g. "Indore") so existing
	// search callers don't break — the repository below now joins through
	// City instead of matching a flat column. See ProfileRepository_TODO.md.
	public List<Profile> searchProfiles(String city, String professionalType) {
		log.debug("Basic search: city={}, professionalType={}", city, professionalType);
		if (city != null && professionalType != null) {
			return profileRepository.findByUser_City_NameIgnoreCaseAndProfileCategory_CodeIgnoreCase(city,
					professionalType);
		} else if (professionalType != null) {
			return profileRepository.findByProfileCategory_CodeIgnoreCase(professionalType);
		} else if (city != null) {
			return profileRepository.findByUser_City_NameIgnoreCase(city);
		}
		log.warn("searchProfiles called with no filters - this loads the ENTIRE table. "
				+ "Prefer searchTopRatedProfiles for unfiltered/browsing use cases.");
		return profileRepository.findAll();
	}

	/**
	 * City/state/country + instrument-aware, rating-sorted, paginated search. This
	 * is the one to use for "top guitarists in Indore" type queries.
	 */
	public Page<Profile> searchTopRatedProfiles(String professionalType, String city, String state, String country,
			String instrument, Pageable pageable) {

		String cityPattern = city != null ? "%" + city.trim() + "%" : null;
		String statePattern = state != null ? "%" + state.trim() + "%" : null;
		String countryPattern = country != null ? "%" + country.trim() + "%" : null;
		String instrumentPattern = instrument != null ? "%" + instrument.trim() + "%" : null;

		log.info("Top-rated search: type={}, city={}, state={}, country={}, instrument={}, page={}, size={}",
				professionalType, city, state, country, instrument, pageable.getPageNumber(), pageable.getPageSize());

		Page<Profile> result = profileRepository.searchTopRatedProfiles(professionalType, cityPattern, statePattern,
				countryPattern, instrumentPattern, pageable);

		log.info("Top-rated search returned {} of {} total matches (page {} of {})", result.getNumberOfElements(),
				result.getTotalElements(), result.getNumber() + 1, Math.max(result.getTotalPages(), 1));

		return result;
	}

	@Transactional
	public Profile addRating(Long profileId, Long ratedByUserId, Integer stars, String comment) {
		log.info("New rating: profileId={}, ratedByUserId={}, stars={}", profileId, ratedByUserId, stars);

		if (stars == null || stars < 1 || stars > 5) {
			log.warn("Rejected rating - stars out of range: {}", stars);
			throw new IllegalArgumentException("Stars must be between 1 and 5");
		}

		Profile profile = getProfileById(profileId);
		if (profile == null) {
			throw new RuntimeException("Profile not found for id: " + profileId);
		}

		ProfileRating rating = new ProfileRating();
		rating.setProfile(profile);
		rating.setRatedByUserId(ratedByUserId);
		rating.setStars(stars);
		rating.setComment(comment);
		ratingRepository.save(rating);

		Double avg = ratingRepository.calculateAverageRating(profileId);
		Long count = ratingRepository.countByProfileId(profileId);

		double roundedAvg = avg != null ? Math.round(avg * 100.0) / 100.0 : 0.0;
		profile.setAvgRating(roundedAvg);
		profile.setRatingCount(count != null ? count.intValue() : 0);

		Profile saved = profileRepository.save(profile);
		log.info("Rating recalculated: profileId={}, newAvgRating={}, totalRatings={}", profileId, saved.getAvgRating(),
				saved.getRatingCount());

		return saved;
	}

	@Transactional
	public void deleteProfile(Long userId) {
		log.warn("Deleting profile: userId={}", userId);
		Profile profile = getProfileById(userId);
		if (profile == null) {
			log.warn("Delete skipped - profile not found: userId={}", userId);
			return;
		}
		profileRepository.delete(profile);
		log.info("Profile deleted: userId={}", userId);
	}
}