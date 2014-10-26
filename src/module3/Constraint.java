package module3;

import java.util.ArrayList;

import javax.script.Invocable;

public class Constraint {
	
	private Invocable code;
	private ArrayList<Variable> variables;

	public Constraint(Invocable code, ArrayList<Variable> variables) {
		this.code = code;
		this.variables = variables;
	}
	
	public ArrayList<Variable> getParticipatingVariables() {
		return variables;
	}
	
	public Invocable getConstraint() {
		return code;
	}
}
