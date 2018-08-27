package me.oenetwork.faillightcontrol.model;


public class Failure {

    private boolean doFail;
    private int failFor;

    public Failure() {
    }

    public Failure(boolean doFail, int failFor) {
        this.doFail = doFail;
        this.failFor = failFor;
    }

    public boolean isDoFail() {
        return doFail;
    }

    public void setDoFail(boolean doFail) {
        this.doFail = doFail;
    }

    public int getFailFor() {
        return failFor;
    }

    public void setFailFor(int failFor) {
        this.failFor = failFor;
    }
}
