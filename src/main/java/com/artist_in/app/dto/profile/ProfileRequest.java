package com.artist_in.app.dto.profile;

import java.util.Map;

import lombok.Data;

@Data
public class ProfileRequest {
	private String professionalType; // "EVENT_MANAGER"
	private Long cityId; // NAYA — City ke liye ID hi use karo, "city" String hataya
	private String mobileNumber; // NAYA
	private Map<String, Object> details; // role-specific data
}