package ca.uhn.cip.modeltracker.controller;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import ca.uhn.cip.modeltracker.ModeltrackerApplication;
import ca.uhn.cip.modeltracker.builder.STRDataBuilder;
import ca.uhn.cip.modeltracker.builder.StudyBuilder;
import ca.uhn.cip.modeltracker.model.STRData;
import ca.uhn.cip.modeltracker.model.Study;
import ca.uhn.cip.modeltracker.repository.STRDataRepository;
import ca.uhn.cip.modeltracker.repository.StudyRepository;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = ModeltrackerApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
//@IntegrationTest("server.port:0")
public class BioSampleControllerIT3 {   

    private static final String ROOT_PATH = "/api";
    private static final String STRDATA_RESOURCE = "/strdata";
    private static final String STRDATALIST_RESOURCE = "/strdatalist";
    private static final Long NON_EXISTING_ID = 999999999L;

    private static final String PANEL_FIELD = "panel";
    private static final String SAMPLEDESCRIPTOR_FIELD = "sampleDescriptor";
    private static final String MARKER_FIELD = "marker";
    private static final String ALLELE1_FIELD = "allele1";
    private static final String ALLELE2_FIELD = "allele2";
    private static final String RECEIVINGDATE_FIELD = "receivingDate";

    private static final String FIRST_STRDATA_PANEL = "First STRDATA PANEL";
    private static final String FIRST_STRDATA_SAMPLEDESCRIPTOR = "First STRDATA SAMPLE DESCRIPTOR";
    private static final String FIRST_STRDATA_MARKER = "First STRDATA MARKER";
    private static final String FIRST_STRDATA_ALLELE1 = "First STRDATA ALLELE1";
    private static final String FIRST_STRDATA_ALLELE2 = "First STRDATA ALLELE2";
    private static final String FIRST_STRDATA_ALLELE3 = "First STRDATA ALLELE3";
    private static final String FIRST_STRDATA_ALLELE4 = "First STRDATA ALLELE4";
    private static final String FIRST_STRDATA_ALLELE1N2 = "First STRDATA ALLELE1N2";
    private static final String FIRST_STRDATA_RECEIVINGDATE = "2019-10-01";
    private static final String SECOND_STRDATA_PANEL = "Second STRDATA PANEL";
    private static final String SECOND_STRDATA_SAMPLEDESCRIPTOR = "First STRDATA SAMPLE DESCRIPTOR";
    private static final String SECOND_STRDATA_MARKER = "SECOND STRDATA MARKER";
    private static final String SECOND_STRDATA_ALLELE1 = "Second STRDATA ALLELE1";
    private static final String SECOND_STRDATA_ALLELE2 = "Second STRDATA ALLELE2";
    private static final String SECOND_STRDATA_ALLELE3 = "Second STRDATA ALLELE3";
    private static final String SECOND_STRDATA_ALLELE4 = "Second STRDATA ALLELE4";
    private static final String SECOND_STRDATA_ALLELE1N2 = "Second STRDATA ALLELE1N2";
    private static final String SECOND_STRDATA_RECEIVINGDATE = "2019-12-01";
    private static final String THIRD_STRDATA_PANEL = "THIRD STRDATA PANEL";
    private static final String THIRD_STRDATA_SAMPLEDESCRIPTOR = "THIRD STRDATA SAMPLE DESCRIPTOR";
    private static final String THIRD_STRDATA_MARKER = "THIRD STRDATA MARKER";
    private static final String THIRD_STRDATA_ALLELE1 = "THIRD STRDATA ALLELE1";
    private static final String THIRD_STRDATA_ALLELE2 = "THIRD STRDATA ALLELE2";
    private static final String THIRD_STRDATA_ALLELE3 = "THIRD STRDATA ALLELE3";
    private static final String THIRD_STRDATA_ALLELE4 = "THIRD STRDATA ALLELE4";
    private static final String THIRD_STRDATA_ALLELE1N2 = "THIRD STRDATA ALLELE1N2";
    private static final String THIRD_STRDATA_COLLECTIONDATE = "2019-11-01";


    @LocalServerPort
    int randomServerPort;
    
