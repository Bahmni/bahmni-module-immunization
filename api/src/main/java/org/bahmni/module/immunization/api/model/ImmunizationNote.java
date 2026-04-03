package org.bahmni.module.immunization.api.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Provider;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"immunization"})
@EqualsAndHashCode(exclude = {"immunization"}, callSuper = false)
public class ImmunizationNote extends BaseOpenmrsData {

	private Integer noteId;

	private FhirImmunization immunization;

	private Provider author;

	private String authorString;

	private Date recordedOn;

	private String text;

	@Override
	public Integer getId() {
		return noteId;
	}

	@Override
	public void setId(Integer id) {
		setNoteId(id);
	}
}
