package edu.tcu.cs.hogwarts_artifacts_online.Wizard;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.tcu.cs.hogwarts_artifacts_online.Wizard.converter.WizardDtoToWizardConverter;
import edu.tcu.cs.hogwarts_artifacts_online.Wizard.converter.WizardToWizardDtoConverter;
import edu.tcu.cs.hogwarts_artifacts_online.Wizard.dto.WizardDto;
import edu.tcu.cs.hogwarts_artifacts_online.system.Result;
import edu.tcu.cs.hogwarts_artifacts_online.system.StatusCode;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/wizards")
public class WizardController {

	private final WizardService wizardService;
	private final WizardToWizardDtoConverter wizardToWizardDtoConverter;
	private final WizardDtoToWizardConverter wizardDtoToWizardConverter;

	public WizardController(WizardService wizardService, WizardToWizardDtoConverter wizardToWizardDtoConverter,
			WizardDtoToWizardConverter wizardDtoToWizardConverter) {
		super();
		this.wizardService = wizardService;
		this.wizardToWizardDtoConverter = wizardToWizardDtoConverter;
		this.wizardDtoToWizardConverter = wizardDtoToWizardConverter;
	}

	@GetMapping()
	public Result findAllWizard() {
		List<Wizard> foundWizards = wizardService.findAll();
		List<WizardDto> foundWizardDto = foundWizards.stream()
				.map(foundWizard -> this.wizardToWizardDtoConverter.convert(foundWizard)).collect(Collectors.toList());
		return new Result(true, StatusCode.SUCCESS, "Find All Success", foundWizardDto);
	}

	@PostMapping()
	public Result addWizard(@Valid @RequestBody WizardDto wizard) {
		Wizard newWizard = this.wizardDtoToWizardConverter.convert(wizard);
		Wizard savedWizard = this.wizardService.save(newWizard);
		WizardDto savedWizardDto = this.wizardToWizardDtoConverter.convert(savedWizard);
		return new Result(true, StatusCode.SUCCESS, "Add Success", savedWizardDto);

	}

	@GetMapping("/{wizardId}")
	public Result findWizardById(@PathVariable Integer wizardId) {
		Wizard foundWizard = this.wizardService.findById(wizardId);
		WizardDto foundWizardDto = this.wizardToWizardDtoConverter.convert(foundWizard);
		return new Result(true, StatusCode.SUCCESS, "Find One Success", foundWizardDto);
	}

	@PutMapping("/{wizardId}")
	public Result updateWizard(@Valid @RequestBody WizardDto updateWizard, @PathVariable Integer wizardId) {
		Wizard newWizard = this.wizardDtoToWizardConverter.convert(updateWizard);
		Wizard savedWizard = this.wizardService.updateWizard(wizardId, newWizard);
		WizardDto savedWizardDto = this.wizardToWizardDtoConverter.convert(savedWizard);
		return new Result(true, StatusCode.SUCCESS, "Update Success", savedWizardDto);
	}

	@DeleteMapping("/{wizardId}")
	public Result deleteWizard(@PathVariable int wizardId) {
		this.wizardService.deleteWizardById(wizardId);
		return new Result(true, StatusCode.SUCCESS, "Delete Success");
	}

	@PutMapping("/{wizardId}/artifacts/{artifactId}")
	public Result assigenArtifact(@PathVariable Integer wizardId, @PathVariable String artifactId) {
		this.wizardService.assignArtifact(wizardId, artifactId);
		return new Result(true,StatusCode.SUCCESS,"Artifact Assignment Success");
	}
	
	
	//Leaderboard controller
	
	@GetMapping("/leaderboard/artifacts")
	public Result getLeaderboard(@RequestParam(defaultValue="10") int limit)
	{
		List<Object> ans=this.wizardService.getLeaderboard("wizards","artifacts",limit);
		return new Result(true,StatusCode.SUCCESS,"Wizard Leaderboard",ans);
	
	}
	
}
