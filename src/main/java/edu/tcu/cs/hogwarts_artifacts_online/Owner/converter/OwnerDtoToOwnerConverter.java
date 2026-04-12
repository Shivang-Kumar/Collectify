package edu.tcu.cs.hogwarts_artifacts_online.Owner.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import edu.tcu.cs.hogwarts_artifacts_online.Owner.Owner;
import edu.tcu.cs.hogwarts_artifacts_online.OwnerDto.dto.OwnerDto;

@Component
public class OwnerDtoToOwnerConverter implements Converter<OwnerDto,Owner> {

	@Override
	public Owner convert(OwnerDto source) {
		Owner newOwner=new Owner();
		 //newWizard.setId(source.id());
		newOwner.setName(source.name());
		return newOwner;
	}
	

}
