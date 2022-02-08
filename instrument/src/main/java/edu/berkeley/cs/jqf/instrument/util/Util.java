package edu.berkeley.cs.jqf.instrument.util;

public class Util {
    private static String[] SIGNATURES = {
        //"edu/berkeley/cs/jqf/examples/InferenceTest.infer",
        "org/nd4j/tensorflow/conversion/graphrunner/GraphRunner.run",
        "org/bytedeco/tensorflowlite/Interpreter.Invoke"
    };

    public static void onBranch(int iid, String method, String clazz, int lineNumber) {
        Stats.onBranch(iid, method, clazz, lineNumber);
    }

    public static boolean isInferenceMethod(String owner, String method) {
        String signature = owner + "." + method;
        for(int i = 0; i < SIGNATURES.length; i++)
            if (SIGNATURES[i].equals(signature))
                return true;
        return false;
    }
}
