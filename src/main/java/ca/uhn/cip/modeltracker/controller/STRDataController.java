package ca.uhn.cip.modeltracker.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.CrossOrigin;

import ca.uhn.cip.modeltracker.model.BioSample;
import ca.uhn.cip.modeltracker.model.DataCount;
import ca.uhn.cip.modeltracker.model.DataFieldOption;
import ca.uhn.cip.modeltracker.model.Study;
import ca.uhn.cip.modeltracker.model.Model;
import ca.uhn.cip.modeltracker.model.Patient;
import ca.uhn.cip.modeltracker.model.STRAnalysis;
import ca.uhn.cip.modeltracker.model.STRData;
import ca.uhn.cip.modeltracker.model.STRError;
import ca.uhn.cip.modeltracker.model.STRMatch;
import ca.uhn.cip.modeltracker.service.BioSampleService;

/**
 * Spring boot controller class for biosample.
 * 
 * @author Kevin Wang
 *
 */

@CrossOrigin(origins = "http://localhost:4200") // whatever the front end is running on
@RestController
@RequestMapping("/api")
public class STRDataController {

    public static final Logger logger = LoggerFactory.getLogger(STRDataController.class);

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BioSampleService biosampleService;


    // ------------------- Search STR analysis pageable--------------------------------------
    @GetMapping("/stranalyses/pagination")
    public ResponseEntity<Page<STRAnalysis>> getSTRAnalysesPageableByParameters(
    		Pageable pageable,
    		@RequestParam(value = "sampleid") Optional<String> sampleId,
    		@RequestParam(value = "modelid") Optional<String> modelId,
    		@RequestParam(value = "ppid") Optional<List<String>> ppid,
    		@RequestParam(name = "exactmatch", required = false, defaultValue = "false") boolean exactMatch) {
    	
        logger.debug("Searching STR analysis pageable with parameter ... ");

    	Page<STRAnalysis> strAnalyses = null;
    	
    	Pageable allSTRAnalyses = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
    	
    	if (modelId.isPresent() && ppid.isPresent()) {
    		logger.debug("cannot search by model Id and ppid at sametime!");
    		return new ResponseEntity<Page<STRAnalysis>>(HttpStatus.NOT_FOUND); 
    	} else if (sampleId.isPresent()) {
    		if (exactMatch) {
        		strAnalyses = biosampleService.findSTRAnalysesPageableBySampleIdIgnoreCase(sampleId.get(), allSTRAnalyses);    			
    		} else {
        		strAnalyses = biosampleService.findSTRAnalysesPageableBySampleIdIgnoreCaseContaining(sampleId.get(), allSTRAnalyses);
    		}
    	} else if (modelId.isPresent()) {
    		if (exactMatch) {
        		strAnalyses = biosampleService.findSTRAnalysesPageableByModelIdIgnoreCase(modelId.get(), allSTRAnalyses);
    		} else {
        		strAnalyses = biosampleService.findSTRAnalysesPageableByModelIdIgnoreCaseContaining(modelId.get(), allSTRAnalyses);
    		}
    	} else if (ppid.isPresent() && ppid.get().size() > 0) {
    		if (exactMatch) {
    			strAnalyses = biosampleService.findSTRAnalysesPageableByPPIdList(ppid.get(), allSTRAnalyses);
    		} else {
    			strAnalyses = biosampleService.findSTRAnalysesPageableByPPIdListContaining(ppid.get(), allSTRAnalyses);
    		}
    	} else {
    		strAnalyses = biosampleService.findAllSTRAnalysesPageable(allSTRAnalyses);
    	}
        
        if (strAnalyses == null || strAnalyses.isEmpty()) {
    		logger.debug("No STR analysis found!");
            return new ResponseEntity<Page<STRAnalysis>>(HttpStatus.NO_CONTENT); // You many decide to return
                                                                               // HttpStatus.NOT_FOUND
        }

        return new ResponseEntity<Page<STRAnalysis>>(strAnalyses, HttpStatus.OK);
    }

