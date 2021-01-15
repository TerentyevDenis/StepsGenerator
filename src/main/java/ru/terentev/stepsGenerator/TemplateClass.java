package ru.terentev.stepsGenerator;

import javax.lang.model.element.Name;
import java.util.ArrayList;

public class TemplateClass {
    private Name templateClass;
    private ArrayList<Widget> widgets;
    private ArrayList<InnerClasse> innerClasses;

    public TemplateClass(Name innerClass, ArrayList<Widget> widgets, ArrayList<InnerClasse> innerClasses) {
        this.templateClass = innerClass;
        this.widgets = widgets;
        this.innerClasses = innerClasses;
    }

    public Name getTemplateClass() {
        return templateClass;
    }

    public void setTemplateClass(Name templateClass) {
        this.templateClass = templateClass;
    }

    public ArrayList<Widget> getWidgets() {
        return widgets;
    }

    public void setWidgets(ArrayList<Widget> widgets) {
        this.widgets = widgets;
    }

    public ArrayList<InnerClasse> getInnerClasses() {
        return innerClasses;
    }

    public void setInnerClasses(ArrayList<InnerClasse> innerClasses) {
        this.innerClasses = innerClasses;
    }
}
