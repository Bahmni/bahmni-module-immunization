package org.bahmni.module.immunization.api;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * Mirrors fhir2's TestFhirSpringConfiguration: loads the OpenMRS service context plus every module's
 * moduleApplicationContext.xml (fhir2 + this module) from the classpath.
 */
@Configuration
@ImportResource({ "classpath:applicationContext-service.xml", "classpath*:moduleApplicationContext.xml" })
public class TestImmunizationSpringConfiguration {

	@Bean
	public static BeanFactoryPostProcessor immunizationMappingRegistrar() {
		return beanFactory -> beanFactory.getBeanDefinition("sessionFactory").getPropertyValues().add("mappingResources",
		    new String[] { "Immunization.hbm.xml", "ImmunizationPerformer.hbm.xml", "ImmunizationNote.hbm.xml" });
	}
}
