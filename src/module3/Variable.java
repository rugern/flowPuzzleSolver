package module3;

import java.util.ArrayList;

import javafx.util.Pair;

public class Variable {
	
	private Pair<Integer, Integer> coordinate;
	private boolean endpoint;
	private ArrayList<Constraint> participatingInConstraints;
	
	public Variable(Pair<Integer, Integer> coordinate, boolean endpoint) {
		this.coordinate = coordinate;
		this.endpoint = endpoint;
	}
	
	public int getRow() {
		return coordinate.getKey();
	}
	
	public int getColumn() {
		return coordinate.getValue();
	}
	
	public boolean isEndpoint() {
		return endpoint;
	}

	public ArrayList<Constraint> getParticipatingInConstraints() {
		return participatingInConstraints;
	}

	public void setParticipatingInConstraints(ArrayList<Constraint> participatingInConstraints) {
		this.participatingInConstraints = participatingInConstraints;
	}

	public Pair<Integer,Integer> getCoordinate() {
		return coordinate;
	}
	
}
