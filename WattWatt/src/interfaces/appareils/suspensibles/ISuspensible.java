package interfaces.appareils.suspensibles;

import interfaces.appareils.IAppareil;

/**
 * L'interface <code>IRefrigerateur</code>
 * 
 * <p>
 * Created on : 2019-10-02
 * </p>
 * 
 * @author 3408625
 */

public interface ISuspensible extends IAppareil {
	
	/**
	 * Suspend l'appareil suspensible
	 * @throws Exception
	 */
	public void suspendre() throws Exception;
	
	/**
	 * L'appareil suspendu reprend son activité
	 * @throws Exception
	 */
	public void reprendre() throws Exception;

}
