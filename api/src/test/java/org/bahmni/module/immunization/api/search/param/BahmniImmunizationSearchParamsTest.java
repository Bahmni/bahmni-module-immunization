package org.bahmni.module.immunization.api.search.param;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.junit.Test;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BahmniImmunizationSearchParamsTest {

	@Test
	public void toSearchParameterMap_patientReferenceIsAlwaysAdded() {
		ReferenceAndListParam patientRef = new ReferenceAndListParam()
				.addAnd(new ReferenceOrListParam().add(new ReferenceParam("Patient/some-uuid")));
		BahmniImmunizationSearchParams params = new BahmniImmunizationSearchParams();
		params.setPatientReference(patientRef);

		SearchParameterMap map = params.toSearchParameterMap();

		assertNotNull(map.getParameters(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER));
	}

	@Test
	public void toSearchParameterMap_withDateRange_shouldAddDateRangeHandler() {
		BahmniImmunizationSearchParams params = new BahmniImmunizationSearchParams();
		params.setDateRange(new DateRangeParam().setLowerBoundInclusive(new java.util.Date()));

		SearchParameterMap map = params.toSearchParameterMap();

		assertNotNull(map.getParameters(FhirConstants.DATE_RANGE_SEARCH_HANDLER));
	}

	@Test
	public void toSearchParameterMap_withNullDateRange_shouldNotAddDateRangeHandler() {
		BahmniImmunizationSearchParams params = new BahmniImmunizationSearchParams();
		params.setDateRange(null);

		SearchParameterMap map = params.toSearchParameterMap();

		assertTrue(map.getParameters(FhirConstants.DATE_RANGE_SEARCH_HANDLER).isEmpty());
	}

	@Test
	public void toSearchParameterMap_withVaccineCode_shouldAddCodedSearchHandler() {
		BahmniImmunizationSearchParams params = new BahmniImmunizationSearchParams();
		params.setVaccineCode(new TokenAndListParam().addAnd(new TokenOrListParam().add(new TokenParam("vaccine-code"))));

		SearchParameterMap map = params.toSearchParameterMap();

		assertNotNull(map.getParameters(FhirConstants.CODED_SEARCH_HANDLER));
	}

	@Test
	public void toSearchParameterMap_withNullVaccineCode_shouldNotAddCodedSearchHandler() {
		BahmniImmunizationSearchParams params = new BahmniImmunizationSearchParams();
		params.setVaccineCode(null);

		SearchParameterMap map = params.toSearchParameterMap();

		assertTrue(map.getParameters(FhirConstants.CODED_SEARCH_HANDLER).isEmpty());
	}

	@Test
	public void toSearchParameterMap_withStatus_shouldAddStatusSearchHandler() {
		BahmniImmunizationSearchParams params = new BahmniImmunizationSearchParams();
		params.setStatus(new TokenAndListParam().addAnd(new TokenOrListParam().add(new TokenParam("completed"))));

		SearchParameterMap map = params.toSearchParameterMap();

		assertNotNull(map.getParameters(FhirConstants.STATUS_SEARCH_HANDLER));
	}

	@Test
	public void toSearchParameterMap_withNullStatus_shouldNotAddStatusSearchHandler() {
		BahmniImmunizationSearchParams params = new BahmniImmunizationSearchParams();
		params.setStatus(null);

		SearchParameterMap map = params.toSearchParameterMap();

		assertTrue(map.getParameters(FhirConstants.STATUS_SEARCH_HANDLER).isEmpty());
	}

	@Test
	public void hasPatientReference_whenNull_shouldReturnFalse() {
		BahmniImmunizationSearchParams params = new BahmniImmunizationSearchParams();
		params.setPatientReference(null);

		assertFalse(params.hasPatientReference());
	}

	@Test
	public void hasPatientReference_whenEmpty_shouldReturnFalse() {
		BahmniImmunizationSearchParams params = new BahmniImmunizationSearchParams();
		params.setPatientReference(new ReferenceAndListParam());

		assertFalse(params.hasPatientReference());
	}

	@Test
	public void hasPatientReference_whenPopulated_shouldReturnTrue() {
		BahmniImmunizationSearchParams params = new BahmniImmunizationSearchParams();
		params.setPatientReference(new ReferenceAndListParam()
				.addAnd(new ReferenceOrListParam().add(new ReferenceParam("Patient/some-uuid"))));

		assertTrue(params.hasPatientReference());
	}

	@Test
	public void hasId_whenNull_shouldReturnFalse() {
		BahmniImmunizationSearchParams params = new BahmniImmunizationSearchParams();
		assertFalse(params.hasId());
	}

	@Test
	public void hasId_whenPopulated_shouldReturnTrue() {
		BahmniImmunizationSearchParams params = new BahmniImmunizationSearchParams();
		params.setId(new TokenAndListParam().addAnd(new TokenOrListParam().add(new TokenParam("some-uuid"))));

		assertTrue(params.hasId());
	}
}
