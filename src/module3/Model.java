package module3;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import javafx.util.Pair;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


public class Model {
	
	//Open and closed arrays for A* search
	private ArrayList<SearchNode> open = new ArrayList<SearchNode>();
	private ArrayList<SearchNode> closed = new ArrayList<SearchNode>();
	
	//CNET
	private ArrayList<Variable> variables = new ArrayList<Variable>();
	private ArrayList<Constraint> constraints = new ArrayList<Constraint>();
	
	//Cost of flowing a square
	private double arcCost = 0.0;
	//Dimension of square board
	private int dimension;
	//Number of color flows (i.e. size of domain)
	private int numberOfColors;
	
	public Model() {
		
	}
	
	/**
	 * Reads input file and stores the data in fields and Variable objects
	 * @param fileName
	 * @return All Variable objects initialized
	 */
	public ArrayList<Domain> readFileAndAnalyze(String fileName) {
		ArrayList<String> fileInfo = null;
		//Read info from the specified file
		try {
			fileInfo = readFile(fileName);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Could not read file");
		}
		
		//Get metadata from file and store it in fields in this class
		analyzeMetadata(fileInfo);
		//Fetch endpoints of flow
		ArrayList<Domain> domains = analyzeVariables(fileInfo);
		//Initialize the remaining variables and concatenate the arrays of variables and return
		domains.addAll(initializeVariablesNotEndpoints(variables));
		return domains;
	}
	
	/**
	 * Initializes a variable for each coordinate of the grid not inhabited by a flow endpoint
	 * @param endpoints
	 * @return ArrayList of variables, where variables are not endpoints
	 */
	public ArrayList<Domain> initializeVariablesNotEndpoints(ArrayList<Variable> endpoints) {
		//Iterate through values to find what variables we need to initialize
		boolean[][] coordinateIsEndpoint = new boolean[dimension][dimension];
		for(Variable v:endpoints) {
			coordinateIsEndpoint[v.getRow()][v.getColumn()] = true;
		}
		ArrayList<Domain> domains = new ArrayList<Domain>();
		for(int i=0; i<coordinateIsEndpoint.length; i++) {
			for(int j=0; j<coordinateIsEndpoint[i].length; j++) {
				//If not an endpoint
				if(!coordinateIsEndpoint[i][j]) {
					//Create full domain to add to each variable
					ArrayList<Integer> domainValues = new ArrayList<Integer>();
					for(int colorIndex=0; colorIndex<numberOfColors; colorIndex++) {
						domainValues.add(colorIndex);
					}
					//Create coordinate pair and create new variable
					Pair<Integer,Integer> coordinates = new Pair<Integer, Integer>(i, j);
					Variable variable = new Variable(coordinates, false);
					//Create new domain referencing this variable
					domains.add(new Domain(domainValues, variable));
					variables.add(variable);
				}
			}
		}
		return domains;
	}
	
	/**
	 * Analyze and store information from first line
	 * @param fileInfo
	 */
	public void analyzeMetadata(ArrayList<String> fileInfo) {
		//We need to arrange information in a useful format
		String workingString = "";
		//use this variable to keep track of separators so we know which element we're dealing with
		int elementNumber = 0;
		//First get dimension and number of colors from line 1
		for(char c:fileInfo.get(0).toCharArray()) {
			if(c != ' ') {
				//use space as data separator
				workingString += c;
			}else if(elementNumber==0){
				//the first extracted element is dimension of square board
				dimension = Integer.parseInt(workingString);
				workingString = "";
				elementNumber++;
			}else {
				//the second extracted element is numberofedges
				numberOfColors = Integer.parseInt(workingString);
				workingString = "";
			}
		}
		if(workingString.length()>0) {
			//invoked by trailing space in input file, which sometimes appears, sometimes not
			numberOfColors = Integer.parseInt(workingString);
			workingString = "";
		}
	}