    // ------------------- Search STR analysis --------------------------------------
    @GetMapping("/stranalyses")
    public ResponseEntity<List<STRAnalysis>> getSTRAnalysesByParameters(
    		@RequestParam(value = "sampleid") Optional<String> sampleId,
    		@RequestParam(value = "modelid") Optional<String> modelId,
    		@RequestParam(value = "ppid") Optional<List<String>> ppid,
    		@RequestParam(name = "exactmatch", required = false, defaultValue = "false") boolean exactMatch) {
    	
        logger.debug("Searching STR analysis with parameterv ... ");

    	List<STRAnalysis> strAnalyses = null;
    	
    	if (modelId.isPresent() && ppid.isPresent()) {
    		logger.debug("cannot search by model Id and ppid at sametime!");
    		return new ResponseEntity<List<STRAnalysis>>(HttpStatus.NOT_FOUND); 
    	} else if (sampleId.isPresent()) {
    		if (exactMatch) {
        		strAnalyses = biosampleService.findSTRAnalysesBySampleIdIgnoreCase(sampleId.get());    			
    		} else {
        		strAnalyses = biosampleService.findSTRAnalysesBySampleIdIgnoreCaseContaining(sampleId.get());	
    		}
    	} else if (modelId.isPresent()) {
    		if (exactMatch) {
        		strAnalyses = biosampleService.findSTRAnalysesByModelIdIgnoreCase(modelId.get());
    		} else {
        		strAnalyses = biosampleService.findSTRAnalysesByModelIdIgnoreCaseContaining(modelId.get());
    		}
    	} else if (ppid.isPresent() && ppid.get().size() > 0) {
    		if (exactMatch) {
    			strAnalyses = biosampleService.findSTRAnalysesByPPIdList(ppid.get());
    		} else {
    			strAnalyses = biosampleService.findSTRAnalysesByPPIdListContaining(ppid.get());
    		}
    	} else {
    		strAnalyses = biosampleService.findAllSTRAnalyses();
    	}
        
        if (strAnalyses == null || strAnalyses.isEmpty()) {
    		logger.debug("No STR analysis found for specified parameter!");
            return new ResponseEntity<List<STRAnalysis>>(HttpStatus.NO_CONTENT); // You many decide to return
                                                                               // HttpStatus.NOT_FOUND
        }

        return new ResponseEntity<List<STRAnalysis>>(strAnalyses, HttpStatus.OK);
    }

    // ------------------- Delete STR analysis by Id --------------------------------------
    @DeleteMapping("/stranalyses")
    public ResponseEntity<STRAnalysis> deleteSTRAnalysisByID(
    		@RequestParam(value = "descriptor", required = true) String sampleDescriptor,
    		@RequestParam(value = "date", required = true) String receivingDate) {

        logger.debug("Deleting STR analysis by ID ... ");

        // hack to get back the [+] plus sign in the sample name
        sampleDescriptor = sampleDescriptor.replaceAll("[\\^]", "+");

    	// logic here is
    	STRAnalysis strAnalysis = biosampleService.findSTRAnalysisBySampleDescriptorAndReceivingDate(
    			sampleDescriptor, receivingDate);
        if (strAnalysis == null) {
        	logger.error("STR Analysis with sample name " + strAnalysis.getSampleDescriptor() + " and " + "STR Date" + strAnalysis.getReceivingDate() + " not found!");
            return new ResponseEntity<STRAnalysis>(HttpStatus.NOT_FOUND);
        }

    	// 1. find all the STRAnalyses belong to the same patient
    	List<STRAnalysis> strAnalysisList = biosampleService.findSTRAnalysesByPPIdIgnoreCase(strAnalysis.getSample().getProtocolParticipantID());
        if (strAnalysis == null || strAnalysisList == null) {
        	logger.debug("STR Analysis with id " + sampleDescriptor + " not found!");
            return new ResponseEntity<STRAnalysis>(HttpStatus.NOT_FOUND);
        }

        // 2. check if the deleted STRAnalysis is the Reference STRAnalysis   
        if (strAnalysis.getIsReferenceStr()) {
        	// if it is, update the match percentage for all the STR to empty
        	for(int i = 0; i < strAnalysisList.size(); i++) {
        		STRAnalysis analysis = strAnalysisList.get(i);
        		
        		analysis.setRefSampleDescriptor(null);
        		analysis.setRefReceivingDate(null);
        		
        		analysis.setPercentageStrMatch(0.0f);
        		analysis.setMatchCount(0);
        		analysis.setTotalCount(0);
        		biosampleService.updateSTRAnalysisBySampleDescriptorAndReceivingDate(analysis.getSampleDescriptor(), analysis.getReceivingDate(), strAnalysis);
        	}        	
        }
        
        // 3. delete the STRAnalysis
        biosampleService.deleteSTRAnalysisBySampleDescriptorAndReceivingDate(sampleDescriptor, receivingDate);
        
        return new ResponseEntity<STRAnalysis>(HttpStatus.NO_CONTENT);
    }

