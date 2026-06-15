package edu.tcu.cs.hogwarts_artifacts_online.user.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.tcu.cs.hogwarts_artifacts_online.user.validations.CreateGroup;
import edu.tcu.cs.hogwarts_artifacts_online.user.validations.UpdateGroup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record UserDto(Integer id ,
		@NotEmpty(message="username is required ",groups = {CreateGroup.class,UpdateGroup.class})
		@Email(message="valid email is required",groups = {CreateGroup.class,UpdateGroup.class})
		String username ,
		boolean enabled,
		@NotEmpty(message="roles are required",groups = {CreateGroup.class,UpdateGroup.class})
		String roles,
		@NotEmpty(message="password is required",groups = {CreateGroup.class})
		String password) {
	

}
