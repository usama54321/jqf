package edu.berkeley.cs.jqf.instrument.tracing.events;

import janala.logger.inst.MemberRef;

public class InferenceEvent extends TraceEvent {

    Object data;

    public InferenceEvent(int iid, MemberRef containingMethod, int lineNumber, Object data) {
        super(iid, containingMethod, lineNumber);
        this.data = data;
    }

    @Override
    public String toString() {
        return String.format("INFERENCE(%d,%d)", iid, lineNumber);
    }

    @Override
    public void applyVisitor(TraceEventVisitor v) {
        v.visitInferenceEvent(this);
    }
}