    // ------------------- Set reference STR analysis --------------------------------------
    @PatchMapping("/stranalyses")
    public ResponseEntity<STRAnalysis> setReferenceSTRAnalysisByID(
    		@RequestParam(value = "descriptor", required = true) String sampleDescriptor,
    		@RequestParam(value = "date", required = true) String receivingDate, 
    		@RequestParam(value = "ppid", required = true) List<String> ppid,
    		@RequestBody STRAnalysis strAnalysis) {
  
        logger.debug("Setting reference STR analysis by Id... ");

        // hack to get back the [+] plus sign in the sample name
        sampleDescriptor = sampleDescriptor.replaceAll("[\\^]", "+");

        // logic here is
    	STRAnalysis strAnalysisRef = biosampleService.findSTRAnalysisBySampleDescriptorAndReceivingDate(
    			sampleDescriptor, receivingDate);

    	// 1. find all the STRAnalyses belong to the same patient
    	List<STRAnalysis> strAnalysisList = new ArrayList<STRAnalysis>();
    	for(int i = 0; i < ppid.size(); i++) {
	    	List<STRAnalysis> retList = biosampleService.findSTRAnalysesByPPIdIgnoreCase(ppid.get(i));
	        if (retList == null || retList == null) {
	        	logger.debug("STR Analysis with ppid " + ppid.get(i) + " not found!");
	        }
	        strAnalysisList.addAll(retList);
    	}
        if (strAnalysisRef == null || strAnalysisList.size() == 0) {
        	logger.debug("STR Analysis with sample id " + sampleDescriptor + " not found!");
            return new ResponseEntity<STRAnalysis>(HttpStatus.NOT_FOUND);
        }
        
    	// 2. update the reference STR for all the STR
    	for(int i = 0; i < strAnalysisList.size(); i++) {
    		strAnalysisList.get(i).setIsReferenceStr(false);
    		strAnalysisList.get(i).setRefSampleDescriptor(strAnalysisRef.getSampleDescriptor());
    		strAnalysisList.get(i).setRefReceivingDate(strAnalysisRef.getReceivingDate());
    	}
    	strAnalysisRef.setIsReferenceStr(true);
    	strAnalysisRef.setRefSampleDescriptor(strAnalysisRef.getSampleDescriptor());
    	strAnalysisRef.setRefReceivingDate(strAnalysisRef.getReceivingDate());
    	
    	// 3. update the match percentage for all the STR 
    	List<STRData> strDataListRef = biosampleService.findSTRDataByBiosampleDescriptorAndReceivingDate(
    			strAnalysisRef.getSampleDescriptor(), strAnalysisRef.getReceivingDate());
    	
    	for(int i = 0; i < strAnalysisList.size(); i++) {
    		
    		STRAnalysis analysis = strAnalysisList.get(i);
    		
    		// 3.1 for each sample find all the STR data
    		List<STRData> strDataList = biosampleService.findSTRDataByBiosampleDescriptorAndReceivingDate(
    				analysis.getSampleDescriptor(), analysis.getReceivingDate());
    		
    		// 3.2 call the match algorithm to get the percentage
    		STRMatch match = biosampleService.computeSTRMatch(strDataListRef, strDataList);
    		
    		analysis.setPercentageStrMatch(match.getPercentageStrMatch());
    		analysis.setMatchCount(match.getMatchCount());
    		analysis.setTotalCount(match.getTotalCount());
    	}
    	
    	for(int i = 0; i < strAnalysisList.size(); i++) {
    		biosampleService.saveSTRAnalysis(strAnalysisList.get(i));
    	}

        return new ResponseEntity<STRAnalysis>(strAnalysisRef, HttpStatus.OK);
    }
    
