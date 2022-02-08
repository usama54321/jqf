package edu.berkeley.cs.jqf.instrument.tracing.events;

import janala.logger.inst.MemberRef;

public class InferenceEvent extends TraceEvent {

    Object data;
    public String clazz, method;

    public InferenceEvent(int iid, MemberRef containingMethod, int lineNumber, Object data, String clazz, String method) {
        super(iid, containingMethod, lineNumber);
        this.data = data;
        this.clazz = clazz;
        this.method = method;
    }

    @Override
    public String toString() {
        return String.format("INFERENCE(%d,%d)", iid, lineNumber);
    }

    @Override
    public void applyVisitor(TraceEventVisitor v) {
        v.visitInferenceEvent(this);
    }

    public Object getData() {
        return data;
    }
}
