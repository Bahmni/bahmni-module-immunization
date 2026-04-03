package org.bahmni.module.immunization.api.service.impl;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import lombok.Getter;
import org.bahmni.module.immunization.api.dao.BahmniFhirImmunizationDao;
import org.bahmni.module.immunization.api.model.FhirImmunization;
import org.bahmni.module.immunization.api.search.param.BahmniImmunizationSearchParams;
import org.bahmni.module.immunization.api.service.BahmniFhirImmunizationService;
import org.bahmni.module.immunization.api.translator.BahmniImmunizationTranslator;
import org.hl7.fhir.r4.model.Immunization;
import org.openmrs.module.fhir2.api.impl.BaseFhirService;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Getter
@Component
@Transactional
public class BahmniFhirImmunizationServiceImpl extends BaseFhirService<Immunization, FhirImmunization>
		implements BahmniFhirImmunizationService {

	private final BahmniFhirImmunizationDao dao;

	private final BahmniImmunizationTranslator translator;

	private final SearchQueryInclude<Immunization> searchQueryInclude;

	private final SearchQuery<FhirImmunization, Immunization, BahmniFhirImmunizationDao, BahmniImmunizationTranslator, SearchQueryInclude<Immunization>> searchQuery;

	public BahmniFhirImmunizationServiceImpl(BahmniFhirImmunizationDao dao, BahmniImmunizationTranslator translator,
			SearchQueryInclude<Immunization> searchQueryInclude,
			SearchQuery<FhirImmunization, Immunization, BahmniFhirImmunizationDao, BahmniImmunizationTranslator, SearchQueryInclude<Immunization>> searchQuery) {
		this.dao = dao;
		this.translator = translator;
		this.searchQueryInclude = searchQueryInclude;
		this.searchQuery = searchQuery;
	}

	@Override
	@Transactional(readOnly = true)
	public IBundleProvider searchImmunizations(BahmniImmunizationSearchParams searchParams) {
		if (!searchParams.hasPatientReference() && !searchParams.hasId()) {
			throw new UnsupportedOperationException(
					"You must specify patient reference or resource _id!");
		}
		return searchQuery.getQueryResults(searchParams.toSearchParameterMap(), dao, translator, searchQueryInclude);
	}
}
