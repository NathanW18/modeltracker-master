package ca.uhn.cip.modeltracker.builder;

import ca.uhn.cip.modeltracker.model.*;

public class StudyBuilder {
  private Study study = new Study();
  
  public StudyBuilder shortName(String shortname) {
    study.setStudyShortName(shortname);
    return this;
  }
  
  public StudyBuilder longName(String longname) {
    study.setStudyLongName(longname);
    return this;
  }

  public StudyBuilder principleInvestigator(String principleInvestigator) {
	study.setPrincipleInvestigator(principleInvestigator);
	return this;
  }
  
  public Study build() {
    return study;
  }
}