package edu.tcu.cs.hogwarts_artifacts_online.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import edu.tcu.cs.hogwarts_artifacts_online.Owner.Owner;
import edu.tcu.cs.hogwarts_artifacts_online.Owner.OwnerRepository;
import edu.tcu.cs.hogwarts_artifacts_online.Owner.OwnerService;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.Artifact;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.ArtifactRepository;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.utils.IdWorker;
import edu.tcu.cs.hogwarts_artifacts_online.system.ObjectNotFoundException;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles(value="dev")
public class WizardServiceTest {

	@Mock
	OwnerRepository wizardRepository;

	@Mock
	ArtifactRepository artifactRepository;

	@Mock
	IdWorker idWorker;

	@InjectMocks
	OwnerService wizardService;

	List<Owner> wizards;

	@BeforeEach
	void setUp() {
		Owner w1 = new Owner();
		w1.setName("Albus Dumbeldore");
		w1.setId(1213245645);

		Owner w2 = new Owner();
		w2.setName("Harry Potter");
		w2.setId(54654651);

		wizards = new ArrayList<Owner>();
		wizards.add(w1);
		wizards.add(w2);
	}

	@Test
	void testFindAllSuccess() {
		// given
		given(wizardRepository.findAll()).willReturn(this.wizards);

		// when

		List<Owner> actualWizards = wizardService.findAll();

		// then
		assertThat(actualWizards.size()).isEqualTo(this.wizards.size());
		verify(wizardRepository, times(1)).findAll();

	}

	@Test
	void testSaveSuccess() {

		List<Artifact> artifacts1 = new ArrayList<>();

		Artifact a1 = new Artifact();
		a1.setId("21232456489892566");
		a1.setName("Deluminator");
		a1.setDescription("A deluminator is a device invented by albus dumbeldore");
		a1.setImageUrl("ImageUrl");
		artifacts1.add(a1);

		Artifact a2 = new Artifact();
		a2.setId("31232456489892567");
		a2.setName("Invisibility Cloak");
		a2.setDescription("An invisibility cloak is a magical garment that makes the wearer invisible.");
		a2.setImageUrl("ImageUrl");
		artifacts1.add(a2);

		// Given
		Owner w = new Owner();
		w.setId(1);
		w.setName("Harry Potter");
		w.setArtifacts(artifacts1);

		given(idWorker.nextId()).willReturn((long) 1);
		given(wizardRepository.save(w)).willReturn(w);

		// when
		Owner savedWizard = wizardService.save(w);

		// then
		assertThat(savedWizard.getId()).isEqualTo(1);
		assertThat(savedWizard.getName()).isEqualTo("Harry Potter");
		assertThat(savedWizard.getNumberOfArtifacts()).isEqualTo(2);
		verify(wizardRepository, times(1)).save(w);

	}

	@Test
	void testFindByIdSuccess() {
		List<Artifact> artifacts1 = new ArrayList<>();

		Artifact a1 = new Artifact();
		a1.setId("21232456489892566");
		a1.setName("Deluminator");
		a1.setDescription("A deluminator is a device invented by albus dumbeldore");
		a1.setImageUrl("ImageUrl");
		artifacts1.add(a1);

		Artifact a2 = new Artifact();
		a2.setId("31232456489892567");
		a2.setName("Invisibility Cloak");
		a2.setDescription("An invisibility cloak is a magical garment that makes the wearer invisible.");
		a2.setImageUrl("ImageUrl");
		artifacts1.add(a2);

		Owner w = new Owner();
		w.setId(1);
		w.setName("Harry Potter");
		w.setArtifacts(artifacts1);

		// Given
		given(wizardRepository.findById(1)).willReturn(Optional.of(w));
		// When
		Owner foundWizard = wizardService.findById(1);
		// Then
		assertThat(foundWizard.getId()).isEqualTo(1);
		assertThat(foundWizard.getName()).isEqualTo("Harry Potter");
		assertThat(foundWizard.getNumberOfArtifacts()).isEqualTo(2);
		verify(wizardRepository, times(1)).findById(1);
	}

