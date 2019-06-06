package com.wework.chao;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    /**
     *
     * @param args regex, input file path, output file path
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String term;
        String inputFile;
        String outputFile;
        if (args.length == 0) {
            System.out.println("Possible Usage: [REGEX] [INPUT FILE PATH] [OUTPUT FILE PATH]");
            System.out.println("No arguments. Read urls.txt from and write output into the current directory");
            inputFile = "urls.txt";
            outputFile = "results.txt";
            System.out.println("Input search term:");
            Scanner scanner = new Scanner(System.in);
            term = scanner.nextLine();
        } else {
            term = args[0];
            inputFile = args[1];
            outputFile = args[2];
        }
        try (FileInputStream input = new FileInputStream(inputFile);
             FileOutputStream output = new FileOutputStream(outputFile, true)) {
            WebSearchExecutor executor = new WebSearchExecutor(new WebSearcher(term), input, output);
            executor.run();
        }
    }
}
