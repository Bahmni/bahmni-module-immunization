package org.bahmni.module.immunization.api.providers;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import org.bahmni.module.immunization.api.search.param.BahmniImmunizationSearchParams;
import org.bahmni.module.immunization.api.service.BahmniFhirImmunizationService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.springframework.stereotype.Component;

@Component("bahmniImmunizationFhirR4ResourceProvider")
@R4Provider
@AllArgsConstructor
public class BahmniImmunizationFhirR4ResourceProvider implements IResourceProvider {

	private final BahmniFhirImmunizationService immunizationService;

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Immunization.class;
	}

	@Read
	public Immunization getImmunizationByUuid(@IdParam IdType id) {
		Immunization immunization = immunizationService.get(id.getIdPart());
		if (immunization == null) {
			throw new ResourceNotFoundException("Immunization not found with id: " + id.getIdPart());
		}
		return immunization;
	}

	@Create
	public MethodOutcome createImmunization(@ResourceParam Immunization immunization) {
		Immunization created = immunizationService.create(immunization);
		MethodOutcome outcome = new MethodOutcome();
		outcome.setCreated(true);
		outcome.setResource(created);
		return outcome;
	}

	@Update
	public MethodOutcome updateImmunization(@IdParam IdType id, @ResourceParam Immunization immunization) {
		Immunization updated = immunizationService.update(id.getIdPart(), immunization);
		MethodOutcome outcome = new MethodOutcome();
		outcome.setResource(updated);
		return outcome;
	}

	@Delete
	public OperationOutcome deleteImmunization(@IdParam IdType id) {
		immunizationService.delete(id.getIdPart());
		OperationOutcome outcome = new OperationOutcome();
		outcome.addIssue()
				.setSeverity(OperationOutcome.IssueSeverity.INFORMATION)
				.setDiagnostics("Immunization " + id.getIdPart() + " successfully deleted");
		return outcome;
	}

	@Search
	public IBundleProvider searchImmunizations(
			@OptionalParam(name = Immunization.SP_PATIENT,
					chainWhitelist = { "", Patient.SP_IDENTIFIER, Patient.SP_NAME },
					targetTypes = Patient.class)
					ReferenceAndListParam patientReference,
			@OptionalParam(name = Immunization.SP_DATE)
					DateRangeParam dateRange,
			@OptionalParam(name = Immunization.SP_VACCINE_CODE)
					TokenAndListParam vaccineCode,
			@OptionalParam(name = Immunization.SP_STATUS)
					TokenAndListParam status,
			@OptionalParam(name = Immunization.SP_RES_ID)
					TokenAndListParam id,
			@OptionalParam(name = "_lastUpdated")
					DateRangeParam lastUpdated,
			@Sort SortSpec sort) {

		BahmniImmunizationSearchParams searchParams = new BahmniImmunizationSearchParams(
				patientReference, dateRange, vaccineCode, status, id, lastUpdated, sort);
		return immunizationService.searchImmunizations(searchParams);
	}
}
