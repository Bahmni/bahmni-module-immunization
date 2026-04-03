package org.bahmni.module.immunization.api.search.param;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.search.param.BaseResourceSearchParams;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SortSpec;

import java.util.HashSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BahmniImmunizationSearchParams extends BaseResourceSearchParams {

	private ReferenceAndListParam patientReference;

	private DateRangeParam dateRange;

	private TokenAndListParam vaccineCode;

	private TokenAndListParam status;

	public BahmniImmunizationSearchParams(ReferenceAndListParam patientReference, DateRangeParam dateRange,
			TokenAndListParam vaccineCode, TokenAndListParam status,
			TokenAndListParam id, DateRangeParam lastUpdated, SortSpec sort) {
		super(id, lastUpdated, sort, new HashSet<Include>(), new HashSet<Include>());
		this.patientReference = patientReference;
		this.dateRange = dateRange;
		this.vaccineCode = vaccineCode;
		this.status = status;
	}

	@Override
	public SearchParameterMap toSearchParameterMap() {
		SearchParameterMap searchParameterMap = baseSearchParameterMap();
		searchParameterMap.addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference);
		if (dateRange != null) {
			searchParameterMap.addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, dateRange);
		}
		if (vaccineCode != null) {
			searchParameterMap.addParameter(FhirConstants.CODED_SEARCH_HANDLER, vaccineCode);
		}
		if (status != null) {
			searchParameterMap.addParameter(FhirConstants.STATUS_SEARCH_HANDLER, status);
		}
		return searchParameterMap;
	}

	public boolean hasPatientReference() {
		return patientReference != null && patientReference.getValuesAsQueryTokens() != null
				&& !patientReference.getValuesAsQueryTokens().isEmpty();
	}

	public boolean hasId() {
		return getId() != null && getId().getValuesAsQueryTokens() != null
				&& !getId().getValuesAsQueryTokens().isEmpty();
	}
}
