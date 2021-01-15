package ru.terentev.stepsGenerator;

import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;

public class PageStepsClass {
    private Name PageObject;
    private TemplateClass TemplatePage;
    private ArrayList<Widget> widgets;
    private ArrayList<InnerClasse> InnerClasses;

    public PageStepsClass( Name pageObject, TemplateClass templatePage, ArrayList<Widget> widgets, ArrayList<InnerClasse> innerClasses) {
        PageObject = pageObject;
        TemplatePage = templatePage;
        this.widgets = widgets;
        InnerClasses = innerClasses;
    }

    public  Name   getPageObject() {
        return PageObject;
    }

    public void setPageObject( Name   pageObject) {
        PageObject = pageObject;
    }

    public TemplateClass getTemplatePage() {
        return TemplatePage;
    }

    public void setTemplatePage(TemplateClass templatePage) {
        TemplatePage = templatePage;
    }

    public ArrayList<Widget> getWidgets() {
        return widgets;
    }

    public void setWidgets(ArrayList<Widget> widgets) {
        this.widgets = widgets;
    }

    public ArrayList<InnerClasse> getInnerClasses() {
        return InnerClasses;
    }

    public void setInnerClasses(ArrayList<InnerClasse> innerClasses) {
        InnerClasses = innerClasses;
    }
}
