package org.bahmni.module.immunization.api.translator.impl;

import lombok.AllArgsConstructor;
import org.bahmni.module.immunization.api.model.FhirImmunization;
import org.bahmni.module.immunization.api.model.ImmunizationNote;
import org.bahmni.module.immunization.api.model.ImmunizationPerformer;
import org.bahmni.module.immunization.api.translator.BahmniImmunizationTranslator;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.SimpleQuantity;
import org.hl7.fhir.r4.model.StringType;
import org.openmrs.Drug;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Provider;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.LocationReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.CodingTranslator;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

import static org.bahmni.module.immunization.ImmunizationModuleConstants.FHIR_EXT_IMMUNIZATION_ADMINISTERED_PRODUCT;
import static org.bahmni.module.immunization.ImmunizationModuleConstants.FHIR_EXT_IMMUNIZATION_BASED_ON;

@Component
@AllArgsConstructor
public class BahmniImmunizationTranslatorImpl implements BahmniImmunizationTranslator {

	private static final String PERFORMER_FUNCTION_SYSTEM = "http://terminology.hl7.org/CodeSystem/v2-0443";
	private static final String PERFORMER_FUNCTION_AP = "AP";
	private static final String PERFORMER_FUNCTION_OP = "OP";
	private static final String MEDICATION_REFERENCE_PREFIX = "Medication/";
	private static final String MEDICATION_REQUEST_REFERENCE_PREFIX = "MedicationRequest/";

	private final PatientReferenceTranslator patientReferenceTranslator;
	private final EncounterReferenceTranslator<Encounter> encounterReferenceTranslator;
	private final LocationReferenceTranslator locationReferenceTranslator;
	private final PractitionerReferenceTranslator<Provider> practitionerReferenceTranslator;
	private final ConceptTranslator conceptTranslator;
	@Qualifier("medicationQuantityCodingTranslatorImpl")
	private final CodingTranslator quantityCodingTranslator;
	private final BahmniImmunizationStatusTranslator statusTranslator;
	private final ConceptService conceptService;
	private final OrderService orderService;

	@Override
	public Immunization toFhirResource(FhirImmunization entity) {
		if (entity == null) {
			return null;
		}

		Immunization immunization = new Immunization();
		immunization.setId(entity.getUuid());
		immunization.setStatus(statusTranslator.toFhirResource(entity.getStatus()));
		immunization.setRecorded(entity.getDateCreated());

		if (entity.getStatusReason() != null) {
			immunization.setStatusReason(conceptTranslator.toFhirResource(entity.getStatusReason()));
		}

		if (entity.getVaccineCode() != null) {
			immunization.setVaccineCode(conceptTranslator.toFhirResource(entity.getVaccineCode()));
		}

		if (entity.getPatient() != null) {
			immunization.setPatient(patientReferenceTranslator.toFhirResource(entity.getPatient()));
		}

		if (entity.getEncounter() != null) {
			immunization.setEncounter(encounterReferenceTranslator.toFhirResource(entity.getEncounter()));
		}

		if (entity.getAdministeredOn() != null) {
			immunization.setOccurrence(new org.hl7.fhir.r4.model.DateTimeType(entity.getAdministeredOn()));
		}

		if (entity.getPrimarySource() != null) {
			immunization.setPrimarySource(entity.getPrimarySource());
		}

		translateLocationToFhir(entity, immunization);

		if (entity.getManufacturer() != null) {
			Reference mfr = new Reference();
			mfr.setDisplay(entity.getManufacturer());
			immunization.setManufacturer(mfr);
		}

		immunization.setLotNumber(entity.getBatchNumber());

		if (entity.getSite() != null) {
			immunization.setSite(conceptTranslator.toFhirResource(entity.getSite()));
		}

		if (entity.getRoute() != null) {
			immunization.setRoute(conceptTranslator.toFhirResource(entity.getRoute()));
		}

		translateDoseToFhir(entity, immunization);

		if (entity.getExpirationDate() != null) {
			immunization.setExpirationDate(entity.getExpirationDate());
		}

		if (entity.getIsSubpotent() != null) {
			immunization.setIsSubpotent(entity.getIsSubpotent());
		}
		if (entity.getSubpotentReason() != null) {
			immunization.addSubpotentReason(conceptTranslator.toFhirResource(entity.getSubpotentReason()));
		}

		translatePerformersToFhir(entity, immunization);
		translateNotesToFhir(entity, immunization);
		translateDrugExtensionToFhir(entity, immunization);
		translateBasedOnToFhir(entity, immunization);

		return immunization;
	}

