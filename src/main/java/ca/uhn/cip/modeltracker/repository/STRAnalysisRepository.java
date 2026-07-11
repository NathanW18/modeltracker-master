package ca.uhn.cip.modeltracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ca.uhn.cip.modeltracker.model.DataCount;
import ca.uhn.cip.modeltracker.model.STRAnalysis;
import ca.uhn.cip.modeltracker.model.STRAnalysisId;

/**
 * Spring data repository class for STRAnalysis.
 * 
 * @author Kevin Wang
 *
 */

@Repository
public interface STRAnalysisRepository extends PagingAndSortingRepository<STRAnalysis, STRAnalysisId>{
	
    /**
	 * 
	 * @param sampleId
	 * @return
	 */
	@Query("SELECT str " 
			+ "FROM STRAnalysis str " 
			+ "JOIN FETCH str.sample sample "
			+ "WHERE sample.sampleDescriptor = :sampleId ")
    @Transactional(readOnly = true)
	public List<STRAnalysis> findBySampleIdIgnoreCase(String sampleId);

	/**
	 * 
	 * @param sampleId
	 * @return
	 */
	@Query("SELECT str " 
			+ "FROM STRAnalysis str " 
			+ "JOIN FETCH str.sample sample "
			+ "WHERE sample.sampleDescriptor LIKE :sampleId ESCAPE '@' ")
    @Transactional(readOnly = true)
	public List<STRAnalysis> findBySampleIdIgnoreCaseContaining(String sampleId);

    /**
	 * 
	 * @param modelId
	 * @return
	 */
	@Query("SELECT str " 
			+ "FROM STRAnalysis str " 
			+ "JOIN FETCH str.sample sample "
			+ "WHERE sample.modelDisplayName = :modelId ")
    @Transactional(readOnly = true)
	public List<STRAnalysis> findByModelId(String modelId);

	/**
	 * 
	 * @param modelId
	 * @return
	 */
	@Query("SELECT str " 
			+ "FROM STRAnalysis str " 
			+ "JOIN FETCH str.sample sample "
			+ "WHERE sample.modelDisplayName = :modelId ")
    @Transactional(readOnly = true)
	public List<STRAnalysis> findByModelIdIgnoreCase(String modelId);

	/**
	 * 
	 * @param modelId
	 * @return
	 */
	@Query("SELECT str " 
			+ "FROM STRAnalysis str " 
			+ "JOIN FETCH str.sample sample "
			+ "WHERE sample.modelDisplayName LIKE :modelId ESCAPE '@' ")
    @Transactional(readOnly = true)
	public List<STRAnalysis> findByModelIdIgnoreCaseContaining(String modelId);

    /**
	 * 
	 * @param ppid
	 * @return
	 */
	@Query("SELECT str " 
			+ "FROM STRAnalysis str " 
			+ "JOIN FETCH str.sample sample "
			+ "WHERE sample.protocolParticipantID = :ppid ")
    @Transactional(readOnly = true)
	public List<STRAnalysis> findByPPIdIgnoreCase(String ppid);

	/**
	 * 
	 * @param ppid
	 * @return
	 */
	@Query("SELECT str " 
			+ "FROM STRAnalysis str " 
			+ "JOIN FETCH str.sample sample "
			+ "WHERE sample.protocolParticipantID LIKE :ppid ESCAPE '@' ")
    @Transactional(readOnly = true)
	public List<STRAnalysis> findByPPIdIgnoreCaseContaining(String ppid);

	/**
	 * 
	 * @param ppid
	 * @return
	 */
	@Query("SELECT str " 
			+ "FROM STRAnalysis str " 
			+ "JOIN FETCH str.sample sample "
			+ "WHERE sample.protocolParticipantID IN :ppids ")
    @Transactional(readOnly = true)
	public List<STRAnalysis> findByPPIdIn(List<String> ppids);

	public List<STRAnalysis> findBySampleDescriptorIgnoreCase(String sampleDescriptor);
	
	public List<STRAnalysis> findBySampleDescriptorIgnoreCaseContaining(String sampleDescriptor);

	public List<STRAnalysis> findByIsReferenceStrTrue();

	public Optional<STRAnalysis> findById(STRAnalysisId id);
	public void deleteById(STRAnalysisId id);
	
	// pagination
	/**
	 * 
	 * @param modelId
	 * @return
	 */
	@Query(value = "SELECT str FROM STRAnalysis str JOIN FETCH str.sample sample WHERE sample.modelDisplayName = :modelId ",
			countQuery = "SELECT count(str) FROM STRAnalysis str JOIN str.sample sample WHERE sample.modelDisplayName = :modelId ")
    @Transactional(readOnly = true)
	public Page<STRAnalysis> findByModelIdIgnoreCase(String modelId, Pageable pageable);

	/**
	 * 
	 * @param modelId
	 * @return
	 */
	@Query(value = "SELECT str FROM STRAnalysis str JOIN FETCH str.sample sample WHERE sample.modelDisplayName LIKE :modelId ESCAPE '@' ",
			countQuery = "SELECT count(str) FROM STRAnalysis str JOIN str.sample sample WHERE sample.modelDisplayName LIKE :modelId ESCAPE '@' ")
    @Transactional(readOnly = true)
	public Page<STRAnalysis> findByModelIdIgnoreCaseContaining(String modelId, Pageable pageable);
	
