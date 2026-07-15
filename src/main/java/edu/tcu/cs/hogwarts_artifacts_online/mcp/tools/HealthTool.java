package edu.tcu.cs.hogwarts_artifacts_online.mcp.tools;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;




@Component
public class HealthTool {

    @Tool(description = "Checks whether the Collectify MCP server is running")
    public String health() {
        return "Collectify MCP Server Running";
    }
}