	@Override
	public FhirImmunization toOpenmrsType(Immunization resource) {
		if (resource == null) {
			return null;
		}
		return toOpenmrsType(new FhirImmunization(), resource);
	}

	@Override
	public FhirImmunization toOpenmrsType(@Nonnull FhirImmunization existing, Immunization resource) {
		if (resource == null) {
			return existing;
		}

		existing.setStatus(statusTranslator.toOpenmrsType(resource.getStatus()));

		if (resource.hasStatusReason()) {
			existing.setStatusReason(conceptTranslator.toOpenmrsType(resource.getStatusReason()));
		}

		if (resource.hasVaccineCode()) {
			existing.setVaccineCode(conceptTranslator.toOpenmrsType(resource.getVaccineCode()));
		}

		if (resource.hasPatient()) {
			existing.setPatient(patientReferenceTranslator.toOpenmrsType(resource.getPatient()));
		}

		if (resource.hasEncounter()) {
			existing.setEncounter(encounterReferenceTranslator.toOpenmrsType(resource.getEncounter()));
		}

		if (resource.hasOccurrenceDateTimeType()) {
			existing.setAdministeredOn(resource.getOccurrenceDateTimeType().getValue());
		}

		if (resource.hasPrimarySource()) {
			existing.setPrimarySource(resource.getPrimarySource());
		}

		translateLocationToOpenmrs(existing, resource);

		if (resource.hasManufacturer() && resource.getManufacturer().hasDisplay()) {
			existing.setManufacturer(resource.getManufacturer().getDisplay());
		}

		if (resource.hasLotNumber()) {
			existing.setBatchNumber(resource.getLotNumber());
		}

		if (resource.hasSite()) {
			existing.setSite(conceptTranslator.toOpenmrsType(resource.getSite()));
		}

		if (resource.hasRoute()) {
			existing.setRoute(conceptTranslator.toOpenmrsType(resource.getRoute()));
		}

		translateDoseToOpenmrs(existing, resource);

		if (resource.hasExpirationDate()) {
			existing.setExpirationDate(resource.getExpirationDate());
		}

		if (resource.hasIsSubpotent()) {
			existing.setIsSubpotent(resource.getIsSubpotent());
		}
		if (resource.hasSubpotentReason()) {
			existing.setSubpotentReason(
					conceptTranslator.toOpenmrsType(resource.getSubpotentReasonFirstRep()));
		}

		translatePerformersToOpenmrs(existing, resource);
		translateNotesToOpenmrs(existing, resource);
		translateDrugExtensionToDrug(existing, resource);
		translateBasedOnToDrugOrder(existing, resource);

		return existing;
	}

	private void translateLocationToFhir(FhirImmunization entity, Immunization immunization) {
		if (entity.getLocation() != null) {
			immunization.setLocation(locationReferenceTranslator.toFhirResource(entity.getLocation()));
		} else if (entity.getLocationText() != null) {
			Reference locationRef = new Reference();
			locationRef.setDisplay(entity.getLocationText());
			immunization.setLocation(locationRef);
		}
	}

	private void translateDoseToFhir(FhirImmunization entity, Immunization immunization) {
		if (entity.getDoseQuantity() != null) {
			SimpleQuantity qty = new SimpleQuantity();
			qty.setValue(BigDecimal.valueOf(entity.getDoseQuantity()));
			if (entity.getDoseUnit() != null) {
				Coding coding = quantityCodingTranslator.toFhirResource(entity.getDoseUnit());
				if (coding != null) {
					qty.setSystem(coding.getSystem());
					qty.setCode(coding.getCode());
					qty.setUnit(coding.getDisplay());
				}
			}
			immunization.setDoseQuantity(qty);
		}

		if (entity.getDoseNumber() != null) {
			Immunization.ImmunizationProtocolAppliedComponent protocol =
					new Immunization.ImmunizationProtocolAppliedComponent();
			try {
				protocol.setDoseNumber(new org.hl7.fhir.r4.model.PositiveIntType(Integer.parseInt(entity.getDoseNumber())));
			} catch (NumberFormatException e) {
				protocol.setDoseNumber(new org.hl7.fhir.r4.model.StringType(entity.getDoseNumber()));
			}
			immunization.addProtocolApplied(protocol);
		}
	}

