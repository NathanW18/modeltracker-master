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

@CrossOrigin(origins = "http://localhost:4200") // whatever the front end is running on
@RestController
@RequestMapping("/api")
public class AnalyticsController {

    public static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BioSampleService biosampleService;

    // ------------------- Search data field option --------------------------------------
    @GetMapping("/datafieldoptions")
    public ResponseEntity<List<DataFieldOption>> getDataFieldOptionsByFieldName(
    		@RequestParam(name = "fieldname", required = false) String fieldname) {
    	
        logger.debug("Searching data field option with parameter... ");

        List<DataFieldOption> dataFieldOptions = new ArrayList<DataFieldOption>();
        
        if (fieldname == null) {
        	dataFieldOptions = biosampleService.findDataFieldOptionsByFieldOptionName("all");
        } else {
        	dataFieldOptions = biosampleService.findDataFieldOptionsByFieldOptionName(fieldname);
        } 

        if (dataFieldOptions == null || dataFieldOptions.isEmpty()) {
        	logger.debug("no field options found!");
            return new ResponseEntity<List<DataFieldOption>>(HttpStatus.NO_CONTENT); // You many decide to return
                                                                           // HttpStatus.NOT_FOUND
        }

        return new ResponseEntity<List<DataFieldOption>>(dataFieldOptions, HttpStatus.OK);
    }
    
    // ------------------- Get study count --------------------------------------
    @GetMapping("/analytics/studies/recordcount")
    public ResponseEntity<List<DataCount>> getStudyAnalytics() {
    	
        logger.debug("Searching study count ... ");

        List<DataCount> countList = new ArrayList<DataCount>();
        DataCount count = new DataCount();
        count.setDataType("Study");
        count.setCount(biosampleService.getNumberOfStudies());
        countList.add(count);
        
        return new ResponseEntity<List<DataCount>>(countList, HttpStatus.OK);
    }

    // ------------------- Get biosample counts by type or site --------------------------------------
    @GetMapping("/analytics/biosamples/recordcount")
    public ResponseEntity<List<DataCount>> getBioSampleAnalyticsByParameters(
    		@RequestParam(value = "fieldname") Optional<String> field) {
    	
        logger.debug("Searching biosample count by parameter... ");
        
        List<DataCount> countList = new ArrayList<DataCount>();

        if (field.isPresent()) {
            countList = biosampleService.getBioSampleAnalyticsByField(field.get());
        	
        } else {
            DataCount count = new DataCount();
            count.setDataType("BioSample");
            count.setCount(biosampleService.getNumberOfBioSamples());
            countList.add(count);
        }

        if (countList == null || countList.isEmpty()) {
        	logger.debug("No counts found for specified parameter!");
            return new ResponseEntity<List<DataCount>>(HttpStatus.NO_CONTENT); // You many decide to return
                                                                           // HttpStatus.NOT_FOUND
        }

        return new ResponseEntity<List<DataCount>>(countList, HttpStatus.OK);
    }

    // ------------------- Get model count --------------------------------------
    @GetMapping("/analytics/models/recordcount")
    public ResponseEntity<List<DataCount>> getModelAnalytics() {
    	
        logger.debug("Searching model count ... ");

        List<DataCount> countList = new ArrayList<DataCount>();
        DataCount count = new DataCount();
        count.setDataType("Model");
        count.setCount(biosampleService.getNumberOfModels());
        countList.add(count);

        return new ResponseEntity<List<DataCount>>(countList, HttpStatus.OK);
    }

    // ------------------- Get patient count --------------------------------------
    @GetMapping("/analytics/patients/recordcount")
    public ResponseEntity<List<DataCount>> getPatientAnalytics() {
    	
        logger.debug("Searching patient count ... ");

        List<DataCount> countList = new ArrayList<DataCount>();
        DataCount count = new DataCount();
        count.setDataType("Patient");
        count.setCount(biosampleService.getNumberOfPatients());
        countList.add(count);

        return new ResponseEntity<List<DataCount>>(countList, HttpStatus.OK);
    }

