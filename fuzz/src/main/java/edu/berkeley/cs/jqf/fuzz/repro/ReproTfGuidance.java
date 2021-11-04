package edu.berkeley.cs.jqf.fuzz.repro;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.berkeley.cs.jqf.fuzz.repro.ReproTfCoverage.IidBranchMap;
import edu.berkeley.cs.jqf.fuzz.repro.ReproTfCoverage.MethodIidMap;
import edu.berkeley.cs.jqf.fuzz.repro.ReproTfCoverage.ClassMethodMap;
import edu.berkeley.cs.jqf.fuzz.util.IOUtils;


public class ReproTfGuidance extends ReproGuidance {

    File[] inputFiles;

    File tempDir;
    public ReproTfGuidance(File[] inputFiles, File traceDir) throws IOException {
        super(inputFiles, null);
        tempDir = traceDir;
        coverage = new ReproTfCoverage();
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
    public void save() {
        ReproTfCoverage cov = (ReproTfCoverage) coverage;
        List<ReproTfCoverage.CoverageData> coverageData = cov.getCoverageData();
        String header = "class,method,iid,line,true,false";
        int phase = 0 ;

        for(ReproTfCoverage.CoverageData cv: coverageData) {
            File f = new File(tempDir, String.format("/tf-log-%d.csv", phase));
            phase += 1;
            IidBranchMap branchMap = cv.iidMapping;
            ClassMethodMap classMethodMap = cv.classMethodMap;
            MethodIidMap methodMapping = cv.methodMapping;

            List<String[]> data = new ArrayList<>();
            for(Map.Entry<String, Set<String>> kv: classMethodMap.entrySet()) {
                String[] row =new String[6];
                data.add(row);
                String cls = kv.getKey();
                Set<String> methods = kv.getValue();
                row[0] = cls;
                methods.forEach(method -> {
                    row[1] = method;
                    Set<Integer> iids = methodMapping.get(method);

                    iids.forEach(branch -> {
                        ReproTfCoverage.BranchData branchData = branchMap.get(branch);
                        row[2] = String.valueOf(branch);
                        row[3] = String.valueOf(branchData.lineNumber);
                        row[4] = String.valueOf(branchData.trueTaken);
                        row[5] = String.valueOf(branchData.falseTaken);
                    });
                });
            }

            PrintWriter pw = null;
            try {
                f.createNewFile();
                pw = new PrintWriter(f);
            }catch (Exception e) {
                e.printStackTrace();
                return;
            }

            pw.write(header + "\n");
            for(String[] row: data) {
                pw.format("%s,%s,%s,%s,%s,%s\n", row[0], row[1], row[2], row[3], row[4], row[5]);
            }
            pw.close();
        }
    }
}
