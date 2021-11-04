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
    int currentCoverage;

    public class BranchData {
        public int trueTaken;
        public int falseTaken;
        public int lineNumber;
    };

    public class CoverageData {
        public MethodIidMap methodMapping;
        public IidBranchMap iidMapping;
        public ClassMethodMap classMethodMap;

        public CoverageData() {
            methodMapping = new MethodIidMap();
            iidMapping = new IidBranchMap();
            classMethodMap = new ClassMethodMap();
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
    }

    //@TODO record classes too
    @Override
    public void visitBranchEvent(BranchEvent e) {
        ClassMethodMap classMethodMap = getClassMethodMap();
        MethodIidMap methodMapping = getMethodMapping();
        IidBranchMap iidMapping = getIidMapping();

        String cls = e.getContainingClass();
        String methodName = e.getContainingMethodName() + "." + e.getContainingMethodDesc();
        Set<String> methods;

        if(classMethodMap.containsKey(cls)) {
            methods = classMethodMap.get(cls);
        } else {
            methods = new TreeSet<>();
            classMethodMap.put(cls, methods);
        }

        methods.add(methodName);

        int iid = e.getIid();
        int arm = e.getArm();

        Set<Integer> iids;
        if (methodMapping.containsKey(methodName)) {
            iids = methodMapping.get(methodName);
        } else {
            iids = new TreeSet<>();
            methodMapping.put(methodName, iids);
        }
        iids.add(iid);

        methodMapping.put(methodName, iids);

        int left, right;
        BranchData kv;
        if (iidMapping.containsKey(iid)) {
            kv = iidMapping.get(iid);
        } else {
            kv = new BranchData();
        }

        kv.trueTaken += (arm == 0 ? 1 : 0);
        kv.falseTaken += (arm == 0 ? 0: 1);
        kv.lineNumber = e.getLineNumber();

        iidMapping.put(iid, kv);

        //mapping.put(key, count);
        //super.visitBranchEvent(e);

    }

    public void setCurrentCoverage(int index) {
        currentCoverage = index;
    }

    private void addNewCoverage() {
        //System.out.println("adding new coverage");
        coverageData.add(new CoverageData());
    }

    @Override
    public void visitInferenceEvent(InferenceEvent e) {
        int newIndex = currentCoverage + 1;
        if (newIndex >= coverageData.size()) {
            addNewCoverage();
        }
        setCurrentCoverage(newIndex);
    }
}
