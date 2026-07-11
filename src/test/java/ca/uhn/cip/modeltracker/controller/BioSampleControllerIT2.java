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
import ca.uhn.cip.modeltracker.builder.ModelBuilder;
import ca.uhn.cip.modeltracker.model.Model;
import ca.uhn.cip.modeltracker.repository.ModelRepository;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = ModeltrackerApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
//@IntegrationTest("server.port:0")
public class BioSampleControllerIT2 {   

    private static final String ROOT_PATH = "/api";
    private static final String MODELS_RESOURCE = "/models";
    private static final String MODEL_RESOURCE = "/models/{id}";
    private static final String NON_EXISTING_ID = "999999999999";
    private static final String NON_EXISTING_STUDY = "demo";

    private static final String MODELDISPLAYNAME_FIELD = "modelDisplayName";
    private static final String PPID_FIELD = "protocolParticipantID";

    private static final String FIRST_MODEL_IDENTIFIER = "001";
    private static final String FIRST_MODEL_STUDYSHORTNAME = "PHLC";
    private static final String FIRST_MODEL_PPID = "G_12345";
    private static final String SECOND_MODEL_IDENTIFIER = "002";
    private static final String SECOND_MODEL_STUDYSHORTNAME = "SCND";
    private static final String SECOND_MODEL_PPID = "G_12346";
    private static final String THIRD_MODEL_IDENTIFIER = "003";
    private static final String THIRD_MODEL_STUDYSHORTNAME = "THRD";
    private static final String THIRD_MODEL_PPID = "G_12347";

    @LocalServerPort
    int randomServerPort;
    
    private static final Model FIRST_MODEL = new ModelBuilder()
    		.modelIdentifier(FIRST_MODEL_IDENTIFIER)
    		.studyShortName(FIRST_MODEL_STUDYSHORTNAME)
    		.modelDisplayName(FIRST_MODEL_STUDYSHORTNAME + FIRST_MODEL_IDENTIFIER)
    		.protocolParticipantId(FIRST_MODEL_PPID)
    		.build();
    private static final Model SECOND_MODEL = new ModelBuilder()
            .modelIdentifier(SECOND_MODEL_IDENTIFIER)
            .studyShortName(SECOND_MODEL_STUDYSHORTNAME)
            .modelDisplayName(SECOND_MODEL_STUDYSHORTNAME + SECOND_MODEL_IDENTIFIER)
            .protocolParticipantId(SECOND_MODEL_PPID)
            .build();
    private static final Model THIRD_MODEL = new ModelBuilder()
            .modelIdentifier(THIRD_MODEL_IDENTIFIER)
            .studyShortName(THIRD_MODEL_STUDYSHORTNAME)
            .modelDisplayName(THIRD_MODEL_STUDYSHORTNAME + THIRD_MODEL_IDENTIFIER)
            .protocolParticipantId(THIRD_MODEL_PPID)
            .build();

    @Autowired
    private ModelRepository modelRepository;

    @Value("${local.server.port}")
    private int serverPort;

    private Model firstModel;
    private Model secondModel;
    private Model thirdModel;
    
    @Before
    public void setUp() {
        // modelRepository.deleteAll();
        firstModel = modelRepository.save(FIRST_MODEL);
        secondModel = modelRepository.save(SECOND_MODEL);
        // thirdModel = modelRepository.save(THIRD_MODEL);
        RestAssured.port = serverPort;
    }

    @Test
    public void getPort() {
        assertThat("Should get a random port greater than zero!", serverPort, greaterThan(0));
    }

    @Test
    public void getModelsShouldReturnAllModels() {
        when()
            .get(ROOT_PATH + MODELS_RESOURCE )
        .then()
            .statusCode(HttpStatus.SC_OK)
            .body(MODELDISPLAYNAME_FIELD, hasItems(FIRST_MODEL_STUDYSHORTNAME + FIRST_MODEL_IDENTIFIER, SECOND_MODEL_STUDYSHORTNAME + SECOND_MODEL_IDENTIFIER))
            .body(PPID_FIELD, hasItems(FIRST_MODEL_PPID, SECOND_MODEL_PPID));
    }

