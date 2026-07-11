package ca.uhn.cip.modeltracker.model;

import java.io.Serializable;

import javax.persistence.IdClass;

/**
 * Spring data model class for model table keys.
 * 
 * @author Kevin Wang
 *
 */
@IdClass(STRAnalysisId.class)
public class STRAnalysisId implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String sampleDescriptor;

	private String receivingDate;
    
    private STRAnalysisId() {}

    public STRAnalysisId(String sampleDescriptor, String receivingDate) {
        this.sampleDescriptor = sampleDescriptor;
        this.receivingDate = receivingDate;
    }

    public String getSampleDescriptor() {
		return this.sampleDescriptor;
	}

	public void setSampleDescriptor(String sampleDescriptor) {
		this.sampleDescriptor = sampleDescriptor;
	}

	public String getReceivingDate() {
		return this.receivingDate;
	}

	public void setReceivingDate(String receivingDate) {
		this.receivingDate = receivingDate;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof STRAnalysisId) {
			STRAnalysisId keyObject = (STRAnalysisId) obj;
			if (this.sampleDescriptor == keyObject.getSampleDescriptor() && this.receivingDate == keyObject.getReceivingDate()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
