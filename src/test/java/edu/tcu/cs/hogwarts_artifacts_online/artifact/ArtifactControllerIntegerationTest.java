package edu.tcu.cs.hogwarts_artifacts_online.artifact;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;

import edu.tcu.cs.hogwarts_artifacts_online.artifact.dto.ArtifactDto;
import edu.tcu.cs.hogwarts_artifacts_online.system.StatusCode;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Integration test for Artifact API endpoints")
@Tag("integration")
@ActiveProfiles(value="dev")
@Testcontainers
public class ArtifactControllerIntegerationTest {

	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Value("${api.endpoint.base-url}")
	String baseUrl;
	
	String token;
	
	@Container
	@ServiceConnection
	static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:6.2.6"));

	
	@BeforeEach
	void setUp() throws Exception {
		//Setting up token to authenticate
		ResultActions resultActions= this.mockMvc.perform(post(this.baseUrl+"/users/login")
				.with(httpBasic("ABC_1","jkc")));
		
		MvcResult mvcResult= resultActions.andDo(print()).andReturn();
		String contentAsString=mvcResult.getResponse().getContentAsString();
		JSONObject json= new JSONObject(contentAsString);
		this.token="Bearer "+ json.getJSONObject("data").getString("token");//DOnt forget to add bearer_ before token 
		
		
	}
	
	@Test
	@DirtiesContext(methodMode=DirtiesContext.MethodMode.BEFORE_METHOD)
	void testFindAllArtifactSuccess() throws Exception {
		this.mockMvc.perform(get(this.baseUrl+"/artifacts").accept(MediaType.APPLICATION_JSON))
		.andExpect(jsonPath("$.flag").value(true))
		.andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
		.andExpect(jsonPath("$.message").value("Find All Success"))
		.andExpect(jsonPath("$.data.content",Matchers.hasSize(6)));
	}
	
	
	@Test
	@DisplayName("Check artifact with valid input(POST")
	void testAddArtifactSuccess() throws Exception {
		

		ArtifactDto newArtifact = new ArtifactDto("21135465456489800","Remembrall","Remembrall is a small glass ball filled with smoke that turns red when the user has forgotten something","Image URL.....",null);

		
		
		String json = this.objectMapper.writeValueAsString(newArtifact);

		this.mockMvc
				.perform(post(this.baseUrl+"/artifacts").header("Authorization",this.token).contentType(MediaType.APPLICATION_JSON).content(json)
						.accept(org.springframework.http.MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.flag").value(true)).andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
				.andExpect(jsonPath("$.message").value("Add Success")).andExpect(jsonPath("$.data.id").isNotEmpty())
				.andExpect(jsonPath("$.data.name").value(newArtifact.name()))
				.andExpect(jsonPath("$.data.description").value(newArtifact.description()))
				.andExpect(jsonPath("$.data.imageUrl").value(newArtifact.imageUrl()));
		this.mockMvc
		.perform(get(this.baseUrl+"/artifacts").contentType(MediaType.APPLICATION_JSON).content(json)
				.accept(org.springframework.http.MediaType.APPLICATION_JSON))
		.andExpect(jsonPath("$.flag").value(true)).andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
		.andExpect(jsonPath("$.message").value("Find All Success"))
		.andExpect(jsonPath("$.data.content", Matchers.hasSize(7)));

	}

}