    // ------------------- Reset STR percentage match scores --------------------------------------
    @GetMapping("/stranalyses/strpercentagematch")
    public ResponseEntity<String> computeSTRPercentageMatch() {
  
        logger.debug("Compute STR percentage match ... ");

    	// logic here is

    	// 1. find all the STRAnalyses belong to the same patient
    	List<STRAnalysis> strAnalysisList = biosampleService.findAllSTRAnalyses();
            	
    	// 2. update the match percentage for all the STR     	
    	for(int i = 0; i < strAnalysisList.size(); i++) {
    		
    		STRAnalysis analysis = strAnalysisList.get(i);
    		
    		// if percentage match is not computed before ( < 0.01 )
    		if (analysis.getPercentageStrMatch() < 0.01) {
	    		// 2.1 find all the STR data
	    		List<STRData> strDataList = biosampleService.findSTRDataByBiosampleDescriptorAndReceivingDate(
	    				analysis.getSampleDescriptor(), analysis.getReceivingDate());
	    		// 2.2 find reference STR data
	        	List<STRData> strDataListRef = biosampleService.findSTRDataByBiosampleDescriptorAndReceivingDate(
	        			analysis.getRefSampleDescriptor(), analysis.getRefReceivingDate());
	
	    		// 2.3 call the match algorithm to get the percentage
	    		STRMatch match = biosampleService.computeSTRMatch(strDataListRef, strDataList);
	    		
	    		analysis.setPercentageStrMatch(match.getPercentageStrMatch());
	    		analysis.setMatchCount(match.getMatchCount());
	    		analysis.setTotalCount(match.getTotalCount());
	    		
	    		biosampleService.saveSTRAnalysis(analysis);
    		}
    	}
    	
        return new ResponseEntity<String>(HttpStatus.OK);
    }
    
    // ------------------- Check against all reference STR analysis --------------------------------------
    @GetMapping("/refstranalyses")
    public ResponseEntity<List<STRAnalysis>> checkAgainstAllReferenceSTRAnalysisByID(
    		@RequestParam(value = "descriptor", required = true) String sampleDescriptor,
    		@RequestParam(value = "date", required = true) String receivingDate) {
  
        logger.debug("Checking against all reference STR analyses by Id... ");

        // hack to get back the [+] plus sign in the sample name
        sampleDescriptor = sampleDescriptor.replaceAll("[\\^]", "+");

    	// logic here is
    	STRAnalysis strAnalysisCur = biosampleService.findSTRAnalysisBySampleDescriptorAndReceivingDate(
    			sampleDescriptor, receivingDate);

    	// 1. find all reference STRAnalyses
    	List<STRAnalysis> strAnalysisRefList = biosampleService.findAllReferenceSTRAnalyses();
        if (strAnalysisCur == null || strAnalysisRefList == null) {
        	logger.debug("STR Analysis with id " + sampleDescriptor + " not found!");
            return new ResponseEntity<List<STRAnalysis>>(HttpStatus.NOT_FOUND);
        }
            	
    	// 2. calculate the match percentages against all reference STR 
    	List<STRData> strDataListCur = biosampleService.findSTRDataByBiosampleDescriptorAndReceivingDate(
    			strAnalysisCur.getSampleDescriptor(), strAnalysisCur.getReceivingDate());
    	
    	for(int i = 0; i < strAnalysisRefList.size(); i++) {
    		
    		STRAnalysis analysis = strAnalysisRefList.get(i);
    		
    		// 2.1 for each sample find all the STR data
    		List<STRData> strDataListRef = biosampleService.findSTRDataByBiosampleDescriptorAndReceivingDate(
    				analysis.getSampleDescriptor(), analysis.getReceivingDate());
    		
    		// 2.2 call the match algorithm to get the percentage
    		STRMatch match = biosampleService.computeSTRMatch(strDataListCur, strDataListRef);
    		
    		analysis.setPercentageStrMatch(match.getPercentageStrMatch());
    		analysis.setMatchCount(match.getMatchCount());
    		analysis.setTotalCount(match.getTotalCount());
    	}
    	
        return new ResponseEntity<List<STRAnalysis>>(strAnalysisRefList, HttpStatus.OK);
    }

