package edu.berkeley.cs.jqf.fuzz.tf.data;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance.Input;

public class Text {

    public Text() {

    }

    public InputStream getAsInputStream(File path, Input<?> input, int lineNumber) throws Exception {
        FileInputStream t = new FileInputStream(path);
        for(int i = 0; i < lineNumber; i++) {
            char c;
            while((c = (char) t.read()) != '\n')
                continue;
        }

        return t;
    }

    public static byte[] decode(SourceOfRandomness s) {
        char t;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        t = (char) s.nextByte(Byte.MIN_VALUE, Byte.MAX_VALUE);
        while (t != '\n') {
            outputStream.write(t);
            t = (char) s.nextByte(Byte.MIN_VALUE, Byte.MAX_VALUE);
        }

        return outputStream.toByteArray();
    }
}
