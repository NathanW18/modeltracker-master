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
import ca.uhn.cip.modeltracker.builder.BioSampleBuilder;
import ca.uhn.cip.modeltracker.builder.StudyBuilder;
import ca.uhn.cip.modeltracker.model.BioSample;
import ca.uhn.cip.modeltracker.model.Study;
import ca.uhn.cip.modeltracker.repository.BioSampleRepository;
import ca.uhn.cip.modeltracker.repository.StudyRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ModeltrackerApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
//@IntegrationTest("server.port:0")
public class BioSampleControllerIT4 {

	private static final String ROOT_PATH = "/api";
	private static final String BIOSAMPLES_RESOURCE = "/biosamples";
	private static final String BIOSAMPLE_RESOURCE = "/biosamples/{id}";
	private static final Long NON_EXISTING_ID = 999999999L;

	private static final String MODELDISPLAYNAME_FIELD = "modelDisplayName";
	private static final String SAMPLEDESCRIPTOR_FIELD = "sampleDescriptor";
	private static final String PPID_FIELD = "protocolPaticipantID";
	private static final String SAMPLETYPE_FIELD = "sampleType";
	private static final String PRIMARYDISEASESITE_FIELD = "primaryDiseaseSite";
	private static final String RECEIVINGDATE_FIELD = "recievingDate";
	private static final String PERCENTAGESTRMATCH_FIELD = "percentageSTRMatch";
	private static final String REFERENCESAMPLEDESCRIPTOR_FIELD = "referenceSampleDescriptor";
	
	private static final String FIRST_BIOSAMPLE_MODELDISPLAYNAME = "First SAMPLE MODEL ID";
	private static final String FIRST_BIOSAMPLE_SAMPLEDESCRIPTOR = "First SAMPLE DESCRIPTOR";
	private static final String FIRST_BIOSAMPLE_PPID = "First SAMPLE MODEL PPID";
	private static final String FIRST_BIOSAMPLE_SAMPLETYPE = "First SAMPLE TYPE";
	private static final String FIRST_BIOSAMPLE_PRIMARYDISEASESITE = "First SAMPLE DISEASE SITE";
	private static final String FIRST_BIOSAMPLE_COLLECTIONDATE = "2019-10-01";
	private static final Float FIRST_BIOSAMPLE_PERCENTAGESTRMATCH = 0.95f;
	private static final String FIRST_BIOSAMPLE_REFERENCESAMPLEDESCRIPTOR = "FIRST REFERENCE DESCRIPTOR";
	private static final String SECOND_BIOSAMPLE_MODELDISPLAYNAME = "SECOND SAMPLE MODEL ID";
	private static final String SECOND_BIOSAMPLE_SAMPLEDESCRIPTOR = "SECOND SAMPLE descriptor";
	private static final String SECOND_BIOSAMPLE_PPID = "SECOND SAMPLE MODEL PPID";
	private static final String SECOND_BIOSAMPLE_SAMPLETYPE = "SECOND SAMPLE TYPE";
	private static final String SECOND_BIOSAMPLE_PRIMARYDISEASESITE = "SECOND SAMPLE DISEASE SITE";
	private static final String SECOND_BIOSAMPLE_COLLECTIONDATE = "2019-09-01";
	private static final Float SECOND_BIOSAMPLE_PERCENTAGESTRMATCH = 0.85f;
	private static final String SECOND_BIOSAMPLE_REFERENCESAMPLEDESCRIPTOR = "SECOND REFERENCE DESCRIPTOR";
	private static final String THIRD_BIOSAMPLE_MODELDISPLAYNAME = "THIRD SAMPLE MODEL ID";
	private static final String THIRD_BIOSAMPLE_SAMPLEDESCRIPTOR = "THIRD SAMPLE DESCRIPTOR";
	private static final String THIRD_BIOSAMPLE_PPID = "THIRD SAMPLE PPID";
	private static final String THIRD_BIOSAMPLE_PRIMARYDISEASESITE = "THIRD SAMPLE DISEASE SITE";
	private static final String THIRD_BIOSAMPLE_SAMPLETYPE = "THIRD SAMPLE TYPE";
	private static final String THIRD_BIOSAMPLE_COLLECTIONDATE = "2019-07-01";
	private static final Float THIRD_BIOSAMPLE_PERCENTAGESTRMATCH = 0.75f;
	private static final String THIRD_BIOSAMPLE_REFERENCESAMPLEDESCRIPTOR = "THIRD REFERENCE DESCRIPTOR";

