package com.artist_in.app.dto.auth;

import com.artist_in.app.enums.AccountType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

	@NotBlank(message = "Username is required.")
	@Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters.")
	@Pattern(regexp = "^[a-zA-Z0-9_.]+$", message = "Username can only contain letters, numbers, underscores, and periods.")
	private String username;

	@NotBlank(message = "Email is required.")
	@Email(message = "Email must be a valid email address.")
	private String email;

	@NotBlank(message = "Password is required.")
	@Size(min = 8, max = 100, message = "Password must be at least 8 characters long.")
	private String password;

	@NotBlank(message = "Display name is required.")
	@Size(min = 1, max = 100, message = "Display name must be between 1 and 100 characters.")
	private String displayName;

	@NotNull(message = "Account type is required.")
	private AccountType accountType; // INDIVIDUAL or BUSINESS

	// ===== used only when accountType = INDIVIDUAL =====
	private String professionalType; // MUSICIAN, PHOTOGRAPHER, EVENT_MANAGER

	// ===== used only when accountType = BUSINESS =====
	private String businessName;
	private String businessType; // SHOP, ACADEMY, SCHOOL, INSTITUTE
	private Long cityId;

	private String deviceId;
	private String deviceType;
	private String fcmToken;
}