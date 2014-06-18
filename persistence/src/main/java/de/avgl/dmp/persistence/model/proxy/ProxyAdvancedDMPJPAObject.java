package de.avgl.dmp.persistence.model.proxy;

import javax.xml.bind.annotation.XmlRootElement;

import de.avgl.dmp.persistence.model.AdvancedDMPJPAObject;

/**
 * An abstract proxy POJO class for where the uri of the real objects should be provided by creation and that can have a name and
 * where the identifier of the real object should be generated by the database.
 * 
 * @author tgaengler
 */
@XmlRootElement
public abstract class ProxyAdvancedDMPJPAObject<POJOCLASS extends AdvancedDMPJPAObject> extends ProxyBasicDMPJPAObject<POJOCLASS> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created object, i.e., no updated or already existing object.
	 * 
	 * @param advancedDMPJPAObjectArg a freshly created object
	 */
	public ProxyAdvancedDMPJPAObject(final POJOCLASS advancedDMPJPAObjectArg) {

		super(advancedDMPJPAObjectArg);
	}

	/**
	 * Creates a new proxy with the given real object and the type how the object was processed by the persistence service, e.g.,
	 * {@link RetrievalType.CREATED}.
	 * 
	 * @param advancedDMPJPAObjectArg an object that was processed by a persistence service
	 * @param typeArg the type how this object was processed by the persistence service
	 */
	public ProxyAdvancedDMPJPAObject(final POJOCLASS advancedDMPJPAObjectArg, final RetrievalType typeArg) {

		super(advancedDMPJPAObjectArg, typeArg);
	}

	/**
	 * Gets the uri of the real entity.
	 * 
	 * @return the uri of the real entity
	 */
	public String getUri() {

		if (dmpObject == null) {

			return null;
		}

		return dmpObject.getUri();
	}
}