    @Test
    public void getModelByIdShouldReturnModel() {
        when()
            .get(ROOT_PATH + MODELS_RESOURCE + "/{id}/{studyname}", firstModel.getModelIdentifier(), firstModel.getStudyShortName())
        .then()
            .statusCode(HttpStatus.SC_OK)
            .body(MODELDISPLAYNAME_FIELD, is(FIRST_MODEL_STUDYSHORTNAME + FIRST_MODEL_IDENTIFIER))
            .body(PPID_FIELD, is(FIRST_MODEL_PPID));
    }

//    @Test
//    public void getPPIdByModelIdShouldReturnPPId() {
//        when()
//            .get(ROOT_PATH + MODELS_RESOURCE + "/{modelid}/ppid", firstModel.getModelDisplayName())
//        .then()
//            .statusCode(HttpStatus.SC_OK)
//            // .body(MODELDISPLAYNAME_FIELD, hasItems(FIRST_MODEL_STUDYSHORTNAME))
//            .body(PPID_FIELD, hasItems(FIRST_MODEL_PPID));
//    }
//
//    @Test
//    public void getModelByPPIdShouldReturnModels() {
//        when()
//            .get(ROOT_PATH + MODELS_RESOURCE + "/pi/{shortname}", firstModel.getProtocolParticipantID())
//        .then()
//            .statusCode(HttpStatus.SC_OK)
//            .body(MODELDISPLAYNAME_FIELD, hasItems(FIRST_MODEL_STUDYSHORTNAME))
//            .body(PPID_FIELD, hasItems(FIRST_MODEL_PPID));
//    }
//
//    @Test
//    public void getModelByModelDisplayNameShouldReturnModels() {
//        when()
//            .get(ROOT_PATH + MODELS_RESOURCE + "/pi/{shortname}", firstModel.getProtocolParticipantID())
//        .then()
//            .statusCode(HttpStatus.SC_OK)
//            .body(MODELDISPLAYNAME_FIELD, hasItems(FIRST_MODEL_STUDYSHORTNAME))
//            .body(PPID_FIELD, hasItems(FIRST_MODEL_PPID));
//    }

    @Test
    public void addModelShouldReturnSavedModel() {
        given()
            .body(THIRD_MODEL)
            .contentType(ContentType.JSON)
        .when()
            .post(ROOT_PATH + MODELS_RESOURCE)
        .then()
            .statusCode(HttpStatus.SC_CREATED)
            .body(MODELDISPLAYNAME_FIELD, is(THIRD_MODEL_STUDYSHORTNAME + THIRD_MODEL_IDENTIFIER))
            .body(PPID_FIELD, is(THIRD_MODEL_PPID));
    }

    @Test
    public void addModelShouldReturnBadRequestWithoutBody() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .post(ROOT_PATH + MODELS_RESOURCE)
        .then()
            .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void updateModelShouldReturnUpdatedModel() {
      // Given an unchecked first MODEL
      Model model = new ModelBuilder()
        .modelIdentifier(FIRST_MODEL_IDENTIFIER)
        .studyShortName(FIRST_MODEL_STUDYSHORTNAME)
        .protocolParticipantId(SECOND_MODEL_PPID)
        .build();
      given()
        .body(model)
        .contentType(ContentType.JSON)
      .when()
        .put(ROOT_PATH + MODELS_RESOURCE + "/{id}/{studyname}", firstModel.getModelIdentifier(), firstModel.getStudyShortName())
      .then()
        .statusCode(HttpStatus.SC_CREATED)
        .body(MODELDISPLAYNAME_FIELD, is(FIRST_MODEL_STUDYSHORTNAME + FIRST_MODEL_IDENTIFIER))
        .body(PPID_FIELD, is(SECOND_MODEL_PPID));
    }
      
    @Test
    public void updateModelShouldReturnBadRequestWithoutBody() {
      when()
        .put(ROOT_PATH + MODELS_RESOURCE + "/{id}/{studyname}", firstModel.getModelIdentifier(), firstModel.getStudyShortName())
      .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST);
    }
    
    @Test
    public void updateModelLShouldBeNotFoundIfNonExistingID() {
      given()
        .body(FIRST_MODEL)
        .contentType(ContentType.JSON)
      .when()
        .put(ROOT_PATH + MODELS_RESOURCE + "/{id}/{studyname}", NON_EXISTING_ID, NON_EXISTING_STUDY)
      .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);
    }
      
    @Test
    public void deleteModelShouldReturnNoContent() {
       when()
        .delete(ROOT_PATH + MODELS_RESOURCE + "/{id}/{studyname}", secondModel.getModelIdentifier(), secondModel.getStudyShortName())
      .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
    }
      
    @Test
    public void deleteModelShouldBeNotFoundIfNonExistingID() {
      when()
        .delete(ROOT_PATH + MODELS_RESOURCE + "/{id}/{studyname}", NON_EXISTING_ID, secondModel.getStudyShortName())
      .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);
    }
}
