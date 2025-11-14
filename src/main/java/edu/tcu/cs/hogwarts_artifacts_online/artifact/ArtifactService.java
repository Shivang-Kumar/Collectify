package edu.tcu.cs.hogwarts_artifacts_online.artifact;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tcu.cs.hogwarts_artifacts_online.artifact.DTO.ArtifactDto;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.utils.IdWorker;
import edu.tcu.cs.hogwarts_artifacts_online.client.ai.chat.ChatClient;
import edu.tcu.cs.hogwarts_artifacts_online.client.ai.chat.dto.ChatRequest;
import edu.tcu.cs.hogwarts_artifacts_online.client.ai.chat.dto.ChatResponse;
import edu.tcu.cs.hogwarts_artifacts_online.client.ai.chat.dto.Content;
import edu.tcu.cs.hogwarts_artifacts_online.client.ai.chat.dto.Part;
import edu.tcu.cs.hogwarts_artifacts_online.system.ObjectNotFoundException;
import io.micrometer.observation.annotation.Observed;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class ArtifactService {

	private final ArtifactRepository artifactRepository;

	private final ChatClient chatClient;

	private final IdWorker idWorker;

	@Autowired
	ObjectMapper objectMapper;

	public ArtifactService(ArtifactRepository artifactRepository, IdWorker idWorker, ChatClient chatClient) {
		super();
		this.artifactRepository = artifactRepository;
		this.chatClient = chatClient;
		this.idWorker = idWorker;
	}

	@Observed(name = "artifact", contextualName = "findByIdService")
	public Artifact findById(String artifactId) {
		return this.artifactRepository.findById(artifactId)
				.orElseThrow(() -> new ObjectNotFoundException("artifact", artifactId));

	}

	public List<Artifact> findAll() {
		return this.artifactRepository.findAll();
	}

	public Artifact save(Artifact newArtifact) {

		newArtifact.setId(idWorker.nextId() + "");
		return this.artifactRepository.save(newArtifact);

	}

	public Artifact update(String artifactId, Artifact update) {

		return this.artifactRepository.findById(artifactId).map(oldArtifact -> {
			oldArtifact.setName(update.getName());
			oldArtifact.setDescription(update.getDescription());
			oldArtifact.setImageUrl(update.getImageUrl());
			return this.artifactRepository.save(oldArtifact);

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
}
