package com.medlyon.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RouteComparisonResponse(
		@JsonProperty("dijkstra") PathResponse dijkstra,
		@JsonProperty("aStar") PathResponse aStar,
		@JsonProperty("dijkstraTimeNanos") long dijkstraTimeNanos,
		@JsonProperty("aStarTimeNanos") long aStarTimeNanos
) {
}
