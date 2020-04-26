package com.shchuko.walk;

import java.io.*;

public class Walk {
    private String inputFileName;
    private String outputFileName;
    private FileReader inputFile = null;
    private FileWriter outputFileWriter = null;

    public static void main(String[] args) throws IOException {
        Walk walk = new Walk();
        walk.doWalk(args[0], args[1]);
    }

    public Walk() {

    }

    public void doWalk(String inputFileName, String outputFileName) throws IOException {
        this.inputFileName = inputFileName;
        this.outputFileName = outputFileName;

        runWalk();
    }

    private void runWalk() throws IOException {
        openInputFile();
        openOutputFile();
    }

    private void openInputFile() throws IOException {
        inputFile = new FileReader(inputFileName);
    }

    private void openOutputFile() throws IOException {
        File outFile = new File(outputFileName);

        if (!outFile.exists()) {
            if (outFile.getParentFile() != null) {
                outFile.getParentFile().mkdirs();
            }
            outFile.createNewFile();
        }

        outputFileWriter = new FileWriter(outFile);
    }
}
