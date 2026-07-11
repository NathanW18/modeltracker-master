package ca.uhn.cip.modeltracker.builder;

import ca.uhn.cip.modeltracker.model.*;

public class ModelBuilder {
  private Model model = new Model();
  
  public ModelBuilder modelIdentifier(String identifier) {
    model.setModelIdentifier(identifier);
    return this;
  }

  public ModelBuilder studyShortName(String studyShortName) {
	model.setStudyShortName(studyShortName);
	return this;
  }
 
  public ModelBuilder modelDisplayName(String modelDisplayName) {
	model.setModelDisplayName(modelDisplayName);
	return this;
  }

  public ModelBuilder protocolParticipantId(String ppid) {
	model.setProtocolParticipantID(ppid);
	return this;
  }

  public ModelBuilder parentModelDisplayName(String parentModelDisplayName) {
	model.setParentModelDisplayName(parentModelDisplayName);
	return this;
  }

  public Model build() {
    return model;
  }
}