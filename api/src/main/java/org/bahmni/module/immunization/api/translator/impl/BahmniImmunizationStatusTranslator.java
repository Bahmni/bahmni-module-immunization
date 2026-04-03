package org.bahmni.module.immunization.api.translator.impl;

import org.bahmni.module.immunization.api.model.FhirImmunizationStatus;
import org.hl7.fhir.r4.model.Immunization;
import org.springframework.stereotype.Component;

@Component
public class BahmniImmunizationStatusTranslator {

	public Immunization.ImmunizationStatus toFhirResource(FhirImmunizationStatus status) {
		if (status == null) {
			return null;
		}
		switch (status) {
			case COMPLETED:
				return Immunization.ImmunizationStatus.COMPLETED;
			case NOT_DONE:
				return Immunization.ImmunizationStatus.NOTDONE;
			case ENTERED_IN_ERROR:
				return Immunization.ImmunizationStatus.ENTEREDINERROR;
			default:
				throw new IllegalArgumentException("Unknown immunization status: " + status);
		}
	}

	public FhirImmunizationStatus toOpenmrsType(Immunization.ImmunizationStatus status) {
		if (status == null) {
			return null;
		}
		switch (status) {
			case COMPLETED:
				return FhirImmunizationStatus.COMPLETED;
			case NOTDONE:
				return FhirImmunizationStatus.NOT_DONE;
			case ENTEREDINERROR:
				return FhirImmunizationStatus.ENTERED_IN_ERROR;
			default:
				throw new IllegalArgumentException("Unknown FHIR immunization status: " + status);
		}
	}
}
