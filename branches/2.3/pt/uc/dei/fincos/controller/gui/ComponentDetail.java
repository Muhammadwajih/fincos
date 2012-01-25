package pt.uc.dei.fincos.controller.gui;

import java.awt.Dialog;

import javax.swing.JDialog;

/**
 * Superclass for several forms where the user can edit properties of the test setup.
 */
public abstract class ComponentDetail extends JDialog {

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
}
