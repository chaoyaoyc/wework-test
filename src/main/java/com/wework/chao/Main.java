package com.wework.chao;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main {
    /**
     *
     * @param args regex, input file path, output file path
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage: [REGEX] [INPUT FILE PATH] [OUT PUT FILE PATH]");
            return;
        }
        try (FileInputStream input = new FileInputStream(args[1]);
             FileOutputStream output = new FileOutputStream(args[2], true)) {
            WebSearchExecutor executor = new WebSearchExecutor(new WebSearcher(args[0]), input, output);
            executor.run();
        }
    }
}
