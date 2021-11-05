package edu.berkeley.cs.jqf.fuzz.tf;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
//import java.io.FileInputStream;
//import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.function.Consumer;
import java.util.Set;
//import java.util.HashSet;
//import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import java.io.IOException;
import java.time.Duration;

//import java.util.Arrays;
//import org.datavec.api.io.filters.BalancedPathFilter;
//import org.datavec.api.io.labels.ParentPathLabelGenerator;


import edu.berkeley.cs.jqf.fuzz.guidance.Guidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.guidance.TimeoutException;
import edu.berkeley.cs.jqf.fuzz.util.Hashing;
import edu.berkeley.cs.jqf.instrument.tracing.events.BranchEvent;
import edu.berkeley.cs.jqf.instrument.tracing.events.CallEvent;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;
import edu.berkeley.cs.jqf.fuzz.util.Coverage;
import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;
import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance.LinearInput;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.Graphics;
public class TfGuidance extends ZestGuidance
{
    File inputDirectory;
    protected static int IMG_SIZE = 256;
    protected static int SEED = 10;
    ByteBuffer buffer;
    List<Path> paths;
    int index;

    //@TODO fix this hack
    public class ImageInput extends LinearInput {
        public void add(Integer pixel) {
            this.values.add(pixel);
        }

        @Override
        public void gc() {
            return;
        }
    };

    protected Input<Integer> createFreshInput() {
        return new ImageInput();
    }

    /**
     * @TODO
     * This implementation is really simplistic and hacky.
     * Assumes reads only happen in blocks of four bytes.
     * Also, read TODO below
     */
    public class ByteBufferBackedInputStream extends InputStream {

        int index;
        int[] data;
        int count;
        boolean isInit;

        public ByteBufferBackedInputStream(int[] buf) {
            this.data = buf;
            int[] transpose = new int[buf.length];

            //@FIXME slow
            for(int i = 0; i < IMG_SIZE; i++) {
                for(int j = 0; j < IMG_SIZE; j++) {
                    transpose[i * IMG_SIZE + j] = buf[j * IMG_SIZE + i];
                }
            }

            this.data = transpose;
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

                //@TODO resetting input stream the first time. Tailored for mtcnn example for now
                if (!isInit && index == 2) {
                    index = 0;
                    isInit = true;
                }

                //System.out.printf("reading index %d", index);
                if(index >= IMG_SIZE * IMG_SIZE) {
                    assert(false);
                }

                int data = this.data[index];
                count++;
                byte[] arr = ByteBuffer.allocate(4).putInt(data).array();

                for (int i = 0; i < len; i++) {
                    bytes[i] = arr[3-i];
                }

                index += 1;

                currentInput.add((int) arr[0]);
                currentInput.add((int) arr[1]);
                currentInput.add((int) arr[2]);
                currentInput.add((int) arr[3]);
                return len;
        }
    }

    public TfGuidance(String testName, Duration duration, Long trials, File outputDirectory, File inputDirectory, Random sourceOfRandomness) throws IOException {
        super(testName, duration, trials, outputDirectory, new File("/home/usama/ml_system/datasets/vgg_face/extracted/Abbie_Cornish.txt"), sourceOfRandomness);
        System.setProperty("jqf.ei.MAX_INPUT_SIZE", "999999999");
        //System.setProperty("janala.instrumentationCacheDir", "./instr");
        //runCoverage = new InferenceCoverage();
        //totalCoverage = new InferenceCoverage();
        //validCoverage = new InferenceCoverage();
        this.inputDirectory = inputDirectory;
        this.paths = new ArrayList<>();
        index = 0;

        Files.walk(Paths.get(this.inputDirectory.getPath()))
            .filter(Files::isRegularFile)
            .forEach(path -> {
                this.paths.add(path);
            });
    }


    /**
     * return next file from the directory
     */
    public InputStream getInput() {
        runCoverage.clear();
        currentInput = createFreshInput();
        BufferedImage image = null;
        BufferedImage orig = null;
        try {
            orig = ImageIO.read(new File(this.paths.get(this.index).toString()));
            //hacky way to resize to input img size
            image = new BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_RGB);
            Graphics g = image.createGraphics();
            g.drawImage(orig, 0, 0, IMG_SIZE, IMG_SIZE, null);
            g.dispose();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Raster raster = image.getData();
        int[] data = new int[raster.getWidth() * raster.getHeight() * raster.getNumBands()];
        raster.getPixels(0, 0,  raster.getWidth(), raster.getHeight(), data);

        DataBufferInt buffer = (DataBufferInt) raster.getDataBuffer();
        this.index++;
        return new ByteBufferBackedInputStream(buffer.getData());
    }


    /**
     * collect coverage results
     */
     
}
