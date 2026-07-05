package com.medlyon.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PointResponse(
		@JsonProperty("lat") double lat,
		@JsonProperty("lng") double lng,
		@JsonProperty("name") String name,
		@JsonProperty("Additional_Information") List<String> additionalInformation
) {
}
