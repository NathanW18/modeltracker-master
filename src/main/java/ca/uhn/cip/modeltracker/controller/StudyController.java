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

public class StudyController {

    public static final Logger logger = LoggerFactory.getLogger(StudyController.class);

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BioSampleService biosampleService;

    // ------------------- Read a study -----------------------------------------------
    @GetMapping("/studies/{id}")
    public ResponseEntity<Study> getStudyByID(@PathVariable("id") String id) {
    	
        logger.debug("Searching study by Id ... ");

        Study study = biosampleService.findStudyById(id);
        if (study == null) {
        	logger.debug("study with id " + id + " not found!");
            return new ResponseEntity<Study>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Study>(study, HttpStatus.OK);

    }

    // ------------------- Search studies by shortname or PI --------------------------------------
    @GetMapping("/studies")
    public ResponseEntity<List<Study>> getStudiesByParameters(
            @RequestParam(name = "shortname", required = false) String shortname,
            @RequestParam(name = "pi", required = false) String pi,
            @RequestParam(name = "exactmatch", required = false, defaultValue = "false") boolean exactMatch) {

        logger.debug("Searching studies with parameter ... ");

        List<Study> studies = new ArrayList<Study>();

        if (shortname != null && pi != null) {
            logger.debug("cannot search by shortname and pi at sametime!");
            return new ResponseEntity<List<Study>>(HttpStatus.NOT_FOUND);
        } else if (shortname != null) {
        	if (exactMatch) {
        		studies = biosampleService.findStudiesByShortNameIgnoreCase(shortname);
        	} else {
        		studies = biosampleService.findStudiesByShortNameIgnoreCaseContaining(shortname);
        	}
        } else if (pi != null) {
        	if (exactMatch) {
        		studies = biosampleService.findStudiesByPIIgnoreCase(pi);
        	} else {
        		studies = biosampleService.findStudiesByPIIgnoreCaseContaining(pi);
        	}
        } else {
            studies = biosampleService.findAllStudies();
        }

        if (studies == null || studies.isEmpty()) {
        	logger.debug("Error searching studies!");
            return new ResponseEntity<List<Study>>(HttpStatus.NO_CONTENT); // You many decide to return
                                                                           // HttpStatus.NOT_FOUND
        }

        return new ResponseEntity<List<Study>>(studies, HttpStatus.OK);
    }

    // ------------------- Create a study ---------------------------------------
    @PostMapping("/studies")
    public ResponseEntity<Study> saveStudy(@RequestBody Study study, UriComponentsBuilder ucBuilder) {

        logger.debug("Saving study " + study.getStudyShortName());
        
        Study existStudy = biosampleService.findStudyById(study.getStudyShortName());
        if (existStudy != null) {
        	logger.debug("Cannot save study, no study " + study.getStudyShortName() + " found!");
        	return new ResponseEntity<Study>(HttpStatus.CONFLICT);
        }

        Study retStudy = biosampleService.saveStudy(study);

        if (retStudy == null) {
        	logger.error("Error Saving study " + study.getStudyShortName());
            return new ResponseEntity<Study>(HttpStatus.NOT_FOUND);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/studies/{id}").buildAndExpand(retStudy.getStudyShortName()).toUri());

        return new ResponseEntity<Study>(retStudy, HttpStatus.CREATED);
    }

    // ------------------- Update a study ----------------------------------------------
    @PutMapping("/studies/{id}")
    public ResponseEntity<Study> updateStudy(@PathVariable("id") String id, @RequestBody Study study,
            UriComponentsBuilder ucBuilder) {

        logger.debug("Updating study " + study.getStudyShortName());

        Study retStudy = biosampleService.updateStudyById(id, study);

        if (retStudy == null) {
        	logger.error("Error updating study " + study.getStudyShortName());
            return new ResponseEntity<Study>(HttpStatus.BAD_REQUEST);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/studies/{id}").buildAndExpand(retStudy.getStudyShortName()).toUri());

        return new ResponseEntity<Study>(retStudy, HttpStatus.OK);
    }

    // ------------------- Delete a Study ---------------------------------------
    @DeleteMapping("/studies/{id}")
    public ResponseEntity<Study> deleteStudy(@PathVariable("id") String id) {

        logger.debug("Fetching & Deleting Study with id " + id);

        Study study = biosampleService.findStudyById(id);
        if (study == null) {
            logger.debug("Unable to delete. Study with id " + id + " not found");
            return new ResponseEntity<Study>(HttpStatus.BAD_REQUEST);
        }
        
        // sanity check to see if there are any models within the study
        List<Model> models = biosampleService.findModelsByStudyId(id);
        if (models != null && models.size() > 0) {
        	logger.debug("study contains models so cannot delete!");
        	return new ResponseEntity<Study>(HttpStatus.NOT_ACCEPTABLE);
        }

        biosampleService.deleteStudyById(id);
        return new ResponseEntity<Study>(study, HttpStatus.NO_CONTENT);
    }

}
