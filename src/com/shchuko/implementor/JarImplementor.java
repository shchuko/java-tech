package com.shchuko.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Random;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;


public class JarImplementor extends Implementor implements JarImpler {
    private Class<?> token;
    private Path tempDir;
    private String tokenSimpleName;

    public JarImplementor() {

    }

    /**
     * Implement class for its name, zip it to JAR
     *
     * @param args First argument - full class name to implement, Second argument - output Jar file path
     */
    public static void main(String[] args) {
        if (args.length == 0 || args[0] == null) {
            System.err.println("Wrong args");
            System.exit(1);
        }

        try {
            Path path = (args.length == 1 || args[1] == null) ? Paths.get("") : Paths.get(args[1]);
            new JarImplementor().implement(Class.forName(args[0]), path.toAbsolutePath());
        } catch (ClassNotFoundException e) {
            System.err.println("Implementation error caused: couldn't found class " + args[0]);
            System.exit(1);
        } catch (ImplerException e) {
            System.err.println("Implementation error caused: " + e);
            System.exit(1);
        }
    }

    /**
     * Produces <tt>.jar</tt> file implementing class or interface specified by provided <tt>token</tt>.
     * <p>
     * Generated class classes name should be same as classes name of the type token with <tt>Impl</tt> suffix
     * added.
     *
     * @param token type token to create implementation for.
     * @param jarFile target <tt>.jar</tt> file.
     * @throws ImplerException when implementation cannot be generated.
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        this.token = token;
        this.tokenSimpleName = token.getSimpleName() + "Impl";

        try {
            tempDir = createTempDirectory(jarFile.toAbsolutePath().getParent());
        } catch (IOException e) {
            throw new ImplerException("Can't create temp directory", e);
        }

        implement(token, tempDir);
        compileJava();

        Manifest manifest = getDefaultManifest();
        try (JarOutputStream outputStream = new JarOutputStream(Files.newOutputStream(createFile(jarFile)), manifest)) {
            String zipEntryPath = token.getPackageName().replace('.', '/') +
                    "/" + tokenSimpleName + ".class";

            Path binaryPath = Paths.get(tempDir.toString(),
                    token.getPackageName().replace('.', File.separatorChar), tokenSimpleName + ".class");

            outputStream.putNextEntry(new ZipEntry(zipEntryPath));
            Files.copy(binaryPath, outputStream);
        } catch (IOException e) {
            throw new ImplerException("Jar creation error", e);
        } finally {
            deleteTempDir();
        }
    }

    private static Path createTempDirectory(Path root) throws IOException {
        String randomDirectoryName = "__temp_" + getRandomAlNumString() + "__";
        return Files.createTempDirectory(root, randomDirectoryName);
    }

    private static String getRandomAlNumString() {
        final String supportedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        char[] chars = new char[10];
        Random random = new Random();
        for (int i = 0; i < chars.length; ++i) {
            chars[i] = supportedChars.charAt(random.nextInt(supportedChars.length()));
        }
        return new String(chars);
    }

    private static Manifest getDefaultManifest() {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        return manifest;
    }

    private void compileJava() throws ImplerException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Couldn't find System Java Compiler");
        }

        String classPath;
        try {
            classPath = Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (URISyntaxException e) {
            throw new ImplerException("Failed to convert URL to URI", e);
        }

        String javaSourcePath = tempDir.resolve(token.getPackageName().replace('.', File.separatorChar))
                .resolve(tokenSimpleName + ".java").toString();


        String[] args = new String[]{
                "-cp",
                classPath,
                javaSourcePath
        };

        if (compiler.run(null, null, null, args) != 0) {
            throw new ImplerException("Can't compile files");
        }
    }

    private static Path createFile(Path path) throws IOException {
        Files.createDirectories(path.getParent());

        if (!path.toFile().exists()) {
            Files.createFile(path);
        }
        return path;
    }

    private void deleteTempDir() {
        try {
            Files.walkFileTree(tempDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path file, IOException e) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.err.println("Temp directory deleting error");
        }
    }
}
