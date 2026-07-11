package ca.uhn.cip.modeltracker.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
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
@IdClass(ModelId.class)
public class Model {
	@Id
	@Column(columnDefinition = "varchar(200)")
    private String modelIdentifier;
    
    @Id
    @Column(columnDefinition = "varchar(50)")
    private String studyShortName;

    private String modelDisplayName;
    
    private String protocolParticipantID;
    
    private String parentModelDisplayName;
    
    private String comment;
}