	@LocalServerPort
	int randomServerPort;

	private static final BioSample FIRST_BIOSAMPLE = new BioSampleBuilder()
			.modelDisplayName(FIRST_BIOSAMPLE_MODELDISPLAYNAME)
			.sampleDescriptor(FIRST_BIOSAMPLE_SAMPLEDESCRIPTOR)
			.protocolParticipantID(FIRST_BIOSAMPLE_PPID)
			.sampleType(FIRST_BIOSAMPLE_SAMPLETYPE)
			.primaryDiseaseSite(FIRST_BIOSAMPLE_PRIMARYDISEASESITE)
			.collectionDate(FIRST_BIOSAMPLE_COLLECTIONDATE)
			.build();
	private static final BioSample SECOND_BIOSAMPLE = new BioSampleBuilder()
			.modelDisplayName(SECOND_BIOSAMPLE_MODELDISPLAYNAME)
			.sampleDescriptor(SECOND_BIOSAMPLE_SAMPLEDESCRIPTOR)
			.protocolParticipantID(SECOND_BIOSAMPLE_PPID)
			.sampleType(SECOND_BIOSAMPLE_SAMPLETYPE)
			.primaryDiseaseSite(SECOND_BIOSAMPLE_PRIMARYDISEASESITE)
			.collectionDate(SECOND_BIOSAMPLE_COLLECTIONDATE)
			.build();
	private static final BioSample THIRD_BIOSAMPLE = new BioSampleBuilder()
			.modelDisplayName(THIRD_BIOSAMPLE_MODELDISPLAYNAME)
			.sampleDescriptor(THIRD_BIOSAMPLE_SAMPLEDESCRIPTOR)
			.protocolParticipantID(THIRD_BIOSAMPLE_PPID)
			.sampleType(THIRD_BIOSAMPLE_SAMPLETYPE)
			.primaryDiseaseSite(THIRD_BIOSAMPLE_PRIMARYDISEASESITE)
			.collectionDate(THIRD_BIOSAMPLE_COLLECTIONDATE)
			.build();

	@Autowired
	private BioSampleRepository biosampleRepository;

	@Value("${local.server.port}")
	private int serverPort;

	private BioSample firstBioSample;
	private BioSample secondBioSample;
	private BioSample thirdBioSample;

	@Before
	public void setUp() {
		// biosampleRepository.deleteAll();
		firstBioSample = biosampleRepository.save(FIRST_BIOSAMPLE);
		secondBioSample = biosampleRepository.save(SECOND_BIOSAMPLE);
		// thirdBioSample = biosampleRepository.save(THIRD_BIOSAMPLE);
		RestAssured.port = serverPort;
	}

	@Test
	public void getPort() {
		assertThat("Should get a random port greater than zero!", serverPort, greaterThan(0));
	}

	@Test
	public void getBiosamplesShouldReturnBothBiosamples() {
		when()
			.get(ROOT_PATH + BIOSAMPLES_RESOURCE)
		.then().statusCode(HttpStatus.SC_OK)
			.body(MODELDISPLAYNAME_FIELD, hasItems(FIRST_BIOSAMPLE_MODELDISPLAYNAME, SECOND_BIOSAMPLE_MODELDISPLAYNAME))
			.body(SAMPLEDESCRIPTOR_FIELD, hasItems(FIRST_BIOSAMPLE_SAMPLEDESCRIPTOR, SECOND_BIOSAMPLE_SAMPLEDESCRIPTOR))
			.body(PPID_FIELD, hasItems(FIRST_BIOSAMPLE_PPID, SECOND_BIOSAMPLE_PPID))
			.body(SAMPLETYPE_FIELD, hasItems(FIRST_BIOSAMPLE_SAMPLETYPE, SECOND_BIOSAMPLE_SAMPLETYPE))
			.body(PRIMARYDISEASESITE_FIELD, hasItems(FIRST_BIOSAMPLE_PRIMARYDISEASESITE, SECOND_BIOSAMPLE_PRIMARYDISEASESITE))
			.body(RECEIVINGDATE_FIELD, hasItems(FIRST_BIOSAMPLE_COLLECTIONDATE, SECOND_BIOSAMPLE_COLLECTIONDATE));
	}

