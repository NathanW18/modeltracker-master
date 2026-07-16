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
public class ModelController {

    public static final Logger logger = LoggerFactory.getLogger(ModelController.class);

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BioSampleService biosampleService;

    // ------------------- Read a model -----------------------------------------------
    @GetMapping("/models/{modeldisplayname}")
    public ResponseEntity<Model> getModelByID(@PathVariable("modeldisplayname") String modelDisplayName) {
 
        logger.debug("Searching model by model Id " + modelDisplayName);

        Model model = biosampleService.findModelByModelDisplayName(modelDisplayName);
        if (model == null) {
        	logger.debug("model with modelDisplayName:" + modelDisplayName + " not found!");
            return new ResponseEntity<Model>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Model>(model, HttpStatus.OK);

    }

    // ------------------- Search models by shortname or PI --------------------------------------
    @GetMapping("/models")
    public ResponseEntity<List<Model>> getModelsByParameters(
    		@RequestParam(name = "modeldisplayname", required = false) String modeldisplayname,
            @RequestParam(name = "ppid", required = false) List<String> ppids,
            @RequestParam(name = "exactmatch", required = false, defaultValue = "false") boolean exactMatch) {
    	
        logger.debug("Searching model by parameter ... ");

        List<Model> models = new ArrayList<Model>();
        
        if (modeldisplayname != null && ppids != null && ppids.size() > 0) {
            logger.debug("cannot search by modeldisplayname and ppid at sametime!");
            return new ResponseEntity<List<Model>>(HttpStatus.NOT_FOUND);
        } else if (modeldisplayname != null) {
        	if (exactMatch) {
        		models = biosampleService.findModelsByModelDisplayNameIgnoreCase(modeldisplayname);
        	} else {
        		models = biosampleService.findModelsByModelDisplayNameIgnoreCaseContaining(modeldisplayname);
        	}
        } else if (ppids != null && ppids.size() > 0) {
        	if (exactMatch) {
        		models = biosampleService.findModelsByPPIdList(ppids);
        	} else {
        		// need to implement a raw SQL call to do partial match with LIKE and OR
        		models = biosampleService.findModelsByPPIdListContaining(ppids);
        	}
        } else {
            models = biosampleService.findAllModels();
        }

        if (models == null || models.isEmpty()) {
        	logger.debug("No Models found for specified parameter!");
            return new ResponseEntity<List<Model>>(HttpStatus.NO_CONTENT); // You many decide to return
                                                                           // HttpStatus.NOT_FOUND
        }

        return new ResponseEntity<List<Model>>(models, HttpStatus.OK);
    }

    // ------------------- Search model pageable --------------------------------------
    @GetMapping("/models/pagination")
    public ResponseEntity<Page<Model>> getModelsPageableByParameters(
    		Pageable pageable,
    		@RequestParam(name = "modeldisplayname", required = false) String modeldisplayname,
            @RequestParam(name = "ppid", required = false) List<String> ppids,
            @RequestParam(name = "exactmatch", required = false, defaultValue = "false") boolean exactMatch) {
    	
        logger.debug("Searching biosample pageable by parameters ... ");
    	
    	Page<Model> models = null;
    	
    	Pageable allModels = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        if (modeldisplayname != null && ppids != null && ppids.size() > 0) {
            logger.debug("cannot search by modeldisplayname and ppid at sametime!");
            return new ResponseEntity<Page<Model>>(HttpStatus.NOT_FOUND);
        } else if (modeldisplayname != null) {
        	if (exactMatch) {
                models = biosampleService.findModelsPageableByModelDisplayNameIgnoreCase(modeldisplayname, allModels);
        	} else {
                models = biosampleService.findModelsPageableByModelDisplayNameIgnoreCaseContaining(modeldisplayname, allModels);
        	}
        } else if ( ppids != null && ppids.size() > 0) {
            if (exactMatch) {
            	models = biosampleService.findModelsPageableByPPIdList(ppids, allModels);
            } else {
            	models = biosampleService.findModelsPageableByPPIdListContaining(ppids, allModels);
            }
        } else {
            models = biosampleService.findAllModelsPageable(allModels);
        }
        
        if (models == null || models.isEmpty()) {
        	logger.debug("No Models found for specified parameters!");
            return new ResponseEntity<Page<Model>>(HttpStatus.NO_CONTENT); // You many decide to return
                                                                               // HttpStatus.NOT_FOUND
        }

        return new ResponseEntity<Page<Model>>(models, HttpStatus.OK);
    }


    // ------------------- Get PPID by model id --------------------------------------
    @GetMapping("/models/{modelid}/ppid")
    public ResponseEntity<Patient> getPPIdByModelId(@PathVariable("modelid") String id) {
        
        logger.debug("Searching PPId by modeldisplayname...");
        
        List<Model> models = biosampleService.findModelsByModelDisplayNameIgnoreCase(id);

        if (models == null || models.isEmpty()) {
        	logger.debug("No PPId found for specified model!");
            return new ResponseEntity<Patient>(HttpStatus.NO_CONTENT); // You many decide to return
                                                                           // HttpStatus.NOT_FOUND
        }

        Patient pt = new Patient();
        pt.setProtocolParticipantId(models.get(0).getProtocolParticipantID());
        return new ResponseEntity<Patient>(pt, HttpStatus.OK);
    }

    // ------------------- Create a model ---------------------------------------
    @PostMapping("/models")
    public ResponseEntity<Model> saveModels(@RequestBody Model model, UriComponentsBuilder ucBuilder) {
    	
        logger.debug("Saving model " + model.getModelIdentifier());
        
    	if (model.getModelIdentifier() == null || model.getStudyShortName() == null) {
    		logger.error("model identifier or studyname is empty so cannot save!");
    		return new ResponseEntity<Model>(HttpStatus.BAD_REQUEST);
    	}

        String modelDisplayName = model.getStudyShortName() + model.getModelIdentifier();
        Model existModel = biosampleService.findModelByModelDisplayName(modelDisplayName);
        if (existModel != null) {
        	logger.error("Duplicate model existed in database, cannot save!");
            return new ResponseEntity<Model>(HttpStatus.CONFLICT);
        }

        Model retModel = biosampleService.saveModel(model);

        if (retModel == null) {
        	logger.error("Error saving model!");
            return new ResponseEntity<Model>(HttpStatus.NOT_FOUND);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/models/{id}/{studyname}").buildAndExpand(retModel.getModelIdentifier(), retModel.getStudyShortName()).toUri());

        return new ResponseEntity<Model>(retModel, HttpStatus.CREATED);
    }

    // ------------------- Update a model -----------------------------------------------
    @PutMapping("/models/{modeldisplayname}")
    public ResponseEntity<Model> updateModel(@PathVariable("modeldisplayname") String modelDisplayName, 
            @RequestBody Model model,
            UriComponentsBuilder ucBuilder) {

        logger.debug("Updating model " + model.getModelDisplayName());

        Model retModel = biosampleService.updateModelByModelDisplayName(modelDisplayName, model);

        if (retModel == null) {
        	logger.debug("Error Updating model " + model.getModelDisplayName());
            return new ResponseEntity<Model>(HttpStatus.NOT_FOUND);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/models/{id}/{studyname}").buildAndExpand(retModel.getModelIdentifier(), retModel.getStudyShortName()).toUri());

        return new ResponseEntity<Model>(retModel, HttpStatus.CREATED);
    }

    // ------------------- Delete a Model -----------------------------------------------
    @DeleteMapping("/models/{modeldisplayname}")
    public ResponseEntity<Model> deleteModel(@PathVariable("modeldisplayname") String modelDisplayName) {

        logger.debug("Fetching & Deleting Model with modeldisplayname:" + modelDisplayName);

        Model model = biosampleService.findModelByModelDisplayName(modelDisplayName);
        if (model == null) {
            logger.error("Unable to delete. Model with modeldisplayname " + modelDisplayName + " not found");
            return new ResponseEntity<Model>(HttpStatus.NOT_FOUND);
        }
        
        // sanity check to see if there are any samples within the model
        List<BioSample> biosamples = biosampleService.findBioSamplesByModelIdIgnoreCase(modelDisplayName);
        if (biosamples != null && biosamples.size() > 0) {
        	logger.error("models contains biosamples so cannot delete models!");
        	return new ResponseEntity<Model>(HttpStatus.NOT_ACCEPTABLE);
        }

        biosampleService.deleteModelByModelDisplayName(modelDisplayName);
        return new ResponseEntity<Model>(HttpStatus.NO_CONTENT);
    }

}
