package edu.tcu.cs.hogwarts_artifacts_online.artifact;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import edu.tcu.cs.hogwarts_artifacts_online.artifact.DTO.ArtifactDto;
import edu.tcu.cs.hogwarts_artifacts_online.system.ObjectNotFoundException;
import edu.tcu.cs.hogwarts_artifacts_online.system.StatusCode;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)//Turning off spring security
@ActiveProfiles(value="dev")
public class ArtifactControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	ArtifactService artifactService;

	@Autowired
	ObjectMapper objectMapper;
	
	@Value("${api.endpoint.base-url}")
	String baseUrl;

	List<Artifact> artifacts;

	@BeforeEach
	void setUp() {
		this.artifacts = new ArrayList<>();

		Artifact a1 = new Artifact();
		a1.setId("21232456489892566");
		a1.setName("Deluminator");
		a1.setDescription("A deluminator is a device invented by albus dumbeldore");
		a1.setImageUrl("ImageUrl");
		this.artifacts.add(a1);

		Artifact a2 = new Artifact();
		a2.setId("31232456489892567");
		a2.setName("Invisibility Cloak");
		a2.setDescription("An invisibility cloak is a magical garment that makes the wearer invisible.");
		a2.setImageUrl("ImageUrl");
		this.artifacts.add(a2);

		Artifact a3 = new Artifact();
		a3.setId("41232456489892568");
		a3.setName("Elder Wand");
		a3.setDescription("The Elder Wand is the most powerful wand in existence.");
		a3.setImageUrl("ImageUrl");
		this.artifacts.add(a3);

		Artifact a4 = new Artifact();
		a4.setId("51232456489892569");
		a4.setName("Resurrection Stone");
		a4.setDescription("The Resurrection Stone can bring back spirits from the dead.");
		a4.setImageUrl("ImageUrl");
		this.artifacts.add(a4);

		Artifact a5 = new Artifact();
		a5.setId("61232456489892570");
		a5.setName("Sorcerer's Stone");
		a5.setDescription("The Sorcerer's Stone can turn any metal into pure gold and produces the Elixir of Life.");
		a5.setImageUrl("ImageUrl");
		this.artifacts.add(a5);

		Artifact a6 = new Artifact();
		a6.setId("71232456489892571");
		a6.setName("Marauder's Map");
		a6.setDescription("The Marauder's Map shows every inch of Hogwarts and the location of everyone in it.");
		a6.setImageUrl("ImageUrl");
		this.artifacts.add(a6);

	}

	@AfterEach
	void tearDown() {

	}

	@Test
	void testFindArtifactByIdSuccess() throws Exception {
		// given

		given(this.artifactService.findById("21232456489892566")).willReturn(this.artifacts.get(0));

		// when and then
		this.mockMvc
				.perform(get(this.baseUrl+"/artifacts/21232456489892566")
						.accept(org.springframework.http.MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.flag").value(true)).andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
				.andExpect(jsonPath("$.message").value("Find one Success"))
				.andExpect(jsonPath("$.data.id").value("21232456489892566"))
				.andExpect(jsonPath("$.data.name").value("Deluminator"));

	}

	@Test
	void testFindArtifactByIdNotFound() throws Exception {
		// given

		given(this.artifactService.findById("21232456489892566"))
				.willThrow(new ObjectNotFoundException("artifact","21232456489892566"));

		// when and then
		this.mockMvc
				.perform(get(this.baseUrl+"/artifacts/21232456489892566")
						.accept(org.springframework.http.MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.flag").value(false)).andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
				.andExpect(jsonPath("$.message").value("Could not find artifact with id 21232456489892566  :("));

	}

	@Test
	void findAllArtifactsSuccess() throws Exception {
		// Given
   Pageable pageable=PageRequest.of(0, 20);
   PageImpl<Artifact> artifactPage=new PageImpl<>(this.artifacts,pageable,this.artifacts.size());
		given(this.artifactService.findAll(Mockito.any(Pageable.class))).willReturn(artifactPage);
		MultiValueMap<String, String> requestParams=new LinkedMultiValueMap<>();
		requestParams.add("page", "0");
		requestParams.add("size","20");
		

		// When and Then
		this.mockMvc.perform(get(this.baseUrl+"/artifacts").accept(org.springframework.http.MediaType.APPLICATION_JSON).params(requestParams))
				.andExpect(jsonPath("$.flag").value(true)).andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
				.andExpect(jsonPath("$.message").value("Find All Success"))
				.andExpect(jsonPath("$.data.content", Matchers.hasSize(this.artifacts.size())))
				.andExpect(jsonPath("$.data.content[0].id").value("21232456489892566"))
				.andExpect(jsonPath("$.data.content[0].name").value("Deluminator"))
				.andExpect(jsonPath("$.data.content[1].id").value("31232456489892567"))
				.andExpect(jsonPath("$.data.content[1].name").value("Invisibility Cloak"));

	}

	@Test
	void testAddArtifactSuccess() throws Exception {
		// Given
		ArtifactDto artifactDto = new ArtifactDto(null, "Rembrall",
				"Remembrall is a small glass ball filled with smoke that turns red when the user has forgotten something",
				"ImageUrl", null);

		String json = this.objectMapper.writeValueAsString(artifactDto);

		Artifact savedArtifact = new Artifact();

		savedArtifact.setId("21135465456489800");
		savedArtifact.setName("Remembrall");
		savedArtifact.setDescription(
				"Remembrall is a small glass ball filled with smoke that turns red when the user has forgotten something");
		savedArtifact.setImageUrl("Image URL.....");

		given(this.artifactService.save(Mockito.any(Artifact.class))).willReturn(savedArtifact);

		// When and Then
		this.mockMvc
				.perform(post(this.baseUrl+"/artifacts").contentType(MediaType.APPLICATION_JSON).content(json)
						.accept(org.springframework.http.MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.flag").value(true)).andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
				.andExpect(jsonPath("$.message").value("Add Success")).andExpect(jsonPath("$.data.id").isNotEmpty())
				.andExpect(jsonPath("$.data.name").value(savedArtifact.getName()))
				.andExpect(jsonPath("$.data.description").value(savedArtifact.getDescription()))
				.andExpect(jsonPath("$.data.imageUrl").value(savedArtifact.getImageUrl()));

	}

	
	
	@Test
	void testUpdateArtifactSuccess() throws Exception {
		//Given 
		
		ArtifactDto artifactDto = new ArtifactDto("13445324535632", "Rembrall",
				"Remembrall is a small glass ball filled with smoke that turns red when the user has forgotten something",
				"ImageUrl", null);

		String json = this.objectMapper.writeValueAsString(artifactDto);

		Artifact updatedArtifact = new Artifact();
		updatedArtifact.setId("13445324535632");
		updatedArtifact.setName("Invisibility Cloak");
		updatedArtifact.setDescription("A new Desription");
		updatedArtifact.setImageUrl("ImageUrl");

		given(this.artifactService.update(eq("13445324535632"),Mockito.any(Artifact.class))).willReturn(updatedArtifact);

		// When and Then
		this.mockMvc
				.perform(put(this.baseUrl+"/artifacts/13445324535632").contentType(MediaType.APPLICATION_JSON).content(json)
						.accept(org.springframework.http.MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.flag").value(true)).andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
				.andExpect(jsonPath("$.message").value("Update Success"))
				.andExpect(jsonPath("$.data.id").value("13445324535632"))
				.andExpect(jsonPath("$.data.name").value(updatedArtifact.getName()))
				.andExpect(jsonPath("$.data.description").value(updatedArtifact.getDescription()))
				.andExpect(jsonPath("$.data.imageUrl").value(updatedArtifact.getImageUrl()));
		
	}
	
	@Test
	void testUpdateArtifactErrorWithNonExistentId() throws Exception {
		//Given 
		
		ArtifactDto artifactDto = new ArtifactDto("13445324535632", "Rembrall",
				"Remembrall is a small glass ball filled with smoke that turns red when the user has forgotten something",
				"ImageUrl", null);

		String json = this.objectMapper.writeValueAsString(artifactDto);


		given(this.artifactService.update(eq("13445324535632"),Mockito.any(Artifact.class))).willThrow(new ObjectNotFoundException("artifact","13445324535632"));

		// When and Then
		this.mockMvc
				.perform(put(this.baseUrl+"/artifacts/13445324535632").contentType(MediaType.APPLICATION_JSON).content(json)
						.accept(org.springframework.http.MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.flag").value(false)).andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
				.andExpect(jsonPath("$.message").value("Could not find artifact with id 13445324535632  :("))
				.andExpect(jsonPath("$.data").isEmpty());
		
	}
	
	
	@Test
	void testDeleteArtifactSuccess() throws Exception {
		//Given
		doNothing().when(this.artifactService).delete("13445324535632");
		
		//when and then
		this.mockMvc
		.perform(delete(this.baseUrl+"/artifacts/13445324535632").accept(org.springframework.http.MediaType.APPLICATION_JSON))
		.andExpect(jsonPath("$.flag").value(true))
		.andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
		.andExpect(jsonPath("$.message").value("Delete Success"))
		.andExpect(jsonPath("$.data").isEmpty());

	}
	
	@Test
	void testDeleteArtifactErrorWithNonExistentId() throws Exception {
		//Given
		doThrow(new ObjectNotFoundException("artifact","13445324535632")).when(this.artifactService).delete("13445324535632");
		
		//when and then
		this.mockMvc
		.perform(delete(this.baseUrl+"/artifacts/13445324535632").accept(org.springframework.http.MediaType.APPLICATION_JSON))
		.andExpect(jsonPath("$.flag").value(false))
		.andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
		.andExpect(jsonPath("$.message").value("Could not find artifact with id 13445324535632  :("))
		.andExpect(jsonPath("$.data").isEmpty());

	}
	
	@Test
	void testSummarizeArtifactSuccess() throws Exception {
		//Given 
		
		given(this.artifactService.summarize(Mockito.anyList())).willReturn("The summary inclues six artifacts");


	

		// When and Then
		this.mockMvc
				.perform(get(this.baseUrl+"/artifacts/summary")
				.accept(org.springframework.http.MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.flag").value(true)).andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
				.andExpect(jsonPath("$.message").value("Summarize Success"))
				.andExpect(jsonPath("$.data").value("The summary inclues six artifacts"));
		
	}
	
	
	
}