	@Test
	public void getBiosamplesByIdShouldReturnStudy() {
		when()
			.get(ROOT_PATH + BIOSAMPLES_RESOURCE + "/{id}", firstBioSample.getId())
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(MODELDISPLAYNAME_FIELD, is(FIRST_BIOSAMPLE_MODELDISPLAYNAME))
			.body(SAMPLEDESCRIPTOR_FIELD, is(FIRST_BIOSAMPLE_SAMPLEDESCRIPTOR))
			.body(PPID_FIELD, is(FIRST_BIOSAMPLE_PPID))
			.body(SAMPLETYPE_FIELD, is(FIRST_BIOSAMPLE_SAMPLETYPE))
			.body(PRIMARYDISEASESITE_FIELD, is(FIRST_BIOSAMPLE_PRIMARYDISEASESITE))
			.body(RECEIVINGDATE_FIELD, is(FIRST_BIOSAMPLE_COLLECTIONDATE));
	}

	@Test
	public void addBiosamplesShouldReturnSavedBiosample() {
		given()
			.body(THIRD_BIOSAMPLE)
			.contentType(ContentType.JSON)
		.when()
			.post(ROOT_PATH + BIOSAMPLES_RESOURCE)
		.then()
			.statusCode(HttpStatus.SC_CREATED)
			.body(MODELDISPLAYNAME_FIELD, is(THIRD_BIOSAMPLE_MODELDISPLAYNAME))
			.body(SAMPLEDESCRIPTOR_FIELD, is(THIRD_BIOSAMPLE_SAMPLEDESCRIPTOR))
			.body(PPID_FIELD, is(THIRD_BIOSAMPLE_PPID))
			.body(SAMPLETYPE_FIELD, is(THIRD_BIOSAMPLE_SAMPLETYPE))
			.body(PRIMARYDISEASESITE_FIELD, is(THIRD_BIOSAMPLE_PRIMARYDISEASESITE))
			.body(RECEIVINGDATE_FIELD, is(THIRD_BIOSAMPLE_COLLECTIONDATE));
	}

	@Test
	public void addBiosampleShouldReturnBadRequestWithoutBody() {
		given()
			.contentType(ContentType.JSON)
		.when()
			.post(ROOT_PATH + BIOSAMPLES_RESOURCE)
		.then()
			.statusCode(HttpStatus.SC_BAD_REQUEST);
	}

	@Test
	public void updateBiosampleShouldReturnUpdatedBiosample() {
		// Given an unchecked first study
		BioSample sample = new BioSampleBuilder()
				.modelDisplayName(THIRD_BIOSAMPLE_MODELDISPLAYNAME)
				//.sampleDescriptor(THIRD_BIOSAMPLE_SAMPLEDESCRIPTOR)
				.protocolParticipantID(THIRD_BIOSAMPLE_PPID)
				.sampleType(THIRD_BIOSAMPLE_SAMPLETYPE)
				.primaryDiseaseSite(THIRD_BIOSAMPLE_PRIMARYDISEASESITE)
				.collectionDate(THIRD_BIOSAMPLE_COLLECTIONDATE)
				.build();
		given()
			.body(sample).contentType(ContentType.JSON)
		.when()
			.put(ROOT_PATH + BIOSAMPLES_RESOURCE + "/{id}", firstBioSample.getId())
		.then()
			.statusCode(HttpStatus.SC_OK)
			.body(MODELDISPLAYNAME_FIELD, is(THIRD_BIOSAMPLE_MODELDISPLAYNAME))
			//.body(SAMPLEDESCRIPTOR_FIELD, is(THIRD_BIOSAMPLE_SAMPLEDESCRIPTOR))
			.body(PPID_FIELD, is(THIRD_BIOSAMPLE_PPID))
			.body(SAMPLETYPE_FIELD, is(THIRD_BIOSAMPLE_SAMPLETYPE))
			.body(PRIMARYDISEASESITE_FIELD, is(THIRD_BIOSAMPLE_PRIMARYDISEASESITE))
			.body(RECEIVINGDATE_FIELD, is(THIRD_BIOSAMPLE_COLLECTIONDATE));
	}

	@Test
	public void updateBiosampleShouldReturnBadRequestWithoutBody() {
		when()
			.put(ROOT_PATH + BIOSAMPLES_RESOURCE + "/{id}", firstBioSample.getId())
		.then()
			.statusCode(HttpStatus.SC_BAD_REQUEST);
	}

	@Test
	public void updateBiosampleShouldBeBadRequestIfNonExistingID() {
		given()
			.body(THIRD_BIOSAMPLE).contentType(ContentType.JSON)
		.when()
			.put(ROOT_PATH + BIOSAMPLES_RESOURCE + "/{id}", NON_EXISTING_ID)
		.then()
			.statusCode(HttpStatus.SC_NOT_FOUND);
	}

}
