package ca.uhn.cip.modeltracker.model;

import javax.persistence.*;

import lombok.Data;

/**
 * Spring data model class for STRData table.
 * 
 * @author Kevin Wang
 *
 */

@Entity
@Table
@Data
public class STRData {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;
	
	private String panel;
	
	private String sampleDescriptor;
	
	private String marker;

	private String allele1;

	private String allele2;
	
	private String allele3;
	
	private String allele4;
	
	private String allele1n2;
	
	private String receivingDate;

	@Transient
	private String uploaderName;
}















