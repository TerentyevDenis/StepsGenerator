package ru.terentev.stepsGenerator;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.List;

public class Widget {
    private String name;
    private List<ExecutableElement> methods;
    private VariableElement field;

    public Widget(String name, List<ExecutableElement> methods) {
        this.name = name;
        this.methods = methods;
    }

    public Widget(Widget widget, VariableElement field) {
        this.name = widget.name;
        this.methods = widget.methods;
        this.field = field;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ExecutableElement> getMethods() {
        return methods;
    }

    public void setMethods(List<ExecutableElement> methods) {
        this.methods = methods;
    }

    public VariableElement getField() {
        return field;
    }

    public void setField(VariableElement field) {
        this.field = field;
    }
}
