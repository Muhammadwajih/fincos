package pt.uc.dei.fincos.adapters;

import java.util.Properties;

/**
 * A factory used to create the appropriate interface to
 * specific CEP products.
 * 
 * This distributable version comes with support only to the open-source engine Esper
 *  
 * @author Marcelo R.N. Mendes
 * 
 * @see CEPEngineInterface
 * @see EsperInterface
 * 
 */
public class CEPEngineFactory {
	/**
	 * A factory method used to create an instance of the appropriate
	 * subclass of the {@link CEPEngineInterface} abstract class.
	 * The information about which subclass must be created is 
	 * one of the connection {@link Properties}
	 * passed as argument. 
	 * 
	 * @param prop			A set of vendor-specific connection Properties 
	 * 						(must include a "Engine" property that indicates which subclass will be created) 	
	 * @return				A concrete subclass of {@link CEPEngineInterface}
	 * @throws Exception 	If there is no "Engine" property
	 * 
	 */
	public static CEPEngineInterface getCEPEngineInterface(Properties prop) throws Exception 
    {
		String engineName = prop.getProperty("Engine");		
		if(engineName == null || engineName.isEmpty())
			throw new Exception ("\"Engine\" propery is missing.");
		
		if (engineName.equalsIgnoreCase("Esper"))
			return new EsperInterface(prop);					
		/*
		 * !!! ADD SUPPORT FOR OTHER CEP ENGINES HERE !!! 
		 */
		else 
			return null;			
    }
	
	
}
