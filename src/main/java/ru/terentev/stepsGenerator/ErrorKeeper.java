package ru.terentev.stepsGenerator;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

public class ErrorKeeper {

    private states st = states.Unknown;
    private Messager messager;
    private boolean debug = false;

    public ErrorKeeper(Messager ms) {
        messager = ms;
        info("++++++++++++++++++++++++++++++++++++++++++++++++++++");
    }

    public void setError(String s, Object... args) {
        error(s, args);
        st = states.HasError;
    }

    public void setDone(String s, Object... args) {
        info(s, args);
        if (states.HasError != st)
            st = states.Finished;
    }

    public boolean isProcessEnded() {
        return st == states.HasError || st == states.Finished;
    }

    public boolean state() {
        return st != states.HasError;
    }

    public void warning(String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.WARNING, String.format(msg, args));
    }

    public void debug(String msg, Object... args) {
        if (debug) {
            messager.printMessage(Diagnostic.Kind.OTHER, String.format(msg, args));
        }
    }

    private void error(String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args));
    }

    private void info(String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.NOTE, String.format(msg, args));
    }

    enum states {
        HasError,
        Finished,
        Unknown
    }
}
