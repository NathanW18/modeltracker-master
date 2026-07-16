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
import org.springframework.web.bind.annotation.CrossOrigin;

import com.fasterxml.jackson.databind.ObjectMapper;

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

@CrossOrigin(origins = "http://localhost:5173") // whatever the front end is running on
@RestController
@RequestMapping("/api")
public class BioSampleController {

    public static final Logger logger = LoggerFactory.getLogger(BioSampleController.class);

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BioSampleService biosampleService;

    // ------------------- Read a sample -----------------------------------------------
    @GetMapping("/biosamples/{id}")
    public ResponseEntity<BioSample> getBioSampleByID(@PathVariable("id") long id) {
 
        logger.debug("Searching biosample by Id ... " + id);

        BioSample biosample = biosampleService.findBioSampleById(id);
        if (biosample == null) {
        	logger.debug("biosample with id " + id + " not found!");
            return new ResponseEntity<BioSample>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<BioSample>(biosample, HttpStatus.OK);

    }

    // ------------------- Search samples --------------------------------------
    @GetMapping("/biosamples")
    public ResponseEntity<List<BioSample>> getBioSamplesByParameters(
    		@RequestParam(value = "sampleid") Optional<String> sampleId,
    		@RequestParam(value = "modelid") Optional<String> modelId,
    		@RequestParam(value = "ppid") Optional<List<String>> ppid,
    		@RequestParam(name = "exactmatch", required = false, defaultValue = "false") boolean exactMatch) {
    	
        logger.debug("Searching biosample with parameter... ");
    	
    	List<BioSample> biosamples = null;
    	
    	if (modelId.isPresent() && ppid.isPresent() ) {
    		logger.debug("cannot search by model Id and ppid at sametime!");
    		return new ResponseEntity<List<BioSample>>(HttpStatus.NOT_FOUND);
    	} else if (sampleId.isPresent()) {
    		if (exactMatch) {
        		biosamples = biosampleService.findBioSamplesBySampleIdIgnoreCase(sampleId.get());
    		} else {
        		biosamples = biosampleService.findBioSamplesBySampleIdIgnoreCaseContaining(sampleId.get());
    		}
    	} else if (modelId.isPresent()) {
    		if (exactMatch) {
        		biosamples = biosampleService.findBioSamplesByModelIdIgnoreCase(modelId.get());
    		} else {
        		biosamples = biosampleService.findBioSamplesByModelIdIgnoreCaseContaining(modelId.get());    			
    		}
    	} else if (ppid.isPresent() && ppid.get().size() > 0) {
    		if (exactMatch) {
    			biosamples = biosampleService.findBioSamplesByPPIdList(ppid.get());
    		} else {
    			biosamples = biosampleService.findBioSamplesByPPIdListContaining(ppid.get());
    		}
    	} else {
    		biosamples = biosampleService.findAllBioSamples();
    	}
        
        if (biosamples == null || biosamples.isEmpty()) {
        	logger.debug("No biosamples found for specified parameter!");
            return new ResponseEntity<List<BioSample>>(HttpStatus.NO_CONTENT); // You many decide to return
                                                                               // HttpStatus.NOT_FOUND
        }

        return new ResponseEntity<List<BioSample>>(biosamples, HttpStatus.OK);
    }

    // ------------------- Search samples --------------------------------------
    @GetMapping("/biosamples/pagination")
    public ResponseEntity<Page<BioSample>> getBioSamplesPageableByParameters(
    		Pageable pageable,
    		@RequestParam(value = "sampleid") Optional<String> sampleId,
    		@RequestParam(value = "modelid") Optional<String> modelId,
    		@RequestParam(value = "ppid") Optional<List<String>> ppid,
    		@RequestParam(name = "exactmatch", required = false, defaultValue = "false") boolean exactMatch) {
    	
        logger.debug("Searching biosample pageable by parameters ... ");
    	
    	Page<BioSample> biosamples = null;
    	
    	Pageable allBiosamples = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
    	
    	if (modelId.isPresent() && ppid.isPresent() ) {
    		logger.debug("cannot search by model Id and ppid at sametime!");
    		return new ResponseEntity<Page<BioSample>>(HttpStatus.NOT_FOUND);
    	} else if (sampleId.isPresent()) {
    		if (exactMatch) {
    			biosamples = biosampleService.findBioSamplesPageableBySampleIdIgnoreCase(sampleId.get(), allBiosamples);
    		} else {
    			biosamples = biosampleService.findBioSamplesPageableBySampleIdIgnoreCaseContaining(sampleId.get(), allBiosamples);
    		}
    	} else if (modelId.isPresent()) {
    		if (exactMatch) {
        		biosamples = biosampleService.findBioSamplesPageableByModelIdIgnoreCase(modelId.get(), allBiosamples);
    		} else {
        		biosamples = biosampleService.findBioSamplesPageableByModelIdIgnoreCaseContaining(modelId.get(), allBiosamples);
    		}
    	} else if (ppid.isPresent() && ppid.get().size() > 0) {
    		if (exactMatch) {
    			biosamples = biosampleService.findBioSamplesPageableByPPIdList(ppid.get(), allBiosamples);
    		} else {
    			biosamples = biosampleService.findBioSamplesPageableByPPIdListContaining(ppid.get(), allBiosamples);    			
    		}
    	} else {
    		biosamples = biosampleService.findAllBioSamplesPageable(allBiosamples);
    	}
    	        
        if (biosamples == null || biosamples.isEmpty()) {
        	logger.debug("No biosamples found!");
            return new ResponseEntity<Page<BioSample>>(HttpStatus.NO_CONTENT); // You many decide to return
                                                                               // HttpStatus.NOT_FOUND
        }

        return new ResponseEntity<Page<BioSample>>(biosamples, HttpStatus.OK);
    }

    // ------------------- Create a sample --------------------------------------
    @PostMapping("/biosamples")
    public ResponseEntity<BioSample> saveBioSample(@RequestBody BioSample biosample, UriComponentsBuilder ucBuilder) {

        logger.debug("Saving biosample " + biosample.getSampleDescriptor());
        
        List<BioSample> retBiosampleList = biosampleService.findBioSamplesBySampleIdIgnoreCase(biosample.getSampleDescriptor());
        if (retBiosampleList != null && retBiosampleList.size() > 0) {
        	logger.error("biosample with sample descriptor " + biosample.getSampleDescriptor() + " already exists in the database!");
            return new ResponseEntity<BioSample>(HttpStatus.CONFLICT);
        }
        
        BioSample retBiosample = biosampleService.saveBioSample(biosample);

        if (retBiosample == null) {
        	logger.error("Error saving biosample!");
            return new ResponseEntity<BioSample>(HttpStatus.NOT_FOUND);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/biosamples/{id}").buildAndExpand(retBiosample.getId()).toUri());

        return new ResponseEntity<BioSample>(retBiosample, HttpStatus.CREATED);
    }

    // ------------------- Update a sample --------------------------------------
    @PutMapping("/biosamples/{id}")
    public ResponseEntity<BioSample> updateBioSampleByID(@PathVariable("id") long id, @RequestBody BioSample biosample) {
    
        logger.debug("Updating biosample " + biosample.getId());
        
    	BioSample biosampleCurrent = biosampleService.findBioSampleById(id);
        if (biosampleCurrent == null) {
        	logger.error("biosample with id " + id + " not found!");
            return new ResponseEntity<BioSample>(HttpStatus.NOT_FOUND);
        }
    	
    	BioSample retBiosample = biosampleService.updateBioSampleById(id, biosample);
    	if (retBiosample == null) {
        	logger.error("Error updating biosample!");
            return new ResponseEntity<BioSample>(HttpStatus.NOT_FOUND);
    	}
    	
    	return new ResponseEntity<BioSample>(biosampleCurrent, HttpStatus.OK);
    }
    
    // ------------------- Delete a sample ---------------------------------------
    @DeleteMapping("/biosamples/{id}")
    public ResponseEntity<BioSample> deleteBioSample(@PathVariable("id") Long id) {

        logger.debug("Fetching & Deleting Biosample with id " + id);

        BioSample biosample = biosampleService.findBioSampleById(id);
        if (biosample == null) {
        	logger.error("biosample with id " + id + " not found!");
            return new ResponseEntity<BioSample>(HttpStatus.NOT_FOUND);
        }
        
        // sanity check to see if there are any models within the study
        List<STRAnalysis> strAnalyses = biosampleService.findSTRAnalysesBySampleDescriptor(biosample.getSampleDescriptor());
        if (strAnalyses != null && strAnalyses.size() > 0) {
        	logger.error("sample contains STR analysis so cannot delete sample!");
        	return new ResponseEntity<BioSample>(HttpStatus.NOT_ACCEPTABLE);
        }
        
        BioSample retBiosample = biosampleService.deleteBioSampleById(id);
    	if (retBiosample == null) {
        	logger.error("Error deleting biosample!");
            return new ResponseEntity<BioSample>(HttpStatus.NOT_FOUND);
    	}
        
        return new ResponseEntity<BioSample>(HttpStatus.NO_CONTENT);
    }
    
    // ------------------- Save batch sample data --------------------------------------
    @PostMapping("/biosamplelist")
    public ResponseEntity<List<BioSample>> saveBioSampleList(@RequestBody List<BioSample> bioSampleList, UriComponentsBuilder ucBuilder) {

        logger.debug("Saving batch biosample data " + bioSampleList.size());
        
        // create the list of STR error msgs
        List<STRError> STRErrorList = new ArrayList<STRError>();

        List<BioSample> retBioSampleList = biosampleService.saveBioSampleList(bioSampleList, STRErrorList);
        
        // add the headers if the STR data list was partially created
        HttpHeaders headers = new HttpHeaders();
        headers.add("content-type", "application/json");
        if (STRErrorList.size() > 0) {

        	String errorText = "";
        	for (int i=0; i < STRErrorList.size(); i++) {
        		STRError strError = STRErrorList.get(i);
        		if (strError.getErrorCode() == HttpStatus.CONFLICT.value()) {
            		errorText += "- Sample Data for sample " + strError.getErrorSTRSampleID() + " was not uploaded because" + strError.getErrorText();  			
        		} else {
                    errorText += "- Sample Data was not uploaded. Please contact support with this error code: " + strError.getErrorCode();
        		}
        	}
        	headers.add("error-code", "Partial_Creation");
            headers.add("error-text", errorText);    
        }

        if (retBioSampleList == null || retBioSampleList.isEmpty()) {
            return new ResponseEntity<List<BioSample>>(headers, HttpStatus.PRECONDITION_FAILED);
        }
        
        return new ResponseEntity<List<BioSample>>(retBioSampleList, headers, HttpStatus.CREATED);
    }

}
