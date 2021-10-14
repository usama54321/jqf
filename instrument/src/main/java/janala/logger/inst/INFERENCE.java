package janala.logger.inst;

import java.util.Map;

public class INFERENCE extends Instruction {
  int i;
  public INFERENCE(Object data) {
    super(-1, -1);
  }

  public void visit(IVisitor visitor) {
    visitor.visitINFERENCE(this); 
  }

  @Override
  public String toString() {
    return "INFERENCE"; //@TODO add unique id
  }
}
