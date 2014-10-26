package module3;

import java.util.ArrayList;

public class SearchNode {
	
	private ArrayList<Domain> domains;
	private ArrayList<SearchNode> children = new ArrayList<SearchNode>();
	private SearchNode parent;
	private Domain lastVariableDomainToAssumeValue;
	
	private double score = -1;
	private double cost = -1;
	private double heuristics = -1;
	
	public SearchNode(ArrayList<Domain> domains) {
		this.domains = domains;
	}
	

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public double getHeuristics() {
		return heuristics;
	}

	public void setHeuristics(double heuristics) {
		this.heuristics = heuristics;
	}

	public ArrayList<SearchNode> getChildren() {
		return children;
	}

	public void addChild(SearchNode child) {
		children.add(child);
	}

	public SearchNode getParent() {
		return parent;
	}

	public void setParent(SearchNode parent) {
		this.parent = parent;
	}

	public Domain getLastVariableDomainToAssumeValue() {
		return lastVariableDomainToAssumeValue;
	}

	public void setLastVariableDomainToAssumeValue(Domain lastVariableToAssumeValue) {
		this.lastVariableDomainToAssumeValue = lastVariableToAssumeValue;
	}
	
	public ArrayList<Domain> getDomains() {
		return domains;
	}
}
