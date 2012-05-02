package pt.uc.dei.fincos.adapters.cep;

import java.util.Properties;


/**
 * A factory used to create the appropriate interface to
 * specific CEP products.
 *
 * This distributable version of FINCoS comes with support only to the open-source engine Esper.
 *
 * @author Marcelo R.N. Mendes
 *
 * @see CEPEngineInterface
 * @see EsperInterface
 *
 */
public class CEPEngineFactory {

    /** List of CEP engines for which there is an implemented adapter. */
    private static final String[] supportedEngines = {"Esper"}; // ADD SUPPORT FOR OTHER CEP ENGINES HERE


    /**
     * A factory method used to create an instance of the appropriate subclass of the {@link CEPEngineInterface} abstract class.
     * The information about which subclass must be created is one of the connection {@link Properties}
     * passed as argument.
     *
     *
     * @param prop          A set of vendor-specific connection Properties
     *                      (must include a "Engine" property that indicates which subclass will be created)
     * @return              A concrete subclass of {@link CEPEngineInterface}
     * @throws Exception    If there is no "Engine" property
     *
     */
    public static CEPEngineInterface getCEPEngineInterface(Properties prop) throws Exception {
        return getCEPEngineInterface(prop.getProperty("Engine"), prop);
    }

    /**
     * A factory method used to create an instance of the appropriate subclass of the {@link CEPEngineInterface} abstract class.
     * The information about which subclass must be created is passed as argument.
     *
     *
     * @param engine        Indicates which subclass will be created
     * @param prop          A set of vendor-specific connection Properties
     * @return              A concrete subclass of {@link CEPEngineInterface}
     * @throws Exception    If the engine argument is null or empty
     *
     */
    public static CEPEngineInterface getCEPEngineInterface(String engine, Properties prop) throws Exception
    {
        if (engine == null || engine.isEmpty()) {
            throw new Exception("\"Engine\" propery is missing.");
        }
        if (engine.equalsIgnoreCase("Esper")) {
            return new EsperInterface(prop);
        } else {
            /* !!! ADD SUPPORT FOR OTHER CEP ENGINES HERE !!! */
            throw new Exception("ERROR: Engine not supported. "
                    + "Could not found an adapter implementation for engine \"" + engine + "\".");
        }
    }

    /**
     *
     * @return  the list of CEP engines for which there is an implemented adapter
     */
    public static String[] getSupportedEngines() {
        return supportedEngines;
    }
}
