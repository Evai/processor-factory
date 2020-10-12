package com.heimdall.processor.core.model;


import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author crh
 * @since 2020-10-03
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MappingModelProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    private Set<String> classes;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        classes = new HashSet<>();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(MappingModels.class.getCanonicalName());
        annotations.add(MappingModel.class.getCanonicalName());
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        try {
            // Scan classes
            for (Element annotatedElement : env.getElementsAnnotatedWith(MappingModel.class)) {

                if (annotatedElement.getKind() != ElementKind.CLASS) {
                    continue;
                }

                TypeElement typeElement = (TypeElement) annotatedElement;

                MappingModel mappingModel = typeElement.getAnnotation(MappingModel.class);

                this.writeFile(typeElement, mappingModel);
            }

            for (Element annotatedElement : env.getElementsAnnotatedWith(MappingModels.class)) {

                if (annotatedElement.getKind() != ElementKind.CLASS) {
                    continue;
                }

                TypeElement typeElement = (TypeElement) annotatedElement;

                MappingModels mappingModels = typeElement.getAnnotation(MappingModels.class);

                for (MappingModel mappingModel : mappingModels.value()) {
                    this.writeFile(typeElement, mappingModel);
                }
            }

        } catch (Exception e) {
            error(null, e.getMessage());
        } finally {
            classes.clear();
        }
        return true;
    }

    private ClassName writeFile(VariableElement typeElement) throws IOException {
        Type type = ((Symbol.VarSymbol) typeElement).asType();
        for (Type typeArgument : type.getTypeArguments()) {
            Element element = typeUtils.asElement(typeArgument);
            MappingModels mappingModels = element.getAnnotation(MappingModels.class);
            MappingModel mappingModel = typeElement.getAnnotation(MappingModel.class);
            if (mappingModels != null) {
                for (MappingModel model : mappingModels.value()) {
                    return this.writeFile(element, model);
                }
            } else if (mappingModel != null) {
                return this.writeFile(element, mappingModel);
            }
        }
        return null;
    }

    private ClassName writeFile(Element element, MappingModel mappingModel) throws IOException {

        PackageElement pkg = elementUtils.getPackageOf(element);
        String packageName = pkg.isUnnamed() ? null : pkg.getQualifiedName().toString();
        String trim = mappingModel.packageName().trim();
        packageName = trim.isEmpty() ? packageName : trim;

        String className = element.getSimpleName() + mappingModel.suffixName();

        String packageClass = packageName + className;

        ClassName classTypeName = ClassName.get(packageName, className);

        List<? extends Element> innerElements = element.getEnclosedElements();
        List<VariableElement> variableElements = innerElements
                .stream()
                .filter(e -> e.getKind().equals(ElementKind.FIELD))
                .map(e -> (VariableElement) e)
                .collect(Collectors.toList());

        StringBuilder toStringMethodStatement = new StringBuilder("return \"" + className + "{");
        List<Object> toStringMethodStatementArgs = new LinkedList<>();

        // class builder
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC);

        String[] includeFields = mappingModel.includeFields();
        String[] excludeFields = mappingModel.excludeFields();

        for (VariableElement variableElement : variableElements) {
            TypeName innerTypeName = TypeName.get(variableElement.asType());
            String fieldName = variableElement.getSimpleName().toString();
            boolean include = this.matchArray(includeFields, fieldName);
            if (!include && includeFields.length > 0) {
                continue;
            }
            boolean exclude = this.matchArray(excludeFields, fieldName);
            if (exclude && excludeFields.length > 0) {
                continue;
            }
            ClassName typeClassName = this.writeFile(variableElement);
            TypeName fieldType = typeClassName == null ? innerTypeName : typeClassName;

            addFieldSpec(toStringMethodStatement, toStringMethodStatementArgs, fieldName, fieldType, typeBuilder, classTypeName);

        }

        this.addExtraField(mappingModel, toStringMethodStatement, toStringMethodStatementArgs, typeBuilder, classTypeName);

        toStringMethodStatement.deleteCharAt(toStringMethodStatement.length() - 2)
                .deleteCharAt(toStringMethodStatement.length() - 1);
        toStringMethodStatement.append("}\"");
        typeBuilder.addMethod(
                MethodSpec.methodBuilder("toString")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .returns(String.class)
                        .addStatement(toStringMethodStatement.toString(), toStringMethodStatementArgs.toArray())
                        .build()
        );

        if (!this.classes.contains(packageClass)) {
            JavaFile.builder(packageName, typeBuilder.build()).build().writeTo(filer);
            this.classes.add(packageClass);
        }
        return classTypeName;
    }

    private void addExtraField(MappingModel mappingModel, StringBuilder toStringMethodStatement, List<Object> toStringMethodStatementArgs, TypeSpec.Builder typeBuilder, TypeName classTypeName) throws IOException {
        List<? extends TypeMirror> typeMirrors = null;
        try {
            mappingModel.addFieldClasses();
        } catch (MirroredTypesException e) {
            typeMirrors = e.getTypeMirrors();
        }

        if (typeMirrors != null && !typeMirrors.isEmpty()) {
            for (TypeMirror typeMirror : typeMirrors) {
                Element asElement = typeUtils.asElement(typeMirror);
                List<VariableElement> varElements = asElement.getEnclosedElements()
                        .stream()
                        .filter(e -> e.getKind().equals(ElementKind.FIELD))
                        .map(e -> (VariableElement) e)
                        .collect(Collectors.toList());
                for (VariableElement varElement : varElements) {
                    TypeName innerTypeName = TypeName.get(varElement.asType());
                    String fieldName = varElement.getSimpleName().toString();
                    ClassName typeClassName = this.writeFile(varElement);
                    TypeName fieldType = typeClassName == null ? innerTypeName : typeClassName;
                    addFieldSpec(toStringMethodStatement, toStringMethodStatementArgs, fieldName, fieldType, typeBuilder, classTypeName);
                }
            }
        }
    }

    private void addFieldSpec(StringBuilder toStringMethodStatement, List<Object> toStringMethodStatementArgs, String fieldName, TypeName fieldType, TypeSpec.Builder typeBuilder, TypeName classTypeName) {
        toStringMethodStatement.append(fieldName)
                .append("=\" ")
                .append("+ $N +")
                .append("\n")
                .append("\"")
                .append(", ");

        toStringMethodStatementArgs.add(fieldName);

        typeBuilder.addField(fieldType, fieldName, Modifier.PRIVATE)
                .addMethod(
                        MethodSpec.methodBuilder("get" + firstCharUpper(fieldName))
                                .addModifiers(Modifier.PUBLIC)
                                .returns(fieldType)
                                .addStatement("return this.$N", fieldName)
                                .build()
                )
                .addMethod(
                        MethodSpec.methodBuilder("set" + firstCharUpper(fieldName))
                                .addModifiers(Modifier.PUBLIC)
                                .returns(classTypeName)
                                .addParameter(fieldType, fieldName)
                                .addStatement("this.$N = $N", fieldName, fieldName)
                                .addStatement("return this")
                                .build()
                );
    }

    private boolean matchArray(String[] array, String target) {
        for (String str : array) {
            if (str.equals(target)) {
                return true;
            }
        }
        return false;
    }

    private static String firstCharUpper(String str) {
        char[] cs = str.toCharArray();
        if (Character.isUpperCase(cs[0])) {
            return String.valueOf(cs);
        }
        cs[0] -= 32;
        return String.valueOf(cs);
    }

    /**
     * Prints an error message
     *
     * @param e   The element which has caused the error. Can be null
     * @param msg The error message
     */
    private void error(Element e, String msg) {
        messager.printMessage(Diagnostic.Kind.ERROR, msg, e);
    }

}


