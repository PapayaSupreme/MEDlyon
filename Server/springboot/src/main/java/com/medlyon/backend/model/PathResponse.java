package com.medlyon.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PathResponse(
		@JsonProperty("Path") List<PointResponse> path
) {
}
