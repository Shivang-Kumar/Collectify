package edu.tcu.cs.hogwarts_artifacts_online.artifact;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import org.hibernate.Hibernate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tcu.cs.hogwarts_artifacts_online.Wizard.Wizard;
import edu.tcu.cs.hogwarts_artifacts_online.Wizard.dto.WizardDto;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.DTO.ArtifactDto;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.converter.ArtifactToArtifactDtoConverter;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.utils.CommonUtils;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.utils.IdWorker;
import edu.tcu.cs.hogwarts_artifacts_online.client.ai.chat.ChatClient;
import edu.tcu.cs.hogwarts_artifacts_online.client.ai.chat.dto.ChatRequest;
import edu.tcu.cs.hogwarts_artifacts_online.client.ai.chat.dto.ChatResponse;
import edu.tcu.cs.hogwarts_artifacts_online.client.ai.chat.dto.Content;
import edu.tcu.cs.hogwarts_artifacts_online.client.ai.chat.dto.Part;
import edu.tcu.cs.hogwarts_artifacts_online.rediscache.RedisLeaderboardCacheClient;
import edu.tcu.cs.hogwarts_artifacts_online.system.ObjectNotFoundException;
import io.micrometer.observation.annotation.Observed;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Service
@Transactional
public class ArtifactService {

	private final ArtifactRepository artifactRepository;
	private final ArtifactToArtifactDtoConverter artifactToArtifactDtoConverter;

	private final ChatClient chatClient;

	private final IdWorker idWorker;
	
	private final RedisLeaderboardCacheClient leaderboardCacheClient;

	@Autowired
	ObjectMapper objectMapper;

	public ArtifactService(ArtifactRepository artifactRepository, IdWorker idWorker,ArtifactToArtifactDtoConverter artifactToArtifactDtoConverter, ChatClient chatClient,RedisLeaderboardCacheClient redisLeaderboardCacheClient) {
		super();
		this.artifactRepository = artifactRepository;
		this.artifactToArtifactDtoConverter=artifactToArtifactDtoConverter;
		this.chatClient = chatClient;
		this.idWorker = idWorker;
		this.leaderboardCacheClient=redisLeaderboardCacheClient;
	}

	@Observed(name = "artifact", contextualName = "findByIdService")
	@Cacheable(value="artifacts",key="#artifactId")
	public ArtifactDto findById(String artifactId) {
		Artifact foundArtifact= this.artifactRepository.findById(artifactId)
				.orElseThrow(() -> new ObjectNotFoundException("artifact", artifactId));
		ArtifactDto foundArtifactDto=this.artifactToArtifactDtoConverter.convert(foundArtifact);
		
		
		return foundArtifactDto;
	}

	public List<Artifact> findAll() {
		return this.artifactRepository.findAll();
	}

	public Artifact save(Artifact newArtifact) {

		newArtifact.setId(idWorker.nextId() + "");
		return this.artifactRepository.save(newArtifact);

	}

	
	@Transactional
	@CachePut(value="artifacts",key="#result.id")
	public ArtifactDto update(String artifactId, @Valid ArtifactDto updateArtifactDto) {

		return this.artifactRepository.findById(artifactId).map(oldArtifact -> {
			oldArtifact.setName(updateArtifactDto.name());
			oldArtifact.setDescription(updateArtifactDto.description());
			oldArtifact.setImageUrl(updateArtifactDto.imageUrl());
			//forcing artifact to load wizard also so that cache has complete object
		   oldArtifact.getOwner();
		   this.artifactRepository.save(oldArtifact);
		   
		   ArtifactDto oldArtifactDto=this.artifactToArtifactDtoConverter.convert(oldArtifact);
		   return oldArtifactDto;
		  

		}).orElseThrow(() -> new ObjectNotFoundException("artifact", artifactId));

	}

	public void delete(String artifactId) {
		Artifact artifact = this.artifactRepository.findById(artifactId)
				.orElseThrow(() -> new ObjectNotFoundException("artifact", artifactId));
		this.artifactRepository.deleteById(artifactId);

	}

	public String summarize(List<ArtifactDto> artifactDtos) throws Exception {

		List<Part> parts = new ArrayList();
		for (ArtifactDto a : artifactDtos) {
			parts.add(new Part(a.name()));
		}

		ChatRequest chatRequest = new ChatRequest(List.of(new Content(parts)));
		ChatResponse response = this.chatClient.generate(chatRequest);

		

		return response.getCandidates().get(0).getContent().getParts().get(0).getText();
	}

	public Page<Artifact> findAll(Pageable pageable) {
		
		return this.artifactRepository.findAll(pageable);
	}
	
//	
//	public List<Object> getLeaderboard(String entityType, String property, int limit) {
//		
//
//		boolean checkedKey=this.leaderboardCacheClient.hasKey(entityType, property);
//		if(!checkedKey)
//		{
//			System.out.println("Reached get Leaderboard--------------------if condition");
//			//check in the database and set the cache
//			Sort sort=Sort.by(property).descending();
//			List<Artifact> sortedWizards=this.artifactRepository.findAll(sort);
//			sortedWizards.stream().forEach(artifact -> {
//				double score=CommonUtils.getScoreOfProperty(artifact, property);
//				try {
//					this.leaderboardCacheClient.saveEntityOfLeaderBoard(entityType, artifact.getId()+"", new ObjectMapper().writeValueAsString(this.artifactToArtifactDtoConverter.convert(artifact)));
//				} catch (JsonProcessingException e) {
//					e.printStackTrace();
//				}
//				this.leaderboardCacheClient.setScore(entityType, property, artifact.getId()+"",score);
//			});
//		}
//	
//		Set<ZSetOperations.TypedTuple<String>> ans= this.leaderboardCacheClient.getTop(entityType, property, limit);
//		return ans.stream().map(tuple ->
//		{
//			 String json = (String) leaderboardCacheClient.getEntityOfLeaderboard(entityType, tuple.getValue());
//		        try {
//		            return new ObjectMapper().readValue(json, ArtifactDto.class);
//		        } catch (JsonProcessingException e) {
//		            throw new RuntimeException(e);
//		        }
//		}
//		).collect(Collectors.toList());
//	}
//	
	
}
