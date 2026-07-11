package ca.uhn.cip.modeltracker.builder;

import ca.uhn.cip.modeltracker.model.*;

public class STRDataBuilder {
  private STRData strData = new STRData();
  
  public STRDataBuilder panel(String panel) {
    strData.setPanel(panel);
    return this;
  }

  public STRDataBuilder sampleDescriptor(String sampleDescriptor) {
	strData.setSampleDescriptor(sampleDescriptor);
	return this;
  }
  
  public STRDataBuilder marker(String marker) {
	strData.setMarker(marker);
	return this;
  }

  public STRDataBuilder allele1(String allele1) {
	strData.setAllele1(allele1);
	return this;
  }
  
  public STRDataBuilder allele2(String allele2) {
	strData.setAllele2(allele2);
	return this;
  }
  
  public STRDataBuilder allele3(String allele3) {
	strData.setAllele3(allele3);
	return this;
  }
  
  public STRDataBuilder allele4(String allele4) {
	strData.setAllele4(allele4);
	return this;
  }
  
  public STRDataBuilder allele1n2(String allele1n2) {
	strData.setAllele1n2(allele1n2);
	return this;
  }

  public STRDataBuilder receivingData(String receivingData) {
	strData.setReceivingDate(receivingData);
	return this;
  }

  public STRData build() {
    return strData;
  }
}
