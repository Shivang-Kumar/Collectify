package edu.tcu.cs.hogwarts_artifacts_online.Wizard;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface WizardRepository extends JpaRepository<Wizard, Integer> {
	
	

}
