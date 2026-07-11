package ca.uhn.cip.modeltracker.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.cip.modeltracker.model.Model;
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
public class PatientController {

    public static final Logger logger = LoggerFactory.getLogger(ModelController.class);

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BioSampleService biosampleService;
    
    // ------------------- Search models by shortname or PI --------------------------------------
    @GetMapping("/ppids")
    public ResponseEntity<List<String>> getPPIDsByParameters(
            @RequestParam(name = "modeltracker-ppid", required = true) String ppid) {
    	
        logger.debug("Searching ppids by parameter ... ");

        List<String> retPPIDList = new ArrayList<String>();
        
        if (ppid == null ) {
            logger.debug("ppid parameter cannot be empty!");
            return new ResponseEntity<List<String>>(HttpStatus.BAD_REQUEST);
        } else if (ppid != null) {
        	retPPIDList = biosampleService.findPPIDsByPPIDContaining(ppid);
        } 

        if (retPPIDList == null || retPPIDList.isEmpty()) {
        	logger.debug("No PPIDs found for specified parameter!");
            return new ResponseEntity<List<String>>(HttpStatus.NO_CONTENT); // You many decide to return
                                                                           // HttpStatus.NOT_FOUND
        }

        return new ResponseEntity<List<String>>(retPPIDList, HttpStatus.OK);
    }


}
