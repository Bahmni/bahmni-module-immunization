package org.bahmni.module.immunization.api.providers;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.bahmni.module.immunization.api.service.BahmniFhirImmunizationService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BahmniImmunizationFhirR4ResourceProviderTest {

	private static final String IMMUNIZATION_UUID = "immunization-uuid";
	private static final String PATIENT_UUID = "patient-uuid";

	@Mock
	private BahmniFhirImmunizationService immunizationService;

	private BahmniImmunizationFhirR4ResourceProvider resourceProvider;

	@Before
	public void setUp() {
		resourceProvider = new BahmniImmunizationFhirR4ResourceProvider(immunizationService);
	}

	@Test
	public void getResourceType_shouldReturnImmunization() {
		assertEquals(Immunization.class, resourceProvider.getResourceType());
	}

	@Test
	public void getImmunizationByUuid_shouldReturnImmunization() {
		Immunization immunization = new Immunization();
		immunization.setId(IMMUNIZATION_UUID);

		when(immunizationService.get(IMMUNIZATION_UUID)).thenReturn(immunization);

		Immunization result = resourceProvider.getImmunizationByUuid(new IdType(IMMUNIZATION_UUID));

		assertNotNull(result);
		assertEquals(IMMUNIZATION_UUID, result.getId());
	}

	@Test(expected = ResourceNotFoundException.class)
	public void getImmunizationByUuid_shouldThrowWhenNotFound() {
		when(immunizationService.get(IMMUNIZATION_UUID)).thenReturn(null);
		resourceProvider.getImmunizationByUuid(new IdType(IMMUNIZATION_UUID));
	}

	@Test
	public void createImmunization_shouldCreateAndReturnId() {
		Immunization input = new Immunization();
		Immunization created = new Immunization();
		created.setId(IMMUNIZATION_UUID);

		when(immunizationService.create(any(Immunization.class))).thenReturn(created);

		MethodOutcome outcome = resourceProvider.createImmunization(input);

		assertNotNull(outcome);
		assertNotNull(outcome.getResource());
		assertEquals(IMMUNIZATION_UUID, ((Immunization) outcome.getResource()).getId());
	}

	@Test
	public void updateImmunization_shouldUpdateAndReturn() {
		Immunization input = new Immunization();
		input.setId(IMMUNIZATION_UUID);

		Immunization updated = new Immunization();
		updated.setId(IMMUNIZATION_UUID);

		when(immunizationService.update(eq(IMMUNIZATION_UUID), any(Immunization.class))).thenReturn(updated);

		MethodOutcome outcome = resourceProvider.updateImmunization(new IdType(IMMUNIZATION_UUID), input);

		assertNotNull(outcome);
		assertNotNull(outcome.getResource());
	}

	@Test
	public void deleteImmunization_shouldDeleteAndReturnOutcome() {
		OperationOutcome result = resourceProvider.deleteImmunization(new IdType(IMMUNIZATION_UUID));

		assertNotNull(result);
		verify(immunizationService).delete(IMMUNIZATION_UUID);
	}

	@Test
	public void searchImmunizations_shouldCallServiceWithParams() {
		ReferenceAndListParam patientRef = new ReferenceAndListParam()
				.addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_UUID)));

		IBundleProvider mockBundle = mock(IBundleProvider.class);
		when(immunizationService.searchImmunizations(any())).thenReturn(mockBundle);

		IBundleProvider result = resourceProvider.searchImmunizations(
				patientRef, null, null, null, null, null, null);

		assertNotNull(result);
		verify(immunizationService).searchImmunizations(any());
	}
}