	@Test
	void testFindByIdNotFound() {
		// Given
		given(wizardRepository.findById(Mockito.any(Integer.class))).willReturn(Optional.empty());
		// When
		Throwable thrown = catchThrowable(() -> {
			Owner foundWizard = wizardService.findById(1);
		});
		// Then
		assertThat(thrown).isInstanceOf(ObjectNotFoundException.class)
				.hasMessage("Could not find owner with id 1  :(");
		verify(wizardRepository, times(1)).findById(1);
	}

	@Test
	void testUpdateWizardSuccess() {
		List<Artifact> artifacts1 = new ArrayList<>();

		Artifact a1 = new Artifact();
		a1.setId("21232456489892566");
		a1.setName("Deluminator");
		a1.setDescription("A deluminator is a device invented by albus dumbeldore");
		a1.setImageUrl("ImageUrl");
		artifacts1.add(a1);

		Artifact a2 = new Artifact();
		a2.setId("31232456489892567");
		a2.setName("Invisibility Cloak");
		a2.setDescription("An invisibility cloak is a magical garment that makes the wearer invisible.");
		a2.setImageUrl("ImageUrl");
		artifacts1.add(a2);

		Owner w = new Owner();
		w.setId(1);
		w.setName("Harry Potter");
		w.setArtifacts(artifacts1);

		Owner updatedWizard = new Owner();
		updatedWizard.setId(1);
		updatedWizard.setName("Komko Sirus");
		updatedWizard.setArtifacts(artifacts1);

		// Given

		given(wizardRepository.findById(1)).willReturn(Optional.of(w));
		given(wizardRepository.save(w)).willReturn(updatedWizard);

		// When
		Owner savedWizard = this.wizardService.updateOwner(1, updatedWizard);

		// Then

		assertThat(savedWizard.getId()).isEqualTo(1);
		assertThat(savedWizard.getName()).isEqualTo("Komko Sirus");
		verify(wizardRepository, times(1)).save(w);
	}

	@Test
	void testUpdateWizardNotFound() {

		List<Artifact> artifacts1 = new ArrayList<>();
		Artifact a1 = new Artifact();
		a1.setId("21232456489892566");
		a1.setName("Deluminator");
		a1.setDescription("A deluminator is a device invented by albus dumbeldore");
		a1.setImageUrl("ImageUrl");

		artifacts1.add(a1);

		Artifact a2 = new Artifact();
		a2.setId("31232456489892567");
		a2.setName("Invisibility Cloak");
		a2.setDescription("An invisibility cloak is a magical garment that makes the wearer invisible.");
		a2.setImageUrl("ImageUrl");
		artifacts1.add(a2);

		Owner w = new Owner();
		w.setId(1);
		w.setName("Harry Potter");
		w.setArtifacts(artifacts1);

		// given
		given(this.wizardRepository.findById(1)).willReturn(Optional.empty());

		// when
		assertThrows(ObjectNotFoundException.class, () -> {

			Owner savedWizard = this.wizardService.updateOwner(1, w);
		});

		// then
		verify(wizardRepository, times(1)).findById(1);

	}

	@Test
	void testDeleteByIdSuccess() {

		List<Artifact> artifacts1 = new ArrayList<>();
		Artifact a1 = new Artifact();
		a1.setId("21232456489892566");
		a1.setName("Deluminator");
		a1.setDescription("A deluminator is a device invented by albus dumbeldore");
		a1.setImageUrl("ImageUrl");

		artifacts1.add(a1);

		Artifact a2 = new Artifact();
		a2.setId("31232456489892567");
		a2.setName("Invisibility Cloak");
		a2.setDescription("An invisibility cloak is a magical garment that makes the wearer invisible.");
		a2.setImageUrl("ImageUrl");
		artifacts1.add(a2);

		Owner w = new Owner();
		w.setId(1);
		w.setName("Harry Potter");
		w.setArtifacts(artifacts1);

		// given
		given(this.wizardRepository.findById(1)).willReturn(Optional.of(w));
		doNothing().when(this.wizardRepository).deleteById(1);

		// when
		this.wizardService.deleteOwnerById(1);

		// then
		verify(wizardRepository, times(1)).deleteById(1);

	}

