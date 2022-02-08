package edu.berkeley.cs.jqf.instrument.util;

import java.util.Set;
import java.util.HashSet;

public class Stats {
    private static int numberOfBranches = 0;
    private static Set<String> branches = new HashSet<String>();

    static void onBranch(int iid, String method, String clazz, int lineNumber) {
        numberOfBranches += 1;
        branches.add(clazz + "." + method + " " + String.valueOf(lineNumber));
    }

    public static void print() {
        System.out.printf("numberOfBranches %d", numberOfBranches);
        for(String branch: branches)
            System.out.println("branch " + branch);
    }

    public static String asString() {
        String data = "";
        for(String branch: branches)
            data += branch + "\n";
        return data;
    }
}
