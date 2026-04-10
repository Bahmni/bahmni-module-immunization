package org.bahmni.module.immunization.api.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Order;
import org.openmrs.Patient;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"performers", "notes", "basedOnOrders"})
@EqualsAndHashCode(exclude = {"performers", "notes", "basedOnOrders"}, callSuper = false)
public class FhirImmunization extends BaseOpenmrsData {

	private Integer immunizationId;

	private FhirImmunizationStatus status;

	private Concept statusReason;

	private Concept vaccineCode;

	private Drug drug;

	private String drugNonCoded;

	private Patient patient;

	private Encounter encounter;

	private Date administeredOn;

	private Boolean primarySource = false;

	private Location location;

	private String locationText;

	private String manufacturer;

	private String batchNumber;

	private Concept site;

	private Concept route;

	private Double doseQuantity;

	private Concept doseUnit;

	private String doseNumber;

	private Date expirationDate;

	private Boolean isSubpotent;

	private Concept subpotentReason;

	private Set<ImmunizationPerformer> performers = new HashSet<>();

	private Set<ImmunizationNote> notes = new HashSet<>();

	private Set<Order> basedOnOrders = new HashSet<>();

	@Override
	public Integer getId() {
		return immunizationId;
	}

	@Override
	public void setId(Integer id) {
		setImmunizationId(id);
	}
}
