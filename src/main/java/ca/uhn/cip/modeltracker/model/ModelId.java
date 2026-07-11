package ca.uhn.cip.modeltracker.model;

import java.io.Serializable;

import javax.persistence.IdClass;


/**
 * Spring data model class for model table keys.
 * 
 * @author Kevin Wang
 *
 */
@IdClass(ModelId.class)
public class ModelId implements Serializable {

	private static final long serialVersionUID = 1L;

    private String modelIdentifier;

    private String studyShortName;
    
    private ModelId() {}

    public ModelId(String modelIdentifier, String studyShortName) {
        this.modelIdentifier = modelIdentifier;
        this.studyShortName = studyShortName;
    }

    public String getModelIdentifier() {
		return this.modelIdentifier;
	}

	public void setModelIdentifier(String modelIdentifier) {
		this.modelIdentifier = modelIdentifier;
	}

	public String getStudyShortName() {
		return this.studyShortName;
	}

	public void setStudyShortName(String studyShortName) {
		this.studyShortName = studyShortName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ModelId) {
			ModelId keyObject = (ModelId) obj;
			if (this.modelIdentifier == keyObject.getModelIdentifier() && this.studyShortName == keyObject.getStudyShortName()) {
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