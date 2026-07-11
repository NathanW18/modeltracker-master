package ca.uhn.cip.modeltracker.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

import java.sql.Timestamp;

/**
 * Spring data model class for STRAnalysis table.
 * 
 * @author Kevin Wang
 *
 */

@Entity
@Table
@Data
@IdClass(STRAnalysisId.class)
public class STRAnalysis {
	
	@Id
	@Column(columnDefinition = "varchar(200)")
	private String sampleDescriptor;

	@Id
	@Column(columnDefinition = "varchar(50)")
	private String receivingDate;
	
	@ManyToOne
	@PrimaryKeyJoinColumn(name = "sample_descriptor", referencedColumnName="sample_descriptor")
	// @JoinColumn(name = "sample_id")
	private BioSample sample;

	private Boolean isReferenceStr;
	
	private String refSampleDescriptor;
	
	private String refReceivingDate;

	private Float percentageStrMatch;	
	
	private Integer matchCount;
	
	private Integer totalCount;
	
	private String comment;

	private String uploaderName;

	private Timestamp operationTimeStamp;

	public void setOperationTimeStamp(Timestamp time) {
		this.operationTimeStamp = time;
	}

}
