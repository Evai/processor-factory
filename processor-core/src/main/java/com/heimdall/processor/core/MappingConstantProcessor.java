package com.heimdall.processor.core;


import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author crh
 * @since 2020-10-03
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MappingConstantProcessor extends AbstractProcessor {

    private static final Pattern UNICODE_PATTERN = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
    private static final Pattern UNDERLINE_PATTERN = Pattern.compile("_(\\w)");

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    private Trees trees;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        this.trees = Trees.instance(processingEnv);

    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new LinkedHashSet<>();
        annotataions.add(MappingConstant.class.getCanonicalName());
        return annotataions;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        try {
            // Scan classes
            for (Element annotatedElement : env.getElementsAnnotatedWith(MappingConstant.class)) {

                if (annotatedElement.getKind() != ElementKind.ENUM) {
                    throw new RuntimeException("MappingConstant Only enum can be annotated with @" + annotatedElement.asType().toString());
                }

                // enum
                TypeElement typeElement = (TypeElement) annotatedElement;

                List<? extends Element> innerElements = typeElement.getEnclosedElements();

                MappingConstant mappingConstant = typeElement.getAnnotation(MappingConstant.class);

                List<VariableElement> variableElements = innerElements
                        .stream()
                        .filter(e -> e.getKind().equals(ElementKind.ENUM_CONSTANT))
                        .map(e -> (VariableElement) e)
                        .collect(Collectors.toList());

                ExecutableElement executableElement = innerElements
                        .stream()
                        .filter(e -> e.getKind().equals(ElementKind.CONSTRUCTOR))
                        .map(e -> (ExecutableElement) e)
                        .findFirst()
                        .get();

                String enumClassName = typeElement.getSimpleName().toString();

                String constantClassName = mappingConstant.className().trim();
                constantClassName = constantClassName.isEmpty() ? enumClassName + "Constant" : constantClassName;

                // class builder
                TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(constantClassName)
                        .addMethod(
                                MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build()
                        )
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

                for (VariableElement variableElement : variableElements) {
                    String fieldName = variableElement.getSimpleName().toString();
                    CodeAnalyzerTreeScanner codeScanner = new CodeAnalyzerTreeScanner();
                    TreePath tp = this.trees.getPath(typeElement);

                    codeScanner.setFieldName(fieldName);
                    codeScanner.scan(tp, this.trees);
                    String fieldInitializer = codeScanner.getFieldInitializer();

                    int start = fieldInitializer.indexOf(enumClassName) + enumClassName.length() + 1;
                    int end = fieldInitializer.lastIndexOf(")");
                    String fieldValue = fieldInitializer.substring(start, end);

                    TypeSpec.Builder classBuilder = TypeSpec.classBuilder(camelCaseName(fieldName))
                            .addMethod(
                                    MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build()
                            )
                            .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC);

                    List<? extends VariableElement> parameters = executableElement.getParameters();

                    List<Class<?>> classes = this.getParameterClasses(parameters);

                    List<EnumElement.Member> memberList = new LinkedList<>();

                    if (mappingConstant.withEnumName()) {
                        EnumElement.Member member = new EnumElement.Member("NAME", TypeName.get(String.class), fieldName);
                        memberList.add(member);
                    }

                    List<Object> values = getValues(fieldValue, classes);

                    for (int i = 0; i < parameters.size(); i++) {
                        VariableElement varElement = parameters.get(i);
                        TypeName typeName = TypeName.get(varElement.asType());
                        EnumElement.Member member = new EnumElement.Member(varElement.getSimpleName().toString(), typeName, values.get(i));
                        memberList.add(member);
                    }

                    EnumElement enumElement = new EnumElement(fieldName, memberList);

                    for (EnumElement.Member member : enumElement.getMembers()) {
                        TypeName typeName = member.getTypeName();
                        String matchVariable = matchVariable(typeName);
                        classBuilder.addField(
                                FieldSpec
                                        .builder(typeName, member.getName().toUpperCase(), Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
                                        .initializer(matchVariable, "$S".equals(matchVariable) ? decodeUnicode((String) member.getValue()) : member.getValue())
                                        .build()
                        );
                    }
                    typeBuilder.addType(classBuilder.build());

                }
                PackageElement pkg = elementUtils.getPackageOf(typeElement);
                String packageName = pkg.isUnnamed() ? null : pkg.getQualifiedName().toString();
                JavaFile.builder(packageName, typeBuilder.build()).build().writeTo(filer);

            }

        } catch (Exception e) {
            error(null, e.getMessage());
        }
        return true;
    }

    private List<Object> getValues(String fieldValue, List<Class<?>> classes) {
        List<Object> values = new ArrayList<>(classes.size());
        int starIndex = 0;
        int lastIndex = fieldValue.length() - 1;
        for (Class<?> clz : classes) {
            int index = fieldValue.indexOf(",", starIndex);
            if (index <= -1) {
                index = lastIndex + 1;
            }
            if (String.class.equals(clz)) {
                int startI = fieldValue.indexOf("\"", starIndex);
                int lastI = fieldValue.indexOf("\"", startI + 1);
                String value = fieldValue.substring(startI + 1, lastI);
                values.add(value);
                index = lastI + 1;
            } else if (isPrimitiveOrBox(clz)) {
                String value = fieldValue.substring(starIndex, index);
                values.add(value);
            } else {
                throw new ProcessorException(String.format("unSupport type with %s", clz.toString()));
            }
            starIndex = index + 1;
        }
        return values;
    }

    private List<Class<?>> getParameterClasses(List<? extends VariableElement> parameters) {
        return parameters.stream()
                .map(e -> {
                    TypeName typeName = TypeName.get(e.asType());
                    String typeString = typeName.box().toString();
                    try {
                        return Class.forName(typeString);
                    } catch (ClassNotFoundException ex) {
                        throw new RuntimeException(ex.getMessage());
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 是否是基本类型或是基本类型的包装类型
     *
     * @param clazz type
     * @return boolean
     */
    private static boolean isPrimitiveOrBox(Class clazz) {
        if (clazz == null) {
            return false;
        }
        if (Byte.class.equals(clazz) || Byte.TYPE.equals(clazz)) {
            return true;
        }
        if (Boolean.class.equals(clazz) || Boolean.TYPE.equals(clazz)) {
            return true;
        }
        if (Character.class.equals(clazz) || Character.TYPE.equals(clazz)) {
            return true;
        }
        if (Short.class.equals(clazz) || Short.TYPE.equals(clazz)) {
            return true;
        }
        if (Integer.class.equals(clazz) || Integer.TYPE.equals(clazz)) {
            return true;
        }
        if (Long.class.equals(clazz) || Long.TYPE.equals(clazz)) {
            return true;
        }
        if (Float.class.equals(clazz) || Float.TYPE.equals(clazz)) {
            return true;
        }
        if (Double.class.equals(clazz) || Double.TYPE.equals(clazz)) {
            return true;
        }
        return false;
    }

    /**
     * 转换为驼峰
     *
     * @param str underline
     * @return camel
     */
    private static String camelCaseName(String str) {
        str = str.toLowerCase();
        final StringBuffer sb = new StringBuffer();
        Matcher m = UNDERLINE_PATTERN.matcher(str);
        while (m.find()) {
            m.appendReplacement(sb, m.group(1).toUpperCase());
        }
        m.appendTail(sb);
        return firstCharUpper(sb.toString());
    }

    private static String matchVariable(TypeName typeName) {
        if (typeName.isPrimitive()) {
            return "$L";
        } else if ("java.lang.String".equals(typeName.toString())) {
            return "$S";
        }
        return "$T";
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
     * unicode编码转中文
     *
     * @param unicodeStr unicode string
     * @return chinese
     */
    private static String decodeUnicode(String unicodeStr) {
        Matcher matcher = UNICODE_PATTERN.matcher(unicodeStr);
        char ch;
        while (matcher.find()) {
            //group
            String group = matcher.group(2);
            //ch:'李四'
            ch = (char) Integer.parseInt(group, 16);
            //group1
            String group1 = matcher.group(1);
            unicodeStr = unicodeStr.replace(group1, ch + "");
        }

        return unicodeStr.replace("\\", "").trim();
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


