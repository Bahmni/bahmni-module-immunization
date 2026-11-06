package org.bahmni.module.immunization.api;

import org.junit.Before;
import org.openmrs.api.cache.CacheConfig;
import org.openmrs.module.fhir2.api.util.FhirGlobalPropertyHolder;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = { TestImmunizationSpringConfiguration.class }, inheritLocations = false)
public abstract class BaseImmunizationContextSensitiveTest extends BaseModuleContextSensitiveTest {

	@Autowired
	private CacheConfig cacheConfig;

	@Before
	public void clearCaches() throws Exception {
		CacheManager cm = cacheConfig.apiCacheManager();
		cm.getCacheNames().forEach(name -> cm.getCache(name).clear());
		FhirGlobalPropertyHolder.reset();
	}
}
