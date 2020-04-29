package com.shchuko.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Walk {
    private static final int FNV_BEGIN = 0x811c9dc5;
    private static final int FNV_STEP = 0x01000193;
    private static final int BYTE_MASK = 0xff;

    private String inputFileName;
    private String outputFileName;

    private InputStreamReader inputStreamReader = null;
    private OutputStreamWriter outputStreamWriter = null;


    public static void main(String[] args) {
        try {
            Walk walk = new Walk();
            walk.doWalk(args[0], args[1]);
        } catch (Exception e) {
//            e.printStackTrace();
        }

    }

    public Walk() {

    }

    public void doWalk(String inputFileName, String outputFileName) throws IOException {
        this.inputFileName = inputFileName;
        this.outputFileName = outputFileName;

        runWalk();
    }

    private void runWalk() throws IOException {
        try {
            openInputFile();
            openOutputFile();

            hashFilesList();
        } finally {
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }

            if (outputStreamWriter != null) {
                outputStreamWriter.close();
            }
        }
    }

    private void openInputFile() throws IOException {
        inputStreamReader = new InputStreamReader(new FileInputStream(inputFileName), StandardCharsets.UTF_8);
    }

    private void openOutputFile() throws IOException {
        File outFile = new File(outputFileName);
        if (!outFile.exists()) {
            if (outFile.getParentFile() != null) {
                outFile.getParentFile().mkdirs();
            }
            outFile.createNewFile();
        }

        outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outFile), StandardCharsets.UTF_8);
    }

    private void hashFilesList() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String fileToHashPath;
        while ((fileToHashPath = bufferedReader.readLine()) != null) {
            hashFile(fileToHashPath);
        }

        bufferedReader.close();
    }

    public void hashFile(String filePath) throws IOException {
        int hash = FNV_BEGIN;
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            int byteRead;
            while ((byteRead = inputStream.read()) != -1) {
                hash = (hash * FNV_STEP) ^ (byteRead & BYTE_MASK);
            }
        } catch (IOException e) {
            hash = 0;
        }

        outputStreamWriter.write(String.format("%08x ", hash) + filePath + System.lineSeparator());
    }
}

