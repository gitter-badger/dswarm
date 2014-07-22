package org.dswarm.persistence.service.resource;

import javax.persistence.EntityManager;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.proxy.ProxyDataModel;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.service.ExtendedBasicDMPJPAService;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * A persistence service for {@link DataModel}s.
 * 
 * @author tgaengler
 */
public class DataModelService extends ExtendedBasicDMPJPAService<ProxyDataModel, DataModel> {

	/**
	 * Creates a new data model persistence service with the given entity manager provider.
	 * 
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public DataModelService(final Provider<EntityManager> entityManagerProvider) {

		super(DataModel.class, ProxyDataModel.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Clears the relationship to schema, data resource and configuration.
	 */
	@Override
	protected void prepareObjectForRemoval(final DataModel object) {

		// should clear the relationship to the schema, data resource, configuration
		object.setSchema(null);
		object.setDataResource(null);
		object.setConfiguration(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateObjectInternal(final DataModel object, final DataModel updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		super.updateObjectInternal(object, updateObject, entityManager);

		final Schema schema = object.getSchema();
		final Resource resource = object.getDataResource();
		final Configuration configuration = object.getConfiguration();

		updateObject.setSchema(schema);
		updateObject.setDataResource(resource);
		updateObject.setConfiguration(configuration);
	}

}