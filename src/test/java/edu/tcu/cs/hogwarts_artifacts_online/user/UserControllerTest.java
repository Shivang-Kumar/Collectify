package edu.tcu.cs.hogwarts_artifacts_online.user;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tcu.cs.hogwarts_artifacts_online.artifact.Artifact;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.dto.ArtifactDto;
import edu.tcu.cs.hogwarts_artifacts_online.system.ObjectNotFoundException;
import edu.tcu.cs.hogwarts_artifacts_online.system.StatusCode;
import edu.tcu.cs.hogwarts_artifacts_online.user.DTO.UserDto;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)//Turning off spring security
@ActiveProfiles(value="dev")
public class UserControllerTest {
	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	UserService userService;

	@Value("${api.endpoint.base-url}")
	String baseUrl;

	@Test
	void testFindAllUserSuccess() throws Exception {
		// given
		User u1 = new User();
		u1.setId(1);
		u1.setUsername("ABC_1");
		u1.setRoles("User");
		u1.setEnabled(true);
		u1.setPassword("jkc");

		User u2 = new User();
		u2.setId(2);
		u2.setUsername("ABC_2");
		u2.setRoles("Admin");
		u2.setEnabled(true);
		u2.setPassword("jhk");

		List<User> findUsers = new ArrayList<>();
		findUsers.add(u1);
		findUsers.add(u2);
		given(userService.findAllUser()).willReturn(findUsers);
		// when and then
		this.mockMvc.perform(get(this.baseUrl + "/users").accept(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.flag").value("true")).andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
				.andExpect(jsonPath("$.message").value("Find All Success")).andExpect(jsonPath("$.data[0].id").value(1))
				.andExpect(jsonPath("$.data[0].username").value("ABC_1"))
				.andExpect(jsonPath("$.data[0].enabled").value(true));
	}

	@Test
	void testAddUserSuccess() throws Exception {
		// Given
		UserDto userDto = new UserDto(null, "My Name", true, "user", "fufj");

		String json = this.objectMapper.writeValueAsString(userDto);

		User savedUser = new User();

		savedUser.setId(5);
		savedUser.setUsername("My Name");
		savedUser.setEnabled(true);
		savedUser.setPassword("fufj");
		savedUser.setRoles("user");

		given(this.userService.addUser(Mockito.any(User.class))).willReturn(savedUser);

		// When and Then
		this.mockMvc
				.perform(post(this.baseUrl + "/users").contentType(MediaType.APPLICATION_JSON).content(json)
						.accept(org.springframework.http.MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.flag").value(true)).andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
				.andExpect(jsonPath("$.message").value("Add Success")).andExpect(jsonPath("$.data.id").isNotEmpty())
				.andExpect(jsonPath("$.data.username").value(savedUser.getUsername()))
				.andExpect(jsonPath("$.data.enabled").value(savedUser.isEnabled()));

	}

	@Test
	void testFindUserByIdSuccess() throws Exception {
		// given
		User u1 = new User();
		u1.setId(1);
		u1.setUsername("ABC_1");
		u1.setRoles("User");
		u1.setEnabled(true);
		u1.setPassword("jkc");

		given(this.userService.findUserById(1)).willReturn(u1);

		// when and then
		this.mockMvc.perform(get(this.baseUrl + "/users/1").accept(org.springframework.http.MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.flag").value(true)).andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
				.andExpect(jsonPath("$.message").value("Find One Success")).andExpect(jsonPath("$.data.id").value(1))
				.andExpect(jsonPath("$.data.username").value(u1.getUsername()))
				.andExpect(jsonPath("$.data.enabled").value(u1.isEnabled()));

	}

	@Test
	void testFindUserByIdNotFound() throws Exception {
		// given

		given(this.userService.findUserById(1)).willThrow(new ObjectNotFoundException("user", 1));

		// when and then
		this.mockMvc.perform(get(this.baseUrl + "/users/1").accept(org.springframework.http.MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.flag").value(false)).andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
				.andExpect(jsonPath("$.message").value("Could not find user with id 1  :("));

	}

	@Test
	void testUpdateArtifactSuccess() throws Exception {
		// Given

		UserDto userDto = new UserDto(null, "My Name", true, "user", "fufj");

		String json = this.objectMapper.writeValueAsString(userDto);

		User savedUser = new User();
		savedUser.setId(1);
		savedUser.setUsername("My Name");
		savedUser.setRoles("user");
		savedUser.setEnabled(true);
		savedUser.setPassword("fufj");

		given(this.userService.updateUser(eq(1), Mockito.any(User.class))).willReturn(savedUser);

		// When and Then
		this.mockMvc
				.perform(put(this.baseUrl + "/users/1").contentType(MediaType.APPLICATION_JSON).content(json)
						.accept(org.springframework.http.MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.flag").value(true)).andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
				.andExpect(jsonPath("$.message").value("Update Success")).andExpect(jsonPath("$.data.id").value("1"))
				.andExpect(jsonPath("$.data.username").value(savedUser.getUsername()))
				.andExpect(jsonPath("$.data.roles").value(savedUser.getRoles()))
				.andExpect(jsonPath("$.data.enabled").value(savedUser.isEnabled()));

	}
	
	@Test
	void testUpdateUserErrorWithNonExistentId() throws Exception {
		//Given 
		
		UserDto userDto = new UserDto(null, "My Name", true, "user", "fufj");

		String json = this.objectMapper.writeValueAsString(userDto);


		given(this.userService.updateUser(eq(1),Mockito.any(User.class))).willThrow(new ObjectNotFoundException("user",1));

		// When and Then
		this.mockMvc
				.perform(put(this.baseUrl+"/users/1").contentType(MediaType.APPLICATION_JSON).content(json)
						.accept(org.springframework.http.MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.flag").value(false)).andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
				.andExpect(jsonPath("$.message").value("Could not find user with id 1  :("))
				.andExpect(jsonPath("$.data").isEmpty());
		
	}
	
	@Test
	void testDeleteUserSuccess() throws Exception {
		//Given
		doNothing().when(this.userService).deleteUserById(1);
		
		//when and then
		this.mockMvc
		.perform(delete(this.baseUrl+"/users/1").accept(org.springframework.http.MediaType.APPLICATION_JSON))
		.andExpect(jsonPath("$.flag").value(true))
		.andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
		.andExpect(jsonPath("$.message").value("Delete Success"))
		.andExpect(jsonPath("$.data").isEmpty());

	}
	
	

}
