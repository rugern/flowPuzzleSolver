package module3;

import java.util.ArrayList;

public class Domain {

	private ArrayList<Integer> domainValues;
	private Variable variable;
	
	public Domain(ArrayList<Integer> domainValues, Variable variable) {
		this.domainValues = domainValues;
		this.variable = variable;
	}
	
	public ArrayList<Integer> getDomainValues() {
		return domainValues;
	}
	
	public void setDomainValues(ArrayList<Integer> domain) {
		this.domainValues = domain;
	}

	public Variable getVariable() {
		return variable;
	}
	
}
