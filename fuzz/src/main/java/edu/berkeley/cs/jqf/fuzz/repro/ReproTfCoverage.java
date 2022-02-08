package edu.berkeley.cs.jqf.fuzz.repro;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.berkeley.cs.jqf.fuzz.util.Coverage;
import edu.berkeley.cs.jqf.instrument.tracing.events.BranchEvent;
import edu.berkeley.cs.jqf.instrument.tracing.events.InferenceEvent;

public class ReproTfCoverage extends Coverage {

    public class ClassMethodMap extends HashMap<String, Set<String>> {}
    public class MethodIidMap extends HashMap<String, Set<Integer>> {}
    public class IidBranchMap extends HashMap<Integer, BranchData> {}
    public Set<BranchData> branchData;
    int currentCoverage;

    public class BranchData implements Comparable<BranchData> {
        public int trueTaken;
        public int falseTaken;
        public int lineNumber;

        public BranchData() {
            trueTaken = 0;
            falseTaken = 0;
        }

        public int compareTo(BranchData d) {
            if (this == d)
                return 0;
            return lineNumber < d.lineNumber ? -1: 1;
        }
    };

    public class CoverageData implements Comparable<CoverageData> {
        public MethodIidMap methodMapping;
        public IidBranchMap iidMapping;
        public ClassMethodMap classMethodMap;
        public Map<String, BranchData> branchData;
        public int lineNumber;
        public String clazz;
        public String method;

        public CoverageData() {
            this("", "", -1);
        }

        @Override
        public int compareTo(CoverageData o) {
            if(o.getSignature().equals(getSignature()))
                return 0;

            return lineNumber < o.lineNumber ? -1 : 1;
        }

        public CoverageData(String clazz, String method, int lineNumber) {
            methodMapping = new MethodIidMap();
            iidMapping = new IidBranchMap();
            classMethodMap = new ClassMethodMap();
            branchData = new HashMap<>();
            this.clazz = clazz;
            this.method = method;
            this.lineNumber = lineNumber;
        }

        public String getSignature() {
            return this.clazz + "." + this.method + ":" + String.valueOf(this.lineNumber);
        }

        public void merge(CoverageData d) {
            //
        }
    }

    protected List<CoverageData> coverageData;

    public List<CoverageData> getCoverageData() {
        return coverageData;
    }

    public MethodIidMap getMethodMapping() {
        return coverageData.get(currentCoverage).methodMapping;
    }

    public IidBranchMap getIidMapping() {
        return coverageData.get(currentCoverage).iidMapping;
    }

    public ClassMethodMap getClassMethodMap() {
        return coverageData.get(currentCoverage).classMethodMap;
    }


    public ReproTfCoverage() {
        coverageData = new ArrayList<>();
        coverageData.add(new CoverageData());
        currentCoverage = 0;
        branchData = new TreeSet<>();
    }

    public void addToCoverage(String cls, String methodName, int iid, int arm, int lineNumber, CoverageData cv) {
        String signature = String.format("%s.%s:%s", cls, methodName, iid);

        CoverageData data = getCurrentCoverage();

        Map<String, BranchData> branchData = data.branchData;
        BranchData kv = null;

        if(branchData.containsKey(signature)) {
            kv = branchData.get(signature);
        } else {
            kv = new BranchData();
        }

        kv.trueTaken += (arm == 0 ? 1 : 0);
        kv.falseTaken += (arm == 0 ? 0: 1);
        branchData.put(signature, kv);
        kv.lineNumber = lineNumber;

        this.branchData.add(kv);
    }

    private CoverageData getCurrentCoverage() {
        return coverageData.get(currentCoverage);
    }
    //@TODO record classes too
    @Override
    public void visitBranchEvent(BranchEvent e) {
        if (e.getContainingClass().contains("tensorflowlite") || e.getContainingClass().contains("GraphRunner"))
            return;
        ClassMethodMap classMethodMap = getClassMethodMap();
        MethodIidMap methodMapping = getMethodMapping();
        IidBranchMap iidMapping = getIidMapping();


        addToCoverage(e.getContainingClass(), e.getContainingMethodName(), e.getLineNumber(), e.getArm(), e.getLineNumber(), getCurrentCoverage());
    }

    public void setCurrentCoverage(int index) {
        currentCoverage = index;
    }

    private void addNewCoverage(InferenceEvent e) {
        //System.out.println("adding new coverage");
        coverageData.add(new CoverageData(e.clazz, e.method, e.getLineNumber()));
    }

    @Override
    public void visitInferenceEvent(InferenceEvent e) {
        int index = 0;
        int newIndex = -1;

        for(CoverageData d: coverageData) {
            if (d.getSignature().equals(e.clazz + "." + e.method + ":" + String.valueOf(e.getLineNumber()))) {
                newIndex = index;
                break;
            }
            index++;
        }

        //if can not find coverage for this signature
        if (newIndex == -1)
            newIndex = currentCoverage + 1;

        if (newIndex >= coverageData.size()) {
            addNewCoverage(e);
        }
        setCurrentCoverage(newIndex);
    }
}
