package com.mirth.commons.encryption.util;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.mirth.commons.encryption.Digester;
import com.mirth.commons.encryption.Output;

public class DigestUtil {
    @SuppressWarnings("static-access")
    public static void main(String[] args) {
        Options options = new Options();

        Option algorithmOption = OptionBuilder.withArgName("algorithm").hasArg().withDescription("the algorithm to use").create("a");
        Option formatOption = OptionBuilder.withArgName("format").hasArg().withDescription("the format to use").create("f");
        Option textOption = OptionBuilder.withArgName("text").hasArg().withDescription("the text to hash").create("t");
        Option helpOption = new Option("h", "print this message");

        options.addOption(algorithmOption);
        options.addOption(formatOption);
        options.addOption(textOption);
        options.addOption(helpOption);

        CommandLineParser parser = new PosixParser();
        Digester digester = new Digester();
        digester.setProvider(new BouncyCastleProvider());

        try {
            CommandLine line = parser.parse(options, args);
            
            if (line.hasOption("t")) {
                if (line.hasOption("a")) {
                    digester.setAlgorithm(line.getOptionValue("a"));
                }

                if (line.hasOption("f")) {
                    if (line.getOptionValue("f").equalsIgnoreCase("base64")) {
                        digester.setFormat(Output.BASE64);
                    } else if (line.getOptionValue("f").equalsIgnoreCase("hex")) {
                        digester.setFormat(Output.HEXADECIMAL);
                    }
                }

                System.out.println(digester.digest(line.getOptionValue("t")));
            } else if (line.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("DigestUtil", options);
            } else {
                System.err.println("No input text specified.");
            }
        } catch (ParseException e) {
            System.err.println("Parsing failed. " + e.getMessage());
        }
    }
}
