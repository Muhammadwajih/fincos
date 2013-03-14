/* FINCoS Framework
 * Copyright (C) 2013 CISUC, University of Coimbra
 *
 * Licensed under the terms of The GNU General Public License, Version 2.
 * A copy of the License has been included with this distribution in the
 * fincos-license.txt file.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 */


package pt.uc.dei.fincos.adapters.cep;

import java.util.Properties;


/**
 * A factory used to create the appropriate interface to specific CEP products.
 *
 * This version of FINCoS comes with native support only to the open-source
 * engine Esper.
 *
 * @author  Marcelo R.N. Mendes
 *
 * @see CEP_EngineInterface
 * @see EsperInterface
 *
 */
public final class CEP_EngineFactory {

    /** List of CEP engines for which there is an implemented adapter. */
    //ADD SUPPORT FOR OTHER CEP ENGINES HERE
    private static final String[] supportedEngines = {"Esper"};


    /**
     * A factory method used to create an instance of the appropriate subclass
     * of the {@link CEP_EngineInterface} abstract class. The information about
     * which subclass must be created is one of the connection
     * {@link Properties} passed as argument.
     *
     *
     * @param prop          A set of vendor-specific connection Properties
     *                      (must include a "Engine" property that indicates
     *                      which subclass will be created)
     * @param rtMode        response time measurement mode
     *                      (either END-TO-END, ADAPTER or NO_RT)
     * @param rtResolution  response time measurement resolution
     *                      (either Milliseconds or Nanoseconds)
     *
     * @return              A concrete subclass of {@link CEP_EngineInterface}
     *
     * @throws Exception    If there is no "Engine" property
     *
     */
    public static CEP_EngineInterface getCEPEngineInterface(Properties prop,
            int rtMode, int rtResolution) throws Exception {
        return getCEPEngineInterface(prop.getProperty("engine"),
                                     prop, rtMode, rtResolution);
    }

    /**
     * A factory method used to create an instance of the appropriate subclass
     * of the {@link CEP_EngineInterface} abstract class. The information about
     * which subclass must be created is passed as argument.
     *
     *
     * @param engine        Indicates which subclass will be created
     * @param prop          A set of vendor-specific connection Properties
     * @param rtMode        response time measurement mode
     *                      (either END-TO-END, ADAPTER or NO_RT)
     * @param rtResolution  response time measurement resolution
     *                      (either Milliseconds or Nanoseconds)
     *
     * @return              A concrete subclass of {@link CEP_EngineInterface}
     *
     * @throws Exception    If the engine argument is null or empty
     *
     */
    public static CEP_EngineInterface getCEPEngineInterface(String engine,
            Properties prop, int rtMode, int rtResolution) throws Exception {
        if (engine == null || engine.isEmpty()) {
            throw new Exception("\"Engine\" property is missing.");
        }
        if (engine.equalsIgnoreCase("Esper")) {
            return EsperInterface.getInstance(prop, rtMode, rtResolution);
        } else {
            /* !!! ADD SUPPORT FOR OTHER CEP ENGINES HERE !!! */
            throw new Exception("ERROR: Engine not supported. "
                    + "Could not found an adapter implementation for engine \""
                    + engine + "\".");
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
