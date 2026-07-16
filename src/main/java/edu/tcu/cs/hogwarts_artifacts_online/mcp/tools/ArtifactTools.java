package edu.tcu.cs.hogwarts_artifacts_online.mcp.tools;


import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import edu.tcu.cs.hogwarts_artifacts_online.artifact.Artifact;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.ArtifactService;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.converter.ArtifactDtoToArtifactConverter;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.converter.ArtifactToArtifactDtoConverter;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.dto.ArtifactDto;
import edu.tcu.cs.hogwarts_artifacts_online.mcp.dto.CreateArtifactRequestTool;
import edu.tcu.cs.hogwarts_artifacts_online.mcp.dto.UpdateArtifactRequestTool;
import edu.tcu.cs.hogwarts_artifacts_online.mcp.validation.McpRequestValidator;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ArtifactTools {

    private final ArtifactService artifactService;

    private final ArtifactDtoToArtifactConverter dtoToArtifactConverter;

    private final ArtifactToArtifactDtoConverter artifactToDtoConverter;

    private final McpRequestValidator validator;

    @Tool(
            name = "findArtifactById",
            description = "Find an artifact using its unique identifier.")
    public ArtifactDto findArtifactById(

            @ToolParam(description = "Unique identifier of the artifact.")
            String artifactId) {

        return artifactService.findById(artifactId);
    }

    @Tool(
            name = "createArtifact",
            description = "Create a new collectible artifact.")
    public ArtifactDto createArtifact(CreateArtifactRequestTool request) {

        validator.validate(request);

        ArtifactDto dto = new ArtifactDto(
                null,
                request.getName(),
                request.getDescription(),
                request.getImageUrl(),
                null);

        Artifact artifact = dtoToArtifactConverter.convert(dto);

        Artifact savedArtifact = artifactService.save(artifact);

        return artifactToDtoConverter.convert(savedArtifact);
    }

    @Tool(
            name = "updateArtifact",
            description = "Update an existing artifact.")
    public ArtifactDto updateArtifact(

            @ToolParam(description = "Artifact identifier.")
            String artifactId,

            UpdateArtifactRequestTool request) {

        validator.validate(request);

        ArtifactDto dto = new ArtifactDto(
                artifactId,
                request.getName(),
                request.getDescription(),
                request.getImageUrl(),
                null);

        return artifactService.update(artifactId, dto);
    }

    @Tool(
            name = "deleteArtifact",
            description = "Delete an artifact permanently.")
    public String deleteArtifact(

            @ToolParam(description = "Artifact identifier.")
            String artifactId) {

        artifactService.delete(artifactId);

        return "Artifact deleted successfully.";
    }



}