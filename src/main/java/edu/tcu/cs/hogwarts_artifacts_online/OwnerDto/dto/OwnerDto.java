package edu.tcu.cs.hogwarts_artifacts_online.OwnerDto.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotEmpty;

public record OwnerDto(
		Integer id,
		@NotEmpty(message="name is required")
		String name,
		Integer numberOfArtifact) implements Serializable {

}
