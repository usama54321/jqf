package janala.logger.inst;

import java.util.Map;

public class INFERENCE extends Instruction {
  int i;
  Object data;
  public String clazz;
  public String method;

  public INFERENCE(Object data, int lineNumber, String clazz, String method) {
    super(-1, lineNumber);
    this.data = data;
    this.clazz = clazz;
    this.method = method;
  }

  public void visit(IVisitor visitor) {
    visitor.visitINFERENCE(this);
  }

  public Object getData() {
    return this.data;
  }

  @Override
  public String toString() {
    return "INFERENCE"; //@TODO add unique id
  }
}
