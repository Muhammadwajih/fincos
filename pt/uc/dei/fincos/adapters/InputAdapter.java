/* FINCoS Framework
 * Copyright (C) 2012 CISUC, University of Coimbra
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


package pt.uc.dei.fincos.adapters;

import pt.uc.dei.fincos.basic.CSV_Event;
import pt.uc.dei.fincos.basic.Event;

/**
 * Interface to send events to a CEP engine or a JMS Provider.
 *
 * @author  Marcelo R.N. Mendes
 */
public interface InputAdapter {
    /**
     *
     *
     * Converts and sends events generated by FINCoS to the target system.
     * Events are strongly typed.
     *
     *
     * @param e             the event to be converted and sent
     * @throws Exception    if an error occurs during event submission
     */
    void send(Event e) throws Exception;

    /**
    *
    * Converts and sends events read from data files to the target system.
    * Events attributes are not typed (all Strings).
    *
    *
    * @param event          the event to be converted and sent
     * @throws Exception    if an error occurs during event submission
    */
    void send(CSV_Event event) throws Exception;
}
