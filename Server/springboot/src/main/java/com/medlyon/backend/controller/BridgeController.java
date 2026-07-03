package com.medlyon.backend.controller;

import com.medlyon.backend.model.PathResponse;
import com.medlyon.backend.model.PointResponse;
import com.medlyon.backend.model.StopSummary;
import com.medlyon.backend.service.TransitService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BridgeController {

	private final TransitService transitService;

	public BridgeController(TransitService transitService) {
		this.transitService = transitService;
	}

	@GetMapping("/position")
	public PointResponse getPosition(@RequestParam("Nodename") String nodeName) {
		return transitService.findByName(nodeName);
	}

	@GetMapping("/closest-node")
	public PointResponse getClosestNode(@RequestParam double lat, @RequestParam double lng) {
		return transitService.findClosestNode(lat, lng);
	}

	@GetMapping("/stops")
	public List<StopSummary> listStops() {
		return transitService.listStops();
	}

	@GetMapping("/path")
	public PathResponse computePath(
			@RequestParam double slat,
			@RequestParam double slng,
			@RequestParam double elat,
			@RequestParam double elng) {
		return transitService.computePath(slat, slng, elat, elng);
	}
}