    // ------------------- Get STRAnalysis count --------------------------------------
    @GetMapping("/analytics/stranalyses/recordcount")
    public ResponseEntity<List<DataCount>> getSTRAnalysisAnalytics() {
    	
        logger.debug("Searching STR analysis count ... ");

        List<DataCount> countList = new ArrayList<DataCount>();
        DataCount count = new DataCount();
        count.setDataType("STRAnalysis");
        count.setCount(biosampleService.getNumberOfSTRAnalyses());
        countList.add(count);
        
        return new ResponseEntity<List<DataCount>>(countList, HttpStatus.OK);
    }

    // ------------------- Get STRAnalysis average --------------------------------------
    @GetMapping("/analytics/stranalyses/averagestrmatch")
    public ResponseEntity<DataCount> getAverageSTRMatchAnalytics() {
    	
        logger.debug("Getting STR analysis average STR match ... ");

        DataCount count = new DataCount();
        count.setDataType("AverageSTR");
        count.setCount((int)biosampleService.getAverageSTRPercentageMatch() * 100);
        
        return new ResponseEntity<DataCount>(count, HttpStatus.OK);
    }

    // ------------------- Get STRAnalysis average --------------------------------------
    @GetMapping("/analytics/stranalyses/averagestrmatch/study/{id}")
    public ResponseEntity<DataCount> getAverageSTRMatchByStudyAnalytics(@PathVariable("id") String id) {
    	
        logger.debug("Getting STR analysis average STR match by study ... ");

        DataCount count = new DataCount();
        count.setDataType("AverageSTRByStudy");
        count.setCount((int)biosampleService.getAverageSTRPercentageMatchByStudyId(id) * 100);
        
        return new ResponseEntity<DataCount>(count, HttpStatus.OK);
    }

    // ------------------- Get STRAnalysis average --------------------------------------
    @GetMapping("/analytics/stranalyses/averagestrmatch/model/{id}")
    public ResponseEntity<DataCount> getAverageSTRMatchByModelAnalytics(@PathVariable("id") String id) {
    	
        logger.debug("Getting STR analysis average STR match by model ... ");

        DataCount count = new DataCount();
        count.setDataType("AverageSTRByModel");
        count.setCount((int)biosampleService.getAverageSTRPercentageMatchByModelId(id) * 100);
        
        return new ResponseEntity<DataCount>(count, HttpStatus.OK);
    }

    // ------------------- Get STRAnalysis average --------------------------------------
    @GetMapping("/analytics/stranalyses/strmatchhistogram")
    public ResponseEntity<List<DataCount>> getSTRMatchHistogramAnalytics() {
    	
        logger.debug("Getting STR analysis histogram ... ");

        List<DataCount> countList = biosampleService.getSTRPercentageMatchHistogram();
        
        return new ResponseEntity<List<DataCount>>(countList, HttpStatus.OK);
    }

    // ------------------- Get STRAnalysis average --------------------------------------
    @GetMapping("/analytics/stranalyses/strmatchhistogram/study/{id}")
    public ResponseEntity<List<DataCount>> getSTRMatchHistogramByStudyAnalytics(@PathVariable("id") String id) {
    	
        logger.debug("Getting STR analysis histogram by study ... ");

        List<DataCount> countList = biosampleService.getSTRPercentageMatchHistogramByStudyId(id);
        
        return new ResponseEntity<List<DataCount>>(countList, HttpStatus.OK);
    }

    // ------------------- Get STRAnalysis average --------------------------------------
    @GetMapping("/analytics/stranalyses/strmatchhistogram/model/{id}")
    public ResponseEntity<List<DataCount>> getSTRMatchHistogramByModelAnalytics(@PathVariable("id") String id) {
    	
        logger.debug("Getting STR analysis histogram by model ... ");

        List<DataCount> countList = biosampleService.getSTRPercentageMatchHistogramByModelId(id);
        
        return new ResponseEntity<List<DataCount>>(countList, HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = { EmptyResultDataAccessException.class, EntityNotFoundException.class })
    public void handleNotFound() {
    }
}
