package org.bahmni.module.immunization.api.translator.impl;

import org.bahmni.module.immunization.api.model.FhirImmunizationStatus;
import org.hl7.fhir.r4.model.Immunization;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BahmniImmunizationStatusTranslatorTest {

	private BahmniImmunizationStatusTranslator translator;

	@Before
	public void setUp() {
		translator = new BahmniImmunizationStatusTranslator();
	}

	@Test
	public void toFhirResource_shouldTranslateCompletedStatus() {
		assertEquals(Immunization.ImmunizationStatus.COMPLETED,
				translator.toFhirResource(FhirImmunizationStatus.COMPLETED));
	}

	@Test
	public void toFhirResource_shouldTranslateNotDoneStatus() {
		assertEquals(Immunization.ImmunizationStatus.NOTDONE,
				translator.toFhirResource(FhirImmunizationStatus.NOT_DONE));
	}

	@Test
	public void toFhirResource_shouldTranslateEnteredInErrorStatus() {
		assertEquals(Immunization.ImmunizationStatus.ENTEREDINERROR,
				translator.toFhirResource(FhirImmunizationStatus.ENTERED_IN_ERROR));
	}

	@Test
	public void toFhirResource_shouldReturnNullForNullInput() {
		assertNull(translator.toFhirResource(null));
	}

	@Test
	public void toOpenmrsType_shouldTranslateCompletedStatus() {
		assertEquals(FhirImmunizationStatus.COMPLETED,
				translator.toOpenmrsType(Immunization.ImmunizationStatus.COMPLETED));
	}

	@Test
	public void toOpenmrsType_shouldTranslateNotDoneStatus() {
		assertEquals(FhirImmunizationStatus.NOT_DONE,
				translator.toOpenmrsType(Immunization.ImmunizationStatus.NOTDONE));
	}

	@Test
	public void toOpenmrsType_shouldTranslateEnteredInErrorStatus() {
		assertEquals(FhirImmunizationStatus.ENTERED_IN_ERROR,
				translator.toOpenmrsType(Immunization.ImmunizationStatus.ENTEREDINERROR));
	}

	@Test
	public void toOpenmrsType_shouldReturnNullForNullInput() {
		assertNull(translator.toOpenmrsType(null));
	}
}