	/**
	 * Analyses and stores info from lines 1 to the end of the file, initializing variables for endpoints
	 * @param fileInfo
	 * @return ArrayList of flow endpoint variables
	 */
	public ArrayList<Domain> analyzeVariables(ArrayList<String> fileInfo) {
		String workingString = "";
		ArrayList<Domain> domains = new ArrayList<Domain>();
		//Now read all endpoints from file
		for(int i=1; i<fileInfo.size(); i++) {
			//Use element number to keep track of separators
			int elementNumber = 0;
			//We receive two endpoint variables on each line
			int firstVariableRow = 0;
			int firstVariableColumn = 0;
			int secondVariableRow = 0;
			int secondVariableColumn = 0;
			char[] line = fileInfo.get(i).toCharArray();
			for(char c:line) {
				if(c != ' ') {
					//Add until we see a space, which is a separator of data
					workingString += c;
				}else if(elementNumber == 0) {
					//This is the index, which the array order implies so we don't store it
					workingString = "";
					elementNumber++;
				}else if(elementNumber == 1) {
					//Store row of first variable
					firstVariableRow = Integer.parseInt(workingString);
					workingString = "";
					elementNumber++;
				}else if(elementNumber == 2) {
					//Store column of first variable
					firstVariableColumn = Integer.parseInt(workingString);
					workingString = "";
					elementNumber++;
				}else if(elementNumber == 3) {
					//Store row of second variable
					secondVariableRow = Integer.parseInt(workingString);
					workingString = "";
					elementNumber++;
				}else if(elementNumber == 4) {
					//Store column of second variable
					secondVariableColumn = Integer.parseInt(workingString);
					workingString = "";
					elementNumber++;
				}
			}
			if(workingString.length()>0) {
				//Invoked by trailing space on line
				//Store column of second variable
				secondVariableColumn = Integer.parseInt(workingString);
				workingString = "";
				elementNumber++;
			}
			//Create pairs for the coordinates of endpoints
			Pair<Integer, Integer> firstVariableCoordinates = new Pair<Integer, Integer>(firstVariableRow, firstVariableColumn);
			Pair<Integer, Integer> secondVariableCoordinates = new Pair<Integer, Integer>(secondVariableRow, secondVariableColumn);
			//Define our domain, which is a singleton of the specified color
			ArrayList<Integer> firstDomainValues = new ArrayList<Integer>();
			firstDomainValues.add(i-1);
			ArrayList<Integer> secondDomainValues = new ArrayList<Integer>();
			secondDomainValues.addAll(firstDomainValues);
			//Initialize variable objects for the endpoints
			Variable firstVariable = new Variable(firstVariableCoordinates, true);
			Variable secondVariable = new Variable(secondVariableCoordinates, true);
			//Create domain objects referencing the variables, and add to array to be returned
			domains.add(new Domain(firstDomainValues, firstVariable));
			domains.add(new Domain(secondDomainValues, secondVariable));
			//Add variables to CNET
			variables.add(firstVariable);
			variables.add(secondVariable);
		}
		return domains;
	}
	
	/**
	 * Reads the input file line for line and stores each line as an element in string array
	 * @param fileName
	 * @return String array
	 * @throws Exception
	 */
	public ArrayList<String> readFile(String fileName) throws Exception {
		//read file and return each line in an array
		InputStream in = new FileInputStream(fileName);
    	InputStreamReader is = new InputStreamReader(in);
    	BufferedReader br = new BufferedReader(is);
    	String temp = br.readLine();
    	ArrayList<String> fileInfo = new ArrayList<String>();
    	while(temp!=null) {
    		fileInfo.add(temp);
    		temp = br.readLine();
    	}
    	br.close();
    	return fileInfo;
    }
	
	/**
	 * Initialized sort on open array, stores result back in the same array
	 */
	public void sortOpenByScore() {
		open = quicksortArray(open);
	}

	public ArrayList<SearchNode> quicksortArray(ArrayList<SearchNode> array) {
		//Array is sorted if only one or zero nodes
		if(array.size()<2) {
			return array;
		}
		//Split array into one with lower score and one with higher
		ArrayList<SearchNode> lowerArray = new ArrayList<SearchNode>();
		ArrayList<SearchNode> higherArray = new ArrayList<SearchNode>();
		//Compare with a random pivot value to split array
		int pivot = (int)Math.round(array.size()/2);
		double pivotValue = array.get(pivot).getScore();
		for(int i=0; i<array.size(); i++) {
			SearchNode temp = array.get(i);
			//Save pivot for later
			if(i == pivot) {
				continue;
			}else if(temp.getScore() < pivotValue) {
				lowerArray.add(temp);
			} else {
				higherArray.add(temp);
			}
		}
		//Add pivot to smallest list to avoid endless loop
		if(higherArray.size() < lowerArray.size()) {
			higherArray.add(array.get(pivot));
		} else {
			lowerArray.add(array.get(pivot));
		}
		//Recursive call until array consist of 1 node
		lowerArray = quicksortArray(lowerArray);			
		higherArray = quicksortArray(higherArray);
		//Concatenate the two sorted arrays
		lowerArray.addAll(higherArray);
		//Return full sorted array
		return lowerArray;
	}
	
