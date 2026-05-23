package edu.tcu.cs.hogwarts_artifacts_online.wizard;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import edu.tcu.cs.hogwarts_artifacts_online.Owner.Owner;
import edu.tcu.cs.hogwarts_artifacts_online.Owner.OwnerService;
import edu.tcu.cs.hogwarts_artifacts_online.OwnerDto.dto.OwnerDto;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.Artifact;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.dto.ArtifactDto;
import edu.tcu.cs.hogwarts_artifacts_online.system.ObjectNotFoundException;
import edu.tcu.cs.hogwarts_artifacts_online.system.StatusCode;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)//Turning off spring security
@ActiveProfiles(value="dev")
public class OwnerControllerTest {
	@Autowired
	MockMvc mockMvc;

	@MockBean
	OwnerService ownerService;

	@Autowired
	ObjectMapper objectMapper;

	@Value("${api.endpoint.base-url}")
	String baseUrl;

	List<Owner> owners;

	@BeforeEach
	void setUp() {

		List<Artifact> artifacts1 = new ArrayList<>();

		Artifact a1 = new Artifact();
		a1.setId("21232456489892566");
		a1.setName("Deluminator");
		a1.setDescription("A deluminator is a device invented by albus dumbeldore");
		a1.setImageUrl("ImageUrl");
		artifacts1.add(a1);

		Artifact a2 = new Artifact();
		a2.setId("31232456489892567");
		a2.setName("Invisibility Cloak");
		a2.setDescription("An invisibility cloak is a magical garment that makes the wearer invisible.");
		a2.setImageUrl("ImageUrl");
		artifacts1.add(a2);

		List<Artifact> artifacts2 = new ArrayList<>();

		Artifact a3 = new Artifact();
		a3.setId("41232456489892568");
		a3.setName("Elder Wand");
		a3.setDescription("The Elder Wand is the most powerful wand in existence.");
		a3.setImageUrl("ImageUrl");
		artifacts2.add(a3);

		Artifact a4 = new Artifact();
		a4.setId("51232456489892569");
		a4.setName("Resurrection Stone");
		a4.setDescription("The Resurrection Stone can bring back spirits from the dead.");
		a4.setImageUrl("ImageUrl");
		artifacts2.add(a4);

		Owner w1 = new Owner();
		w1.setId(1);
		w1.setName("Albus Dumbeldore");
		w1.setArtifacts(artifacts1);

		Owner w2 = new Owner();
		w2.setId(2);
		w2.setName("Harry Potter");
		w2.setArtifacts(artifacts2);

		owners = new ArrayList<>();
		owners.add(w1);
		owners.add(w2);

	}