	@Test
	void testDeleteNotFound() {
		// given

		given(wizardRepository.findById(1)).willReturn(Optional.empty());

		// Above method will throw optional so belowe method is not required as it will
		// not be executed
		// doNothing().when(wizardRepository).deleteById(1);

		// when
		assertThrows(ObjectNotFoundException.class, () -> {
			wizardService.deleteOwnerById(1);
		});
		// then
		verify(wizardRepository, times(1)).findById(1);

	}

	@Test
	void assignArtifactToWizardSuccess() {

		Artifact a1 = new Artifact();
		a1.setId("21232456489892566");
		a1.setName("Deluminator");
		a1.setDescription("A deluminator is a device invented by albus dumbeldore");
		a1.setImageUrl("ImageUrl");

		Owner w = new Owner();
		w.setId(1);
		w.setName("Harry Potter");

		// Given
		given(this.artifactRepository.findById("1")).willReturn(Optional.of(a1));
		given(this.wizardRepository.findById(3)).willReturn(Optional.of(w));

		// When
		this.wizardService.assignArtifactToOwner("1", 3);

		// Then
		verify(wizardRepository, times(1)).findById(3);
		verify(wizardRepository, times(1)).save(w);
		verify(artifactRepository, times(1)).findById("1");
		verify(artifactRepository, times(1)).save(a1);

	}
	
	@Test
	void testAssignArtifactSuccess() {
		//Given
		Artifact a1 = new Artifact();
		a1.setId("21232456489892566");
		a1.setName("Deluminator");
		a1.setDescription("A deluminator is a device invented by albus dumbeldore");
		a1.setImageUrl("ImageUrl");
		
		Owner w2=new Owner();
		w2.setId(2);
		w2.setName("Harry Potter");
		w2.addArtifact(a1);
		
		Owner w3=new Owner();
		w3.setId(3);
		w3.setName("Naville Longbottom");
		
		given(this.artifactRepository.findById("21232456489892566")).willReturn(Optional.of(a1));
		given(this.wizardRepository.findById(3)).willReturn(Optional.of(w3));
		
		//When
		this.wizardService.assignArtifact(3, "21232456489892566");
		//Then
		assertThat(a1.getOwner().getId()).isEqualTo(3);
		assertThat(w3.getArtifacts()).contains(a1);
	}
	
	@Test
	void testAssignArtifactErrorWithNonExistentWizardId() {
		//Given
		Artifact a1 = new Artifact();
		a1.setId("21232456489892566");
		a1.setName("Deluminator");
		a1.setDescription("A deluminator is a device invented by albus dumbeldore");
		a1.setImageUrl("ImageUrl");
		
		Owner w2=new Owner();
		w2.setId(2);
		w2.setName("Harry Potter");
		w2.addArtifact(a1);
		
	
		
		given(this.artifactRepository.findById("21232456489892566")).willReturn(Optional.of(a1));
		given(this.wizardRepository.findById(3)).willReturn(Optional.empty());
		
		//When
		Throwable thrown=assertThrows(ObjectNotFoundException.class,() -> {
			this.wizardService.assignArtifact(3, "21232456489892566");
		});
		//Then
		assertThat(thrown).isInstanceOf(ObjectNotFoundException.class).hasMessage("Could not find owner with id 3  :(");
		assertThat(a1.getOwner().getId()).isEqualTo(2);
	}
	
	@Test
	void testAssignArtifactErrorWithNonExistentArtifactId() {
		
		given(this.artifactRepository.findById("21232456489892566")).willReturn(Optional.empty());
		
		//When
		Throwable thrown=assertThrows(ObjectNotFoundException.class,() -> {
			this.wizardService.assignArtifact(3, "21232456489892566");
		});		//Then
		assertThat(thrown).isInstanceOf(ObjectNotFoundException.class).hasMessage("Could not find artifact with id 21232456489892566  :(");
	}

}
