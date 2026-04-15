package org.bahmni.module.immunization.api.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openmrs.Provider;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"immunization"})
@EqualsAndHashCode(exclude = {"immunization"}, callSuper = false)
public class ImmunizationPerformer {

	private Integer performerId;

	private FhirImmunization immunization;

	private String function;

	private Provider actor;

}
