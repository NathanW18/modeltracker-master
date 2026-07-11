package ca.uhn.cip.modeltracker.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * Spring data model class for biosample table.
 * 
 * @author Kevin Wang
 *
 */

@Entity
@Table
@Data
public class Study {
	@Id
    private String studyShortName;
    
    private String studyLongName;
	
	private String principleInvestigator;
	
	private String comment;
}