package ca.uhn.cip.modeltracker.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ca.uhn.cip.modeltracker.model.Study;

/**
 * Spring data repository class for study.
 * 
 * @author Kevin Wang
 *
 */

@Repository
public interface StudyRepository extends CrudRepository<Study, String> {
    
    List<Study> findByStudyShortNameIgnoreCase(String studyShortName);

    List<Study> findByStudyShortNameIgnoreCaseContaining(String studyShortName);

    List<Study> findByPrincipleInvestigatorIgnoreCase(String principleInvestigator);

    List<Study> findByPrincipleInvestigatorIgnoreCaseContaining(String principleInvestigator);
}
