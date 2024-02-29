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

import com.mirth.commons.encryption.Encryptor;
import com.mirth.commons.encryption.Encryptor.EncryptedData;
import com.mirth.commons.encryption.KeyEncryptor;
import com.mirth.commons.encryption.Output;
import com.mirth.commons.encryption.PBEEncryptor;

public class EncryptionUtil {
    @SuppressWarnings("static-access")
    public static void main(String[] args) {
        Options options = new Options();

        Option encryptOption = OptionBuilder.withArgName("encrypt").withDescription("encrypt the string").create("e");
        Option decryptOption = OptionBuilder.withArgName("decrypt").withDescription("decrypt the string").create("d");
        Option algorithmOption = OptionBuilder.withArgName("algorithm").hasArg().withDescription("the algorithm to use").create("a");
        Option formatOption = OptionBuilder.withArgName("format").hasArg().withDescription("the format to use").create("f");
        Option textOption = OptionBuilder.withArgName("text").hasArg().withDescription("the text to hash").create("t");
        Option passwordOption = OptionBuilder.withArgName("password").hasArg().withDescription("the password to use (PBE)").create("p");
        Option helpOption = new Option("h", "print this message");

        options.addOption(encryptOption);
        options.addOption(decryptOption);
        options.addOption(algorithmOption);
        options.addOption(formatOption);
        options.addOption(textOption);
        options.addOption(passwordOption);
        options.addOption(helpOption);

        CommandLineParser parser = new PosixParser();

        try {
            CommandLine line = parser.parse(options, args);
            PBEEncryptor encryptor = null;

            if (line.hasOption("t")) {
                encryptor = new PBEEncryptor();
                encryptor.setProvider(new BouncyCastleProvider());

                if (line.hasOption("p")) {
                    encryptor.setPassword(line.getOptionValue("p"));
                } else {
                    System.err.println("Password not specified.");
                }

                encryptor.setIncludeSalt(true);
                encryptor.setIterations(5000);
                encryptor.setSaltSizeBytes(8);

                if (line.hasOption("a")) {
                    encryptor.setAlgorithm(line.getOptionValue("a"));
                } else {
                    encryptor.setAlgorithm("PBEWithMD5AndDES");
                }

                if (line.hasOption("s")) {
                    encryptor.setIncludeSalt(true);
                }

                if (line.hasOption("S")) {
                    encryptor.setSaltSizeBytes(Integer.parseInt(line.getOptionValue("S")));
                }

                if (line.hasOption("f")) {
                    if (line.getOptionValue("f").equalsIgnoreCase("base64")) {
                        encryptor.setFormat(Output.BASE64);
                    } else if (line.getOptionValue("f").equalsIgnoreCase("hex")) {
                        encryptor.setFormat(Output.HEXADECIMAL);
                    }
                }

                if (line.hasOption("d")) {
                    System.out.println(encryptor.decrypt(line.getOptionValue("t")));
                } else if (line.hasOption("e")) {
                    System.out.println(encryptor.encrypt(line.getOptionValue("t")));
                } else {
                    System.err.println("Mode not specified.");
                }
            } else if (line.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("EncryptionUtil", options);
            } else {
                System.err.println("No input text specified.");
            }
        } catch (ParseException e) {
            System.err.println("Parsing failed. " + e.getMessage());
        }
    }

    /**
     * Convenience method for re-encrypting data where all settings are the same except for the
     * algorithm.
     */
    public static String decryptAndReencrypt(String message, KeyEncryptor encryptor, String oldAlgorithm) throws Exception {
        return (String) doDecryptAndReencrypt(null, message, encryptor, oldAlgorithm);
    }

    public static String decryptAndReencrypt(String message, Encryptor decryptor, Encryptor encryptor) throws Exception {
        return (String) doDecryptAndReencrypt(null, message, decryptor, encryptor);
    }

    public static EncryptedData decryptAndReencrypt(String header, byte[] message, KeyEncryptor encryptor, String oldAlgorithm) throws Exception {
        return (EncryptedData) doDecryptAndReencrypt(header, message, encryptor, oldAlgorithm);
    }

    public static EncryptedData decryptAndReencrypt(String header, byte[] message, Encryptor decryptor, Encryptor encryptor) throws Exception {
        return (EncryptedData) doDecryptAndReencrypt(header, message, decryptor, encryptor);
    }

    private static Object doDecryptAndReencrypt(String header, Object message, KeyEncryptor encryptor, String oldAlgorithm) throws Exception {
        KeyEncryptor decryptor = new KeyEncryptor();
        decryptor.setProvider(encryptor.getProvider());
        decryptor.setFormat(encryptor.getFormat());
        decryptor.setKey(encryptor.getKey());
        decryptor.setAlgorithm(oldAlgorithm);
        decryptor.setCharset(encryptor.getCharset());

        return doDecryptAndReencrypt(header, message, decryptor, encryptor);
    }

    private static Object doDecryptAndReencrypt(String header, Object message, Encryptor decryptor, Encryptor encryptor) throws Exception {
        if (message instanceof String) {
            return encryptor.encrypt(decryptor.decrypt((String) message));
        } else {
            return encryptor.encrypt(decryptor.decrypt(header, (byte[]) message));
        }
    }
}
