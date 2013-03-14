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


package pt.uc.dei.fincos.controller.gui;

import java.awt.Color;
import java.awt.Dialog;

import javax.swing.JDialog;

/**
 * Superclass for several forms where the user can edit properties of the test setup.
 *
 * @author  Marcelo R.N. Mendes
 *
 */
public abstract class ComponentDetail extends JDialog {

    /** Color used to identify fields at the UI that were not properly filled. */
    public static final Color INVALID_INPUT_COLOR = new Color(245, 255, 150);

    /**
     * Creates a non modal form.
     *
     * @param owner the owner <code>Dialog</code> from which the dialog is displayed,
     *     or <code>null</code> if this dialog has no owner
     */
    public ComponentDetail(Dialog owner) {
        super(owner);
    }

    /** serial id. */
    private static final long serialVersionUID = -2132157874090310589L;

    /** Mode of the form (either EDIT or INSERT). */
    int op;

    /** UPDATE GUI mode (a previously created component is being edited). */
    static final int UPDATE = 0;

    /** INSERT GUI mode (a new component is being configured). */
    static final int INSERT = 1;

    /**
     * Checks if the fields in the UI have been correctly filled.
     *
     * @return  <tt>true</tt> if all the parameters have been correctly filled,
     *          <tt>false</tt> otherwise
     *
     * @throws Exception  for customized error messages
     */
    protected abstract boolean validateFields() throws Exception;
}
