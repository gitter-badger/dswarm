package de.avgl.dmp.controller.resources;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.utils.AdvancedDMPResourceUtils;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.AdvancedDMPJPAObject;
import de.avgl.dmp.persistence.model.BasicDMPJPAObject;
import de.avgl.dmp.persistence.model.proxy.ProxyAdvancedDMPJPAObject;
import de.avgl.dmp.persistence.service.AdvancedDMPJPAService;

/**
 * A generic resource (controller service) implementation for {@link BasicDMPJPAObject}s, i.e., objects where the identifier will
 * be generated by the database and that can have a name.
 * 
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE> the concrete persistence service of the resource that is related to the concrete POJO
 *            class
 * @param <POJOCLASS> the concrete POJO class of the resource
 */
public abstract class AdvancedDMPResource<POJOCLASSRESOURCEUTILS extends AdvancedDMPResourceUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS>, POJOCLASSPERSISTENCESERVICE extends AdvancedDMPJPAService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyAdvancedDMPJPAObject<POJOCLASS>, POJOCLASS extends AdvancedDMPJPAObject>
		extends BasicDMPResource<POJOCLASSRESOURCEUTILS, POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS> {

	private static final Logger	LOG	= LoggerFactory.getLogger(AdvancedDMPResource.class);

	/**
	 * Creates a new resource (controller service) for the given concrete POJO class with the provider of the concrete persistence
	 * service, the object mapper and metrics registry.
	 * 
	 * @param clasz a concrete POJO class
	 * @param persistenceServiceProviderArg the concrete persistence service that is related to the concrete POJO class
	 * @param objectMapperArg an object mapper
	 * @param dmpStatusArg a metrics registry
	 */
	public AdvancedDMPResource(final POJOCLASSRESOURCEUTILS pojoClassResourceUtilsArg, final DMPStatus dmpStatusArg) {

		super(pojoClassResourceUtilsArg, dmpStatusArg);
	}

	@Override
	protected POJOCLASS retrieveObject(final Long id, final String jsonObjectString) throws DMPControllerException {

		if (jsonObjectString == null) {

			return super.retrieveObject(id, jsonObjectString);
		}

		// what should we do if the uri is a different one, i.e., someone tries to manipulate the uri? => check whether an entity
		// with this uri exists and manipulate this one instead
		// note: we could also throw an exception instead

		final POJOCLASS objectFromJSON = pojoClassResourceUtils.deserializeObjectJSONString(jsonObjectString);

		// get persistent object per uri

		final POJOCLASSPERSISTENCESERVICE persistenceService = pojoClassResourceUtils.getPersistenceService();

		POJOCLASS object = null;
		try {
			object = persistenceService.getObjectByUri(objectFromJSON.getUri());
		} catch (final DMPPersistenceException e) {

			AdvancedDMPResource.LOG
					.debug("couldn't retrieve " + pojoClassResourceUtils.getClaszName() + " for uri '" + objectFromJSON.getUri() + "'");

			return null;
		}

		if (object == null) {

			AdvancedDMPResource.LOG.debug(pojoClassResourceUtils.getClaszName() + " for uri '" + objectFromJSON.getUri()
					+ "' does not exist, i.e., it cannot be updated");

			return null;
		}

		AdvancedDMPResource.LOG.debug("got " + pojoClassResourceUtils.getClaszName() + " with uri '" + objectFromJSON.getUri() + "'");
		AdvancedDMPResource.LOG.trace(" = '" + ToStringBuilder.reflectionToString(object) + "'");

		return object;
	}
}
