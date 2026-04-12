package edu.tcu.cs.hogwarts_artifacts_online.system;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import edu.tcu.cs.hogwarts_artifacts_online.Owner.Owner;
import edu.tcu.cs.hogwarts_artifacts_online.Owner.OwnerRepository;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.Artifact;
import edu.tcu.cs.hogwarts_artifacts_online.artifact.ArtifactRepository;
import edu.tcu.cs.hogwarts_artifacts_online.user.User;
import edu.tcu.cs.hogwarts_artifacts_online.user.UserService;



@Component
@Profile("dev")
public class DbDataInitializer implements CommandLineRunner {
	
	private final ArtifactRepository artifactRepository;
	
	private final OwnerRepository wizardRepository; 
	
	private final UserService userService;
	
	




	public DbDataInitializer(ArtifactRepository artifactRepository, OwnerRepository wizardRepository,
			UserService userService) {
		super();
		this.artifactRepository = artifactRepository;
		this.wizardRepository = wizardRepository;
		this.userService = userService;
	}






	@Override
	public void run(String... args) throws Exception {

		Artifact a1 = new Artifact();
		a1.setId("21232456489892566");
		a1.setName("Deluminator");
		a1.setDescription("A deluminator is a device invented by Albus Dumbledore");
		a1.setImageUrl("ImageUrl");

		Artifact a2 = new Artifact();
		a2.setId("31232456489892567");
		a2.setName("Invisibility Cloak");
		a2.setDescription("An invisibility cloak is a magical garment that makes the wearer invisible.");
		a2.setImageUrl("ImageUrl");

		Artifact a3 = new Artifact();
		a3.setId("41232456489892568");
		a3.setName("Elder Wand");
		a3.setDescription("The Elder Wand is the most powerful wand in existence.");
		a3.setImageUrl("ImageUrl");

		Artifact a4 = new Artifact();
		a4.setId("51232456489892569");
		a4.setName("Resurrection Stone");
		a4.setDescription("The Resurrection Stone can bring back spirits from the dead.");
		a4.setImageUrl("ImageUrl");

		Artifact a5 = new Artifact();
		a5.setId("61232456489892570");
		a5.setName("Sorcerer's Stone");
		a5.setDescription("The Sorcerer's Stone can turn any metal into pure gold and produces the Elixir of Life.");
		a5.setImageUrl("ImageUrl");

		Artifact a6 = new Artifact();
		a6.setId("71232456489892571");
		a6.setName("Marauder's Map");
		a6.setDescription("The Marauder's Map shows every inch of Hogwarts and the location of everyone in it.");
		a6.setImageUrl("ImageUrl");

		Owner w1 = new Owner();
		w1.setId(1);
		w1.setName("Albus Dumbledore");
		w1.addArtifact(a1);
		w1.addArtifact(a3);


		Owner w2 = new Owner();
		w2.setId(2);
		w2.setName("Harry Potter");
		w2.addArtifact(a2);
		w2.addArtifact(a4);


		Owner w3 = new Owner();
		w3.setId(3);
		w3.setName("Hermione Granger");
		w3.addArtifact(a5);
		
		
		
		wizardRepository.save(w1);
		wizardRepository.save(w2);
		wizardRepository.save(w3);
		artifactRepository.save(a6);
		
		
		
		User u1 = new User();
		u1.setUsername("ABC_1");
		u1.setRoles("admin");
		u1.setEnabled(true);
		u1.setPassword("jkc");

		User u2 = new User();
		u2.setUsername("ABC_2");
		u2.setRoles("user");
		u2.setEnabled(true);
		u2.setPassword("jhk");
		
		this.userService.addUser(u1);
		this.userService.addUser(u2);

		

	}

}
