package com.artist_in.app.dto.instument;

import java.math.BigDecimal;

public interface InstrumentProjection {
	Integer getId();

	String getModel();

	String getBrandName();

	String getTypeName();

	BigDecimal getPrice();

	String getImageUrl();

	Long getUsageCount();
}
