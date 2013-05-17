package cascalog.aggregator;

import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;
import cascalog.Util;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.RT;

public class ClojureMonoidFunctor extends FoldFunctor<ISeq> {
  final CombinerSpec combinerSpec;
  protected IFn prepareFn;
  protected IFn combineFn;
  protected IFn presentFn;

  public ClojureMonoidFunctor(Fields fields, CombinerSpec combinerSpec) {
    super(fields);
    this.combinerSpec = combinerSpec;
  }

  @Override
  public ISeq prepare(TupleEntry args) {
    prepareFn = combinerSpec.getPrepareFn();
    combineFn = combinerSpec.getCombineFn();
    presentFn = combinerSpec.getPresentFn();
    if (null != prepareFn) {
      return RT.seq(Util.coerceToList(prepareFn.applyTo(Util.coerceFromTuple(args))));
    } else {
      return RT.seq(Util.coerceFromTuple(args));
    }
  }

  @Override
  public ISeq fold(ISeq acc, TupleEntry newArgs) {
    return RT.seq(Util.coerceToList(
        combineFn.applyTo(
            Util.cat(acc, RT.seq(Util.coerceFromTuple(newArgs))))));
  }

  @Override
  public Tuple present(ISeq finalValue) {
    if (null != presentFn) {
      return Util.coerceToTuple(presentFn.applyTo(finalValue));
    } else {
      return Util.coerceToTuple(finalValue);
    }
  }
}
