package janala.logger.inst;

import java.util.Map;

public class INFERENCE extends Instruction {
  int i;
  Object data;
  public INFERENCE(Object data) {
    super(-1, -1);
    this.data = data;
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
