package interfaces;

import data.StringData;
import fr.sorbonne_u.components.interfaces.DataOfferedI;

/**
 * L'interface <code>IAppareil</code>
 * 
 * <p>
 * Created on : 2019-10-02
 * </p>
 * 
 * @author 3410456
 *
 */
public interface IStringDataOffered  extends DataOfferedI{
//	/**
//	 * 
//	 * @throws Exception
//	 */
//	public void allumer() throws Exception;
//
//	/**
//	 * 
//	 * @throws Exception
//	 */
//	public void eteindre() throws Exception;
//
//	/**
//	 * Renvoie le nombre de kWh consomme a l'instant T
//	 * 
//	 * @return Nombre de kWh consomme a l'instant T
//	 * @throws Exception
//	 */
//	public int getConsommation() throws Exception;
	
	/**
	 * Methode qui sert a l'envoi d'un message StringData
	 * 
	 * @param uri : URI du composant destinataire
	 * @return un StringData
	 * @throws Exception
	 */
	public StringData sendMessage(String uri) throws Exception;
	
}