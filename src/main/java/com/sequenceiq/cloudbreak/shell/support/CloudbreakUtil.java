package com.sequenceiq.cloudbreak.shell.support;

public class CloudbreakUtil {

    private CloudbreakUtil() {

    }

    public static void printlnToConsole(String format, String... params) {
        System.out.println(String.format(format, params));
    }

    public static void printToConsole(String format, String... params) {
        System.out.print(String.format(format, params));
    }
}
