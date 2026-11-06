package org.bahmni.module.immunization.api.dao.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThrows;

import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ParamPrefixEnum;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.bahmni.module.immunization.api.BaseImmunizationContextSensitiveTest;
import org.bahmni.module.immunization.api.dao.BahmniFhirImmunizationDao;
import org.bahmni.module.immunization.api.model.FhirImmunization;
import org.bahmni.module.immunization.api.model.FhirImmunizationStatus;
import org.bahmni.module.immunization.api.model.ImmunizationNote;
import org.bahmni.module.immunization.api.model.ImmunizationPerformer;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class BahmniFhirImmunizationDaoImplTest extends BaseImmunizationContextSensitiveTest {

	private static final String DATASET = "org/bahmni/module/immunization/api/dao/impl/BahmniFhirImmunizationDaoImplTest_initial_data.xml";

	private static final String IMM_1 = "imm-9001-0000-0000-0000-000000000001";
	private static final String IMM_2 = "imm-9002-0000-0000-0000-000000000002";
	private static final String IMM_3 = "imm-9003-0000-0000-0000-000000000003";
	private static final String IMM_4 = "imm-9004-0000-0000-0000-000000000004";
	private static final String IMM_5 = "imm-9005-0000-0000-0000-000000000005";

	private static final String PATIENT_2_UUID = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";

	private static final String PATIENT_6_UUID = "a7e04421-525f-442f-8138-05b619d16def";

	private static final String CODE_SYSTEM = "urn:test:sstrm";
	private static final String SNOMED_SYSTEM = "http://snomed.info/sct";

	private static final String VACCINE_A_CODE = "WGT234";

	private static final String VACCINE_B_CODE = "CD41003";

	private static final String VACCINE_A_CONCEPT_UUID = "c607c80f-1ea9-4da3-bb88-6276ce8868dd";

	@Autowired
	private BahmniFhirImmunizationDao dao;

	@Before
	public void loadDataset() throws Exception {
		executeDataSet(DATASET);
	}

	@Test
	public void search_withoutParameters_returnsAllNonVoidedImmunizations() {
		assertThat(getSearchResultUuids(new SearchParameterMap()), containsInAnyOrder(IMM_1, IMM_2, IMM_3, IMM_4, IMM_5));
	}

	@Test
	public void search_byPatient_returnsOnlyThatPatientsImmunizations() {
		assertThat(getSearchResultUuids(map().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patient(PATIENT_6_UUID))),
		    containsInAnyOrder(IMM_4, IMM_5));
	}

	@Test
	public void search_byPatient_doesNotReturnVoidedImmunizationOfThatPatient() {
		assertThat(getSearchResultUuids(map().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patient(PATIENT_2_UUID))),
		    containsInAnyOrder(IMM_1, IMM_2, IMM_3));
	}

	@Test
	public void search_byUnknownPatient_returnsNothing() {
		assertThat(getSearchResultUuids(map().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patient("00000000-0000-0000-0000-000000000000"))), empty());
	}

	@Test
	public void search_byStatus_completed_returnsOnlyCompleted() {
		assertThat(getSearchResultUuids(statusSearch("completed")), containsInAnyOrder(IMM_1, IMM_3, IMM_4));
	}

	@Test
	public void search_byStatus_hyphenatedFhirCodesAreTranslated() {
		assertThat(getSearchResultUuids(statusSearch("not-done")), contains(IMM_2));
		assertThat(getSearchResultUuids(statusSearch("entered-in-error")), contains(IMM_5));
	}

	@Test
	public void search_byStatus_isCaseInsensitive() {
		assertThat(getSearchResultUuids(statusSearch("COMPLETED")), containsInAnyOrder(IMM_1, IMM_3, IMM_4));
	}

	@Test
	public void search_byStatus_orList_returnsUnion() {
		TokenAndListParam status = new TokenAndListParam()
		        .addAnd(new TokenOrListParam().add(new TokenParam("not-done")).add(new TokenParam("entered-in-error")));
		assertThat(getSearchResultUuids(map().addParameter(FhirConstants.STATUS_SEARCH_HANDLER, status)), containsInAnyOrder(IMM_2, IMM_5));
	}

	@Test
	public void search_byStatus_andList_ofContradictingStatuses_returnsNothing() {
		TokenAndListParam status = new TokenAndListParam().addAnd(new TokenParam("completed"))
		        .addAnd(new TokenParam("not-done"));
		assertThat(getSearchResultUuids(map().addParameter(FhirConstants.STATUS_SEARCH_HANDLER, status)), empty());
	}

	@Test
	public void search_byUnrecognisedStatus_isIgnoredAndDoesNotFilter() {
		assertThat(getSearchResultUuids(statusSearch("not-a-real-status")), containsInAnyOrder(IMM_1, IMM_2, IMM_3, IMM_4, IMM_5));
	}

	@Test
	public void search_byDate_greaterOrEqual_isInclusiveOfBoundary() {
		assertThat(getSearchResultUuids(dateSearch(new DateRangeParam(new DateParam(ParamPrefixEnum.GREATERTHAN_OR_EQUALS, "2024-03-15")))),
		    containsInAnyOrder(IMM_2, IMM_3, IMM_5));
	}

	@Test
	public void search_byDate_lessOrEqual_isInclusiveOfBoundary() {
		assertThat(getSearchResultUuids(dateSearch(new DateRangeParam(new DateParam(ParamPrefixEnum.LESSTHAN_OR_EQUALS, "2024-02-01")))),
		    containsInAnyOrder(IMM_1, IMM_4));
	}

	@Test
	public void search_byDate_boundedRange_returnsOnlyRowsInside() {
		assertThat(getSearchResultUuids(dateSearch(new DateRangeParam(new DateParam(ParamPrefixEnum.GREATERTHAN_OR_EQUALS, "2024-02-01"),
		        new DateParam(ParamPrefixEnum.LESSTHAN_OR_EQUALS, "2024-05-05")))), containsInAnyOrder(IMM_2, IMM_4, IMM_5));
	}

	@Test
	public void search_byDate_exactDay_returnsOnlyThatDay() {
		assertThat(getSearchResultUuids(dateSearch(new DateRangeParam(new DateParam(ParamPrefixEnum.EQUAL, "2024-03-15")))), contains(IMM_2));
	}

	@Test
	public void search_byVaccineCode_systemAndCode_matchesViaConceptMapping() {
		assertThat(getSearchResultUuids(codeSearch(new TokenParam(CODE_SYSTEM, VACCINE_A_CODE))), containsInAnyOrder(IMM_1, IMM_4, IMM_5));
		assertThat(getSearchResultUuids(codeSearch(new TokenParam(CODE_SYSTEM, VACCINE_B_CODE))), containsInAnyOrder(IMM_2, IMM_3));
	}

	@Test
	public void search_byVaccineCode_matchesTheSameConceptThroughADifferentSource() {
		assertThat(getSearchResultUuids(codeSearch(new TokenParam(SNOMED_SYSTEM, "2332523"))), containsInAnyOrder(IMM_1, IMM_4, IMM_5));
	}

	@Test
	public void search_byVaccineCode_conceptUuid_matches() {
		assertThat(getSearchResultUuids(codeSearch(new TokenParam(VACCINE_A_CONCEPT_UUID))), containsInAnyOrder(IMM_1, IMM_4, IMM_5));
	}

	@Test
	public void search_byVaccineCode_unknownCode_returnsNothing() {
		assertThat(getSearchResultUuids(codeSearch(new TokenParam(CODE_SYSTEM, "NO-SUCH-CODE"))), empty());
	}

	@Test
	public void search_byVaccineCode_codeFromWrongSystem_returnsNothing() {
		assertThat(getSearchResultUuids(codeSearch(new TokenParam(SNOMED_SYSTEM, VACCINE_A_CODE))), empty());
	}

	@Test
	public void search_combiningPatientStatusDateAndCode_appliesAllFilters() {
		SearchParameterMap params = map()
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patient(PATIENT_2_UUID))
		        .addParameter(FhirConstants.STATUS_SEARCH_HANDLER, new TokenAndListParam().addAnd(new TokenParam("completed")))
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		            new DateRangeParam(new DateParam(ParamPrefixEnum.GREATERTHAN_OR_EQUALS, "2024-02-01")))
		        .addParameter(FhirConstants.CODED_SEARCH_HANDLER,
		            new TokenAndListParam().addAnd(new TokenParam(CODE_SYSTEM, VACCINE_B_CODE)));
		assertThat(getSearchResultUuids(params), contains(IMM_3));
	}

	@Test
	public void search_combiningCodeWithPatient_doesNotBreakJoinReuse() {
		SearchParameterMap params = map()
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patient(PATIENT_6_UUID))
		        .addParameter(FhirConstants.CODED_SEARCH_HANDLER,
		            new TokenAndListParam().addAnd(new TokenParam(CODE_SYSTEM, VACCINE_A_CODE)));
		assertThat(getSearchResultUuids(params), containsInAnyOrder(IMM_4, IMM_5));
	}

	@Test
	public void search_sortedByDateAscending_ordersByAdministeredOn() {
		SearchParameterMap params = map().setSortSpec(new SortSpec("date", SortOrderEnum.ASC));
		assertThat(getSearchResultUuids(params), contains(IMM_1, IMM_4, IMM_2, IMM_5, IMM_3));
	}

	@Test
	public void search_sortedByDateDescending_ordersByAdministeredOn() {
		SearchParameterMap params = map().setSortSpec(new SortSpec("date", SortOrderEnum.DESC));
		assertThat(getSearchResultUuids(params), contains(IMM_3, IMM_5, IMM_2, IMM_4, IMM_1));
	}

	@Test
	public void count_matchesNumberOfResults_andRespectsFilters() {
		assertThat(dao.getSearchResultsCount(map()), equalTo(5));
		SearchParameterMap completed = statusSearch("completed");
		assertThat(dao.getSearchResultsCount(completed), equalTo(dao.getSearchResults(completed).size()));
		assertThat(dao.getSearchResultsCount(completed), equalTo(3));
	}

	@Test
	public void get_byUuid_returnsMappedImmunization() {
		FhirImmunization immunization = dao.get(IMM_1);

		assertThat(immunization, notNullValue());
		assertThat(immunization.getStatus(), equalTo(FhirImmunizationStatus.COMPLETED));
		assertThat(immunization.getPatient().getUuid(), equalTo(PATIENT_2_UUID));
		assertThat(immunization.getVaccineCode().getUuid(), equalTo(VACCINE_A_CONCEPT_UUID));
		assertThat(immunization.getPrimarySource(), equalTo(true));
	}

	@Test
	public void get_unknownUuid_returnsNull() {
		assertThat(dao.get("does-not-exist"), nullValue());
	}

	@Test
	public void createOrUpdate_persistsScalarsEnumAndAllChildCollections() {
		Patient patient = Context.getPatientService().getPatient(2);
		Concept vaccine = Context.getConceptService().getConcept(5089);
		Provider provider = Context.getProviderService().getProvider(1);
		Order order = Context.getOrderService().getOrder(1);

		FhirImmunization immunization = new FhirImmunization();
		immunization.setStatus(FhirImmunizationStatus.NOT_DONE);
		immunization.setPatient(patient);
		immunization.setVaccineCode(vaccine);
		immunization.setPrimarySource(true);
		immunization.setAdministeredOn(new Date());
		immunization.setManufacturer("Acme Pharma");
		immunization.setBatchNumber("LOT-42");
		immunization.setDoseQuantity(0.5);

		ImmunizationPerformer performer = new ImmunizationPerformer();
		performer.setImmunization(immunization);
		performer.setFunction("AP");
		performer.setActor(provider);
		immunization.getPerformers().add(performer);

		ImmunizationNote note = new ImmunizationNote();
		note.setImmunization(immunization);
		note.setAuthor(provider);
		note.setText("Patient felt dizzy afterwards");
		note.setRecordedOn(new Date());
		immunization.getNotes().add(note);

		immunization.getBasedOnOrders().add(order);

		String uuid = dao.createOrUpdate(immunization).getUuid();
		flushAndClear();

		FhirImmunization reloaded = dao.get(uuid);
		assertThat(reloaded.getId(), notNullValue());
		assertThat(reloaded.getStatus(), equalTo(FhirImmunizationStatus.NOT_DONE));
		assertThat(reloaded.getManufacturer(), equalTo("Acme Pharma"));
		assertThat(reloaded.getBatchNumber(), equalTo("LOT-42"));
		assertThat(reloaded.getDoseQuantity(), equalTo(0.5));
		assertThat(reloaded.getCreator(), notNullValue());
		assertThat(reloaded.getPerformers(), hasSize(1));
		assertThat(reloaded.getPerformers().iterator().next().getFunction(), equalTo("AP"));
		assertThat(reloaded.getPerformers().iterator().next().getActor().getId(), equalTo(provider.getId()));
		assertThat(reloaded.getNotes(), hasSize(1));
		assertThat(reloaded.getNotes().iterator().next().getText(), equalTo("Patient felt dizzy afterwards"));
		assertThat(reloaded.getBasedOnOrders().stream().map(Order::getUuid).collect(Collectors.toList()),
		    contains(order.getUuid()));

		assertThat(getSearchResultUuids(statusSearch("not-done")), hasItem(uuid));
	}

	@Test
	public void createOrUpdate_removingChildFromLoadedEntity_deletesOrphan() {
		Provider provider = Context.getProviderService().getProvider(1);
		FhirImmunization immunization = dao.get(IMM_1);
		ImmunizationPerformer performer = new ImmunizationPerformer();
		performer.setImmunization(immunization);
		performer.setFunction("OP");
		performer.setActor(provider);
		immunization.getPerformers().add(performer);
		dao.createOrUpdate(immunization);
		flushAndClear();
		assertThat(dao.get(IMM_1).getPerformers(), hasSize(1));

		FhirImmunization loaded = dao.get(IMM_1);
		loaded.getPerformers().clear();
		dao.createOrUpdate(loaded);
		flushAndClear();

		assertThat(dao.get(IMM_1).getPerformers(), empty());
	}

	@Test
	public void delete_isASoftDelete_voidedRowDisappearsFromSearchButStaysInTheTable() {
		assertThat(dao.delete(IMM_4), notNullValue());
		flushAndClear();

		assertThat(getSearchResultUuids(map()), not(hasItem(IMM_4)));
		FhirImmunization voided = dao.get(IMM_4);
		assertThat(voided, notNullValue());
		assertThat(voided.getVoided(), equalTo(true));
	}

	@Test
	public void delete_unknownUuid_returnsNull() {
		assertThat(dao.delete("does-not-exist"), nullValue());
	}

	@Test
	public void unauthenticatedUser_cannotSearchOrGet() {
		Context.logout();

		assertThrows(APIAuthenticationException.class, () -> dao.getSearchResults(map()));
		assertThrows(APIAuthenticationException.class, () -> dao.getSearchResultsCount(map()));
		assertThrows(APIAuthenticationException.class, () -> dao.get(IMM_1));
	}

	private SearchParameterMap map() {
		return new SearchParameterMap();
	}

	private List<String> getSearchResultUuids(SearchParameterMap params) {
		return dao.getSearchResults(params).stream().map(FhirImmunization::getUuid).collect(Collectors.toList());
	}

	private ReferenceAndListParam patient(String uuid) {
		return new ReferenceAndListParam().addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(uuid)));
	}

	private SearchParameterMap statusSearch(String code) {
		return map().addParameter(FhirConstants.STATUS_SEARCH_HANDLER, new TokenAndListParam().addAnd(new TokenParam(code)));
	}

	private SearchParameterMap dateSearch(DateRangeParam range) {
		return map().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, range);
	}

	private SearchParameterMap codeSearch(TokenParam token) {
		return map().addParameter(FhirConstants.CODED_SEARCH_HANDLER, new TokenAndListParam().addAnd(token));
	}

	private void flushAndClear() {
		Context.flushSession();
		Context.clearSession();
	}
}
