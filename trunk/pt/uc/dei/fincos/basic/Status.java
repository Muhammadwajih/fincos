package pt.uc.dei.fincos.basic;

import java.io.Serializable;

/**
 * A basic class which stores status information (current step and progress)
 * for components of the framework (e.g. Drivers and Sinks).
 *
 * @author Marcelo R.N. Mendes
 *
 * @see Step
 *
 */
public class Status implements Serializable{
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
