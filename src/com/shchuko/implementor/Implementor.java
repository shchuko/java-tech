package com.shchuko.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Implementor implements Impler {
    private static final String SPACE = " ";
    private static final String TAB = " ".repeat(4);
    private static final String EOL = System.lineSeparator();
    private static final String COMMA = ",";

    private String classImplShortName;
    private String classPackageName;
    private Class<?> token;

    public Implementor() {
    }

    /**
     * Implement class for its name
     *
     * @param args First argument - full class name to implement, Second argument - output root directory
     */
    public static void main(String[] args) {
        if (args.length == 0 || args[0] == null) {
            System.err.println("Wrong args");
            System.exit(1);
        }

        try {
            Path path = (args.length == 1 || args[1] == null) ? Paths.get("") : Paths.get(args[1]);
            new Implementor().implement(Class.forName(args[0]), path.toAbsolutePath());
        } catch (ClassNotFoundException e) {
            System.err.println("Implementation error caused: couldn't found class " + args[0]);
            System.exit(1);
        } catch (ImplerException e) {
            System.err.println("Implementation error caused: " + e);
            System.exit(1);
        }
    }

    /**
     * Produces code implementing class or interface specified by provided <var>token</var>.
     * <p>
     * Generated class classes name should be same as classes name of the type token with <var>Impl</var> suffix
     * added. Generated source code should be placed in the correct subdirectory of the specified
     * <var>root</var> directory and have correct file name. For example, the implementation of the
     * interface {@link java.util.List} should go to <var>$root/java/util/ListImpl.java</var>
     *
     * @param token type token to create implementation for.
     * @param root  root directory.
     * @throws ImplerException when implementation cannot be generated.
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (token.isArray() || token == Enum.class || Modifier.isFinal(token.getModifiers())) {
            throw new ImplerException("Unsupported class type");
        }

        if (Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Private token-class");
        }

        this.classImplShortName = token.getSimpleName() + "Impl";
        this.classPackageName = token.getPackage().getName();
        this.token = token;
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(getClassImplFile(root)))) {
            writeFileHeader(writer);
            writeClassConstructors(writer);
            writeClassMethods(writer);
            writeClassEnding(writer);
        } catch (IOException e) {
            throw new ImplerException(e);
        }
    }

    private File getClassImplFile(Path root) throws IOException {
        Path fullPackagePath = root.resolve(classPackageName.replace('.', File.separatorChar));
        Files.createDirectories(fullPackagePath);

        Path fullClassImplPath = fullPackagePath.resolve(classImplShortName + ".java");
        if (!fullClassImplPath.toFile().exists()) {
            Files.createFile(fullClassImplPath);
        }
        return fullClassImplPath.toFile();
    }

    private void writeFileHeader(OutputStreamWriter writer) throws IOException {
        writePackageName(writer);
        writer.write(EOL);
        writeClassHeader(writer);
    }

    private void writePackageName(OutputStreamWriter writer) throws IOException {
        writer.write("package" + SPACE + classPackageName + ";" + EOL);
    }

    private void writeClassHeader(OutputStreamWriter writer) throws IOException {
        String classGenericParams;
        String fatherGenericParams;

        if (token.toGenericString().matches(".*<.*>.*")) {
            classGenericParams = token.toGenericString().replaceAll(".*<", "<").replace(">.*", ">");
            fatherGenericParams = parseGenericParamsNames(classGenericParams);
        } else {
            classGenericParams = "";
            fatherGenericParams = "";
        }

       String classHeaderLine = "public class" + SPACE + classImplShortName + classGenericParams + SPACE +
                (token.isInterface() ? "implements" : "extends") + SPACE + token.getCanonicalName() + fatherGenericParams + SPACE + "{" + EOL;
        writer.write(classHeaderLine);
    }

    private static String parseGenericParamsNames(String generic) {
        return generic.replaceAll("\\s*extends.*?,", ", ")
                .replaceAll("\\s*extends.*?>", ">");
    }

    private void writeClassConstructors(OutputStreamWriter writer) throws IOException, ImplerException {
        if (token.isInterface()) {
            return;
        }

        Constructor<?>[] constructors = Arrays.stream(token.getDeclaredConstructors())
                .filter(x -> !Modifier.isPrivate(x.getModifiers()))
                .toArray(Constructor<?>[]::new);

        if (constructors.length == 0) {
            throw new ImplerException("No public constructors");
        }

        for (Constructor<?> constructor : constructors) {
            writeExecutable(writer, constructor);
        }
    }

    private void writeClassMethods(OutputStreamWriter writer) throws IOException {
        Set<MethodWrapper> methods_set = new HashSet<>();
        Set<MethodWrapper> final_methods_set = new HashSet<>();

        addMethodsToSet(token.getMethods(), methods_set, method -> Modifier.isAbstract(method.getModifiers()));
        addMethodsToSet(token.getMethods(), final_methods_set, method -> Modifier.isFinal(method.getModifiers()));
        while (token != null) {
            addMethodsToSet(token.getDeclaredMethods(), methods_set, method -> Modifier.isAbstract(method.getModifiers()));
            addMethodsToSet(token.getDeclaredMethods(), final_methods_set, method -> Modifier.isFinal(method.getModifiers()));

            token = token.getSuperclass();
        }

        methods_set.removeAll(final_methods_set);

        for (MethodWrapper method : methods_set) {
            writeExecutable(writer, method.getMethod());
            writer.write(EOL);
        }
    }

    private void writeClassEnding(OutputStreamWriter writer) throws IOException {
        writer.write("}");
    }

    private void addMethodsToSet(Method[] methods, Set<MethodWrapper> methods_set, Predicate<Method> methodPredicate) {
        Arrays.stream(methods)
                .filter(methodPredicate)
                .map(MethodWrapper::new)
                .collect(Collectors.toCollection(() -> methods_set));
    }

    private void writeExecutable(OutputStreamWriter writer, Executable executable) throws IOException {
        StringBuilder builder = new StringBuilder(TAB);

        for (Annotation annotation : executable.getAnnotations()) {
            builder.append(annotation.toString()).append(EOL).append(TAB);
        }

        builder.append(getExecutableModifiers(executable));

        if (executable instanceof Method) {
            Method method = (Method) executable;
            builder.append(getMethodReturnType(method)).append(SPACE);
            builder.append(method.getName());
        } else {
            builder.append(classImplShortName);
        }

        builder.append(getExecutableParameters(executable, false)).append(SPACE);
        builder.append(getExecutableExceptions(executable)).append("{").append(EOL);

        builder.append(TAB).append(TAB).append(getExecutableBody(executable)).append(EOL);
        builder.append(TAB).append("}").append(EOL);

        writer.write(builder.toString());
    }

    private static String getExecutableModifiers(Executable executable) {
        int mod = executable.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.NATIVE & ~Modifier.TRANSIENT;
        return mod == 0 ? "" : Modifier.toString(mod) + SPACE;
    }

    private static String getMethodReturnType(Method method) {
        Type genericReturnType = method.getGenericReturnType();
        if (genericReturnType instanceof ParameterizedType) {
            return genericReturnType.getTypeName();
        }

        if (genericReturnType.getClass() != Class.class) {
            return "<" + genericReturnType + ">" + SPACE + genericReturnType;
        }

        return method.getReturnType().getCanonicalName();
    }

    private static String getExecutableParameters(Executable executable, boolean onlyName) {
        if (onlyName) {
            return Arrays.stream(executable.getParameters())
                    .map(Parameter::getName)
                    .collect(Collectors.joining(COMMA + SPACE, "(", ")"));
        }

        StringBuilder builder = new StringBuilder();
        Parameter[] parameters = executable.getParameters();


        for (Parameter parameter : parameters) {
            if (parameter.getParameterizedType() instanceof ParameterizedType) {
                builder.append(parameter.getParameterizedType().getTypeName()).append(SPACE);
            } else {
                builder.append(parameter.getType().getCanonicalName()).append(SPACE);
            }

            builder.append(parameter.getName()).append(COMMA).append(SPACE);

        }

        if (builder.length() != 0) {
            builder.delete(builder.length() - 2, builder.length());
        }

        return "(" + builder.toString() + ")";
    }

    private static String getExecutableExceptions(Executable executable) {
        StringBuilder exceptionsStr = new StringBuilder();
        Class<?>[] exceptions = executable.getExceptionTypes();
        if (exceptions.length == 0) {
            return "";
        }

        exceptionsStr.append("throws" + SPACE);
        exceptionsStr.append(Arrays.stream(exceptions)
                .map(Class::getCanonicalName)
                .collect(Collectors.joining(COMMA + SPACE)));
        return exceptionsStr.append(SPACE).toString();
    }

    private static String getExecutableBody(Executable executable) {
        if (executable instanceof Method) {
            String returnToken = getDefaultTokenValue(((Method) executable).getReturnType());
            return (returnToken.length() == 0) ? "" : "return" + returnToken + ";";
        } else {
            return "super" + getExecutableParameters(executable, true) + ";";
        }
    }

    private static String getDefaultTokenValue(Class<?> token) {
        if (token.equals(void.class)) {
            return "";
        } else if (token.equals(boolean.class)) {
            return SPACE + "false";
        } else if (token.isPrimitive()) {
            return SPACE + "0";
        }
        return SPACE + "null";
    }

    private static class MethodWrapper {
        private final Method method;

        public MethodWrapper(Method method) {
            this.method = method;
        }

        public Method getMethod() {
            return method;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MethodWrapper that = (MethodWrapper) o;
            return that.method.getName().equals(this.method.getName()) &&
                    Arrays.equals(this.method.getParameterTypes(), that.method.getParameterTypes());
        }

        @Override
        public int hashCode() {
            return Objects.hash(method.getName(), Arrays.hashCode(method.getParameterTypes()));
        }
    }
}
