package evaluation;

public abstract class EvalMeasure {
	public abstract String getName();
	public abstract Result eval(Hyp hyp);
}
