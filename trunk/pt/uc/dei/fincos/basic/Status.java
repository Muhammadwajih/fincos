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


package pt.uc.dei.fincos.basic;

import java.io.Serializable;

/**
 * A basic class which stores status information (current step and progress)
 * for components of the framework (e.g. Drivers and Sinks).
 *
 * @author  Marcelo R.N. Mendes
 *
 * @see Step
 *
 */
public class Status implements Serializable {
    private static final long serialVersionUID = 2111379592220537821L;

    private Step step;
    private double progress;

    public Status() {
        this(Step.DISCONNECTED, 0);
    }

    public Status(Step step, double progress) {
        this.setStep(Step.DISCONNECTED);
        this.setProgress(0);
    }

    public void setStep(Step step) {
        this.step = step;
    }

    public Step getStep() {
        return step;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public double getProgress() {
        return progress;
    }
}
