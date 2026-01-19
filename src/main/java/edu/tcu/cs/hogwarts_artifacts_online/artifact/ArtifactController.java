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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;


import edu.tcu.cs.hogwarts_artifacts_online.artifact.DTO.ArtifactDto;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.converter.ArtifactDtoToArtifactConverter;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.converter.ArtifactToArtifactDtoConverter;
import edu.tcu.cs.hogwarts_artifacts_online.system.Result;
import edu.tcu.cs.hogwarts_artifacts_online.system.StatusCode;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("${api.endpoint.base-url}/artifacts")
@Tag(name = "Artifact Management", description = "Operations related to Hogwarts artifacts")
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
	    @Operation(
	        summary = "Get artifact by ID",
	        description = "Retrieve a single artifact using its unique ID"
	    )
	    @ApiResponses({
	        @ApiResponse(responseCode = "200", description = "Artifact found",
	            content = @Content(schema = @Schema(implementation = Result.class))),
	        @ApiResponse(responseCode = "404", description = "Artifact not found"),
	        @ApiResponse(responseCode = "500", description = "Server error")
	    })
	public Result findArtifactById(@PathVariable String artifactId) {
		ArtifactDto foundArtifactDto = this.artifactService.findById(artifactId);
		meterRegistry.counter("artifacd.id."+artifactId).increment();
		return new Result(true, StatusCode.SUCCESS, "Find one Success", foundArtifactDto);
	}

	@GetMapping
    @Operation(summary = "Get all artifacts", description = "Retrieve artifacts with pagination")
	public Result findAllArtifact(Pageable pageable) {

		Page<Artifact> foundArtifactsPage = this.artifactService.findAll(pageable);
		// Convert Found Artifacts to Page of ArtifactDtos
		Page<ArtifactDto> artifactDtoPage = foundArtifactsPage
				.map(foundArtifact -> this.artifactDtoConverter.convert(foundArtifact));
		return new Result(true, StatusCode.SUCCESS, "Find All Success", artifactDtoPage);
	}

	@PostMapping
    @Operation(summary = "Update an artifact")
	public Result addArtifact(@Valid   @RequestBody ArtifactDto artifactDto) {
		Artifact newArtifact = this.artifactDtoToArtifactConverter.convert(artifactDto);
		Artifact savedArtifact = this.artifactService.save(newArtifact);
		ArtifactDto savedArtifactDto = this.artifactDtoConverter.convert(savedArtifact);
		return new Result(true, StatusCode.SUCCESS, "Add Success", savedArtifactDto);
	}
	
	@PutMapping("/{artifactId}")
	public Result updateArtifact(@Parameter(description = "Artifact data", required = true)
			@PathVariable  String artifactId,@Valid @RequestBody ArtifactDto updateArtifactDto) {
		
		ArtifactDto updatedArtifactDto=this.artifactService.update(artifactId, updateArtifactDto);
		return new Result(true,StatusCode.SUCCESS,"Update Success",updatedArtifactDto);		
	}
	
	
	@DeleteMapping("/{artifactId}")
	public Result deleteArtifact(@PathVariable String artifactId)
	{  this.artifactService.delete(artifactId);
		return new Result(true,StatusCode.SUCCESS,"Delete Success");
	}
	 
	
	@GetMapping("/summary")
    @Operation(summary = "Summarize artifacts")
    public Result summarizeArtifact() throws Exception {
		List<Artifact> foundArtifacts=this.artifactService.findAll();
		
		List<ArtifactDto> artifactDtos=foundArtifacts.stream().map(found -> this.artifactDtoConverter.convert(found)).collect(Collectors.toList());
		String artifactSummary=this.artifactService.summarize(artifactDtos);
	     return new Result(true,StatusCode.SUCCESS,"Summarize Success",artifactSummary);
	}
	
//		Artifact Leaderboard controller we can pass any property later on 
//		@GetMapping("/leaderboard/{property}")
//		public Result getLeaderboard(@PathVariable property,@RequestParam(defaultValue="10") int limit)
//		{
//			List<Object> ans=this.artifactService.getLeaderboard("artifacts",property,limit);
//			return new Result(true,StatusCode.SUCCESS,"Artifact Leaderboard",ans);
//		
//		}

}