    // ------------------- Update STRAnalysis reference data --------------------------------------
    // this is a temp fix so that we can update all reference sample and receivingdate field for stranalyses 
    // since they were not there originally, there was only a field to indicate if it is reference str.
    @PutMapping("/refstranalyses")
    public ResponseEntity<List<STRAnalysis>> updateSTRAnalysisReferenceData() {
    	
        logger.debug("Updating STR analysis reference data ... ");
        
        List<STRAnalysis> strList = null;

        // 1. find all PPIDs
        List<String> PPIDList = biosampleService.findAllUniquePPID();
        
        
        // 2. find all the STRAnalyses belong to the same patient PPID
        for (int i = 0; i < PPIDList.size(); i++) {
        	List<STRAnalysis> strAnalysisList = biosampleService.findSTRAnalysesByPPIdIgnoreCase(PPIDList.get(i));
            if (strAnalysisList == null) {
            	logger.debug("STR Analysis with PPID not found!");
            	continue;
            }
            
            STRAnalysis strAnalysisRef = biosampleService.findRefSTRAnalysisBySTRAnalysis(strAnalysisList.get(0));
        	if (strAnalysisRef == null) {
        		logger.debug("reference STR Analysis with PPID not found!");
        		continue;
        	}
        	// 3. update the reference STR data for all the STR
        	for(int j = 0; j < strAnalysisList.size(); j++) {
        		STRAnalysis analysis = strAnalysisList.get(j); 
        		if (analysis.getRefSampleDescriptor() == null || analysis.getRefSampleDescriptor().isEmpty() 
        				|| analysis.getRefReceivingDate() == null || analysis.getRefReceivingDate().isEmpty()) {
	    			analysis.setRefSampleDescriptor(strAnalysisRef.getSampleDescriptor());
	    			analysis.setRefReceivingDate(strAnalysisRef.getReceivingDate());
	        		biosampleService.updateSTRAnalysisBySampleDescriptorAndReceivingDate(analysis.getSampleDescriptor(), analysis.getReceivingDate(), analysis);
        		}
        	}
        }

        
        return new ResponseEntity<List<STRAnalysis>>(strList, HttpStatus.OK);
    }
    
