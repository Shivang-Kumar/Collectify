package edu.tcu.cs.hogwarts_artifacts_online.mcp.dto;


import org.springframework.ai.tool.annotation.ToolParam;

import jakarta.validation.constraints.NotBlank;

public class CreateArtifactRequestTool {

    @ToolParam(description = "Name of the collectible artifact.")
    @NotBlank(message = "Artifact name is required.")
    private String name;

    @ToolParam(description = "Description of the collectible artifact.")
    @NotBlank(message = "Artifact description is required.")
    private String description;

    @ToolParam(description = "Public image URL of the artifact.")
    @NotBlank(message = "Artifact image URL is required.")
    private String imageUrl;

    public CreateArtifactRequestTool() {
    }

    public CreateArtifactRequestTool(String name, String description, String imageUrl) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}