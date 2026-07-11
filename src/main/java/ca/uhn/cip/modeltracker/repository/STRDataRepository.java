package ca.uhn.cip.modeltracker.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import ca.uhn.cip.modeltracker.model.STRData;

public interface STRDataRepository extends CrudRepository<STRData, Long> {
	
	public List<STRData> findBySampleDescriptorAndReceivingDate(String sampleDescriptor, String receivingDate);
	
	@Transactional
	public Long deleteBySampleDescriptorAndReceivingDate(String sampleDescriptor, String receivingDate);

	@Transactional
	public Long deleteBySampleDescriptor(String sampleDescriptor);

    @Transactional
	public List<STRData> removeBySampleDescriptor(String sampleDescriptor);
}