	public boolean isPossibleSolution(SearchNode node) {
		ArrayList<Domain> domains = node.getDomains();
		for(Domain d:domains) {
			if(d.getDomainValues().size()==0) {
				//Not a viable solution if domain has been reduced to 0
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks if all domains are singleton, which would imply a goal state
	 * @param node
	 * @return True if node is goal, false if not
	 */
	public boolean isGoal(SearchNode node) {
		ArrayList<Domain> domains = node.getDomains();
		for(Domain d:domains) {
			if(d.getDomainValues().size()!=1) {
				//Not a solution if a domain is not singleton
				return false;
			}
		}
		//Not sure if solution before we can trace a path between all corresponding endpoints
		//Need to do this without loops or multiple branches
		for(int i=0; i<numberOfColors; i++) {
			ArrayList<Variable> variables = findVariablesWithDomainValue(i, domains);
			ArrayList<Variable> endpoints = findEndpointVariables(variables);
			//Use one of the endpoints as starting point, so we cover path
			//from one end to the other
			Variable initialVariable = endpoints.get(0);
			ArrayList<Variable> closed = new ArrayList<Variable>();
			if(!recursivePathTesting(initialVariable, variables, closed)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Recursively find a neighbour of nextVariable that has not been closed and continue tracing from this,
	 * adding it to closed. 
	 * @param nextVariable
	 * @param variables
	 * @param oldClosed
	 * @return True if the last recursion is an endpoint, false if not or finds a dead end
	 */
	public boolean recursivePathTesting(Variable nextVariable, ArrayList<Variable> variables, ArrayList<Variable> oldClosed) {
		ArrayList<Variable> closed = new ArrayList<Variable>();
		closed.addAll(oldClosed);
		closed.add(nextVariable);
		//If closed is equal size as variables, we have controlled all variables
		if(closed.size()==variables.size()) {
			if(nextVariable.isEndpoint()) {
				return true;
			} else {
				return false;
			}
		}
		ArrayList<Variable> neighbours = findNeighbours(nextVariable, variables);
		for(Variable neighbour:neighbours) {
			if(!closed.contains(neighbour)) {
				//Try all neighbours not closed as next variable on the path
				if(recursivePathTesting(neighbour, variables, closed)) {
					return true;
				}
			}
		}
		//Dead end
		return false;
	}
	
	/**
	 * Iterates through variable array and adds the endpoint variables to an array
	 * @param variables
	 * @return ArrayList of endpoints
	 */
	public ArrayList<Variable> findEndpointVariables(ArrayList<Variable> variables) {
		ArrayList<Variable> endpoints = new ArrayList<Variable>();
		for(Variable v: variables) {
			if(v.isEndpoint()) {
				endpoints.add(v);
			}
		}
		return endpoints;
	}
	
	/**
	 * Finds domain objects where the only domain value is domainValue, i.e. the same color
	 * @param domainValue
	 * @param domains
	 * @return Domains with single domain value equal domainValue
	 */
	public ArrayList<Domain> findDomainsWithValue(int domainValue, ArrayList<Domain> domains) {
		ArrayList<Domain> valueDomains = new ArrayList<Domain>();
		for(Domain d:domains) {
			if(d.getDomainValues().get(0)==domainValue && !d.getVariable().isEndpoint()) {
				valueDomains.add(d);
			}
		}
		return valueDomains;
	}
	
	/**
	 * Return index of nodeToFind in closed array
	 * @param nodeToFind
	 * @return index of node if found, -1 if node isn't present
	 */
	public int indexOfNodeInClosed(SearchNode nodeToFind) {
		return indexOfNode(nodeToFind, closed);		
	}
	
	/**
	 * Return index of nodeToFind in open array
	 * @param nodeToFind
	 * @return index of node if found, -1 if node isn't present
	 */
	public int indexOfNodeInOpen(SearchNode nodeToFind) {
		return indexOfNode(nodeToFind, open);
	}
	
	public int indexOfNode(SearchNode nodeToFind, ArrayList<SearchNode> array) {
		//Get all variables of node to find
		ArrayList<Domain> nodeToFindDomains= nodeToFind.getDomains();
		//For each node in array
		for(int i=0; i<array.size(); i++) {
			//Get all variables of this node to compare
			ArrayList<Domain> domainsToCompare = array.get(i).getDomains();
			//We assume this is the correct node
			boolean foundCorrectNode = true;
			//For each variable of the current node
			for(int j=0; j<domainsToCompare.size(); j++) {
				//Get corresponding domains to compare
				Domain nodeToFindDomain = nodeToFindDomains.get(j);
				Domain domainToCompare = domainsToCompare.get(findDomainIndex(nodeToFindDomain, domainsToCompare));
				ArrayList<Integer> nodeToFindDomainValues = nodeToFindDomain.getDomainValues();
				ArrayList<Integer> domainToCompareValues = domainToCompare.getDomainValues();
				//If domains are not equal, this is not the correct node
				if(nodeToFindDomainValues.size() == domainToCompareValues.size()) {
					for(int k=0; k<nodeToFindDomainValues.size(); k++) {
						if(nodeToFindDomainValues.get(k) != domainToCompareValues.get(k)) {
							//break to avoid unnecessary checks
							foundCorrectNode = false;
							break;
						}
					}
				}else {
					//Domains are not equal size, this is not correct node
					//break to avoid unnecessary checks
					foundCorrectNode = false;
					break;
				}
				if(!foundCorrectNode) {
					//We found a domain value that's not equal, this is not correct node
					//break to avoid unnecessary checks
					break;
				}
			}
			if(foundCorrectNode) {
				//We compared domain of all variables in this searchnode without finding differences, i.e. this is the correct node
				return i;
			}
		}
		//We have searched all nodes in array without finding an equal node, so return -1 to signify this
		return -1;
	}
	
	/**
	 * Create successors by finding optimal variable to generate successors from
	 * and makes each value in domain an assumption in child
	 * @param node
	 * @return Array of children nodes
	 */
	public ArrayList<SearchNode> generateSuccessors(SearchNode node) {
		//Array to be returned
		ArrayList<SearchNode> children = new ArrayList<SearchNode>();
		//Variables to be considered
		ArrayList<Domain> domains = node.getDomains();
		//Finds optimal variable to generate successors from
		Domain domain = findSmallestDomain(domains);
		if(domain==null) {
			return null;
		}
		ArrayList<Integer> domainValues = domain.getDomainValues();
		//For each domain value, set value as assumption
		for(int i=0; i<domainValues.size(); i++) {
			//Deep copy variables for new searchnode
			ArrayList<Domain> childDomains = copyDomainArray(domains);
			//Create new domain with value i as assumption
			ArrayList<Integer> newDomain = new ArrayList<Integer>();
			newDomain.add(domainValues.get(i));
			//Find corresponding variable (based on coordinates) in copy
			Domain domainWithAssumption = childDomains.get(findDomainIndex(domain, childDomains));
			//Set new domain and initialize searchnode
			domainWithAssumption.setDomainValues(newDomain);
			SearchNode childNode = new SearchNode(childDomains);
			//Set field for remembering this was the variable the assumption was made about
			childNode.setLastVariableDomainToAssumeValue(domainWithAssumption);
			//Add node to children
			children.add(childNode);
		}
		return children;		
	}
	
	/**
	 * Copies the Domain array by initializing a new Domain for each
	 * element in the array, sharing Variable reference but new domain values reference
	 * @param domains
	 * @return
	 */
	public ArrayList<Domain> copyDomainArray(ArrayList<Domain> domains) {
		ArrayList<Domain> domainsCopy = new ArrayList<Domain>();
		for(Domain d: domains) {
			ArrayList<Integer> copyDomainValues = new ArrayList<Integer>();
			//Integer references are okay, as long as array reference is new
			copyDomainValues.addAll(d.getDomainValues());
			//Add new array of domains, and copy reference to variable in CNET
			domainsCopy.add(new Domain(copyDomainValues, d.getVariable()));
		}
		return domainsCopy;
	}
	
	/**
	 * Iterate through all variables and initialize appropriate constraints for each of them
	 * @param variables
	 * @return ArrayList of constraint objects
	 */
	public void generateConstraints() {
		for(Variable v:variables) {
			//Create function name. Doesn't need to be unique, as variables reference the constraint objects
			String functionName = "constraint";
			//Returns true if this variable colors is equal to the neighbour
			String expression = "thisVariable==neighbourVariable";
			//Arraylist of variables to pass as arguments
			ArrayList<String> arguments = new ArrayList<String>();
			arguments.add("thisVariable");
			arguments.add("neighbourVariable");
			ArrayList<Variable> neighbours = findNeighbours(v, variables);
			//Generate function
			Invocable code = createFunction(functionName, expression, arguments);
			//Create an array of contraints this variable is participating in
			ArrayList<Constraint> participatingInConstraints = new ArrayList<Constraint>();
			for(Variable neighbour:neighbours) {
				//Pass participating variables as array
				ArrayList<Variable> constraintVariables = new ArrayList<Variable>();
				constraintVariables.add(v);
				constraintVariables.add(neighbour);
				//Initialize new constraint
				participatingInConstraints.add(new Constraint(code, constraintVariables));
			}
			//Pointer from variable to constraints it participates in (except neighbour's constraints, which are duplicates and not same object)
			v.setParticipatingInConstraints(participatingInConstraints);
			//Add constraints to constraint array
			constraints.addAll(participatingInConstraints);
		}
	}

	/**
	 * Finds neighbours of a variable (in square grid)
	 * @param variable
	 * @param variables
	 * @return ArrayList of neighbour variables
	 */
	public ArrayList<Variable> findNeighbours(Variable variable, ArrayList<Variable> variables) {
		ArrayList<Variable> neighbours = new ArrayList<Variable>();
		int variableRow = variable.getRow();
		int variableColumn = variable.getColumn();
		for(Variable neighbour: variables) {
			int neighbourRow = neighbour.getRow();
			int neighbourColumn = neighbour.getColumn();
			//If this variable is not the same as "variable"
			if(neighbourColumn != variableColumn || neighbourRow != variableRow) {
				//If this variable is adjacent to "variable"
				int offsetColumn = Math.abs(neighbourColumn-variableColumn);
				int offsetRow = Math.abs(neighbourRow-variableRow);
				if((offsetColumn==1 && offsetRow==0) || (offsetColumn==0 && offsetRow==1)) {
					//Add to array to be returned
					neighbours.add(neighbour);
				}
			}
		}
		return neighbours;
	}
	
	/**
	 * Finds index of Domain in arraylist based on shared variable reference
	 * @param domain
	 * @param domains
	 * @return Index of Domain in arraylist
	 */
	public int findDomainIndex(Domain domain, ArrayList<Domain> domains) {
		Variable domainToFindVariable = domain.getVariable();
		for(int i=0; i<domains.size(); i++) {
			//If domains reference to same variable, they are corresponding domains across different search nodes
			if(domainToFindVariable.equals(domains.get(i).getVariable())) {
				return i;
			}
		}
		//Should not get here
		return -1;
	}
	
	/**
	 * Finds domain of given variable
	 * @param variable
	 * @param domains
	 * @return
	 */
	public boolean isCorrectEndpoint(Variable variable, ArrayList<Domain> domains, int domainValue) {
		for(Domain d:domains) {
			if(variable.equals(d.getVariable()) && d.getDomainValues().get(0) == domainValue) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Find smallest domain with 2 or more values, i.e. the smallest domain we
	 * can generate successors from
	 * @param domains
	 * @return Smallest domain larger than 2
	 */
	public Domain findSmallestDomain(ArrayList<Domain> domains) {
		int domainSize = 100;
		ArrayList<Domain> possibleDomains = new ArrayList<Domain>();
		for(Domain d:domains) {
			int currentDomainSize = d.getDomainValues().size();
			if(currentDomainSize<domainSize && currentDomainSize >= 2) {
				//New smallest domain, start new list collecting domains of equal size
				possibleDomains = new ArrayList<Domain>();
				possibleDomains.add(d);
				domainSize = currentDomainSize;
			} else if(currentDomainSize==domainSize) {
				//Add equal sized domains in case we want to be able to choose
				//which we want
				possibleDomains.add(d);
			}
		}
		if(possibleDomains.size()>0) {
//			Random rand = new Random();
//			return possibleDomains.get(rand.nextInt(possibleDomains.size()));
			if(possibleDomains.size()>1) {
				//Turns out we utilize GAC better if we choose a domain not adjacent
				//to our latest pick. Works this way because we work our way from left to
				//right on the board by always choosing one of the first domains, which
				//were those generated first
				return possibleDomains.get(1);
			}
			return possibleDomains.get(0);
		} else {
			return null;
		}
	}
	
	/**
	 * Evaluate heuristic by subtracting the number of flowed squares from the total number of squares to be filled
	 * @param node
	 * @return Number of squares left to flow
	 */
	public double evaluateHeuristic(SearchNode node) {
		double heuristics = dimension*dimension*numberOfColors;
		heuristics -= numberOfDomainReductions(node);
		heuristics += evaluateHeuristicFunction(node);
		return heuristics;
	}
	
	/**
	 * Finds how many values we have pruned from this nodes domains, either
	 * by assumptions or CSP
	 * @param node
	 * @return Reduced size compared to root node
	 */
	public int numberOfDomainReductions(SearchNode node) {
		ArrayList<Domain> domains = node.getDomains();
		int reducedSize = 0;
		for(int i=0; i<domains.size(); i++) {
			reducedSize += numberOfColors - domains.get(i).getDomainValues().size();
		}
		return reducedSize;
	}
	
	/**
	 * Evaluates heuristic based on path length, rewarding nodes with half the 
	 * number of variables number of assumptions. This forces the A* to go deep early
	 * and then breadth
	 * @param node
	 * @return Heuristic value of node
	 */
	public double evaluateHeuristicFunction(SearchNode node) {
		int pathLength = 1;
		while(node.getParent()!=null) {
			node = node.getParent();
			pathLength++;
		}
		pathLength -= (dimension*dimension-numberOfColors)/2;
		return Math.abs(pathLength);
	}
	
	/**
	 * Finds variables corresponding to domains with the single domain value of domainValue
	 * @param domainValue
	 * @param domains
	 * @return Variables with domain with domainValue
	 */
	public ArrayList<Variable> findVariablesWithDomainValue(int domainValue, ArrayList<Domain> domains) {
		ArrayList<Variable> targetVariables = new ArrayList<Variable>();
		for(Domain d:domains) {
			if(d.getDomainValues().get(0)==domainValue) {
				targetVariables.add(d.getVariable());
			}
		}
		return targetVariables;
	}
	
	/**
	 * Finds endpoint domains from an array of domains
	 * @param domains
	 * @return Returns ArrayList of endpoint variables' domains
	 */
	public ArrayList<Domain> getEndpoints(ArrayList<Domain> domains) {
		ArrayList<Domain> endpoints = new ArrayList<Domain>();
		for(Domain d:domains) {
			if(d.getVariable().isEndpoint()) {
				endpoints.add(d);
			}
		}
		return endpoints;
	}
	
	/**
	 * Finds and returns manhattan distance between the two variables
	 * @param start
	 * @param end
	 * @return
	 */
	public double findDistance(Variable start, Variable end) {
		double distance = Math.abs(start.getColumn()-end.getColumn());
		distance += Math.abs(start.getRow()-end.getRow());
		return distance;
	}
	
	/**
	 * Using Javascript Nashorn to evaluate expression
	 * @param functionName
	 * @param expression
	 * @param variables
	 * @return Invocable function 
	 */
	public Invocable createFunction(String functionName, String expression, ArrayList<String> variables) {
		//Using JavaScript Nashorn for dynamically creating functions
		//The manager discovers classes and stores all bindings
		//The engine is an interface to the methods and may bind variables locally (default) or globally
		//Thus we initialize one engine per function to guarantee separation of values
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");
		
		//Javascript function consists of: functionName(variables) { return expression_value; }
		//Lets build that string for eval with available information
		String script = "function " + functionName + "(arguments) { ";
		for(int i=0; i<variables.size(); i++) {
			script += variables.get(i) + "=arguments[" + i + "]; ";
		}
		script += "return " + expression + "; }";
		//Try to evaluate the script
		try {
			engine.eval(script);
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		//"function" is collection of all scripts bound to engine
		Invocable function = (Invocable) engine;
		//Call function by function.invokeFunction(functionName, variable1, variable2,...)
		return function;
	}

	public ArrayList<Variable> getVariables() {
		return variables;
	}

	public ArrayList<Constraint> getConstraints() {
		return constraints;
	}
	
	public int getNumberOfSuccessors() {
		return open.size() + closed.size();
	}
	
	public boolean openIsEmpty() {
		return open.isEmpty();
	}
	
	public void addClosedNode(SearchNode node) {
		closed.add(node);
	}
	
	public void addOpenNode(SearchNode node) {
		open.add(node);
	}
	
	public SearchNode getFromClosed(int index) {
		return closed.get(index);
	}
	
	public SearchNode getFromOpen(int index) {
		return open.get(index);
	}
	
	public SearchNode popFromOpen() {
		return open.remove(0);
	}
	
	public double getArcCost() {
		return arcCost;
	}
	
	public int openSize() {
		return open.size();
	}
	
	public ArrayList<SearchNode> getClosed() {
		return closed;
	}
}
