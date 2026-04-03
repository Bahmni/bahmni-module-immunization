package org.bahmni.module.immunization.api.service;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.bahmni.module.immunization.api.search.param.BahmniImmunizationSearchParams;
import org.hl7.fhir.r4.model.Immunization;
import org.openmrs.module.fhir2.api.FhirService;

public interface BahmniFhirImmunizationService extends FhirService<Immunization> {

	IBundleProvider searchImmunizations(BahmniImmunizationSearchParams searchParams);
}
