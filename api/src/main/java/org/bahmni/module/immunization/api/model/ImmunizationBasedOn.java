package org.bahmni.module.immunization.api.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openmrs.Order;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"immunization"})
@EqualsAndHashCode(exclude = {"immunization"}, callSuper = false)
public class ImmunizationBasedOn {

	private Integer basedOnId;

	private FhirImmunization immunization;

	private Order order;

}