	private void translateDrugExtensionToFhir(FhirImmunization entity, Immunization immunization) {
		if (entity.getDrug() != null) {
			Reference drugRef = new Reference();
			drugRef.setReference(MEDICATION_REFERENCE_PREFIX + entity.getDrug().getUuid());
			drugRef.setDisplay(entity.getDrug().getName());
			immunization.addExtension(FHIR_EXT_IMMUNIZATION_ADMINISTERED_PRODUCT, drugRef);
		} else if (entity.getDrugNonCoded() != null) {
			Reference drugRef = new Reference();
			drugRef.setDisplay(entity.getDrugNonCoded());
			immunization.addExtension(FHIR_EXT_IMMUNIZATION_ADMINISTERED_PRODUCT, drugRef);
		}
	}

	private void translateBasedOnToFhir(FhirImmunization entity, Immunization immunization) {
		for (Order order : entity.getBasedOnOrders()) {
			Reference orderRef = new Reference();
			orderRef.setReference(MEDICATION_REQUEST_REFERENCE_PREFIX + order.getUuid());
			if (order.getConcept() != null && order.getConcept().getName() != null) {
				orderRef.setDisplay(order.getConcept().getName().getName());
			}
			immunization.addExtension(FHIR_EXT_IMMUNIZATION_BASED_ON, orderRef);
		}
	}

	private void translateLocationToOpenmrs(FhirImmunization existing, Immunization resource) {
		if (resource.hasLocation()) {
			Reference locationRef = resource.getLocation();
			if (locationRef.hasReference()) {
				existing.setLocation(locationReferenceTranslator.toOpenmrsType(locationRef));
			} else if (locationRef.hasDisplay()) {
				existing.setLocationText(locationRef.getDisplay());
			}
		}
	}

	private void translateDoseToOpenmrs(FhirImmunization existing, Immunization resource) {
		if (resource.hasDoseQuantity()) {
			existing.setDoseQuantity(resource.getDoseQuantity().getValue().doubleValue());
			existing.setDoseUnit(quantityCodingTranslator.toOpenmrsType(resource.getDoseQuantity()));
		}

		if (resource.hasProtocolApplied()) {
			Immunization.ImmunizationProtocolAppliedComponent protocol = resource.getProtocolAppliedFirstRep();
			if (protocol.hasDoseNumberPositiveIntType()) {
				existing.setDoseNumber(String.valueOf(protocol.getDoseNumberPositiveIntType().getValue()));
			} else if (protocol.hasDoseNumberStringType()) {
				existing.setDoseNumber(protocol.getDoseNumberStringType().getValue());
			}
		}
	}

	private void translateDrugExtensionToDrug(FhirImmunization existing, Immunization resource) {
		Extension drugExt = resource.getExtensionByUrl(FHIR_EXT_IMMUNIZATION_ADMINISTERED_PRODUCT);
		if (drugExt != null && drugExt.getValue() instanceof Reference) {
			Reference drugRef = (Reference) drugExt.getValue();
			String ref = drugRef.getReference();
			if (ref != null && ref.startsWith(MEDICATION_REFERENCE_PREFIX)) {
				String drugUuid = ref.substring(MEDICATION_REFERENCE_PREFIX.length());
				Drug drug = conceptService.getDrugByUuid(drugUuid);
				if (drug == null) {
					throw new InvalidRequestException("Could not find drug with UUID: " + drugUuid);
				}
				existing.setDrug(drug);
			} else if (drugRef.hasDisplay()) {
				existing.setDrugNonCoded(drugRef.getDisplay());
			}
		}
	}

