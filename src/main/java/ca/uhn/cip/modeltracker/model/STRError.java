package ca.uhn.cip.modeltracker.model;

import lombok.Data;

@Data
public class STRError {
	
	int ErrorCode;
	
	String ErrorText;
	
	String ErrorSTRSampleID;
	
	String ErrorSTRAnalysisDate;

}
