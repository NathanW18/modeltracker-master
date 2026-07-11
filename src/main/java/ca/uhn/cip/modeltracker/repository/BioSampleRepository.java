package ca.uhn.cip.modeltracker.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ca.uhn.cip.modeltracker.model.BioSample;

/**
 * Spring data repository class for biosample.
 * 
 * @author Kevin Wang
 *
 */

@Repository
public interface BioSampleRepository extends PagingAndSortingRepository<BioSample, Long> {
	
	public List<BioSample> findByModelDisplayNameIgnoreCase(String modelDisplayName);

	public List<BioSample> findByModelDisplayNameIgnoreCaseContaining(String modelDisplayName);

	public List<BioSample> findByProtocolParticipantID(String ppid);

	public List<BioSample> findByProtocolParticipantIDIgnoreCaseContaining(String ppid);
	
	public List<BioSample> findByProtocolParticipantIDIn(List<String> ppids);

	public List<BioSample> findBySampleDescriptorIgnoreCase(String sampleDescriptor);

	public List<BioSample> findBySampleDescriptorIgnoreCaseContaining(String sampleDescriptor);

	@Query(value = "SELECT COUNT(s.id) as cnt, s.sampleType from BioSample s GROUP BY s.sampleType")
	public List<Object[]> getCountBySampleType();
	
	@Query(value = "SELECT COUNT(s.id) as cnt, s.primaryDiseaseSite from BioSample s GROUP BY s.primaryDiseaseSite")
	public List<Object[]> getCountByDiseaseSite();

	@Query("SELECT bsp.protocolParticipantID " 
		    + "FROM BioSample bsp " 
		    + "GROUP BY bsp.protocolParticipantID")
	@Transactional(readOnly = true)
	public List<String> findAllUniquePPID();
	
	// pagingation
	public Page<BioSample> findByModelDisplayNameIgnoreCase(String modelDisplayName, Pageable pageable);

	public Page<BioSample> findByModelDisplayNameIgnoreCaseContaining(String modelDisplayName, Pageable pageable);
	
	public Page<BioSample> findByProtocolParticipantIDIgnoreCaseContaining(String ppid, Pageable pageable);
	
	public Page<BioSample> findByProtocolParticipantIDIn(List<String> ppid, Pageable pageable);

	public Page<BioSample> findBySampleDescriptorIgnoreCase(String sampleDescriptor, Pageable pageable);

	public Page<BioSample> findBySampleDescriptorIgnoreCaseContaining(String sampleDescriptor, Pageable pageable);

}
