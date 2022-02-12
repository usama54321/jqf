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
import edu.berkeley.cs.jqf.fuzz.tf.data.Image;
import edu.berkeley.cs.jqf.fuzz.tf.data.Text;
import edu.berkeley.cs.jqf.fuzz.util.Hashing;
import edu.berkeley.cs.jqf.instrument.tracing.events.BranchEvent;
import edu.berkeley.cs.jqf.instrument.tracing.events.CallEvent;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;
import edu.berkeley.cs.jqf.fuzz.util.Coverage;
import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;
import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance.LinearInput;
//import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.Graphics;
import java.awt.Graphics2D;

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
        @Override
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

    public TfGuidance(String testName, Duration duration, Long trials, File outputDirectory, File inputDirectory, Random sourceOfRandomness, int inputWidth, int inputHeight ) throws IOException {
        this(testName, duration, trials, outputDirectory, new File(""), sourceOfRandomness);
    }

    public TfGuidance(String testName, Duration duration, Long trials, File outputDirectory, File inputDirectory, Random sourceOfRandomness) throws IOException {
        //@TODO FIXME not using input directory
        super(testName, duration, trials, outputDirectory, sourceOfRandomness);

        this.runCoverage = new InferenceCoverage();
        this.totalCoverage = new InferenceCoverage();
        this.validCoverage = new InferenceCoverage();

        this.inputDirectory = inputDirectory;
        this.paths = new ArrayList<>();
        index = 0;

        Files.walk(Paths.get(this.inputDirectory.getPath()))
            .filter(Files::isRegularFile)
            .forEach(path -> {
                //System.out.println(file.toString());
                this.paths.add(path);
            });
    }

    private boolean isImages() {
        Path path = this.paths.get(0);
        String mimetype = null;
        try {
            mimetype = Files.probeContentType(path);
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("LOG: Warning returning as images");
            return true;
        }

        if (mimetype != null && mimetype.split("/")[0].equals("image")) {
            return true;
        }

        return false;
    }

    /**
     * return next file from the directory
     */
    public InputStream getInput() {
        runCoverage.clear();
        currentInput = createFreshInput();

        if (isImages()) {
            BufferedImage image = null;
            BufferedImage orig = null;
            try {
                image = ImageIO.read(new File(this.paths.get(this.index).toString()));
            } catch (Exception e) {
                System.out.println("failed to read file");
                e.printStackTrace();
                return null;
            }

            BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

            Graphics2D g = newImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            this.index++;

            //for now hardcoded to images since only format supported
            return new Image().getImageAsInputStream(newImage, currentInput);
        } else {
            File file = this.paths.get(0).toFile();
            try {
                //System.out.println(file.toString());
                return new Text().getAsInputStream(file, currentInput, this.index++);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
