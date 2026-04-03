package org.bahmni.module.immunization.api.dao.impl;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import org.bahmni.module.immunization.api.dao.BahmniFhirImmunizationDao;
import org.bahmni.module.immunization.api.model.FhirImmunization;
import org.bahmni.module.immunization.api.model.FhirImmunizationStatus;
import org.bahmni.module.immunization.api.translator.impl.BahmniImmunizationStatusTranslator;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Immunization;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.impl.BaseFhirDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
public class BahmniFhirImmunizationDaoImpl extends BaseFhirDao<FhirImmunization> implements BahmniFhirImmunizationDao {

	private static final Logger log = LoggerFactory.getLogger(BahmniFhirImmunizationDaoImpl.class);

	private final BahmniImmunizationStatusTranslator statusTranslator;

	@Autowired
	public BahmniFhirImmunizationDaoImpl(BahmniImmunizationStatusTranslator statusTranslator) {
		this.statusTranslator = statusTranslator;
	}

	@Override
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		super.setupSearchParams(criteria, theParams);
		theParams.getParameters().forEach(param -> {
			switch (param.getKey()) {
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					param.getValue().forEach(
							p -> handlePatientReference(criteria, (ReferenceAndListParam) p.getParam(), "patient"));
					break;
				case FhirConstants.STATUS_SEARCH_HANDLER:
					param.getValue().forEach(
							p -> handleStatus(criteria, (TokenAndListParam) p.getParam()));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					param.getValue().forEach(
							p -> handleDateRange("administeredOn", (DateRangeParam) p.getParam()).ifPresent(criteria::add));
					break;
				case FhirConstants.CODED_SEARCH_HANDLER:
					param.getValue().forEach(
							p -> handleVaccineCode(criteria, (TokenAndListParam) p.getParam()));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(param.getValue()).ifPresent(criteria::add);
					break;
			}
		});
	}

	private void handleStatus(Criteria criteria, TokenAndListParam status) {
		if (status != null) {
			handleAndListParam(status, token -> {
				if (token.getValue() != null) {
					try {
						Immunization.ImmunizationStatus fhirStatus =
								Immunization.ImmunizationStatus.fromCode(token.getValue().toLowerCase(Locale.ROOT));
						FhirImmunizationStatus openmrsStatus = statusTranslator.toOpenmrsType(fhirStatus);
						if (openmrsStatus != null) {
							return Optional.of(Restrictions.eq("status", openmrsStatus));
						}
					} catch (FHIRException | IllegalArgumentException e) {
						log.debug("Invalid immunization status code: {}", token.getValue(), e);
					}
				}
				return Optional.empty();
			}).ifPresent(criteria::add);
		}
	}

	private void handleVaccineCode(Criteria criteria, TokenAndListParam vaccineCode) {
		if (vaccineCode != null) {
			if (lacksAlias(criteria, "vc")) {
				criteria.createAlias("vaccineCode", "vc");
			}
			handleCodeableConcept(criteria, vaccineCode, "vc", "cm", "crt").ifPresent(criteria::add);
		}
	}
}
