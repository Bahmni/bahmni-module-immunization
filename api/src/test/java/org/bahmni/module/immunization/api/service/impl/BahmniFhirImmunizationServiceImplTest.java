package org.bahmni.module.immunization.api.service.impl;

import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.bahmni.module.immunization.api.dao.BahmniFhirImmunizationDao;
import org.bahmni.module.immunization.api.model.FhirImmunization;
import org.bahmni.module.immunization.api.model.FhirImmunizationStatus;
import org.bahmni.module.immunization.api.search.param.BahmniImmunizationSearchParams;
import org.bahmni.module.immunization.api.translator.BahmniImmunizationTranslator;
import org.hl7.fhir.r4.model.Immunization;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class BahmniFhirImmunizationServiceImplTest {

	private static final String PATIENT_UUID = "patient-uuid";
	private static final String IMMUNIZATION_UUID = "immunization-uuid";

	@Mock
	private ContextDAO contextDAO;

	@Mock
	private UserContext userContext;

	@Mock
	private User user;

	@Mock
	private BahmniFhirImmunizationDao immunizationDao;

	@Mock
	private BahmniImmunizationTranslator immunizationTranslator;

	@Mock
	private SearchQueryInclude<Immunization> searchQueryInclude;

	@Mock
	private SearchQuery<FhirImmunization, Immunization, BahmniFhirImmunizationDao, BahmniImmunizationTranslator, SearchQueryInclude<Immunization>> searchQuery;

	@Mock
	private FhirGlobalPropertyService globalPropertyService;

	private BahmniFhirImmunizationServiceImpl immunizationService;

	@Before
	public void setUp() {
		when(userContext.getAuthenticatedUser()).thenReturn(user);
		Context.setDAO(contextDAO);
		Context.openSession();
		Context.setUserContext(userContext);

		immunizationService = new BahmniFhirImmunizationServiceImpl(
				immunizationDao, immunizationTranslator, searchQueryInclude, searchQuery) {
			@Override
			protected void validateObject(FhirImmunization object) {
				// Override to avoid Context.getAdministrationService()
			}
		};
	}

	@Test
	public void get_shouldReturnImmunizationByUuid() {
		FhirImmunization entity = new FhirImmunization();
		entity.setUuid(IMMUNIZATION_UUID);

		Immunization fhirImmunization = new Immunization();
		fhirImmunization.setId(IMMUNIZATION_UUID);

		when(immunizationDao.get(IMMUNIZATION_UUID)).thenReturn(entity);
		when(immunizationTranslator.toFhirResource(entity)).thenReturn(fhirImmunization);

		Immunization result = immunizationService.get(IMMUNIZATION_UUID);

		assertNotNull(result);
		assertEquals(IMMUNIZATION_UUID, result.getId());
		verify(immunizationDao).get(IMMUNIZATION_UUID);
	}

	@Test
	public void create_shouldCreateNewImmunization() {
		FhirImmunization entity = new FhirImmunization();
		entity.setUuid(IMMUNIZATION_UUID);
		entity.setStatus(FhirImmunizationStatus.COMPLETED);

		Immunization fhirImmunization = new Immunization();
		fhirImmunization.setId(IMMUNIZATION_UUID);
		fhirImmunization.setStatus(Immunization.ImmunizationStatus.COMPLETED);

		when(immunizationTranslator.toOpenmrsType(any(Immunization.class))).thenReturn(entity);
		when(immunizationDao.createOrUpdate(entity)).thenReturn(entity);
		when(immunizationTranslator.toFhirResource(entity)).thenReturn(fhirImmunization);

		Immunization result = immunizationService.create(fhirImmunization);

		assertNotNull(result);
		assertEquals(IMMUNIZATION_UUID, result.getId());
		verify(immunizationDao).createOrUpdate(entity);
	}

	@Test
	public void update_shouldUpdateExistingImmunization() {
		FhirImmunization existing = new FhirImmunization();
		existing.setUuid(IMMUNIZATION_UUID);
		existing.setStatus(FhirImmunizationStatus.COMPLETED);

		FhirImmunization updated = new FhirImmunization();
		updated.setUuid(IMMUNIZATION_UUID);
		updated.setManufacturer("NewManufacturer");

		Immunization fhirInput = new Immunization();
		fhirInput.setId(IMMUNIZATION_UUID);

		Immunization fhirResult = new Immunization();
		fhirResult.setId(IMMUNIZATION_UUID);

		when(immunizationDao.get(IMMUNIZATION_UUID)).thenReturn(existing);
		when(immunizationTranslator.toOpenmrsType(existing, fhirInput)).thenReturn(updated);
		when(immunizationDao.createOrUpdate(updated)).thenReturn(updated);
		when(immunizationTranslator.toFhirResource(updated)).thenReturn(fhirResult);

		Immunization result = immunizationService.update(IMMUNIZATION_UUID, fhirInput);

		assertNotNull(result);
		verify(immunizationDao).get(IMMUNIZATION_UUID);
		verify(immunizationDao).createOrUpdate(updated);
	}

	@Test
	public void searchImmunizations_shouldSearchByPatientReference() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam()
				.addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_UUID)));

		BahmniImmunizationSearchParams searchParams = new BahmniImmunizationSearchParams(
				patientReference, null, null, null, null, null, null);

		immunizationService.searchImmunizations(searchParams);

		ArgumentCaptor<SearchParameterMap> mapCaptor = ArgumentCaptor.forClass(SearchParameterMap.class);
		verify(searchQuery).getQueryResults(mapCaptor.capture(), any(), any(), any());

		SearchParameterMap actualMap = mapCaptor.getValue();
		assertNotNull(actualMap.getParameters(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER));
	}

	@Test(expected = InvalidRequestException.class)
	public void searchImmunizations_shouldThrowExceptionWhenNoRequiredParams() {
		BahmniImmunizationSearchParams searchParams = new BahmniImmunizationSearchParams(
				null, null, null, null, null, null, null);
		immunizationService.searchImmunizations(searchParams);
	}

	@Test
	public void searchImmunizations_shouldSearchById() {
		TokenAndListParam id = new TokenAndListParam()
				.addAnd(new TokenOrListParam().add(new TokenParam(IMMUNIZATION_UUID)));

		BahmniImmunizationSearchParams searchParams = new BahmniImmunizationSearchParams(
				null, null, null, null, id, null, null);

		immunizationService.searchImmunizations(searchParams);

		verify(searchQuery).getQueryResults(any(), any(), any(), any());
	}

	@Test
	public void searchImmunizations_shouldSearchByStatus() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam()
				.addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_UUID)));
		TokenAndListParam status = new TokenAndListParam()
				.addAnd(new TokenOrListParam().add(new TokenParam("completed")));

		BahmniImmunizationSearchParams searchParams = new BahmniImmunizationSearchParams(
				patientReference, null, null, status, null, null, null);

		immunizationService.searchImmunizations(searchParams);

		ArgumentCaptor<SearchParameterMap> mapCaptor = ArgumentCaptor.forClass(SearchParameterMap.class);
		verify(searchQuery).getQueryResults(mapCaptor.capture(), any(), any(), any());

		assertNotNull(mapCaptor.getValue().getParameters(FhirConstants.STATUS_SEARCH_HANDLER));
	}

	@Test
	public void searchImmunizations_shouldSearchByDateRange() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam()
				.addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_UUID)));
		DateRangeParam dateRange = new DateRangeParam(new DateParam("2023-01-01"), new DateParam("2023-12-31"));

		BahmniImmunizationSearchParams searchParams = new BahmniImmunizationSearchParams(
				patientReference, dateRange, null, null, null, null, null);

		immunizationService.searchImmunizations(searchParams);

		ArgumentCaptor<SearchParameterMap> mapCaptor = ArgumentCaptor.forClass(SearchParameterMap.class);
		verify(searchQuery).getQueryResults(mapCaptor.capture(), any(), any(), any());

		assertNotNull(mapCaptor.getValue().getParameters(FhirConstants.DATE_RANGE_SEARCH_HANDLER));
	}

	@Test
	public void searchImmunizations_shouldSearchByVaccineCode() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam()
				.addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_UUID)));
		TokenAndListParam vaccineCode = new TokenAndListParam()
				.addAnd(new TokenOrListParam().add(new TokenParam("http://hl7.org/fhir/sid/cvx", "140")));

		BahmniImmunizationSearchParams searchParams = new BahmniImmunizationSearchParams(
				patientReference, null, vaccineCode, null, null, null, null);

		immunizationService.searchImmunizations(searchParams);

		ArgumentCaptor<SearchParameterMap> mapCaptor = ArgumentCaptor.forClass(SearchParameterMap.class);
		verify(searchQuery).getQueryResults(mapCaptor.capture(), any(), any(), any());

		assertNotNull(mapCaptor.getValue().getParameters(FhirConstants.CODED_SEARCH_HANDLER));
	}

	@Test
	public void delete_shouldVoidImmunization() {
		FhirImmunization entity = new FhirImmunization();
		entity.setUuid(IMMUNIZATION_UUID);
		when(immunizationDao.delete(IMMUNIZATION_UUID)).thenReturn(entity);

		immunizationService.delete(IMMUNIZATION_UUID);

		verify(immunizationDao).delete(IMMUNIZATION_UUID);
	}
}
