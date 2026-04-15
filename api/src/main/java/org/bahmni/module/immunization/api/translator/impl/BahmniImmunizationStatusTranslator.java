package org.bahmni.module.immunization.api.translator.impl;

import org.bahmni.module.immunization.api.model.FhirImmunizationStatus;
import org.hl7.fhir.r4.model.Immunization;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

@Component
public class BahmniImmunizationStatusTranslator {

	private static final Map<FhirImmunizationStatus, Immunization.ImmunizationStatus> STATUS_TO_FHIR_MAP;

	private static final Map<Immunization.ImmunizationStatus, FhirImmunizationStatus> FHIR_TO_STATUS_MAP;

	static {
		Map<FhirImmunizationStatus, Immunization.ImmunizationStatus> toFhir = new EnumMap<>(FhirImmunizationStatus.class);
		toFhir.put(FhirImmunizationStatus.COMPLETED, Immunization.ImmunizationStatus.COMPLETED);
		toFhir.put(FhirImmunizationStatus.NOT_DONE, Immunization.ImmunizationStatus.NOTDONE);
		toFhir.put(FhirImmunizationStatus.ENTERED_IN_ERROR, Immunization.ImmunizationStatus.ENTEREDINERROR);
		STATUS_TO_FHIR_MAP = Collections.unmodifiableMap(toFhir);

		Map<Immunization.ImmunizationStatus, FhirImmunizationStatus> toOpenmrs = new EnumMap<>(Immunization.ImmunizationStatus.class);
		toOpenmrs.put(Immunization.ImmunizationStatus.COMPLETED, FhirImmunizationStatus.COMPLETED);
		toOpenmrs.put(Immunization.ImmunizationStatus.NOTDONE, FhirImmunizationStatus.NOT_DONE);
		toOpenmrs.put(Immunization.ImmunizationStatus.ENTEREDINERROR, FhirImmunizationStatus.ENTERED_IN_ERROR);
		FHIR_TO_STATUS_MAP = Collections.unmodifiableMap(toOpenmrs);
	}

	public Immunization.ImmunizationStatus toFhirResource(FhirImmunizationStatus status) {
		if (status == null) {
			return null;
		}
		Immunization.ImmunizationStatus result = STATUS_TO_FHIR_MAP.get(status);
		if (result == null) {
			throw new IllegalArgumentException("Unknown immunization status: " + status);
		}
		return result;
	}

	public FhirImmunizationStatus toOpenmrsType(Immunization.ImmunizationStatus status) {
		if (status == null) {
			return null;
		}
		FhirImmunizationStatus result = FHIR_TO_STATUS_MAP.get(status);
		if (result == null) {
			throw new IllegalArgumentException("Unknown FHIR immunization status: " + status);
		}
		return result;
	}
}
