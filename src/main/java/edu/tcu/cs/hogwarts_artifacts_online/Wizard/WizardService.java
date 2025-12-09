package edu.tcu.cs.hogwarts_artifacts_online.Wizard;

import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tcu.cs.hogwarts_artifacts_online.Wizard.converter.WizardToWizardDtoConverter;
import edu.tcu.cs.hogwarts_artifacts_online.Wizard.dto.WizardDto;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.Artifact;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.ArtifactRepository;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.utils.CommonUtils;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.utils.IdWorker;
import edu.tcu.cs.hogwarts_artifacts_online.rediscache.RedisLeaderboardCacheClient;
import edu.tcu.cs.hogwarts_artifacts_online.system.ObjectNotFoundException;

@Service
public class WizardService {

	private final WizardRepository wizardRepository;
	private final IdWorker idWorker;
	private final ArtifactRepository artifactRepository;
	private final RedisLeaderboardCacheClient leaderboardCacheClient;
	private final WizardToWizardDtoConverter wizardToWizardDtoConverter;

	public WizardService(WizardRepository wizardRepository, IdWorker idWorker, ArtifactRepository artifactRepository,RedisLeaderboardCacheClient redisLeaderboardCacheClient,WizardToWizardDtoConverter wizardToWizardDtoConverter) {
		super();
		this.wizardRepository = wizardRepository;
		this.idWorker = idWorker;
		this.artifactRepository = artifactRepository;
		this.leaderboardCacheClient=redisLeaderboardCacheClient;
		this.wizardToWizardDtoConverter=wizardToWizardDtoConverter;
	}

	public List<Wizard> findAll() {
		List<Wizard> wizards = wizardRepository.findAll();
		return wizards;
	}

	public Wizard save(Wizard newWizard) {
		newWizard.setId((int) idWorker.nextId());
		return wizardRepository.save(newWizard);
	}

	public Wizard findById(Integer wizardId) {
		Wizard foundWizard = this.wizardRepository.findById(wizardId)
				.orElseThrow(() -> new ObjectNotFoundException("wizard", wizardId));

		return foundWizard;
	}

	public Wizard updateWizard(Integer wizardId, Wizard wizard) {
		Wizard updatedWizard = this.wizardRepository.findById(wizardId).map(foundWizard -> {
			foundWizard.setName(wizard.getName());
			return this.wizardRepository.save(foundWizard);
		}).orElseThrow(() -> new ObjectNotFoundException("wizard", wizardId));

		return updatedWizard;
	}

	public void deleteWizardById(int wizardId) {
		Wizard foundWizard = this.wizardRepository.findById(wizardId)
				.orElseThrow(() -> new ObjectNotFoundException("wizard", wizardId));

		// Before the deletion we will unassign this wizard artifact
		foundWizard.removeAllArtifacts();
		this.wizardRepository.deleteById(wizardId);
	}

	public void assignArtifactToWizard(String artifactID, Integer wizardId) {

		Wizard foundWizard = this.wizardRepository.findById(wizardId)
				.orElseThrow(() -> new ObjectNotFoundException("wizard", wizardId));
		Artifact foundArtifact = this.artifactRepository.findById(artifactID)
				.orElseThrow(() -> new ObjectNotFoundException("artifact", artifactID));

		foundWizard.getArtifacts().add(foundArtifact);
		foundArtifact.setOwner(foundWizard);
		wizardRepository.save(foundWizard);
		artifactRepository.save(foundArtifact);
	}
	
	public void assignArtifact(Integer wizardId,String artifactId)
	{
		Artifact foundArtifact=this.artifactRepository.findById(artifactId).orElseThrow(() -> new ObjectNotFoundException("artifact", artifactId));
		Wizard foundWizard=this.wizardRepository.findById(wizardId).orElseThrow(() -> new ObjectNotFoundException("wizard", wizardId));
		
		//Artifact assignment
		//We need to see if artifact is already owned by some owner
		if(foundArtifact.getOwner()!=null)
		{
			foundArtifact.getOwner().removeArtifact(foundArtifact);
		}
		foundWizard.addArtifact(foundArtifact);
		this.artifactRepository.save(foundArtifact);
		this.wizardRepository.save(foundWizard);
		
		
	}

	public List<Object> getLeaderboard(String entityType, String property, int limit) {
		System.out.println("Reached get Leaderboard-------------------just reached");

		boolean checkedKey=this.leaderboardCacheClient.hasKey(entityType, property);
		if(!checkedKey)
		{
			System.out.println("Reached get Leaderboard--------------------if condition");
			//check in the database and set the cache
			Sort sort=Sort.by(property).descending();
			List<Wizard> sortedWizards=this.wizardRepository.findAll(sort);
			sortedWizards.stream().forEach(wizard -> {
				double score=CommonUtils.getScoreOfProperty(wizard, property);
				try {
					this.leaderboardCacheClient.saveEntityOfLeaderBoard(entityType, wizard.getId()+"", new ObjectMapper().writeValueAsString(this.wizardToWizardDtoConverter.convert(wizard)));
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
				this.leaderboardCacheClient.setScore(entityType, property, wizard.getId()+"",score);
			});
		}
	
		Set<ZSetOperations.TypedTuple<String>> ans= this.leaderboardCacheClient.getTop(entityType, property, limit);
		return ans.stream().map(tuple ->
		{
			 String json = (String) leaderboardCacheClient.getEntityOfLeaderboard(entityType, tuple.getValue());
		        try {
		            return new ObjectMapper().readValue(json, WizardDto.class);
		        } catch (JsonProcessingException e) {
		            throw new RuntimeException(e);
		        }
		}
		).collect(Collectors.toList());
	}
	
	


}
