package ca.uhn.cip.modeltracker.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;
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
public class BioSample {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;
	
	private String sampleDescriptor;
	
	private String modelDisplayName;
		
	@Column(name = "protocolPaticipantID")
	private String protocolParticipantID;
	
	private String barcode;
	
	private String sampleType;
	
	private String primaryDiseaseSite;
	
	private String collectionDate;
	
    private String interventionDescription;
    
    private String interventionStartDate;
    
    private String interventionEndDate;
	
	private String comment;

}
