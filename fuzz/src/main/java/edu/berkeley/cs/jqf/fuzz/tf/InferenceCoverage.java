package edu.berkeley.cs.jqf.fuzz.tf;

import edu.berkeley.cs.jqf.fuzz.util.Coverage;
import edu.berkeley.cs.jqf.instrument.tracing.events.BranchEvent;
import edu.berkeley.cs.jqf.instrument.tracing.events.CallEvent;
import edu.berkeley.cs.jqf.instrument.tracing.events.InferenceEvent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class InferenceCoverage extends Coverage {

    @Override
    public void visitCallEvent(CallEvent e) {
        //do nothing for call events
    }

    @Override
    public void visitBranchEvent(BranchEvent b) {
        if (!b.getContainingClass().contains("GraphRunner") && !b.getContainingClass().contains("tensorflowlite"))
            super.visitBranchEvent(b);
    }
}
