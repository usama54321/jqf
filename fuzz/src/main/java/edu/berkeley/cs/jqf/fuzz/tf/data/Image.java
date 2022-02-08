package edu.berkeley.cs.jqf.fuzz.tf.data;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.EnumSet;

import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance.Input;

/**
 * Takes as input a source of randomness and
 * image dimensions, and reads data from source
 * in the desired format
 */
public class Image {
    private int width;
    private int height;

    public enum Attr {
        WIDTH,
        HEIGHT,
        DATA
    }

    public class ImageByteStream extends InputStream {
        int index;
        BufferedImage image;
        int count;
        boolean isInit;
        Input<?> input;
        int width;
        int height;

        public ImageByteStream(BufferedImage image, Input<?> input) {
            this.input = input;
            this.image = image;
            this.width = image.getWidth();
            this.height = image.getHeight();
            index = 0;
            count = 0;
            isInit = false;
        }

        public int read() throws IOException {
            assert(false);
            return 220;
        }

        public int read(byte[] bytes, int off, int len)
                throws IOException {
            assert(len == 4);

            int data;
            switch(count) {
                case 0:
                case 1:
                    data = count == 0 ? width : height;
                    break;
                default:
                    int x = index / height;
                    int y = index % height;
                    data = this.image.getRGB(x, y);
                    index += 1;
            }

            byte[] arr;
            //@TODO Clean this
            arr = ByteBuffer.allocate(4).putInt(data).array();
            for (int i = 0; i < len; i++) {
                bytes[i] = arr[3-i];
                if (isInit)
                    input.add((int) bytes[i]);

            }

            count++;
            return len;
        }
    }

    public InputStream getImageAsInputStream(BufferedImage image, Input<?> input) {
        return new ImageByteStream(image, input);
    }

    public Image() {
    }

    private static BufferedImage getDataFromSource(SourceOfRandomness s, int width, int height, int type) {
        BufferedImage image = new BufferedImage(width, height, type);
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                int color = s.nextInt();
                int a = color & 0xFF;
                int r = (color >> 24) & 0xFF;
                int g = (color >> 16) & 0xFF;
                int b = (color >> 8) & 0xFF;

                image.setRGB(i, j, color);
            }
        }

        return image;
    }

    public static BufferedImage decode(SourceOfRandomness s, int type) {
        int width = -1, height = -1;
        BufferedImage data = null;
        for(Attr attr: EnumSet.allOf(Attr.class)) {
            switch(attr) {
                case WIDTH:
                    width = s.nextInt();
                    break;
                case HEIGHT:
                    height = s.nextInt();
                    break;

                case DATA:
                    //width = 400; height = 400;
                    assert(width != -1 && height != -1);
                    data = getDataFromSource(s, width, height, type);
                    break;
            }

        }

        assert(data != null);
        return data;
    }

    public static BufferedImage RGBToGray(BufferedImage source, int type) {
        int width = source.getWidth(),
            height = source.getHeight();

        BufferedImage target = new BufferedImage(width, height, type);
        int FF = 0xFF;
        for(int i =  0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                int color = source.getRGB(i, j);
                int a = color & 0xFF;
                int r = (color >> 24) & FF;
                int g = (color >> 16) & FF;
                int b = (color >> 8) & FF;

                int gray = (int)((float) r * 0.3 + (float)g * 0.59 + (float)b * 0.11);

                gray = (0xFF << 24) | (gray << 16) | (gray << 8) | gray;
                target.setRGB(i, j, gray);

            }
        }

        return target;
    }
}
