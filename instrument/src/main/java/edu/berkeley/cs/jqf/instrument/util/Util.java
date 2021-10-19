package edu.berkeley.cs.jqf.instrument.util;

public class Util {
    private static String[] SIGNATURES = {
        //"edu/berkeley/cs/jqf/examples/InferenceTest.infer",
        //"org/nd4j/tensorflow/conversion/graphrunner/GraphRunner.run"
    };

    public static boolean isInferenceMethod(String owner, String method) {
        String signature = owner + "." + method;
        for(int i = 0; i < SIGNATURES.length; i++)
            if (SIGNATURES[i].equals(signature))
                return true;
        return false;
    }
}
