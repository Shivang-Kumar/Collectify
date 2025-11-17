package edu.tcu.cs.hogwarts_artifacts_online.artifact.DTO;

import java.io.Serializable;

import edu.tcu.cs.hogwarts_artifacts_online.Wizard.dto.WizardDto;



public record ArtifactDto(
		
		
		String id, 
		
		@jakarta.validation.constraints.NotEmpty(message="name is required.")
		String name, 
		
		@jakarta.validation.constraints.NotEmpty(message="description is required")
		String description ,
		
		@jakarta.validation.constraints.NotEmpty(message="imageUrl is required")
		String imageUrl,
		
		WizardDto owner) implements Serializable{

}