    private static final STRData FIRST_STRDATA = new STRDataBuilder()
    		.panel(FIRST_STRDATA_PANEL)
    		.sampleDescriptor(FIRST_STRDATA_SAMPLEDESCRIPTOR)
    		.marker(FIRST_STRDATA_MARKER)
    		.allele1(FIRST_STRDATA_ALLELE1)
    		.allele2(FIRST_STRDATA_ALLELE2)
    		.allele3(FIRST_STRDATA_ALLELE3)
    		.allele4(FIRST_STRDATA_ALLELE4)
    		.allele1n2(FIRST_STRDATA_ALLELE1N2)
    		.receivingData(FIRST_STRDATA_RECEIVINGDATE)
    		.build();
    private static final STRData SECOND_STRDATA = new STRDataBuilder()
            .panel(SECOND_STRDATA_PANEL)
    		.sampleDescriptor(SECOND_STRDATA_SAMPLEDESCRIPTOR)
    		.marker(SECOND_STRDATA_MARKER)
            .allele1(SECOND_STRDATA_ALLELE1)
            .allele2(SECOND_STRDATA_ALLELE2)
            .allele3(SECOND_STRDATA_ALLELE3)
            .allele4(SECOND_STRDATA_ALLELE4)
            .allele1n2(SECOND_STRDATA_ALLELE1N2)
            .receivingData(SECOND_STRDATA_RECEIVINGDATE)
            .build();
    private static final STRData THIRD_STRDATA = new STRDataBuilder()
            .panel(THIRD_STRDATA_PANEL)
    		.sampleDescriptor(THIRD_STRDATA_SAMPLEDESCRIPTOR)
    		.marker(THIRD_STRDATA_MARKER)
            .allele1(THIRD_STRDATA_ALLELE1)
            .allele2(THIRD_STRDATA_ALLELE2)
            .allele3(THIRD_STRDATA_ALLELE3)
            .allele4(THIRD_STRDATA_ALLELE4)
            .allele1n2(THIRD_STRDATA_ALLELE1N2)
            .receivingData(THIRD_STRDATA_COLLECTIONDATE)
            .build();

    @Autowired
    private STRDataRepository strDataRepository;

    @Value("${local.server.port}")
    private int serverPort;

    private STRData firstSTRData;
    private STRData secondSTRData;
    private STRData thirdSTRData;
    
    @Before
    public void setUp() {
    	// strDataRepository.deleteAll();
    	firstSTRData = strDataRepository.save(FIRST_STRDATA);
    	secondSTRData = strDataRepository.save(SECOND_STRDATA);

        RestAssured.port = serverPort;
    }

    @Test
    public void getPort() {
        assertThat("Should get a random port greater than zero!", serverPort, greaterThan(0));
    }

    @Test
    public void getSTRDataBySampleDescriptorShouldReturnSTRData() {
        when()
            .get(ROOT_PATH + STRDATA_RESOURCE + "/{id}", FIRST_STRDATA_SAMPLEDESCRIPTOR)
        .then()
            .statusCode(HttpStatus.SC_OK)
            .body(PANEL_FIELD, hasItems(FIRST_STRDATA_PANEL, SECOND_STRDATA_PANEL))
            .body(SAMPLEDESCRIPTOR_FIELD, hasItems(FIRST_STRDATA_SAMPLEDESCRIPTOR, SECOND_STRDATA_SAMPLEDESCRIPTOR))
            .body(MARKER_FIELD, hasItems(FIRST_STRDATA_MARKER, SECOND_STRDATA_MARKER))
            .body(ALLELE1_FIELD, hasItems(FIRST_STRDATA_ALLELE1, SECOND_STRDATA_ALLELE1))
        	.body(ALLELE2_FIELD, hasItems(FIRST_STRDATA_ALLELE2, SECOND_STRDATA_ALLELE2))
        	.body(RECEIVINGDATE_FIELD, hasItems(FIRST_STRDATA_RECEIVINGDATE, SECOND_STRDATA_RECEIVINGDATE));
    }
    
    @Test
    public void addSTRDataShouldReturnSavedSTRData() {
    	List<STRData> list = new ArrayList<STRData>();
    	list.add(THIRD_STRDATA);
        given()
            .body(list)
            .contentType(ContentType.JSON)
        .when()
            .post(ROOT_PATH + STRDATALIST_RESOURCE)
        .then()
            .statusCode(HttpStatus.SC_CREATED);
//            .body(PANEL_FIELD, is(THIRD_STRDATA_PANEL))
//            .body(SAMPLEDESCRIPTOR_FIELD, is(THIRD_STRDATA_SAMPLEDESCRIPTOR))
//            .body(MARKER_FIELD, is(THIRD_STRDATA_MARKER))
//            .body(ALLELE1_FIELD, is(THIRD_STRDATA_ALLELE1))
//        	.body(ALLELE2_FIELD, is(THIRD_STRDATA_ALLELE2))
//        	.body(COLLECTIONDATE_FIELD, is(THIRD_STRDATA_COLLECTIONDATE));
    }

    @Test
    public void addSTRDataShouldReturnBadRequestWithoutBody() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .post(ROOT_PATH + STRDATALIST_RESOURCE)
        .then()
            .statusCode(HttpStatus.SC_BAD_REQUEST);
    }
      
}
