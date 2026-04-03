package org.bahmni.module.immunization.api;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Drug;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class TestDataFactory {

	private static final IParser R4_PARSER = FhirContext.forR4().newJsonParser();

	public static IBaseResource loadResourceFromFile(String filename) throws IOException {
		InputStream inputStream = TestDataFactory.class.getClassLoader().getResourceAsStream(filename);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			return R4_PARSER.parseResource(reader);
		}
	}

	public static Concept exampleConcept(String name, String uuid) {
		Concept concept = new Concept();
		concept.setUuid(uuid);
		ConceptName conceptName = new ConceptName(name, Locale.ENGLISH);
		concept.addName(conceptName);
		concept.setPreferredName(conceptName);
		return concept;
	}

	public static Concept exampleConcept(String name) {
		return exampleConcept(name, UUID.randomUUID().toString());
	}

	public static Provider exampleProvider(String name, String uuid) {
		Provider provider = new Provider();
		provider.setUuid(uuid);
		provider.setName(name);
		return provider;
	}

	public static Patient examplePatient(String uuid) {
		Patient patient = new Patient();
		patient.setUuid(uuid);
		return patient;
	}

	public static Location exampleLocation(String name, String uuid) {
		Location location = new Location();
		location.setUuid(uuid);
		location.setName(name);
		return location;
	}

	public static Drug exampleDrug(String name, String uuid) {
		Drug drug = new Drug();
		drug.setUuid(uuid);
		drug.setName(name);
		return drug;
	}

	public static Encounter exampleEncounter(String uuid) {
		Encounter encounter = new Encounter();
		encounter.setUuid(uuid);
		return encounter;
	}

	public static Order exampleOrder(String uuid) {
		Order order = new Order();
		order.setUuid(uuid);
		return order;
	}

	public static User exampleUser() {
		User user = new User();
		user.setUuid(UUID.randomUUID().toString());
		return user;
	}
}