	private void translateBasedOnToDrugOrder(FhirImmunization existing, Immunization resource) {
		java.util.List<Extension> orderExtensions = resource.getExtensionsByUrl(FHIR_EXT_IMMUNIZATION_BASED_ON);
		if (orderExtensions.isEmpty()) {
			// For an update: if the existing record had orders but the incoming
			// resource carries no basedOn extension, de-associate those orders.
			if (!existing.getBasedOnOrders().isEmpty()) {
				existing.getBasedOnOrders().clear();
			}
			return;
		}
		existing.getBasedOnOrders().clear();
		for (Extension orderExt : orderExtensions) {
			if (orderExt.getValue() instanceof Reference) {
				Reference orderRef = (Reference) orderExt.getValue();
				String ref = orderRef.getReference();
				if (ref != null && ref.startsWith(MEDICATION_REQUEST_REFERENCE_PREFIX)) {
					String orderUuid = ref.substring(MEDICATION_REQUEST_REFERENCE_PREFIX.length());
					Order order = orderService.getOrderByUuid(orderUuid);
					if (order == null) {
						throw new InvalidRequestException("Could not find order with UUID: " + orderUuid);
					}
					existing.getBasedOnOrders().add(order);
				}
			}
		}
	}

	private void translatePerformersToFhir(FhirImmunization entity, Immunization immunization) {
		if (entity.getPerformers() == null || entity.getPerformers().isEmpty()) {
			return;
		}
		for (ImmunizationPerformer performer : entity.getPerformers()) {
			Immunization.ImmunizationPerformerComponent fhirPerformer =
					new Immunization.ImmunizationPerformerComponent();

			CodeableConcept function = new CodeableConcept();
			function.addCoding(new Coding()
					.setSystem(PERFORMER_FUNCTION_SYSTEM)
					.setCode(performer.getFunction()));
			fhirPerformer.setFunction(function);

			if (performer.getActor() != null) {
				fhirPerformer.setActor(practitionerReferenceTranslator.toFhirResource(performer.getActor()));
			}

			immunization.addPerformer(fhirPerformer);
		}
	}

	private void translateNotesToFhir(FhirImmunization entity, Immunization immunization) {
		if (entity.getNotes() == null || entity.getNotes().isEmpty()) {
			return;
		}
		for (ImmunizationNote note : entity.getNotes()) {
			Annotation fhirAnnotation = new Annotation();
			fhirAnnotation.setText(note.getText());

			if (note.getAuthor() != null) {
				fhirAnnotation.setAuthor(practitionerReferenceTranslator.toFhirResource(note.getAuthor()));
			} else if (note.getAuthorString() != null) {
				fhirAnnotation.setAuthor(new StringType(note.getAuthorString()));
			}

			if (note.getRecordedOn() != null) {
				fhirAnnotation.setTime(note.getRecordedOn());
			}

			immunization.addNote(fhirAnnotation);
		}
	}

	private void translatePerformersToOpenmrs(FhirImmunization existing, Immunization resource) {
		if (!resource.hasPerformer()) {
			if (!existing.getPerformers().isEmpty()) {
				existing.getPerformers().clear();
			}
			return;
		}
		existing.getPerformers().clear();
		for (Immunization.ImmunizationPerformerComponent fhirPerformer : resource.getPerformer()) {
			ImmunizationPerformer performer = new ImmunizationPerformer();
			performer.setImmunization(existing);

			if (fhirPerformer.hasFunction() && fhirPerformer.getFunction().hasCoding()) {
				performer.setFunction(fhirPerformer.getFunction().getCodingFirstRep().getCode());
			}

			if (fhirPerformer.hasActor()) {
				performer.setActor(practitionerReferenceTranslator.toOpenmrsType(fhirPerformer.getActor()));
			}

			existing.getPerformers().add(performer);
		}
	}

	private void translateNotesToOpenmrs(FhirImmunization existing, Immunization resource) {
		if (!resource.hasNote()) {
			return;
		}
		existing.getNotes().clear();
		for (Annotation fhirAnnotation : resource.getNote()) {
			if (!fhirAnnotation.hasText()) {
				throw new InvalidRequestException("Immunization note must have text");
			}
			ImmunizationNote note = new ImmunizationNote();
			note.setImmunization(existing);
			note.setText(fhirAnnotation.getText());

			if (fhirAnnotation.hasAuthorReference()) {
				note.setAuthor(practitionerReferenceTranslator.toOpenmrsType(
						fhirAnnotation.getAuthorReference()));
			} else if (fhirAnnotation.hasAuthorStringType()) {
				note.setAuthorString(fhirAnnotation.getAuthorStringType().getValue());
			}

			if (fhirAnnotation.hasTime()) {
				note.setRecordedOn(fhirAnnotation.getTime());
			}

			existing.getNotes().add(note);
		}
	}
}