	@Test
	void testFindAllOwnerSuccess() throws Exception {
		// given
		given(ownerService.findAll()).willReturn(this.owners);
		// when and then
		this.mockMvc.perform(get(this.baseUrl + "/wizards").accept(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.flag").value("true")).andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
				.andExpect(jsonPath("$.message").value("Find All Success")).andExpect(jsonPath("$.data[0].id").value(1))
				.andExpect(jsonPath("$.data[0].name").value("Albus Dumbeldore"))
				.andExpect(jsonPath("$.data[0].numberOfArtifact").value(2));
	}

	@Test
	void testAddOwnerSuccess() throws Exception {

		OwnerDto ownerDto = new OwnerDto(null, "Harry Potter", null);

		String json = this.objectMapper.writeValueAsString(ownerDto);

		Owner savedOwner = new Owner();
		savedOwner.setId(6);
		savedOwner.setName("Harry Potter");

		// given
		given(this.ownerService.save(Mockito.any(Owner.class))).willReturn(savedOwner);

		// when and then
		this.mockMvc
				.perform(post(this.baseUrl + "/wizards").contentType(MediaType.APPLICATION_JSON).content(json)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.flag").value(true)).andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
				.andExpect(jsonPath("$.message").value("Add Success")).andExpect(jsonPath("$.data.id").value(6))
				.andExpect(jsonPath("$.data.name").value("Harry Potter"));

	}

	@Test
	void testFindOwnerByIdSuccess() throws Exception {
		// Given
		given(ownerService.findById(1)).willReturn(this.owners.get(0));
		// When and Then
		this.mockMvc.perform(get(this.baseUrl + "/wizards/1").accept(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.flag").value(true)).andExpectAll(jsonPath("$.code").value(StatusCode.SUCCESS))
				.andExpectAll(jsonPath("$.message").value("Find One Success")).andExpect(jsonPath("$.data.id").value(1))
				.andExpect(jsonPath("$.data.name").value("Albus Dumbeldore"));

	}

	@Test
	void testFindOwnerByIdNotFound() throws Exception {
		// Given
		given(this.ownerService.findById(1)).willThrow(new ObjectNotFoundException("owner", 1));
		// When and Then
		this.mockMvc.perform(get(this.baseUrl + "/wizards/1").accept(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.flag").value(false)).andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
				.andExpect(jsonPath("$.message").value("Could not find owner with id 1  :("))
				.andExpect(jsonPath("$.data").isEmpty());
	}

	@Test
	void testOwnerUpdateSuccess() throws Exception {
		OwnerDto wizardDto = new OwnerDto(null, "Goku Loki", null);

		String json = this.objectMapper.writeValueAsString(wizardDto);

		Owner savedWizard = new Owner();
		savedWizard.setId(6);
		savedWizard.setName("Harry Potter");

		given(this.ownerService.updateOwner(eq(1), Mockito.any(Owner.class))).willReturn(savedWizard);

		// When and Then
		this.mockMvc
				.perform(put(this.baseUrl + "/wizards/1").contentType(MediaType.APPLICATION_JSON).content(json)
						.accept(org.springframework.http.MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.flag").value(true)).andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
				.andExpect(jsonPath("$.message").value("Update Success")).andExpect(jsonPath("$.data.id").value(6))
				.andExpect(jsonPath("$.data.name").value("Harry Potter"));
	}

	@Test
	void testOwnerUpdateWithIdNotFound() throws Exception {

		OwnerDto ownerDto = new OwnerDto(null, "Goku Loki", null);

		String json = this.objectMapper.writeValueAsString(ownerDto);

		given(this.ownerService.updateOwner(eq(1), Mockito.any(Owner.class)))
				.willThrow(new ObjectNotFoundException("owner", 1));

		// When and Then
		this.mockMvc
				.perform(put(this.baseUrl + "/wizards/1").contentType(MediaType.APPLICATION_JSON).content(json)
						.accept(org.springframework.http.MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.flag").value(false)).andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
				.andExpect(jsonPath("$.message").value("Could not find owner with id 1  :("))
				.andExpect(jsonPath("$.data").isEmpty());
	}

	@Test
	void testDeleteOwnerSuccess() throws Exception {
		// Given
		doNothing().when(this.ownerService).deleteOwnerById(1);

		// when and then
		this.mockMvc
				.perform(
						delete(this.baseUrl + "/wizards/1").accept(org.springframework.http.MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.flag").value(true)).andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
				.andExpect(jsonPath("$.message").value("Delete Success")).andExpect(jsonPath("$.data").isEmpty());

	}

	@Test
	void testDeleteOwnerIdNotFound() throws Exception {
		// Given
		doThrow(new ObjectNotFoundException("wizard", 1)).when(ownerService).deleteOwnerById(1);

		// When and then
		this.mockMvc
				.perform(
						delete(this.baseUrl + "/wizards/1").accept(org.springframework.http.MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.flag").value(false)).andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
				.andExpect(jsonPath("$.message").value("Could not find wizard with id 1  :("))
				.andExpect(jsonPath("$.data").isEmpty());

	}

	@Test
	void testAssignArtifactSuccess() throws Exception {
		// Given
		doNothing().when(this.ownerService).assignArtifact(2, "546489712315648975");
		// When and then
		this.mockMvc
				.perform(put(this.baseUrl + "/wizards/2/artifacts/546489712315648975")
						.accept(org.springframework.http.MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.flag").value(true)).andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
				.andExpect(jsonPath("$.message").value("Artifact Assignment Success"))
				.andExpect(jsonPath("$.data").isEmpty());

	}

	@Test
	void testAssignArtifactErrorWithNonExistentOwnerId() throws Exception {
		// Given
		doThrow(new ObjectNotFoundException("owner", 3)).when(this.ownerService).assignArtifact(3,
				"546489712315648975");

		// When and then
		this.mockMvc
				.perform(put(this.baseUrl + "/wizards/3/artifacts/546489712315648975")
						.accept(org.springframework.http.MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.flag").value(false)).andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
				.andExpect(jsonPath("$.message").value("Could not find owner with id 3  :("))
				.andExpect(jsonPath("$.data").isEmpty());

	}
	
	@Test
	void testAssignArtifactErrorWithNonExistentArtifactId() throws Exception {
		// Given
		doThrow(new ObjectNotFoundException("artifact", "546489712315648975")).when(this.ownerService).assignArtifact(3,
				"546489712315648975");

		// When and then
		this.mockMvc
				.perform(put(this.baseUrl + "/wizards/3/artifacts/546489712315648975")
						.accept(org.springframework.http.MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.flag").value(false)).andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
				.andExpect(jsonPath("$.message").value("Could not find artifact with id 546489712315648975  :("))
				.andExpect(jsonPath("$.data").isEmpty());

	}

}
