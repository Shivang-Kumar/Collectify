package edu.tcu.cs.hogwarts_artifacts_online.artifact.dto;

import java.io.Serializable;

import edu.tcu.cs.hogwarts_artifacts_online.OwnerDto.dto.OwnerDto;



public record ArtifactDto(
		
		
		String id, 
		
		@jakarta.validation.constraints.NotEmpty(message="name is required.")
		String name, 
		
		@jakarta.validation.constraints.NotEmpty(message="description is required")
		String description ,
		
		@jakarta.validation.constraints.NotEmpty(message="imageUrl is required")
		String imageUrl,
		
		OwnerDto owner) implements Serializable{

}
