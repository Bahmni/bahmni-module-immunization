package org.bahmni.module.immunization.api.translator;

import org.bahmni.module.immunization.api.model.FhirImmunization;
import org.hl7.fhir.r4.model.Immunization;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirTranslator;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirUpdatableTranslator;

public interface BahmniImmunizationTranslator
		extends OpenmrsFhirTranslator<FhirImmunization, Immunization>,
		OpenmrsFhirUpdatableTranslator<FhirImmunization, Immunization> {

}
