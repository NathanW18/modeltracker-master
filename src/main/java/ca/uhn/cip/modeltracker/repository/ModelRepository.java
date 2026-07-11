package ca.uhn.cip.modeltracker.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ca.uhn.cip.modeltracker.model.Model;
import ca.uhn.cip.modeltracker.model.ModelId;


/**
 * Spring data repository class for Model.
 * 
 * @author Kevin Wang
 *
 */

@Repository
public interface ModelRepository extends PagingAndSortingRepository<Model, ModelId> {

    /**
	 * 
	 * @param specimen_id
	 * @param worksheet_inst
	 * @return
	 */
	@Query("SELECT mdl " 
    + "FROM Model mdl " 
    + "WHERE mdl.modelIdentifier = :modelIdentifier "
    + "AND mdl.studyShortName = :studyShortName ")
    @Transactional(readOnly = true)
    public List<Model> findByModelIdentifierAndStudyName(
        @Param("modelIdentifier") String modelIdentifier,
        @Param("studyShortName") String studyShortName);

    /**
	 * 
	 * @param specimen_id
	 * @param worksheet_inst
	 * @return
	 */
	@Query("SELECT COUNT (DISTINCT mdl.protocolParticipantID) " 
    + "FROM Model mdl ")
    @Transactional(readOnly = true)
    public long countByPPID();

    /**
	 * 
	 * @param specimen_id
	 * @param worksheet_inst
	 * @return
	 */
	@Query("DELETE " 
    + "FROM Model mdl " 
    + "WHERE mdl.modelDisplayName = :modelDisplayName ")
    @Transactional
    @Modifying
    public void deleteByModelDisplayName(
        @Param("modelDisplayName") String modelDisplayName);
	
	@Query("SELECT DISTINCT mdl.protocolParticipantID " 
		    + "FROM Model mdl WHERE mdl.protocolParticipantID LIKE :ppid ")
	@Transactional(readOnly = true)
	public List<String> findPPIDsByProtocolParticipantIDIgnoreCaseContaining(@Param("ppid") String ppid);
	
	public List<Model> findByModelDisplayName(String modelDisplayName);

	public List<Model> findByModelDisplayNameIgnoreCase(String modelDisplayName);

	public List<Model> findByModelDisplayNameIgnoreCaseContaining(String modelDisplayName);

	public List<Model> findByProtocolParticipantIDIgnoreCase(String ppid);
	
	public List<Model> findByProtocolParticipantIDIgnoreCaseContaining(String ppid);

	public List<Model> findByProtocolParticipantIDIn(List<String> ppids);

	public List<Model> findByStudyShortName(String studyId);
	
	// pagination
	public Page<Model> findByModelDisplayNameIgnoreCase(String modelDisplayName, Pageable pageable);

	public Page<Model> findByModelDisplayNameIgnoreCaseContaining(String modelDisplayName, Pageable pageable);

	public Page<Model> findByProtocolParticipantIDIgnoreCaseContaining(String ppid, Pageable pageable);

	public Page<Model> findByProtocolParticipantIDIn(List<String> ppids, Pageable pageable);

}
