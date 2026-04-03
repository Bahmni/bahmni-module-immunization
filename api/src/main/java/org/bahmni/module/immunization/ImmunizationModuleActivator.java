package org.bahmni.module.immunization;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.BaseModuleActivator;

public class ImmunizationModuleActivator extends BaseModuleActivator {

	private static final Log LOG = LogFactory.getLog(ImmunizationModuleActivator.class);

	@Override
	public void willStart() {
		LOG.info("Starting Immunization Module");
	}

	@Override
	public void willStop() {
		LOG.info("Shutting down Immunization Module");
	}

	@Override
	public void started() {
		super.started();
		LOG.info("Immunization Module Started");
	}

	@Override
	public void willRefreshContext() {
		super.willRefreshContext();
		LOG.info("Immunization Module Will refresh context");
	}

	@Override
	public void stopped() {
		super.stopped();
		LOG.info("Immunization Module Stopped");
	}
}
