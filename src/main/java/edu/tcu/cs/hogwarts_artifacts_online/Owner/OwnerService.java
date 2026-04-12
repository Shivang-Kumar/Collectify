package edu.tcu.cs.hogwarts_artifacts_online.Owner;

import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tcu.cs.hogwarts_artifacts_online.Owner.converter.OwnerToOwnerDtoConverter;
import edu.tcu.cs.hogwarts_artifacts_online.OwnerDto.dto.OwnerDto;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.Artifact;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.ArtifactRepository;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.utils.CommonUtils;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.utils.IdWorker;
import edu.tcu.cs.hogwarts_artifacts_online.rediscache.RedisLeaderboardCacheClient;
import edu.tcu.cs.hogwarts_artifacts_online.system.ObjectNotFoundException;

@Service
public class OwnerService {

	private final OwnerRepository ownerRepository;
	private final IdWorker idWorker;
	private final ArtifactRepository artifactRepository;
	private final RedisLeaderboardCacheClient leaderboardCacheClient;
	private final OwnerToOwnerDtoConverter ownerToOwnerDtoConverter;
	

	public OwnerService(OwnerRepository ownerRepository, IdWorker idWorker, ArtifactRepository artifactRepository,RedisLeaderboardCacheClient redisLeaderboardCacheClient,OwnerToOwnerDtoConverter ownerToOwnerDtoConverter) {
		super();
		this.ownerRepository = ownerRepository;
		this.idWorker = idWorker;
		this.artifactRepository = artifactRepository;
		this.leaderboardCacheClient=redisLeaderboardCacheClient;
		this.ownerToOwnerDtoConverter=ownerToOwnerDtoConverter;
	}

	public List<Owner> findAll() {
		List<Owner> owners = ownerRepository.findAll();
		return owners;
	}

	public Owner save(Owner owner) {
		owner.setId((int) idWorker.nextId());
		return ownerRepository.save(owner);
	}

	public Owner findById(Integer ownerId) {
		Owner foundOwner = this.ownerRepository.findById(ownerId)
				.orElseThrow(() -> new ObjectNotFoundException("owner", ownerId));

		return foundOwner;
	}

	public Owner updateOwner(Integer ownerId, Owner owner) {
		Owner updatedOwner = this.ownerRepository.findById(ownerId).map(foundOwner -> {
			foundOwner.setName(owner.getName());
			return this.ownerRepository.save(foundOwner);
		}).orElseThrow(() -> new ObjectNotFoundException("owner", ownerId));

		return updatedOwner;
	}

	public void deleteOwnerById(int ownerId) {
		Owner foundOwner = this.ownerRepository.findById(ownerId)
				.orElseThrow(() -> new ObjectNotFoundException("owner", ownerId));

		// Before the deletion we will unassign this owner artifact
		foundOwner.removeAllArtifacts();
		this.ownerRepository.deleteById(ownerId);
	}

	public void assignArtifactToOwner(String artifactID, Integer ownerId) {

		Owner foundOwner = this.ownerRepository.findById(ownerId)
				.orElseThrow(() -> new ObjectNotFoundException("owner", ownerId));
		Artifact foundArtifact = this.artifactRepository.findById(artifactID)
				.orElseThrow(() -> new ObjectNotFoundException("artifact", artifactID));

		foundOwner.getArtifacts().add(foundArtifact);
		foundArtifact.setOwner(foundOwner);
		ownerRepository.save(foundOwner);
		artifactRepository.save(foundArtifact);
	}
	
	public void assignArtifact(Integer ownerId,String artifactId)
	{
		Artifact foundArtifact=this.artifactRepository.findById(artifactId).orElseThrow(() -> new ObjectNotFoundException("artifact", artifactId));
		Owner foundOwner=this.ownerRepository.findById(ownerId).orElseThrow(() -> new ObjectNotFoundException("owner", ownerId));
		
		//Artifact assignment
		//We need to see if artifact is already owned by some owner
		if(foundArtifact.getOwner()!=null)
		{
			foundArtifact.getOwner().removeArtifact(foundArtifact);
		}
		foundOwner.addArtifact(foundArtifact);
		this.artifactRepository.save(foundArtifact);
		this.ownerRepository.save(foundOwner);
		
		
	}

	public List<Object> getLeaderboard(String entityType, String property, int limit) {

		boolean checkedKey=this.leaderboardCacheClient.hasKey(entityType, property);
		if(!checkedKey)
		{
			System.out.println("Reached get Leaderboard--------------------if condition");
			//check in the database and set the cache
			Sort sort=Sort.by(property).descending();
			List<Owner> sortedOwners=this.ownerRepository.findAll(sort);
			sortedOwners.stream().forEach( owner -> {
				double score=CommonUtils.getScoreOfProperty(owner, property);
				try {
					this.leaderboardCacheClient.saveEntityOfLeaderBoard(entityType, owner.getId()+"", new ObjectMapper().writeValueAsString(this.ownerToOwnerDtoConverter.convert(owner)));
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
				this.leaderboardCacheClient.setScore(entityType, property, owner.getId()+"",score);
			});
		}
	
		Set<ZSetOperations.TypedTuple<String>> ans= this.leaderboardCacheClient.getTop(entityType, property, limit);
		return ans.stream().map(tuple ->
		{
			 String json = (String) leaderboardCacheClient.getEntityOfLeaderboard(entityType, tuple.getValue());
		        try {
		            return new ObjectMapper().readValue(json, OwnerDto.class);
		        } catch (JsonProcessingException e) {
		            throw new RuntimeException(e);
		        }
		}
		).collect(Collectors.toList());
	}

	public long getOwnerRank(String entityType, String property, String ownerId) {
		return this.leaderboardCacheClient.getEntityRank(entityType, property, ownerId);
	}
	
	


}
