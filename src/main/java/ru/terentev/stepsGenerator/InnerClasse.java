package ru.terentev.stepsGenerator;

import javax.lang.model.element.TypeElement;
import java.util.ArrayList;

public class InnerClasse {
    private TypeElement innerClass;
    private ArrayList<Widget> widgets;

    public InnerClasse(TypeElement innerClass, ArrayList<Widget> widgets) {
        this.innerClass = innerClass;
        this.widgets = widgets;

    }

    public TypeElement getInnerClass() {
        return innerClass;
    }

    public void setInnerClass(TypeElement innerClass) {
        this.innerClass = innerClass;
    }

    public ArrayList<Widget> getWidgets() {
        return widgets;
    }

    public void setWidgets(ArrayList<Widget> widgets) {
        this.widgets = widgets;
    }
}
