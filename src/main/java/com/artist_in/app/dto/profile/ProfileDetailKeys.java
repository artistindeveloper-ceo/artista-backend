package com.artist_in.app.dto.profile;

/**
 * Central place for all JSONB "details" keys used across professional types.
 * Naya professional type add karte waqt, uske keys yahin add karo — taaki
 * controller, validator, aur frontend sab same spelling use karein.
 */
public final class ProfileDetailKeys {

	private ProfileDetailKeys() {
		// utility class, instantiate mat karo
	}

	// ===== MUSICIAN =====
	public static final String PRIMARY_INSTRUMENT = "primaryInstrument";
	public static final String SECONDARY_INSTRUMENT = "secondaryInstrument";

	// ===== PHOTOGRAPHER =====
	public static final String CAMERA_GEAR = "cameraGear";
	public static final String SPECIALIZATION = "specialization";

	// ===== SOUND_ENGINEER (future) =====
	public static final String EQUIPMENT = "equipment";

	// ===== EVENT_MANAGER (future) =====
	public static final String TEAM_SIZE = "teamSize";
}