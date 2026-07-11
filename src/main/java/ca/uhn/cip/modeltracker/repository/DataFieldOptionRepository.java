package ca.uhn.cip.modeltracker.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import ca.uhn.cip.modeltracker.repository.ReadOnlyRepository;
import ca.uhn.cip.modeltracker.model.DataFieldOption;

/**
 * Spring data repository class for data field option.
 * 
 * @author Kevin Wang
 *
 */

@Repository
public interface DataFieldOptionRepository extends ReadOnlyRepository<DataFieldOption, Long> {

	public List<DataFieldOption> findByFieldOptionName(String fieldOptionName);
	
	public List<DataFieldOption> findAll();
}
