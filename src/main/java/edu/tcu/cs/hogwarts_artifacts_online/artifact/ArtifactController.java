package edu.tcu.cs.hogwarts_artifacts_online.artifact;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.tcu.cs.hogwarts_artifacts_online.artifact.DTO.ArtifactDto;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.converter.ArtifactDtoToArtifactConverter;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.converter.ArtifactToArtifactDtoConverter;
import edu.tcu.cs.hogwarts_artifacts_online.system.Result;
import edu.tcu.cs.hogwarts_artifacts_online.system.StatusCode;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;

@RestController
@RequestMapping("${api.endpoint.base-url}/artifacts")
public class ArtifactController {

	private final ArtifactService artifactService;
	private final ArtifactToArtifactDtoConverter artifactDtoConverter;
	private final ArtifactDtoToArtifactConverter artifactDtoToArtifactConverter;
	private final MeterRegistry meterRegistry;

	public ArtifactController(ArtifactService artifactService, ArtifactToArtifactDtoConverter artifactDtoConverter,
			ArtifactDtoToArtifactConverter artifactDtoToArtifactConverter,MeterRegistry meterRegistry) {
		super();
		this.artifactService = artifactService;
		this.artifactDtoConverter = artifactDtoConverter;
		this.artifactDtoToArtifactConverter = artifactDtoToArtifactConverter;
		this.meterRegistry = meterRegistry;
	}

	@GetMapping("/{artifactId}")
	public Result findArtifactById(@PathVariable String artifactId) {
		Artifact foundArtifact = this.artifactService.findById(artifactId);
		meterRegistry.counter("artifacd.id."+artifactId).increment();
		ArtifactDto artifactDto = this.artifactDtoConverter.convert(foundArtifact);
		return new Result(true, StatusCode.SUCCESS, "Find one Success", artifactDto);
	}

	@GetMapping
	public Result findAllArtifact(Pageable pageable) {

		Page<Artifact> foundArtifactsPage = this.artifactService.findAll(pageable);
		// Convert Found Artifacts to Page of ArtifactDtos
		Page<ArtifactDto> artifactDtoPage = foundArtifactsPage
				.map(foundArtifact -> this.artifactDtoConverter.convert(foundArtifact));
		return new Result(true, StatusCode.SUCCESS, "Find All Success", artifactDtoPage);
	}

	@PostMapping
	public Result addArtifact(@Valid   @RequestBody ArtifactDto artifactDto) {
		Artifact newArtifact = this.artifactDtoToArtifactConverter.convert(artifactDto);
		Artifact savedArtifact = this.artifactService.save(newArtifact);
		ArtifactDto savedArtifactDto = this.artifactDtoConverter.convert(savedArtifact);
		return new Result(true, StatusCode.SUCCESS, "Add Success", savedArtifactDto);
	}
	
	@PutMapping("/{artifactId}")
	public Result updateArtifact( @PathVariable  String artifactId,@Valid @RequestBody ArtifactDto artifactDto) {
		Artifact update=this.artifactDtoToArtifactConverter.convert(artifactDto);
		Artifact updatedArtifact=this.artifactService.update(artifactId, update);
		ArtifactDto updatedArtifactDto=this.artifactDtoConverter.convert(updatedArtifact);
		return new Result(true,StatusCode.SUCCESS,"Update Success",updatedArtifactDto);
		
		
	}
	
	
	@DeleteMapping("/{artifactId}")
	public Result deleteArtifact(@PathVariable String artifactId)
	{  this.artifactService.delete(artifactId);
		return new Result(true,StatusCode.SUCCESS,"Delete Success");
	}
	
	
	@GetMapping("/summary")
    public Result summarizeArtifact() throws Exception {
		List<Artifact> foundArtifacts=this.artifactService.findAll();
		
		List<ArtifactDto> artifactDtos=foundArtifacts.stream().map(found -> this.artifactDtoConverter.convert(found)).collect(Collectors.toList());
		String artifactSummary=this.artifactService.summarize(artifactDtos);
	     return new Result(true,StatusCode.SUCCESS,"Summarize Success",artifactSummary);
	}

}
