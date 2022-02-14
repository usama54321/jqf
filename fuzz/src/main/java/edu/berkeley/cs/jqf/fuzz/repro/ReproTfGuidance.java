package edu.berkeley.cs.jqf.fuzz.repro;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import edu.berkeley.cs.jqf.fuzz.repro.ReproTfCoverage.IidBranchMap;
import edu.berkeley.cs.jqf.fuzz.repro.ReproTfCoverage.MethodIidMap;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.repro.ReproTfCoverage.ClassMethodMap;
import edu.berkeley.cs.jqf.fuzz.util.Coverage;
import edu.berkeley.cs.jqf.fuzz.util.IOUtils;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;


public class ReproTfGuidance extends ReproGuidance {

    File[] inputFiles;
    List<Integer> coverageSize;

    File tempDir;
    public ReproTfGuidance(File[] inputFiles, File traceDir) throws IOException {
        super(inputFiles, null);
        tempDir = traceDir;
        coverage = new ReproTfCoverage();
        coverageSize = new ArrayList<Integer>();
    }

    public ReproTfGuidance(File inputFile, File traceDir) throws IOException {
        //super(inputFile, traceDir);
        this(IOUtils.resolveInputFileOrDirectory(inputFile), traceDir);
    }

    @Override
    public InputStream getInput() {
        //new input reset current coverage counter
        ((ReproTfCoverage) coverage).setCurrentCoverage(0);
        return super.getInput();
    }

    @Override
    public void handleResult(Result result, Throwable error) {
        super.handleResult(result, error);
        int sum = 0;

        for(ReproTfCoverage.BranchData br: ((ReproTfCoverage) coverage).branchData) {
            sum += br.trueTaken > 0 ? 1 : 0;
            sum += br.falseTaken > 0 ? 1 : 0;
        }

        coverageSize.add(sum);

        ((ReproTfCoverage) coverage).handleResult(result);

    }

    @Override
    public void save() {
        ReproTfCoverage cov = (ReproTfCoverage) coverage;
        List<ReproTfCoverage.CoverageData> coverageData = cov.getCoverageData();
        String header = "class,method,iid,line,true,false,inference";
        int phase = 0 ;

        for(ReproTfCoverage.CoverageData cv: coverageData) {
            File f = new File(tempDir, String.format("/tf-log-%d.csv", phase));
            File stats = new File(tempDir, String.format("/results-%d.csv", phase));
            List<String> plotData = ((ReproTfCoverage) coverage).plotData.get(phase);
            phase += 1;
            //IidBranchMap branchMap = cv.iidMapping;
            //ClassMethodMap classMethodMap = cv.classMethodMap;
            //MethodIidMap methodMapping = cv.methodMapping;

            List<String[]> data = new ArrayList<>();
            for(Map.Entry<String, ReproTfCoverage.BranchData> kv: cv.branchData.entrySet()) {
                String signature = kv.getKey();
                ReproTfCoverage.BranchData br = kv.getValue();

                String[] split = signature.split("\\.");
                String cls = split[0];
                String[] splitTwo = split[1].split(":");
                String method = splitTwo[0];
                String iid = splitTwo[1];

                String[] row =new String[7];
                data.add(row);
                row[0] = cls;
                row[1] = method;
                row[2] = String.valueOf(iid);
                row[3] = String.valueOf(iid);
                row[4] = String.valueOf(br.trueTaken);
                row[5] = String.valueOf(br.falseTaken);
                row[6] = String.valueOf(cv.clazz + "." + cv.method + ":" + String.valueOf(cv.lineNumber));
            }

            /*
            for(Map.Entry<String, Set<String>> kv: classMethodMap.entrySet()) {
                String cls = kv.getKey();
                Set<String> methods = kv.getValue();
                methods.forEach(method -> {
                    Set<Integer> iids = methodMapping.get(method);

                    iids.forEach(branch -> {
                        String[] row =new String[7];
                        data.add(row);
                        row[0] = cls;
                        row[1] = method;
                        ReproTfCoverage.BranchData branchData = branchMap.get(branch);
                        row[2] = String.valueOf(branch);
                        row[3] = String.valueOf(branchData.lineNumber);
                        row[4] = String.valueOf(branchData.trueTaken);
                        row[5] = String.valueOf(branchData.falseTaken);
                        row[6] = String.valueOf(cv.clazz + "." + cv.method + ":" + String.valueOf(cv.lineNumber));
                    });
                });
            }
            */

            PrintWriter pw = null;
            PrintWriter pwStats = null;
            try {
                f.createNewFile();
                stats.createNewFile();
                pw = new PrintWriter(f);
                pwStats = new PrintWriter(stats);
            }catch (Exception e) {
                e.printStackTrace();
                return;
            }

            pw.write(header + "\n");
            for(String[] row: data) {
                pw.format("%s,%s,%s,%s,%s,%s,%s\n", row[0], row[1], row[2], row[3], row[4], row[5], row[6]);
            }

            for(String row: plotData) {
                pwStats.format("%s\n", row);
            }
            pw.close();
            pwStats.close();
        }

        File f = new File(tempDir, "/coverage_size");
        PrintWriter pw = null;
        try {
            f.createNewFile();
            pw = new PrintWriter(f);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        for(Integer data: coverageSize) {
            pw.println(data);
        }

        pw.close();
    }
}
