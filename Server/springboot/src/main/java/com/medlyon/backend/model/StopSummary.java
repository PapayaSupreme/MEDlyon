package com.medlyon.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StopSummary(
		@JsonProperty("id") String id,
		@JsonProperty("lat") double lat,
		@JsonProperty("lng") double lng,
		@JsonProperty("name") String name
) {
}
