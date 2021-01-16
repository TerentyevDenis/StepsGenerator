package ru.terentev.stepsGenerator;

import com.google.auto.service.AutoService;
import ru.terentev.stepsGenerator.Annotations.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.*;

@SupportedAnnotationTypes(value = {"ru.terentev.stepsGenerator.Annotations.*"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class MyAnnotationProcessor extends AbstractProcessor {

    ArrayList<PageStepsClass> pageStepsClasses;
    Map<String, TemplateClass> templateClasses;
    Map<TypeMirror, Widget> widgets;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        ErrorKeeper msg = new ErrorKeeper(processingEnv.getMessager());
        widgets = collectWidgetsClasses(roundEnv, msg);
        if (widgets.isEmpty()) msg.setDone("No Widgets found! Can't generate, bye-bye");
        templateClasses = collectTemplateClasses(roundEnv, msg);
        pageStepsClasses = collectPageStepsClasses(roundEnv, msg);
        if (pageStepsClasses.isEmpty()) msg.setDone("No PageObjects found! Can't generate, bye-bye");
        else msg.debug("pages: " + pageStepsClasses.size());
        VariableElement keeper = checkKeeper(roundEnv, msg);
        if (msg.isProcessEnded()) return msg.state();
        new ClassCreator().generateSteps(pageStepsClasses, keeper, msg, processingEnv.getFiler());
        return true;
    }

    private Map<TypeMirror, Widget> collectWidgetsClasses(RoundEnvironment roundEnv, ErrorKeeper msg) {
        Map<TypeMirror, Widget> widgets = new HashMap<TypeMirror, Widget>();
        for (Element widget : roundEnv.getElementsAnnotatedWith(ru.terentev.stepsGenerator.Annotations.Widget.class)) {
            // Check if a *class* (not interface or enum) has been annotated with @Widget
            if (widget.getKind() != ElementKind.CLASS) {
                msg.setError("%s: Only classes can be annotated with @%s", widget.getSimpleName(), Widget.class.getSimpleName());
            } else {
                msg.debug("Found @Widget at " + widget);
                List<ExecutableElement> methods = ElementFilter.methodsIn(widget.getEnclosedElements());
                methods.removeIf(m -> m.getModifiers().contains(Modifier.PRIVATE));
                msg.debug(widget.getSimpleName() + " methods: " + methods);
                if (methods.isEmpty()) {
                    msg.warning("No public methods in @%s found!", widget.getSimpleName());
                } else {
                    widgets.put(widget.asType(), new Widget(widget.getSimpleName().toString(), methods));
                }
            }

        }
        return widgets;
    }

    private ArrayList<PageStepsClass> collectPageStepsClasses(RoundEnvironment roundEnv, ErrorKeeper msg) {
        ArrayList<PageStepsClass> pageStepsClasses = new ArrayList<PageStepsClass>();
        for (Element page : roundEnv.getElementsAnnotatedWith(PageObject.class)) {
            // Check if a *class* (not interface or enum) has been annotated with @PageObject
            if (page.getKind() != ElementKind.CLASS) {
                msg.setError("%s: Only classes can be annotated with @%s", page.getSimpleName(), PageObject.class.getSimpleName());
                return pageStepsClasses; // Exit processing
            }
            ArrayList<Widget> widgets = collectPageWidgets(page, msg);
            TemplateClass templateClass = collectPageTemplate(page, msg);
            ArrayList<InnerClasse> innerClasses = collectInnerClasses(page, msg);
            pageStepsClasses.add(new PageStepsClass(page.getSimpleName(), templateClass, widgets, innerClasses));
        }
        return pageStepsClasses;
    }

    private Map<String, TemplateClass> collectTemplateClasses(RoundEnvironment roundEnv, ErrorKeeper msg) {

        Map<String, TemplateClass> templateClasses = new HashMap<String, TemplateClass>();
        for (Element page : roundEnv.getElementsAnnotatedWith(TemplatePage.class)) {
            // Check if a *class* (not interface or enum) has been annotated with @PageObject
            if (page.getKind() != ElementKind.CLASS) {
                msg.setError("%s: Only classes can be annotated with @%s", page.getSimpleName(), TemplatePage.class.getSimpleName());
                return templateClasses; // Exit processing
            } else {
                ArrayList<Widget> widgets = collectPageWidgets(page, msg);
                ArrayList<InnerClasse> innerClasses = collectInnerClasses(page, msg);
                templateClasses.put(page.getSimpleName().toString(), new TemplateClass(page.getSimpleName(), widgets, innerClasses));
            }
        }
        return templateClasses;
    }

    private ArrayList<Widget> collectPageWidgets(Element page, ErrorKeeper msg) {
        ArrayList<Widget> widgets = new ArrayList<Widget>();
        List<VariableElement> fields = ElementFilter.fieldsIn(page.getEnclosedElements());
        msg.debug(page.getSimpleName() + " fields: " + fields);
        for (VariableElement field :
                fields) {
            Widget widget = this.widgets.get(field.asType());
            if (widget == null) {
                msg.setError("Class %s annotated with @%s wasn't found", field.asType().toString(), Widget.class.getSimpleName());
            } else {
                widgets.add(new Widget(widget, field));
            }
        }

        return widgets;
    }

    private TemplateClass collectPageTemplate(Element page, ErrorKeeper msg) {
        String annotationString = page.getAnnotation(PageObject.class).toString();
        if (!annotationString.equals("@ru.terentev.stepsGenerator.Annotations.PageObject(baseClass=ru.terentev.stepsGenerator.Annotations.PageObject)")) {
            msg.debug("Catch annotation with custom class : " + annotationString);
            String className = annotationString.substring(annotationString.lastIndexOf(".") + 1).replace(")", "");
            msg.debug("Class to append : '" + className + "'");
            TemplateClass templateClass = templateClasses.get(className);
            return templateClass;
        }
        return null;

    }

    private ArrayList<InnerClasse> collectInnerClasses(Element page, ErrorKeeper msg) {
        ArrayList<InnerClasse> innerClasses = new ArrayList<InnerClasse>();
        List<TypeElement> elements = ElementFilter.typesIn(page.getEnclosedElements());
        if (elements.size() != 0) {
            for (TypeElement subclass : elements) {
                ArrayList<ru.terentev.stepsGenerator.Widget> widgets = collectPageWidgets(subclass, msg);
                msg.debug("SubPage " + subclass.getSimpleName() + " fields: " + widgets);
                innerClasses.add(new InnerClasse(subclass, widgets));
            }
        }
        return innerClasses;
    }

    private VariableElement checkKeeper(RoundEnvironment roundEnv, ErrorKeeper msg) {
        // Get all @RouterKeeper annotated elements and check it is unique
        Set<? extends Element> keepers = roundEnv.getElementsAnnotatedWith(RouterKeeper.class);
        if (keepers.size() == 0) {
            msg.setDone("No RouterKeeper found! Can't generate, bye-bye");
            return null;
        }
        if (keepers.size() > 1) {
            msg.setDone("RouterKeeper must be unique! Can't generate, bye-bye");
            return null;
        }
        return (VariableElement) keepers.iterator().next();
    }
}
