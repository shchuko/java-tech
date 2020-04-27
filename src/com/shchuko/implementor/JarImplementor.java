package com.shchuko.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import java.nio.file.Path;

public class JarImplementor extends Implementor implements JarImpler  {
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        // TODO
        throw new ImplerException("Not implemented yet");
    }
}
