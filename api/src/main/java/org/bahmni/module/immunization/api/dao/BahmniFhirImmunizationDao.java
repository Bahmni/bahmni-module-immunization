package org.bahmni.module.immunization.api.dao;

import org.bahmni.module.immunization.api.model.FhirImmunization;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

import static org.bahmni.module.immunization.ImmunizationsPrivilegeConstants.*;

public interface BahmniFhirImmunizationDao extends FhirDao<FhirImmunization> {

	@Override
	@Authorized({ GET_IMMUNIZATION })
	FhirImmunization get(@Nonnull String uuid);

	@Override
	@Authorized({ GET_IMMUNIZATION })
	List<FhirImmunization> get(@Nonnull Collection<String> uuids);

	@Override
	@Authorized({ GET_IMMUNIZATION })
	List<FhirImmunization> getSearchResults(@Nonnull SearchParameterMap searchParameterMap);

	@Override
	@Authorized({ GET_IMMUNIZATION })
	int getSearchResultsCount(@Nonnull SearchParameterMap searchParameterMap);

	@Override
	@Authorized({ ADD_IMMUNIZATION, EDIT_IMMUNIZATION })
	FhirImmunization createOrUpdate(@Nonnull FhirImmunization immunization);

	@Override
	@Authorized({ DELETE_IMMUNIZATION })
	FhirImmunization delete(@Nonnull String uuid);
}
