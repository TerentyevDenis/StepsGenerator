package ru.terentev.stepsGenerator;

import com.squareup.javapoet.*;
import io.qameta.allure.Step;
import ru.terentev.stepsGenerator.Annotations.GeneratedSteps;

import javax.annotation.processing.Filer;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class ClassCreator {
    void generateSteps(ArrayList<PageStepsClass> pageStepsClasses, VariableElement keeper, ErrorKeeper msg, Filer filer){
        String pack = keeper.getEnclosingElement().getEnclosingElement().toString() + ".generatedSteps";
        //String pack = "debugPages.generatedSteps";
        msg.debug("package: '" + pack + "'" + " pagesKeeper: '" + keeper.getSimpleName().toString() + "'");

        for (PageStepsClass pageStepsClass :
                pageStepsClasses) {
            generateStepClass(pageStepsClass, keeper, pack, msg, filer);
        }

    }

    private void generateStepClass(PageStepsClass pageStepsClass, VariableElement keeper, String pack, ErrorKeeper msg, Filer filer){
        String stepsClassName = pageStepsClass.getPageObject().toString() + "GeneratedSteps";
        msg.debug("generating: '" + stepsClassName + "'");
        TypeSpec.Builder stepsSpec = defaultStepsClass(stepsClassName, keeper);
        stepsSpec.addMethod(defaultPageWaiter(keeper, pageStepsClass.getPageObject().toString()).build());
        generateMethods(stepsSpec,pageStepsClass, keeper,msg,ClassName.get(pack, stepsClassName));
        //writing class as file to generated sources
        try {
            JavaFile.builder(pack, stepsSpec.build()).build().writeTo(filer);
        } catch (IOException e) {
            msg.setError("Error writing stepsFile!" + e.getMessage());
        }
    }

    private void generateMethods(TypeSpec.Builder stepsSpec, PageStepsClass pageStepsClass, VariableElement keeper, ErrorKeeper msg, ClassName className){
        for (Widget widget:
             pageStepsClass.getWidgets()) {
            for (ExecutableElement method : widget.getMethods()) {
                generateWidgetMethod(widget, method, stepsSpec, pageStepsClass, keeper, msg, className,null);
            }
        }

        if (pageStepsClass.getTemplatePage() != null) {
            for (Widget widget :
                    pageStepsClass.getTemplatePage().getWidgets()) {
                for (ExecutableElement method : widget.getMethods()) {
                    generateWidgetMethod(widget, method, stepsSpec, pageStepsClass, keeper, msg, className, null);
                }
            }
            generateInnerClassMethods(pageStepsClass.getTemplatePage().getInnerClasses(), stepsSpec, pageStepsClass, keeper, msg, className);
        }
        generateInnerClassMethods(pageStepsClass.getInnerClasses(), stepsSpec, pageStepsClass, keeper, msg, className);

    }


    private void generateInnerClassMethods(ArrayList<InnerClasse> innerClasses, TypeSpec.Builder stepsSpec, PageStepsClass pageStepsClass, VariableElement keeper, ErrorKeeper msg, ClassName className){
        if (pageStepsClass.getInnerClasses() != null) {
            for (InnerClasse innerClasse :
                    innerClasses) {
                for (Widget widget :
                        innerClasse.getWidgets()) {
                    for (ExecutableElement method : widget.getMethods()) {
                        generateWidgetMethod(widget, method, stepsSpec, pageStepsClass, keeper, msg, className, innerClasse.getInnerClass());
                    }
                }
            }
        }

    }


    private void generateWidgetMethod(Widget widget, ExecutableElement method, TypeSpec.Builder stepsSpec, PageStepsClass pageStepsClass, VariableElement keeper, ErrorKeeper msg, ClassName className, TypeElement subPage){
        MethodSpec.Builder stepSpec = defaultStep(method.getSimpleName(), subPage, widget.getField());
        //init one step default descriptor

        //collecting method "main function call" like "baseRouter.authorizationPage().login.fill" !without last brackets!
        StringBuilder mainCall = collectMainCall(keeper, pageStepsClass.getPageObject().toString(), subPage, widget.getField(), method);

        //checking widget parameters, if none found - adding last brackets to "main function call"
        if (method.getParameters().size() == 0) {
            mainCall.append("()");
        }
        else {
            appendParametersToCallAndSpec(mainCall, method, stepSpec, msg);
        }
        //checking return statement type. If something returned - adding return statement to step signature and returning "main function call"
        if (method.getReturnType().toString().equals(keeper.asType().toString()))
            addMainCallWithReturnThis(stepSpec, mainCall, className);
        else if (!method.getReturnType().toString().equals("void"))
            addMainCallWithReturn(stepSpec, method, mainCall);
        else
            addMainCallWithoutReturn(stepSpec, mainCall);
        //adding method to class descriptor
        stepsSpec.addMethod(stepSpec.build());
    }


    private StringBuilder collectMainCall(VariableElement pageRouter, String pageName, TypeElement subPage, VariableElement field, ExecutableElement method) {
        if ( subPage == null) {
            return new StringBuilder("" + pageRouter.getSimpleName() + "." + swapToLower(pageName) + "()." + field + "." + method.getSimpleName());
        } else {
            StringJoiner paramName = new StringJoiner(", ");

            ElementFilter.constructorsIn(subPage.getEnclosedElements()).get(0).getParameters().forEach(par -> paramName.add(par.getSimpleName().toString()));

            return new StringBuilder("" + pageRouter.getSimpleName() + "." + swapToLower(pageName) + "()." + swapToLower(subPage.getSimpleName().toString()) + "(" + paramName + ")." + field + "." + method.getSimpleName());
        }
    }



    private MethodSpec.Builder defaultStep(Name methodName, TypeElement subPage, VariableElement field) {
        String action;
        if (subPage != null) action = methodName.toString() + "_" + swapToLower(subPage.getSimpleName().toString()) + "_" + field.toString();
        else action = methodName.toString() + "_" + field.getSimpleName().toString();

        MethodSpec.Builder builder = MethodSpec.methodBuilder(action)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("logger.info(\"$L '$L' $L\")", "Auto-generated keyword", action, "started")
                .addAnnotation(AnnotationSpec.builder(Step.class).addMember("value", "\"" + action + "\"").build());

        if (subPage!=null && subPage.getEnclosedElements()!=null) {
            List<ExecutableElement> constructors = ElementFilter.constructorsIn(subPage.getEnclosedElements());
            for (VariableElement param : constructors.get(0).getParameters()) {
                ParameterSpec p = ParameterSpec.builder(ParameterizedTypeName.get(param.asType()), param.getSimpleName().toString()).build();
                builder.addParameter(p);
            }
        }

        return builder;

    }

    private TypeSpec.Builder defaultStepsClass(String stepsClassName, VariableElement finalKeeper) {
        TypeSpec.Builder stepsSpec = TypeSpec
                .classBuilder(stepsClassName) //something like <class AuthorizationPageGeneratedSteps>
                .superclass(ClassName.get(finalKeeper.getEnclosingElement().asType())) // extends <class containing keeper>
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(GeneratedSteps.class);

        // adding "logger" field, ClassName "loggerClass" is needed for import
        ClassName loggerClass = ClassName.get("org.apache.log4j", "Logger");
        FieldSpec logger = FieldSpec.builder(loggerClass, "logger")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .initializer("$T.getLogger($L.class)", loggerClass, stepsClassName)
                .build();
        stepsSpec.addField(logger);
        return stepsSpec;
    }

    private void appendParametersToCallAndSpec(StringBuilder mainCall, ExecutableElement method, MethodSpec.Builder stepSpec, ErrorKeeper msg) {
        //collecting parameters
        mainCall.append("(");
        String delimiter = "";
        for (VariableElement param : method.getParameters()) {
            msg.debug("adding parameter: " + param.asType().toString() + " " + param.getSimpleName().toString());
            mainCall.append(delimiter);
            delimiter = ",";
            //adding parameters to "main function call"
            mainCall.append(param.getSimpleName().toString());
            //collecting param spec and adding it to step descriptor
            ParameterSpec p = ParameterSpec.builder(ParameterizedTypeName.get(param.asType()), param.getSimpleName().toString()).build();
            if (ParameterizedTypeName.get(param.asType()).toString().contains("java.lang.String[]")) {
                stepSpec.addParameter(p).varargs(true);
            } else {
                stepSpec.addParameter(p);
            }
        }
        mainCall.append(")");
    }

    private MethodSpec.Builder defaultPageWaiter(VariableElement pageRouter, String pageName) {
        return MethodSpec.methodBuilder("wait_page")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("logger.info(\"$L\")", "Waiting " + pageName)
                .addStatement("" + pageRouter.getSimpleName() + "." + swapToLower(pageName) + "()");
    }

    private static String swapToLower(String pageName) {
        //swap first letter of pageName to lower case for correct main call build
        char[] c = pageName.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }

    private void addMainCallWithReturnThis(MethodSpec.Builder stepSpec, StringBuilder mainCall, ClassName classType) {
        stepSpec.returns(classType);
        stepSpec.addStatement("$L", mainCall.toString());
        stepSpec.addStatement("return this");
    }

    private void addMainCallWithReturn(MethodSpec.Builder stepSpec, ExecutableElement method, StringBuilder mainCall) {
        stepSpec.returns(ParameterizedTypeName.get(method.getReturnType()));
        stepSpec.addStatement("return $L", mainCall.toString());
    }

    private void addMainCallWithoutReturn(MethodSpec.Builder stepSpec, StringBuilder mainCall) {
        stepSpec.addStatement("$L", mainCall.toString());
    }
}
