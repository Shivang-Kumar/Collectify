package edu.tcu.cs.hogwarts_artifacts_online.Owner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.tcu.cs.hogwarts_artifacts_online.artifact.Artifact;
import jakarta.annotation.Generated;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Owner  implements Serializable{
	
	@Id
	//@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	private String name;
	 
	
	@OneToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REMOVE},mappedBy= "owner")
	private List<Artifact> artifacts=new ArrayList<>();
	
	public Owner() {
		super();
	}

	public List<Artifact> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(List<Artifact> artifacts) {
		this.artifacts = artifacts;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addArtifact(Artifact artifact) {
		artifact.setOwner(this);
		this.artifacts.add(artifact);
		
	}

	public Integer getNumberOfArtifacts() {
		
		return this.artifacts.size();
	}
	
	public void removeAllArtifacts() {
		this.artifacts.stream().forEach(artifact -> artifact.setOwner(null));
		this.artifacts=null;
	}

	public void removeArtifact(Artifact foundArtifact) {

		//Remove artifact owner
	   foundArtifact.setOwner(null);
	   this.artifacts.remove(foundArtifact);
	}
	
	
	

}
