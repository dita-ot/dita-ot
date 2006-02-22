package com.idiominc.ws.opentopic.fo.xep;


public class Logger implements com.renderx.xep.lib.Logger {

    private Runner runner;

    public Logger(Runner runner) {
        this.runner = runner;
    }

    public void openDocument() {
    }

    public void closeDocument() {
    }

    public void event(String name, String message) {
    }

    public void openState(String name) {
    }

    public void closeState(String name) {
    }

    public void info(String message) {
    }

    public void warning(String message) {
        this.runner.fail();
        System.out.println("WARNING: " + message);
    }

    public void error(String message) {
        this.runner.fail();
        System.out.println("ERROR: " + message);
    }

    public void exception(String message, java.lang.Exception except) {
        this.runner.fail();
        System.out.println("EXCEPTION: " + message);
    }
}

