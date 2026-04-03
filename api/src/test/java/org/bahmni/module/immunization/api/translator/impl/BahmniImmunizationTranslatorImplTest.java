package org.bahmni.module.immunization.api.translator.impl;

import org.bahmni.module.immunization.api.TestDataFactory;
import org.bahmni.module.immunization.api.model.FhirImmunization;
import org.bahmni.module.immunization.api.model.FhirImmunizationStatus;
import org.bahmni.module.immunization.api.model.ImmunizationNote;
import org.bahmni.module.immunization.api.model.ImmunizationPerformer;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.LocationReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.impl.MedicationQuantityCodingTranslatorImpl;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;

import static org.bahmni.module.immunization.ImmunizationModuleConstants.FHIR_EXT_IMMUNIZATION_ADMINISTERED_PRODUCT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BahmniImmunizationTranslatorImplTest {

	private static final String PATIENT_UUID = "patient-uuid";
	private static final String ENCOUNTER_UUID = "encounter-uuid";
	private static final String LOCATION_UUID = "location-uuid";
	private static final String PERFORMER_UUID = "performer-uuid";
	private static final String ORDERER_UUID = "orderer-uuid";
	private static final String DRUG_UUID = "drug-uuid";
	private static final String VACCINE_CONCEPT_UUID = "vaccine-concept-uuid";
	private static final String SITE_CONCEPT_UUID = "site-concept-uuid";
	private static final String ROUTE_CONCEPT_UUID = "route-concept-uuid";
	private static final String STATUS_REASON_UUID = "status-reason-uuid";
	private static final String DOSE_UNIT_CONCEPT_UUID = "dose-unit-concept-uuid";

	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;

	@Mock
	private EncounterReferenceTranslator<Encounter> encounterReferenceTranslator;

	@Mock
	private LocationReferenceTranslator locationReferenceTranslator;

	@Mock
	private PractitionerReferenceTranslator<Provider> practitionerReferenceTranslator;

	@Mock
	private ConceptTranslator conceptTranslator;

	@Mock
	private MedicationQuantityCodingTranslatorImpl quantityCodingTranslator;

	@Mock
	private org.openmrs.api.ConceptService conceptService;

	@Mock
	private org.openmrs.api.OrderService orderService;

	private BahmniImmunizationStatusTranslator statusTranslator;

	private BahmniImmunizationTranslatorImpl translator;

	@Before
	public void setUp() {
		statusTranslator = new BahmniImmunizationStatusTranslator();
		translator = new BahmniImmunizationTranslatorImpl(
				patientReferenceTranslator,
				encounterReferenceTranslator,
				locationReferenceTranslator,
				practitionerReferenceTranslator,
				conceptTranslator,
				quantityCodingTranslator,
				statusTranslator,
				conceptService,
				orderService
		);
	}

	// ========== toFhirResource tests ==========

	@Test
	public void toFhirResource_shouldReturnNullForNullInput() {
		assertNull(translator.toFhirResource(null));
	}

	@Test
	public void toFhirResource_shouldTranslateUuid() {
		FhirImmunization entity = createBasicImmunization();
		entity.setUuid("test-uuid-123");

		Immunization result = translator.toFhirResource(entity);

		assertEquals("test-uuid-123", result.getId());
	}

	@Test
	public void toFhirResource_shouldTranslateCompletedStatus() {
		FhirImmunization entity = createBasicImmunization();
		entity.setStatus(FhirImmunizationStatus.COMPLETED);

		Immunization result = translator.toFhirResource(entity);

		assertEquals(Immunization.ImmunizationStatus.COMPLETED, result.getStatus());
	}

	@Test
	public void toFhirResource_shouldTranslateNotDoneStatus() {
		FhirImmunization entity = createBasicImmunization();
		entity.setStatus(FhirImmunizationStatus.NOT_DONE);

		Immunization result = translator.toFhirResource(entity);

		assertEquals(Immunization.ImmunizationStatus.NOTDONE, result.getStatus());
	}

	@Test
	public void toFhirResource_shouldTranslateStatusReason() {
		FhirImmunization entity = createBasicImmunization();
		entity.setStatus(FhirImmunizationStatus.NOT_DONE);
		Concept statusReason = TestDataFactory.exampleConcept("Patient refused", STATUS_REASON_UUID);
		entity.setStatusReason(statusReason);

		when(conceptTranslator.toFhirResource(statusReason))
				.thenReturn(new org.hl7.fhir.r4.model.CodeableConcept().setText("Patient refused"));

		Immunization result = translator.toFhirResource(entity);

		assertNotNull(result.getStatusReason());
	}

	@Test
	public void toFhirResource_shouldTranslateVaccineCode() {
		FhirImmunization entity = createBasicImmunization();
		Concept vaccine = TestDataFactory.exampleConcept("Measles", VACCINE_CONCEPT_UUID);
		entity.setVaccineCode(vaccine);

		org.hl7.fhir.r4.model.CodeableConcept fhirConcept =
				new org.hl7.fhir.r4.model.CodeableConcept().setText("Measles");
		when(conceptTranslator.toFhirResource(vaccine)).thenReturn(fhirConcept);

		Immunization result = translator.toFhirResource(entity);

		assertEquals(fhirConcept, result.getVaccineCode());
	}

	@Test
	public void toFhirResource_shouldTranslatePatientReference() {
		FhirImmunization entity = createBasicImmunization();
		Patient patient = TestDataFactory.examplePatient(PATIENT_UUID);
		entity.setPatient(patient);

		Reference patientRef = new Reference("Patient/" + PATIENT_UUID);
		when(patientReferenceTranslator.toFhirResource(patient)).thenReturn(patientRef);

		Immunization result = translator.toFhirResource(entity);

		assertEquals("Patient/" + PATIENT_UUID, result.getPatient().getReference());
	}

	@Test
	public void toFhirResource_shouldTranslateEncounterReference() {
		FhirImmunization entity = createBasicImmunization();
		Encounter encounter = TestDataFactory.exampleEncounter(ENCOUNTER_UUID);
		entity.setEncounter(encounter);

		Reference encounterRef = new Reference("Encounter/" + ENCOUNTER_UUID);
		when(encounterReferenceTranslator.toFhirResource(encounter)).thenReturn(encounterRef);

		Immunization result = translator.toFhirResource(entity);

		assertEquals("Encounter/" + ENCOUNTER_UUID, result.getEncounter().getReference());
	}

	@Test
	public void toFhirResource_shouldTranslateOccurrenceDateTime() {
		FhirImmunization entity = createBasicImmunization();
		Date administeredOn = new Date();
		entity.setAdministeredOn(administeredOn);

		Immunization result = translator.toFhirResource(entity);

		assertEquals(administeredOn, result.getOccurrenceDateTimeType().getValue());
	}

	@Test
	public void toFhirResource_shouldTranslatePrimarySource() {
		FhirImmunization entity = createBasicImmunization();
		entity.setPrimarySource(true);

		Immunization result = translator.toFhirResource(entity);

		assertTrue(result.getPrimarySource());
	}

	@Test
	public void toFhirResource_shouldTranslateLocationReference() {
		FhirImmunization entity = createBasicImmunization();
		Location location = TestDataFactory.exampleLocation("IOM MHAC Nairobi", LOCATION_UUID);
		entity.setLocation(location);

		Reference locationRef = new Reference("Location/" + LOCATION_UUID);
		when(locationReferenceTranslator.toFhirResource(location)).thenReturn(locationRef);

		Immunization result = translator.toFhirResource(entity);

		assertEquals("Location/" + LOCATION_UUID, result.getLocation().getReference());
	}

	@Test
	public void toFhirResource_shouldTranslateManufacturer() {
		FhirImmunization entity = createBasicImmunization();
		entity.setManufacturer("Medsource");

		Immunization result = translator.toFhirResource(entity);

		assertEquals("Medsource", result.getManufacturer().getDisplay());
	}

	@Test
	public void toFhirResource_shouldTranslateLotNumber() {
		FhirImmunization entity = createBasicImmunization();
		entity.setBatchNumber("BATCH-12345");

		Immunization result = translator.toFhirResource(entity);

		assertEquals("BATCH-12345", result.getLotNumber());
	}

	@Test
	public void toFhirResource_shouldTranslateSite() {
		FhirImmunization entity = createBasicImmunization();
		Concept site = TestDataFactory.exampleConcept("Shoulder", SITE_CONCEPT_UUID);
		entity.setSite(site);

		org.hl7.fhir.r4.model.CodeableConcept fhirSite =
				new org.hl7.fhir.r4.model.CodeableConcept().setText("Shoulder");
		when(conceptTranslator.toFhirResource(site)).thenReturn(fhirSite);

		Immunization result = translator.toFhirResource(entity);

		assertEquals(fhirSite, result.getSite());
	}

	@Test
	public void toFhirResource_shouldTranslateRoute() {
		FhirImmunization entity = createBasicImmunization();
		Concept route = TestDataFactory.exampleConcept("Intramuscular", ROUTE_CONCEPT_UUID);
		entity.setRoute(route);

		org.hl7.fhir.r4.model.CodeableConcept fhirRoute =
				new org.hl7.fhir.r4.model.CodeableConcept().setText("Intramuscular");
		when(conceptTranslator.toFhirResource(route)).thenReturn(fhirRoute);

		Immunization result = translator.toFhirResource(entity);

		assertEquals(fhirRoute, result.getRoute());
	}

	@Test
	public void toFhirResource_shouldTranslateDoseQuantity() {
		FhirImmunization entity = createBasicImmunization();
		Concept doseUnit = TestDataFactory.exampleConcept("ml", DOSE_UNIT_CONCEPT_UUID);
		entity.setDoseQuantity(0.5);
		entity.setDoseUnit(doseUnit);

		Coding coding = new Coding(null, DOSE_UNIT_CONCEPT_UUID, "ml");
		when(quantityCodingTranslator.toFhirResource(doseUnit)).thenReturn(coding);

		Immunization result = translator.toFhirResource(entity);

		assertNotNull(result.getDoseQuantity());
		assertEquals(0.5, result.getDoseQuantity().getValue().doubleValue(), 0.001);
		assertEquals(DOSE_UNIT_CONCEPT_UUID, result.getDoseQuantity().getCode());
		assertEquals("ml", result.getDoseQuantity().getUnit());
	}

	@Test
	public void toFhirResource_shouldTranslateDoseSequence() {
		FhirImmunization entity = createBasicImmunization();
		entity.setDoseSequence(3);

		Immunization result = translator.toFhirResource(entity);

		assertFalse(result.getProtocolApplied().isEmpty());
		assertEquals(3, result.getProtocolApplied().get(0).getDoseNumberPositiveIntType().getValue().intValue());
	}

	@Test
	public void toFhirResource_shouldTranslateExpirationDate() {
		FhirImmunization entity = createBasicImmunization();
		Date expirationDate = new Date();
		entity.setExpirationDate(expirationDate);

		Immunization result = translator.toFhirResource(entity);

		assertEquals(expirationDate, result.getExpirationDate());
	}

	@Test
	public void toFhirResource_shouldTranslateIsSubpotent() {
		FhirImmunization entity = createBasicImmunization();
		entity.setIsSubpotent(true);

		Immunization result = translator.toFhirResource(entity);

		assertTrue(result.getIsSubpotent());
	}

	@Test
	public void toFhirResource_shouldTranslateSubpotentReason() {
		FhirImmunization entity = createBasicImmunization();
		Concept reason = TestDataFactory.exampleConcept("Partial dose", "subpotent-reason-uuid");
		entity.setSubpotentReason(reason);

		org.hl7.fhir.r4.model.CodeableConcept fhirReason =
				new org.hl7.fhir.r4.model.CodeableConcept().setText("Partial dose");
		when(conceptTranslator.toFhirResource(reason)).thenReturn(fhirReason);

		Immunization result = translator.toFhirResource(entity);

		assertFalse(result.getSubpotentReason().isEmpty());
		assertEquals("Partial dose", result.getSubpotentReasonFirstRep().getText());
	}

	@Test
	public void toFhirResource_shouldTranslatePerformers() {
		FhirImmunization entity = createBasicImmunization();
		Provider apProvider = TestDataFactory.exampleProvider("Aisha Khan", PERFORMER_UUID);
		Provider opProvider = TestDataFactory.exampleProvider("Dr S.Johnson", ORDERER_UUID);

		ImmunizationPerformer ap = new ImmunizationPerformer();
		ap.setFunction("AP");
		ap.setActor(apProvider);
		ap.setImmunization(entity);

		ImmunizationPerformer op = new ImmunizationPerformer();
		op.setFunction("OP");
		op.setActor(opProvider);
		op.setImmunization(entity);

		entity.setPerformers(new HashSet<>());
		entity.getPerformers().add(ap);
		entity.getPerformers().add(op);

		when(practitionerReferenceTranslator.toFhirResource(apProvider))
				.thenReturn(new Reference("Practitioner/" + PERFORMER_UUID));
		when(practitionerReferenceTranslator.toFhirResource(opProvider))
				.thenReturn(new Reference("Practitioner/" + ORDERER_UUID));

		Immunization result = translator.toFhirResource(entity);

		assertEquals(2, result.getPerformer().size());
	}

	@Test
	public void toFhirResource_shouldTranslateAnnotations() {
		FhirImmunization entity = createBasicImmunization();
		Provider author = TestDataFactory.exampleProvider("Aisha Khan", PERFORMER_UUID);

		ImmunizationNote note = new ImmunizationNote();
		note.setText("Third dose completed successfully.");
		note.setAuthor(author);
		note.setRecordedOn(new Date());
		note.setImmunization(entity);

		entity.setNotes(new HashSet<>());
		entity.getNotes().add(note);

		when(practitionerReferenceTranslator.toFhirResource(author))
				.thenReturn(new Reference("Practitioner/" + PERFORMER_UUID));

		Immunization result = translator.toFhirResource(entity);

		assertTrue(result.hasNote());
		assertEquals(1, result.getNote().size());
		assertEquals("Third dose completed successfully.", result.getNote().get(0).getText());
	}

	@Test
	public void toFhirResource_shouldTranslateAnnotationWithAuthorString() {
		FhirImmunization entity = createBasicImmunization();

		ImmunizationNote note = new ImmunizationNote();
		note.setText("External note.");
		note.setAuthorString("Dr. External");
		note.setImmunization(entity);

		entity.setNotes(new HashSet<>());
		entity.getNotes().add(note);

		Immunization result = translator.toFhirResource(entity);

		assertTrue(result.hasNote());
		assertEquals("Dr. External", result.getNote().get(0).getAuthorStringType().getValue());
	}

	@Test
	public void toFhirResource_shouldHandleEmptyAnnotations() {
		FhirImmunization entity = createBasicImmunization();
		entity.setNotes(new HashSet<>());

		Immunization result = translator.toFhirResource(entity);

		assertFalse(result.hasNote());
	}

	@Test
	public void toFhirResource_shouldHandleNullAnnotations() {
		FhirImmunization entity = createBasicImmunization();
		entity.setNotes(null);

		Immunization result = translator.toFhirResource(entity);

		assertFalse(result.hasNote());
	}

	@Test
	public void toFhirResource_shouldTranslateDrugAsExtension() {
		FhirImmunization entity = createBasicImmunization();
		Drug drug = TestDataFactory.exampleDrug("MisoPrime", DRUG_UUID);
		entity.setDrug(drug);

		Immunization result = translator.toFhirResource(entity);

		Extension ext = result.getExtensionByUrl(FHIR_EXT_IMMUNIZATION_ADMINISTERED_PRODUCT);
		assertNotNull(ext);
		Reference drugRef = (Reference) ext.getValue();
		assertEquals("Medication/" + DRUG_UUID, drugRef.getReference());
		assertEquals("MisoPrime", drugRef.getDisplay());
	}

	@Test
	public void toFhirResource_shouldTranslateDrugNonCodedAsExtensionWithDisplayOnly() {
		FhirImmunization entity = createBasicImmunization();
		entity.setDrugNonCoded("Some Unlisted Vaccine");

		Immunization result = translator.toFhirResource(entity);

		Extension ext = result.getExtensionByUrl(FHIR_EXT_IMMUNIZATION_ADMINISTERED_PRODUCT);
		assertNotNull(ext);
		Reference drugRef = (Reference) ext.getValue();
		assertNull(drugRef.getReference());
		assertEquals("Some Unlisted Vaccine", drugRef.getDisplay());
	}

	@Test
	public void toFhirResource_shouldPreferDrugOverDrugNonCodedInExtension() {
		FhirImmunization entity = createBasicImmunization();
		Drug drug = TestDataFactory.exampleDrug("MisoPrime", DRUG_UUID);
		entity.setDrug(drug);
		entity.setDrugNonCoded("Some Unlisted Vaccine");

		Immunization result = translator.toFhirResource(entity);

		Extension ext = result.getExtensionByUrl(FHIR_EXT_IMMUNIZATION_ADMINISTERED_PRODUCT);
		assertNotNull(ext);
		Reference drugRef = (Reference) ext.getValue();
		assertEquals("Medication/" + DRUG_UUID, drugRef.getReference());
	}

	@Test
	public void toFhirResource_shouldTranslateLocationTextAsDisplayOnlyReference() {
		FhirImmunization entity = createBasicImmunization();
		entity.setLocationText("Field Clinic B");

		Immunization result = translator.toFhirResource(entity);

		assertNotNull(result.getLocation());
		assertFalse(result.getLocation().hasReference());
		assertEquals("Field Clinic B", result.getLocation().getDisplay());
	}

	@Test
	public void toFhirResource_shouldPreferLocationOverLocationText() {
		FhirImmunization entity = createBasicImmunization();
		Location location = TestDataFactory.exampleLocation("IOM MHAC Nairobi", LOCATION_UUID);
		entity.setLocation(location);
		entity.setLocationText("Field Clinic B");

		Reference locationRef = new Reference("Location/" + LOCATION_UUID);
		when(locationReferenceTranslator.toFhirResource(location)).thenReturn(locationRef);

		Immunization result = translator.toFhirResource(entity);

		assertEquals("Location/" + LOCATION_UUID, result.getLocation().getReference());
	}

	@Test
	public void toFhirResource_shouldTranslateRecordedDate() {
		FhirImmunization entity = createBasicImmunization();
		Date dateCreated = new Date();
		entity.setDateCreated(dateCreated);

		Immunization result = translator.toFhirResource(entity);

		assertEquals(dateCreated, result.getRecorded());
	}

	@Test
	public void toFhirResource_shouldHandleEmptyPerformers() {
		FhirImmunization entity = createBasicImmunization();
		entity.setPerformers(new HashSet<>());

		Immunization result = translator.toFhirResource(entity);

		assertTrue(result.getPerformer().isEmpty());
	}

	// ========== toOpenmrsType tests ==========

	@Test
	public void toOpenmrsType_shouldReturnNullForNullResource() {
		assertNull(translator.toOpenmrsType(null));
	}

	@Test
	public void toOpenmrsType_shouldTranslateCompletedStatus() throws IOException {
		Immunization resource = (Immunization) TestDataFactory.loadResourceFromFile("example-immunization-administered.json");

		setupMocksForAdministeredResource();

		FhirImmunization result = translator.toOpenmrsType(resource);

		assertEquals(FhirImmunizationStatus.COMPLETED, result.getStatus());
	}

	@Test
	public void toOpenmrsType_shouldTranslateNotDoneStatus() throws IOException {
		Immunization resource = (Immunization) TestDataFactory.loadResourceFromFile("example-immunization-waiver.json");

		setupMocksForWaiverResource();

		FhirImmunization result = translator.toOpenmrsType(resource);

		assertEquals(FhirImmunizationStatus.NOT_DONE, result.getStatus());
	}

	@Test
	public void toOpenmrsType_shouldTranslatePatient() throws IOException {
		Immunization resource = (Immunization) TestDataFactory.loadResourceFromFile("example-immunization-administered.json");
		Patient patient = TestDataFactory.examplePatient(PATIENT_UUID);

		setupMocksForAdministeredResource();
		when(patientReferenceTranslator.toOpenmrsType(any(Reference.class))).thenReturn(patient);

		FhirImmunization result = translator.toOpenmrsType(resource);

		assertEquals(patient, result.getPatient());
	}

	@Test
	public void toOpenmrsType_shouldTranslateOccurrenceDateTime() throws IOException {
		Immunization resource = (Immunization) TestDataFactory.loadResourceFromFile("example-immunization-administered.json");

		setupMocksForAdministeredResource();

		FhirImmunization result = translator.toOpenmrsType(resource);

		assertNotNull(result.getAdministeredOn());
	}

	@Test
	public void toOpenmrsType_shouldTranslateLotNumber() throws IOException {
		Immunization resource = (Immunization) TestDataFactory.loadResourceFromFile("example-immunization-administered.json");

		setupMocksForAdministeredResource();

		FhirImmunization result = translator.toOpenmrsType(resource);

		assertEquals("BATCH-12345", result.getBatchNumber());
	}

	@Test
	public void toOpenmrsType_shouldTranslateManufacturer() throws IOException {
		Immunization resource = (Immunization) TestDataFactory.loadResourceFromFile("example-immunization-administered.json");

		setupMocksForAdministeredResource();

		FhirImmunization result = translator.toOpenmrsType(resource);

		assertEquals("Medsource", result.getManufacturer());
	}

	@Test
	public void toOpenmrsType_shouldTranslateDoseQuantity() throws IOException {
		Immunization resource = (Immunization) TestDataFactory.loadResourceFromFile("example-immunization-administered.json");
		Concept doseUnit = TestDataFactory.exampleConcept("ml", DOSE_UNIT_CONCEPT_UUID);

		setupMocksForAdministeredResource();
		when(quantityCodingTranslator.toOpenmrsType(any())).thenReturn(doseUnit);

		FhirImmunization result = translator.toOpenmrsType(resource);

		assertEquals(0.5, result.getDoseQuantity(), 0.001);
		assertNotNull(result.getDoseUnit());
		assertEquals(DOSE_UNIT_CONCEPT_UUID, result.getDoseUnit().getUuid());
	}

	@Test
	public void toOpenmrsType_shouldTranslateDoseSequence() throws IOException {
		Immunization resource = (Immunization) TestDataFactory.loadResourceFromFile("example-immunization-administered.json");

		setupMocksForAdministeredResource();

		FhirImmunization result = translator.toOpenmrsType(resource);

		assertEquals(Integer.valueOf(3), result.getDoseSequence());
	}

	@Test
	public void toOpenmrsType_shouldTranslateExpirationDate() {
		Immunization resource = new Immunization();
		resource.setStatus(Immunization.ImmunizationStatus.COMPLETED);
		resource.setPatient(new Reference("Patient/" + PATIENT_UUID));
		Date expDate = new Date();
		resource.setExpirationDate(expDate);

		setupMocksForAdministeredResource();

		FhirImmunization result = translator.toOpenmrsType(resource);

		assertEquals(expDate, result.getExpirationDate());
	}

	@Test
	public void toOpenmrsType_shouldTranslateIsSubpotent() {
		Immunization resource = new Immunization();
		resource.setStatus(Immunization.ImmunizationStatus.COMPLETED);
		resource.setPatient(new Reference("Patient/" + PATIENT_UUID));
		resource.setIsSubpotent(true);

		setupMocksForAdministeredResource();

		FhirImmunization result = translator.toOpenmrsType(resource);

		assertTrue(result.getIsSubpotent());
	}

	@Test
	public void toOpenmrsType_shouldTranslateSubpotentReason() {
		Immunization resource = new Immunization();
		resource.setStatus(Immunization.ImmunizationStatus.COMPLETED);
		resource.setPatient(new Reference("Patient/" + PATIENT_UUID));
		resource.addSubpotentReason(new org.hl7.fhir.r4.model.CodeableConcept().setText("Partial dose"));

		Concept reason = TestDataFactory.exampleConcept("Partial dose", "subpotent-reason-uuid");
		setupMocksForAdministeredResource();
		when(conceptTranslator.toOpenmrsType(any(org.hl7.fhir.r4.model.CodeableConcept.class))).thenReturn(reason);

		FhirImmunization result = translator.toOpenmrsType(resource);

		assertNotNull(result.getSubpotentReason());
	}

	@Test
	public void toOpenmrsType_shouldTranslatePerformersFromFhirResource() throws IOException {
		Immunization resource = (Immunization) TestDataFactory.loadResourceFromFile("example-immunization-administered.json");
		Provider apProvider = TestDataFactory.exampleProvider("Aisha Khan", PERFORMER_UUID);
		Provider opProvider = TestDataFactory.exampleProvider("Dr S.Johnson", ORDERER_UUID);

		setupMocksForAdministeredResource();
		when(practitionerReferenceTranslator.toOpenmrsType(any(Reference.class)))
				.thenReturn(apProvider)
				.thenReturn(opProvider);

		FhirImmunization result = translator.toOpenmrsType(resource);

		assertNotNull(result.getPerformers());
		assertEquals(2, result.getPerformers().size());
	}

	@Test
	public void toOpenmrsType_shouldTranslateAnnotationsFromFhirResource() throws IOException {
		Immunization resource = (Immunization) TestDataFactory.loadResourceFromFile("example-immunization-administered.json");

		setupMocksForAdministeredResource();

		FhirImmunization result = translator.toOpenmrsType(resource);

		assertNotNull(result.getNotes());
		assertEquals(1, result.getNotes().size());
		ImmunizationNote note = result.getNotes().iterator().next();
		assertEquals("Third dose of measles vaccination completed successfully.", note.getText());
		assertEquals(result, note.getImmunization());
	}

	@Test
	public void toOpenmrsType_shouldTranslateDrugExtension() throws IOException {
		Immunization resource = (Immunization) TestDataFactory.loadResourceFromFile("example-immunization-administered.json");
		Drug drug = TestDataFactory.exampleDrug("MisoPrime", DRUG_UUID);

		setupMocksForAdministeredResource();
		when(conceptService.getDrugByUuid(DRUG_UUID)).thenReturn(drug);

		FhirImmunization result = translator.toOpenmrsType(resource);

		assertNotNull(result.getDrug());
		assertEquals(DRUG_UUID, result.getDrug().getUuid());
	}

	@Test
	public void toOpenmrsType_shouldTranslateDrugExtensionWithDisplayOnlyToDrugNonCoded() {
		Immunization resource = new Immunization();
		resource.setStatus(Immunization.ImmunizationStatus.COMPLETED);
		resource.setPatient(new Reference("Patient/" + PATIENT_UUID));

		Reference drugRef = new Reference();
		drugRef.setDisplay("Some Unlisted Vaccine");
		resource.addExtension(FHIR_EXT_IMMUNIZATION_ADMINISTERED_PRODUCT, drugRef);

		setupMocksForAdministeredResource();

		FhirImmunization result = translator.toOpenmrsType(resource);

		assertNull(result.getDrug());
		assertEquals("Some Unlisted Vaccine", result.getDrugNonCoded());
	}

	@Test
	public void toOpenmrsType_shouldNotSetDrugNonCodedWhenReferenceIsPresent() throws IOException {
		Immunization resource = (Immunization) TestDataFactory.loadResourceFromFile("example-immunization-administered.json");
		Drug drug = TestDataFactory.exampleDrug("MisoPrime", DRUG_UUID);

		setupMocksForAdministeredResource();
		when(conceptService.getDrugByUuid(DRUG_UUID)).thenReturn(drug);

		FhirImmunization result = translator.toOpenmrsType(resource);

		assertNotNull(result.getDrug());
		assertNull(result.getDrugNonCoded());
	}

	@Test
	public void toOpenmrsType_shouldTranslateLocationWithDisplayOnlyToLocationText() {
		Immunization resource = new Immunization();
		resource.setStatus(Immunization.ImmunizationStatus.COMPLETED);
		resource.setPatient(new Reference("Patient/" + PATIENT_UUID));

		Reference locationRef = new Reference();
		locationRef.setDisplay("Field Clinic B");
		resource.setLocation(locationRef);

		setupMocksForAdministeredResource();

		FhirImmunization result = translator.toOpenmrsType(resource);

		assertNull(result.getLocation());
		assertEquals("Field Clinic B", result.getLocationText());
	}

	@Test
	public void toOpenmrsType_shouldResolveLocationReferenceWhenReferenceIsPresent() {
		Immunization resource = new Immunization();
		resource.setStatus(Immunization.ImmunizationStatus.COMPLETED);
		resource.setPatient(new Reference("Patient/" + PATIENT_UUID));
		resource.setLocation(new Reference("Location/" + LOCATION_UUID));

		Location location = TestDataFactory.exampleLocation("IOM MHAC Nairobi", LOCATION_UUID);
		setupMocksForAdministeredResource();
		when(locationReferenceTranslator.toOpenmrsType(any(Reference.class))).thenReturn(location);

		FhirImmunization result = translator.toOpenmrsType(resource);

		assertNotNull(result.getLocation());
		assertNull(result.getLocationText());
		assertEquals(LOCATION_UUID, result.getLocation().getUuid());
	}

	@Test
	public void toOpenmrsType_shouldTranslateWaiverStatusReason() throws IOException {
		Immunization resource = (Immunization) TestDataFactory.loadResourceFromFile("example-immunization-waiver.json");
		Concept reason = TestDataFactory.exampleConcept("Patient refused", STATUS_REASON_UUID);

		setupMocksForWaiverResource();
		when(conceptTranslator.toOpenmrsType(any())).thenReturn(reason);

		FhirImmunization result = translator.toOpenmrsType(resource);

		assertNotNull(result.getStatusReason());
	}

	// ========== Update (toOpenmrsType with existing) tests ==========

	@Test
	public void toOpenmrsType_shouldUpdateExistingEntity() throws IOException {
		FhirImmunization existing = createBasicImmunization();
		existing.setImmunizationId(1);
		existing.setManufacturer("OldManufacturer");

		Immunization resource = (Immunization) TestDataFactory.loadResourceFromFile("example-immunization-administered.json");

		setupMocksForAdministeredResource();

		FhirImmunization result = translator.toOpenmrsType(existing, resource);

		assertEquals(Integer.valueOf(1), result.getImmunizationId());
		assertEquals("Medsource", result.getManufacturer());
	}

	// ========== Helpers ==========

	private FhirImmunization createBasicImmunization() {
		FhirImmunization entity = new FhirImmunization();
		entity.setUuid("test-uuid");
		entity.setStatus(FhirImmunizationStatus.COMPLETED);
		entity.setVaccineCode(TestDataFactory.exampleConcept("Measles", VACCINE_CONCEPT_UUID));
		entity.setPatient(TestDataFactory.examplePatient(PATIENT_UUID));
		entity.setDateCreated(new Date());
		return entity;
	}

	private void setupMocksForAdministeredResource() {
		Patient patient = TestDataFactory.examplePatient(PATIENT_UUID);
		when(patientReferenceTranslator.toOpenmrsType(any(Reference.class))).thenReturn(patient);

		Concept vaccine = TestDataFactory.exampleConcept("Measles", VACCINE_CONCEPT_UUID);
		when(conceptTranslator.toOpenmrsType(any())).thenReturn(vaccine);

		Encounter encounter = TestDataFactory.exampleEncounter(ENCOUNTER_UUID);
		when(encounterReferenceTranslator.toOpenmrsType(any(Reference.class))).thenReturn(encounter);

		Location location = TestDataFactory.exampleLocation("IOM MHAC Nairobi", LOCATION_UUID);
		when(locationReferenceTranslator.toOpenmrsType(any(Reference.class))).thenReturn(location);
	}

	private void setupMocksForWaiverResource() {
		Patient patient = TestDataFactory.examplePatient(PATIENT_UUID);
		when(patientReferenceTranslator.toOpenmrsType(any(Reference.class))).thenReturn(patient);

		Concept vaccine = TestDataFactory.exampleConcept("Hepatitis B", VACCINE_CONCEPT_UUID);
		when(conceptTranslator.toOpenmrsType(any())).thenReturn(vaccine);
	}
}
