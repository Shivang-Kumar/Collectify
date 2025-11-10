package edu.tcu.cs.hogwarts_artifacts_online.user;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import edu.tcu.cs.hogwarts_artifacts_online.system.Result;
import edu.tcu.cs.hogwarts_artifacts_online.system.StatusCode;
import edu.tcu.cs.hogwarts_artifacts_online.user.DTO.UserDto;
import edu.tcu.cs.hogwarts_artifacts_online.user.converter.UserDtoToUserConverter;
import edu.tcu.cs.hogwarts_artifacts_online.user.converter.UserToUserDtoConverter;
import edu.tcu.cs.hogwarts_artifacts_online.user.validations.CreateGroup;
import edu.tcu.cs.hogwarts_artifacts_online.user.validations.UpdateGroup;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "${api.endpoint.base-url}/users")
public class UserController {

	@Autowired
	UserService userService;

	@Autowired
	UserToUserDtoConverter userToUserDtoConverter;
	@Autowired
	UserDtoToUserConverter userDtoToUserConverter;

	@GetMapping
	public Result findAllUser() {

		List<User> allUsers = this.userService.findAllUser();
		List<UserDto> allUsersDto = allUsers.stream().map(foundUser -> this.userToUserDtoConverter.convert(foundUser))
				.collect(Collectors.toList());

		return new Result(true, StatusCode.SUCCESS, "Find All Success", allUsersDto);
	}

	@PostMapping
	public Result addUser(@Validated(CreateGroup.class) @RequestBody UserDto userDto) {
		User user = userDtoToUserConverter.convert(userDto);
		User savedUser = userService.addUser(user);
		UserDto savedUserDto = userToUserDtoConverter.convert(savedUser);
		return new Result(true,StatusCode.SUCCESS,"Add Success",savedUserDto);

	}
	
	@GetMapping("/{userId}")
	public Result findUserById(@PathVariable Integer userId) {
		User user=this.userService.findUserById(userId);
		UserDto userDto=this.userToUserDtoConverter.convert(user);
		return new Result(true,StatusCode.SUCCESS,"Find One Success",userDto);
		
	}
	
	@PutMapping("/{userId}")
	public Result updateUser(@PathVariable Integer userId,@Validated(UpdateGroup.class) @RequestBody UserDto newUserDto)
	{
		User newUser=userDtoToUserConverter.convert(newUserDto);
		User updatedUser=this.userService.updateUser(userId,newUser);
		UserDto updateUserDto=this.userToUserDtoConverter.convert(updatedUser);
		return new Result(true,StatusCode.SUCCESS,"Update Success",updateUserDto);
	}
	
	@DeleteMapping("/{userId}")
	public Result deleteUserById(@PathVariable Integer userId) {
		this.userService.deleteUserById(userId);
		return new Result(true,StatusCode.SUCCESS,"Delete Success");
	}
	
	@PatchMapping("/{userId}/password")
	public Result changePassword(@PathVariable Integer userId, @RequestBody Map<String,String> passwordMap)
	{
		String oldPassword=passwordMap.get("oldPassword");
		String newPassword=passwordMap.get("newPassword");
		String confirmPassword=passwordMap.get("confirmPassword");
		this.userService.changePassword(userId,oldPassword,newPassword,confirmPassword);
		return new Result(true,StatusCode.SUCCESS,"Change Password Success", null);
	}

}
