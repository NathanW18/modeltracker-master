package ca.uhn.cip.modeltracker.controller;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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
import ca.uhn.cip.modeltracker.builder.StudyBuilder;
import ca.uhn.cip.modeltracker.model.Study;
import ca.uhn.cip.modeltracker.repository.StudyRepository;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = ModeltrackerApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
//@IntegrationTest("server.port:0")
public class BioSampleControllerIT {   

    private static final String ROOT_PATH = "/api";
    private static final String STUDIES_RESOURCE = "/studies";
    private static final String STUDY_RESOURCE = "/studies/{id}";
    private static final Long NON_EXISTING_ID = 999999999L;

    private static final String LONGNAME_FIELD = "studyLongName";
    private static final String SHORTNAME_FIELD = "studyShortName";
    private static final String PI_FIELD = "principleInvestigator";

    private static final String FIRST_STUDY_SHORTNAME = "First STUDY";
    private static final String FIRST_STUDY_LONGNAME = "First STUDY LONG";
    private static final String FIRST_STUDY_PI = "First STUDY PI";
    private static final String SECOND_STUDY_SHORTNAME = "Second STUDY";
    private static final String SECOND_STUDY_LONGNAME = "Second STUDY LONG";
    private static final String SECOND_STUDY_PI = "Second STUDY PI";
    private static final String THIRD_STUDY_SHORTNAME = "THIRD STUDY";
    private static final String THIRD_STUDY_LONGNAME = "THIRD STUDY LONG";
    private static final String THIRD_STUDY_PI = "Third STUDY PI";

    @LocalServerPort
    int randomServerPort;
    
    private static final Study FIRST_STUDY = new StudyBuilder()
        .shortName(FIRST_STUDY_SHORTNAME)
        .longName(FIRST_STUDY_LONGNAME)
        .principleInvestigator(FIRST_STUDY_PI)
        .build();
    private static final Study SECOND_STUDY = new StudyBuilder()
        .shortName(SECOND_STUDY_SHORTNAME)
        .longName(SECOND_STUDY_LONGNAME)
        .principleInvestigator(SECOND_STUDY_PI)
        .build();
    private static final Study THIRD_STUDY = new StudyBuilder()
        .shortName(THIRD_STUDY_SHORTNAME)
        .longName(THIRD_STUDY_LONGNAME)
        .principleInvestigator(THIRD_STUDY_PI)
        .build();

    @Autowired
    private StudyRepository studyRepository;

    @Value("${local.server.port}")
    private int serverPort;

    private Study firstStudy;
    private Study secondStudy;
    private Study thirdStudy;
    
    @Before
    public void setUp() {
        // studyRepository.deleteAll();
        firstStudy = studyRepository.save(FIRST_STUDY);
        secondStudy = studyRepository.save(SECOND_STUDY);
        // thirdStudy = studyRepository.save(THIRD_STUDY);
        RestAssured.port = serverPort;
    }

    @Test
    public void getPort() {
        assertThat("Should get a random port greater than zero!", serverPort, greaterThan(0));
    }

    @Test
    public void getStudiesShouldReturnBothStudies() {
        when()
            .get(ROOT_PATH + STUDIES_RESOURCE )
        .then()
            .statusCode(HttpStatus.SC_OK)
            .body(SHORTNAME_FIELD, hasItems(FIRST_STUDY_SHORTNAME, SECOND_STUDY_SHORTNAME))
            .body(LONGNAME_FIELD, hasItems(FIRST_STUDY_LONGNAME, SECOND_STUDY_LONGNAME))
            .body(PI_FIELD, hasItems(FIRST_STUDY_PI, SECOND_STUDY_PI));
    }

    @Test
    public void getStudyByIDShouldReturnStudy() {
        when()
            .get(ROOT_PATH + STUDIES_RESOURCE + "/{id}", firstStudy.getStudyShortName())
        .then()
            .statusCode(HttpStatus.SC_OK)
            .body(SHORTNAME_FIELD, is(FIRST_STUDY_SHORTNAME))
            .body(LONGNAME_FIELD, is(FIRST_STUDY_LONGNAME))
        	.body(PI_FIELD, is(FIRST_STUDY_PI));
    }
    
    @Test
    public void addStudyShouldReturnSavedStudy() {
        given()
            .body(THIRD_STUDY)
            .contentType(ContentType.JSON)
        .when()
            .post(ROOT_PATH + STUDIES_RESOURCE)
        .then()
            .statusCode(HttpStatus.SC_CREATED)
            .body(SHORTNAME_FIELD, is(THIRD_STUDY_SHORTNAME))
            .body(LONGNAME_FIELD, is(THIRD_STUDY_LONGNAME))
    		.body(PI_FIELD, is(THIRD_STUDY_PI));
    }

    @Test
    public void addStudyShouldReturnBadRequestWithoutBody() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .post(ROOT_PATH + STUDIES_RESOURCE)
        .then()
            .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void updateStudyShouldReturnUpdatedStudy() {
      // Given an unchecked first study
      Study study = new StudyBuilder()
        .shortName(firstStudy.getStudyShortName())
        .longName(SECOND_STUDY_LONGNAME)
        .principleInvestigator(THIRD_STUDY_PI)
        .build();
      given()
        .body(study)
        .contentType(ContentType.JSON)
      .when()
        .put(ROOT_PATH + STUDIES_RESOURCE + "/{id}", firstStudy.getStudyShortName())
      .then()
        .statusCode(HttpStatus.SC_OK)
        .body(PI_FIELD, is(THIRD_STUDY_PI))
        .body(LONGNAME_FIELD, is(SECOND_STUDY_LONGNAME));
    }
      
    @Test
    public void updateStudyShouldReturnBadRequestWithoutBody() {
      when()
        .put(ROOT_PATH + STUDIES_RESOURCE + "/{id}", firstStudy.getStudyShortName())
      .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST);
    }
    
    @Test
    public void updateStudyShouldBeBadRequestIfNonExistingID() {
      given()
        .body(FIRST_STUDY)
        .contentType(ContentType.JSON)
      .when()
        .put(ROOT_PATH + STUDIES_RESOURCE + "/{id}", NON_EXISTING_ID)
      .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST);
    }
      
    @Test
    public void deleteStudyShouldReturnNoContent() {
       when()
        .delete(ROOT_PATH + STUDIES_RESOURCE + "/{id}", secondStudy.getStudyShortName())
      .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
    }
      
    @Test
    public void deleteStudyShouldBeBadRequestIfNonExistingID() {
      when()
        .delete(ROOT_PATH + STUDIES_RESOURCE + "/{id}", NON_EXISTING_ID)
      .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST);
    }
}