    // ------------------- Search STR data --------------------------------------
    @GetMapping("/strdatalist")
    public ResponseEntity<List<STRData>> getSTRDataByBiosampleDescriptorAndReceivingDate(
    		@RequestParam(value = "descriptor", required = true) String descriptor,
    		@RequestParam(value = "date", required = true) String date) {
    	
        logger.debug("Searching STR data with parameter... ");
        
        // hack to get back the [+] plus sign in the sample name
        descriptor = descriptor.replaceAll("[\\^]", "+");
    	
        List<STRData> strDataList = biosampleService.findSTRDataByBiosampleDescriptorAndReceivingDate(descriptor, date);
        if (strDataList == null) {
            logger.debug("STR data with id:" + descriptor + " not found!");
            return new ResponseEntity<List<STRData>>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<List<STRData>>(strDataList, HttpStatus.OK);

    }

    // ------------------- Save STR data --------------------------------------
    @PostMapping("/strdatalist")
    public ResponseEntity<List<STRData>> saveSTRDataList(@RequestBody List<STRData> strDataList, UriComponentsBuilder ucBuilder) {

        logger.debug("Saving STR data " + strDataList.size());
        
        // create the list of STR error msgs
        List<STRError> STRErrorList = new ArrayList<STRError>();

        List<STRData> retSTRDataList = biosampleService.saveSTRDataList(strDataList, STRErrorList);
        
        // add the headers if the STR data list was partially created
        HttpHeaders headers = new HttpHeaders();
        headers.add("content-type", "application/json");
        if (STRErrorList.size() > 0) {

        	String errorText = "";
        	for (int i=0; i < STRErrorList.size(); i++) {
        		STRError strError = STRErrorList.get(i);
        		if (strError.getErrorCode() == HttpStatus.NOT_FOUND.value()) {
            		errorText += "STR Data for sample " + strError.getErrorSTRSampleID() + " was not uploaded because sample " + strError.getErrorSTRSampleID() + " is not registered in the database. ";  			
        		} else if (strError.getErrorCode() == HttpStatus.CONFLICT.value()) {
            		errorText += "STR Data for sample " + strError.getErrorSTRSampleID() + " on date " + strError.getErrorSTRAnalysisDate() + " was not uploaded because an STR Data set for that sample and that date are already stored in the database. ";  			
        		} else {
					errorText += "STR Data was not uploaded. Please contact support with error code: " + strError.getErrorCode();
        		}
        	}
        	headers.add("error-code", "Partial_Creation");
            headers.add("error-text", errorText);    
        }

        if (retSTRDataList == null || retSTRDataList.isEmpty()) {
            return new ResponseEntity<List<STRData>>(headers, HttpStatus.PRECONDITION_FAILED);
        }
        
        // logic to save STR data
        // 1.
        List<STRAnalysis> strAnalysisList = biosampleService.findSTRAnalysesBySTRDataList(retSTRDataList);
        
        
        for (int i=0; i<strAnalysisList.size(); i++) {
        	STRAnalysis refSTRAnalysis;
        	STRAnalysis curSTRAnalysis = strAnalysisList.get(i);
        	refSTRAnalysis = biosampleService.findRefSTRAnalysisBySTRAnalysis(curSTRAnalysis);
        	
        	if (refSTRAnalysis != null) {
        		curSTRAnalysis.setIsReferenceStr(false);
        		
        	} else {
        		curSTRAnalysis.setIsReferenceStr(true);
        		refSTRAnalysis = curSTRAnalysis;
        	}
        	curSTRAnalysis.setRefSampleDescriptor(refSTRAnalysis.getSampleDescriptor());
        	curSTRAnalysis.setRefReceivingDate(refSTRAnalysis.getReceivingDate());
        	
        	// 3. update the match percentage for all the STR 
        	List<STRData> refSTRDataList = biosampleService.findSTRDataByBiosampleDescriptorAndReceivingDate(
        			refSTRAnalysis.getSampleDescriptor(), refSTRAnalysis.getReceivingDate());
        	
    		// 3.1 for each sample find all the STR data
    		List<STRData> curSTRDataList = biosampleService.findSTRDataByBiosampleDescriptorAndReceivingDate(
    				curSTRAnalysis.getSampleDescriptor(), curSTRAnalysis.getReceivingDate());

    		// 3.2 call the match algorithm to get the percentage
    		STRMatch match = biosampleService.computeSTRMatch(refSTRDataList, curSTRDataList);
    		
    		curSTRAnalysis.setPercentageStrMatch(match.getPercentageStrMatch());
    		curSTRAnalysis.setMatchCount(match.getMatchCount());
    		curSTRAnalysis.setTotalCount(match.getTotalCount());
    		
    		STRAnalysis ret = biosampleService.saveSTRAnalysis(curSTRAnalysis);
        }

        return new ResponseEntity<List<STRData>>(retSTRDataList, headers, HttpStatus.CREATED);
    }

}
