package org.bahmni.module.immunization.api.dao.impl;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import org.bahmni.module.immunization.api.dao.BahmniFhirImmunizationDao;
import org.bahmni.module.immunization.api.model.FhirImmunization;
import org.bahmni.module.immunization.api.model.FhirImmunizationStatus;
import org.bahmni.module.immunization.api.translator.impl.BahmniImmunizationStatusTranslator;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Immunization;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.impl.BaseFhirDao;
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaContext;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
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
	protected <U> void setupSearchParams(OpenmrsFhirCriteriaContext<FhirImmunization, U> criteriaContext,
	        SearchParameterMap theParams) {
		super.setupSearchParams(criteriaContext, theParams);
		theParams.getParameters().forEach(param -> {
			switch (param.getKey()) {
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					param.getValue().forEach(p -> getSearchQueryHelper()
							.handlePatientReference(criteriaContext, (ReferenceAndListParam) p.getParam(), "patient"));
					break;
				case FhirConstants.STATUS_SEARCH_HANDLER:
					param.getValue()
							.forEach(p -> handleStatus(criteriaContext, (TokenAndListParam) p.getParam()));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					param.getValue().forEach(p -> getSearchQueryHelper()
							.handleDateRange(criteriaContext, "administeredOn", (DateRangeParam) p.getParam())
							.ifPresent(criteriaContext::addPredicate));
					break;
				case FhirConstants.CODED_SEARCH_HANDLER:
					param.getValue()
							.forEach(p -> handleVaccineCode(criteriaContext, (TokenAndListParam) p.getParam()));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(criteriaContext, param.getValue())
							.ifPresent(criteriaContext::addPredicate);
					break;
			}
		});
	}

	private <U> void handleStatus(OpenmrsFhirCriteriaContext<FhirImmunization, U> criteriaContext,
	        TokenAndListParam status) {
		if (status != null) {
			handleAndListParam(criteriaContext.getCriteriaBuilder(), status, token -> {
				if (token.getValue() != null) {
					try {
						Immunization.ImmunizationStatus fhirStatus =
								Immunization.ImmunizationStatus.fromCode(token.getValue().toLowerCase(Locale.ROOT));
						FhirImmunizationStatus openmrsStatus = statusTranslator.toOpenmrsType(fhirStatus);
						if (openmrsStatus != null) {
							return Optional.of(criteriaContext.getCriteriaBuilder().equal(
									criteriaContext.getRoot().get("status"), openmrsStatus));
						}
					} catch (FHIRException | IllegalArgumentException e) {
						log.debug("Invalid immunization status code: {}", token.getValue(), e);
					}
				}
				return Optional.empty();
			}).ifPresent(criteriaContext::addPredicate);
		}
	}

	@Override
	protected <V, U> Path<?> paramToProp(OpenmrsFhirCriteriaContext<V, U> criteriaContext, String param) {
		switch (param) {
			case Immunization.SP_DATE:
				return criteriaContext.getRoot().get("administeredOn");
			case Immunization.SP_STATUS:
				return criteriaContext.getRoot().get("status");
			default:
				return null;
		}
	}

	private <U> void handleVaccineCode(OpenmrsFhirCriteriaContext<FhirImmunization, U> criteriaContext,
	        TokenAndListParam vaccineCode) {
		if (vaccineCode != null) {
			Join<?, ?> vaccineCodeJoin = criteriaContext.getJoin("vc").orElseGet(() -> criteriaContext.addJoin("vaccineCode", "vc"));
			getSearchQueryHelper()
					.handleCodeableConcept(criteriaContext, vaccineCode, vaccineCodeJoin, "cm", "crt")
					.ifPresent(criteriaContext::addPredicate);
		}
	}
}
