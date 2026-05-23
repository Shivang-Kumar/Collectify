package edu.tcu.cs.hogwarts_artifacts_online.Owner;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.tcu.cs.hogwarts_artifacts_online.Owner.converter.OwnerDtoToOwnerConverter;
import edu.tcu.cs.hogwarts_artifacts_online.Owner.converter.OwnerToOwnerDtoConverter;
import edu.tcu.cs.hogwarts_artifacts_online.OwnerDto.dto.OwnerDto;
import edu.tcu.cs.hogwarts_artifacts_online.observability.logging.Logged;
import edu.tcu.cs.hogwarts_artifacts_online.observability.tracing.Traced;
import edu.tcu.cs.hogwarts_artifacts_online.system.Result;
import edu.tcu.cs.hogwarts_artifacts_online.system.StatusCode;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/owners")
public class OwnerController {

	private final OwnerService ownerService;
	private final OwnerToOwnerDtoConverter ownerToOwnerDtoConverter;
	private final OwnerDtoToOwnerConverter ownerDtoToOwnerConverter;

	public OwnerController(OwnerService ownerService, OwnerToOwnerDtoConverter ownerToOwnerDtoConverter,
			OwnerDtoToOwnerConverter ownerDtoToOwnerConverter) {
		super();
		this.ownerService = ownerService;
		this.ownerToOwnerDtoConverter = ownerToOwnerDtoConverter;
		this.ownerDtoToOwnerConverter = ownerDtoToOwnerConverter;
	}

	@Traced("owner-controller.findAllOwners")
	@Logged
	@GetMapping()
	public Result findAllOwners() {
		List<Owner> foundOwners = ownerService.findAll();
		List<OwnerDto> foundOwnerDto = foundOwners.stream()
				.map(foundOwner -> this.ownerToOwnerDtoConverter.convert(foundOwner)).collect(Collectors.toList());
		return new Result(true, StatusCode.SUCCESS, "Find All Success", foundOwnerDto);
	}

	@PostMapping()
	@Traced("owner-controller.addOwner")
	@Logged
	public Result addOwner(@Valid @RequestBody OwnerDto ownerDto) {
		Owner newOwner = this.ownerDtoToOwnerConverter.convert(ownerDto);
		Owner savedOwner = this.ownerService.save(newOwner);
		OwnerDto savedOwnerDto = this.ownerToOwnerDtoConverter.convert(savedOwner);
		return new Result(true, StatusCode.SUCCESS, "Add Success", savedOwnerDto);

	}

	@GetMapping("/{ownerId}")
	@Traced("owner-controller.findOwnerById")
	@Logged
	public Result findOwnerById(@PathVariable Integer ownerId) {
		Owner foundOwner = this.ownerService.findById(ownerId);
		OwnerDto foundOwnerDto = this.ownerToOwnerDtoConverter.convert(foundOwner);
		return new Result(true, StatusCode.SUCCESS, "Find One Success", foundOwnerDto);
	}

	@PutMapping("/{ownerId}")
	@Traced("owner-controller.updateOwner")
	@Logged
	public Result updateOwner(@Valid @RequestBody OwnerDto updateOwner, @PathVariable Integer ownerId) {
		Owner newOwner = this.ownerDtoToOwnerConverter.convert(updateOwner);
		Owner savedOwner = this.ownerService.updateOwner(ownerId, newOwner);
		OwnerDto savedOwnerDto = this.ownerToOwnerDtoConverter.convert(savedOwner);
		return new Result(true, StatusCode.SUCCESS, "Update Success", savedOwnerDto);
	}

	@DeleteMapping("/{ownerId}")
	@Traced("owner-controller.deleteOwner")
	@Logged
	public Result deleteOwner(@PathVariable int ownerId) {
		this.ownerService.deleteOwnerById(ownerId);
		return new Result(true, StatusCode.SUCCESS, "Delete Success");
	}

	@PutMapping("/{ownerId}/artifacts/{artifactId}")
	@Traced("owner-controller.assigenArtifact")
	@Logged
	public Result assigenArtifact(@PathVariable Integer ownerId, @PathVariable String artifactId) {
		this.ownerService.assignArtifact(ownerId, artifactId);
		return new Result(true, StatusCode.SUCCESS, "Artifact Assignment Success");
	}

	@GetMapping("/leaderboard/owner")
	@Traced("owner-controller.getLeaderboard")
	@Logged
	public Result getLeaderboard(@RequestParam(defaultValue = "10") int limit) {
		List<Object> ans = this.ownerService.getLeaderboard("owners", "artifacts", limit);
		return new Result(true, StatusCode.SUCCESS, "Owner Leaderboard", ans);

	}

	@GetMapping("/leaderboard/owners/{ownerId}")
	@Traced("owner-controller.getLeaderboard-for-owner-rank")
	@Logged
	public Result getLeaderboard(@PathVariable String ownerId) {
		long ans = this.ownerService.getOwnerRank("owners", "artifacts", ownerId) + 1;
		return new Result(true, StatusCode.SUCCESS, "Owners Leaderboard", ans);

	}

}
