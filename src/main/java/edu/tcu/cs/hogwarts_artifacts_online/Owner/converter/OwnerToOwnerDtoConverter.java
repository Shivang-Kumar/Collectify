package edu.tcu.cs.hogwarts_artifacts_online.Owner.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import edu.tcu.cs.hogwarts_artifacts_online.Owner.Owner;
import edu.tcu.cs.hogwarts_artifacts_online.OwnerDto.dto.OwnerDto;
import edu.tcu.cs.hogwarts_artifacts_online.observability.logging.Logged;
import edu.tcu.cs.hogwarts_artifacts_online.observability.tracing.Traced;


@Component
public class OwnerToOwnerDtoConverter implements Converter<Owner, OwnerDto>{

	@Override
	@Traced("owner-to-ownerDto-converter.convert")
	@Logged
	public OwnerDto convert(Owner source) {
		OwnerDto ownerDto=new OwnerDto(source.getId(),source.getName(),source.getNumberOfArtifacts());
		return ownerDto;
	}

}
