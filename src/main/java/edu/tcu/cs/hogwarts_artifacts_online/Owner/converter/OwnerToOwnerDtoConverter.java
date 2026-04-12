package edu.tcu.cs.hogwarts_artifacts_online.Owner.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import edu.tcu.cs.hogwarts_artifacts_online.Owner.Owner;
import edu.tcu.cs.hogwarts_artifacts_online.OwnerDto.dto.OwnerDto;


@Component
public class OwnerToOwnerDtoConverter implements Converter<Owner, OwnerDto>{

	@Override
	public OwnerDto convert(Owner source) {
		OwnerDto ownerDto=new OwnerDto(source.getId(),source.getName(),source.getNumberOfArtifacts());
		return ownerDto;
	}

}
