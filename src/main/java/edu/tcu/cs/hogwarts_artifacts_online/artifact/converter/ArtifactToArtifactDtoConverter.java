package edu.tcu.cs.hogwarts_artifacts_online.artifact.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import edu.tcu.cs.hogwarts_artifacts_online.Owner.converter.OwnerToOwnerDtoConverter;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.Artifact;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.DTO.ArtifactDto;


@Component
public class ArtifactToArtifactDtoConverter  implements Converter<Artifact, ArtifactDto> {

	
	private final OwnerToOwnerDtoConverter wizardDtoConverter;
	
	public ArtifactToArtifactDtoConverter(OwnerToOwnerDtoConverter wizardDtoConverter) {
		super();
		this.wizardDtoConverter = wizardDtoConverter;
	}

	@Override
	public ArtifactDto convert(Artifact source) {
		
		
		ArtifactDto artifactDto=new ArtifactDto(source.getId(),source.getName(),source.getDescription(),source.getImageUrl(),source.getOwner()!= null ? wizardDtoConverter.convert(source.getOwner()):null);
		
		return artifactDto;
	}
	

}
