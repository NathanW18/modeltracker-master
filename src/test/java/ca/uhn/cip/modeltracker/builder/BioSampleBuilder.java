package ca.uhn.cip.modeltracker.builder;

import ca.uhn.cip.modeltracker.model.*;

public class BioSampleBuilder {
  private BioSample bioSample = new BioSample();
  
  public BioSampleBuilder sampleDescriptor(String descriptor) {
    bioSample.setSampleDescriptor(descriptor);
    return this;
  }
  
  public BioSampleBuilder modelDisplayName(String modelDisplayName) {
	    bioSample.setModelDisplayName(modelDisplayName);
	    return this;
	  }
	  
  public BioSampleBuilder protocolParticipantID(String ppid) {
    bioSample.setProtocolParticipantID(ppid);
    return this;
  }
  
  public BioSampleBuilder sampleType(String sampleType) {
    bioSample.setSampleType(sampleType);
    return this;
  }

  public BioSampleBuilder primaryDiseaseSite(String primaryDiseaseSite) {
	    bioSample.setPrimaryDiseaseSite(primaryDiseaseSite);
	    return this;
	  }
  
  public BioSampleBuilder interventionDescription(String interventionDescription) {
	  bioSample.setInterventionDescription(interventionDescription);
	  return this;
  }
  
  public BioSampleBuilder interventionStartDate(String interventionStartDate) {
	  bioSample.setInterventionStartDate(interventionStartDate);
	  return this;
  }

  public BioSampleBuilder interventionEndDate(String interventionEndDate) {
	  bioSample.setInterventionEndDate(interventionEndDate);
	  return this;
  }

  public BioSampleBuilder collectionDate(String collectionDate) {
	bioSample.setCollectionDate(collectionDate);
	return this;
  }

  public BioSample build() {
    return bioSample;
  }
}