    /**
	 * 
	 * @param ppid
	 * @return
	 */
	@Query(value = "SELECT str FROM STRAnalysis str JOIN FETCH str.sample sample WHERE sample.protocolParticipantID LIKE :ppid ESCAPE '@' ",
			countQuery = "SELECT count(str) FROM STRAnalysis str JOIN str.sample sample WHERE sample.protocolParticipantID LIKE :ppid ESCAPE '@' ")
    @Transactional(readOnly = true)
	public Page<STRAnalysis> findByPPIdIgnoreCaseContaining(String ppid, Pageable pageable);

    /**
	 * 
	 * @param ppid
	 * @return
	 */
	@Query(value = "SELECT str FROM STRAnalysis str JOIN FETCH str.sample sample WHERE sample.protocolParticipantID IN :ppids ",
			countQuery = "SELECT count(str) FROM STRAnalysis str JOIN str.sample sample WHERE sample.protocolParticipantID IN :ppids ")
    @Transactional(readOnly = true)
	public Page<STRAnalysis> findByPPIdIn(List<String> ppids, Pageable pageable);

    /**
	 * 
	 * @param sampleId
	 * @return
	 */
	@Query(value = "SELECT str FROM STRAnalysis str JOIN FETCH str.sample sample WHERE sample.sampleDescriptor = :sampleId ",
			countQuery = "SELECT count(str) FROM STRAnalysis str JOIN str.sample sample WHERE sample.sampleDescriptor = :sampleId ")
    @Transactional(readOnly = true)
	public Page<STRAnalysis> findBySampleIdIgnoreCase(String sampleId, Pageable pageable);

	/**
	 * 
	 * @param sampleId
	 * @return
	 */
	@Query(value = "SELECT str FROM STRAnalysis str JOIN FETCH str.sample sample WHERE sample.sampleDescriptor LIKE :sampleId ESCAPE '@' ",
			countQuery = "SELECT count(str) FROM STRAnalysis str JOIN str.sample sample WHERE sample.sampleDescriptor LIKE :sampleId ESCAPE '@' ")
    @Transactional(readOnly = true)
	public Page<STRAnalysis> findBySampleIdIgnoreCaseContaining(String sampleId, Pageable pageable);

	// Analytics
	/**
	 * 
	 * @return
	 */
	@Query(value = "SELECT AVG(str.percentageStrMatch) "
					+ "FROM STRAnalysis str "
					+ "WHERE str.isReferenceStr = false ")
    @Transactional(readOnly = true)
	public float getAverageSTRPercentageMatch();
	
	/**
	 * 
	 * @return
	 */
	@Query(value = "SELECT AVG(str.percentageStrMatch) "
					+ "FROM STRAnalysis str "
					+ "JOIN str.sample sample "
					+ "WHERE str.isReferenceStr = false AND sample.modelDisplayName LIKE :studyId ESCAPE '@' ")
    @Transactional(readOnly = true)
	public float getAverageSTRPercentageMatchByStudyId(String studyId);

	/**
	 * 
	 * @return
	 */
	@Query(value = "SELECT AVG(str.percentageStrMatch) "
					+ "FROM STRAnalysis str "
					+ "JOIN str.sample sample "
					+ "WHERE str.isReferenceStr = false AND sample.modelDisplayName = :modelId ")
    @Transactional(readOnly = true)
	public float getAverageSTRPercentageMatchByModelId(String modelId);

	/**
	 * 
	 * @return
	 */
	@Query(value = "SELECT FLOOR(percentage_str_match*10)/10 AS dataType, COUNT(*) AS count "
					+ "FROM stranalysis "
					+ "WHERE is_reference_str = false GROUP BY 1 ORDER BY 1 ",
					nativeQuery = true)
    @Transactional(readOnly = true)
	public List<Object[]> getSTRPercentageMatchHistogram();

	/**
	 * 
	 * @return
	 */
	@Query(value = "SELECT FLOOR(percentage_str_match*10)/10, COUNT(*) "
					+ "FROM stranalysis as str "
					+ "JOIN bio_sample as sample on sample.sample_descriptor = str.sample_descriptor "
					+ "WHERE str.is_reference_str = false AND sample.model_display_name LIKE :studyId GROUP BY 1 ORDER BY 1 ",
					nativeQuery = true)
    @Transactional(readOnly = true)
	public List<Object[]> getSTRPercentageMatchHistogramByStudyId(String studyId);

	/**
	 * 
	 * @return
	 */
	@Query(value = "SELECT FLOOR(percentage_str_match*10)/10 AS dataType, COUNT(*) AS count "
					+ "FROM stranalysis as str "
					+ "JOIN bio_sample as sample on sample.sample_descriptor = str.sample_descriptor "
					+ "WHERE str.is_reference_str = false AND sample.model_display_name = :modelId GROUP BY 1 ORDER BY 1",
					nativeQuery = true)
    @Transactional(readOnly = true)
	public List<Object[]> getSTRPercentageMatchHistogramByModelId(String modelId);

	
}
