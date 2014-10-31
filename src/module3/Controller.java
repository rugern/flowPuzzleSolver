package module3;

import java.util.ArrayList;

import javafx.scene.paint.Color;

public class Controller {
	
	private View view;
	private Model model;
	private GAC gac;
	private AStar astar;

	public Controller(String fileName, View view) {
		//Initialize game state
		this.view = view;
		model = new Model();
		ArrayList<Domain> domains = model.readFileAndAnalyze(fileName);
		model.generateConstraints();
		view.initializeStage(domains);
		
		//Initialize seach and domain filtering
		gac = new GAC();
		astar = new AStar(model);
		initializeSearchState(domains);
		//Rerun all search nodes in open until open is empty or solution is found
		new Thread() {
			public void run() {
				while(!model.openIsEmpty()) {
					SearchNode nextNode = model.popFromOpen();
					model.addClosedNode(nextNode);
					updateView(nextNode);
					gac.reRun(nextNode);
					if(model.isGoal(nextNode)) {
						outputResults(nextNode);
						updateView(nextNode);
						return;
					}
					if(model.isPossibleSolution(nextNode)) {
						//If node is a viable solution, i.e. no domains are empty
						astar.search(nextNode);
					}
				}
				System.out.println("No solution found");
			}
		}.start();
	}
	
	/**
	 * Updates view according to state of variable domains
	 * When we encounter a singleton domain we check if we can
	 * update this variables parent or child (flow direction)
	 * @param node
	 */
	public void updateView(SearchNode node) {
		ArrayList<Domain> domains = node.getDomains();
		for(Domain d:domains) {
			ArrayList<Integer> domain = d.getDomainValues();
			if(domain.size()==1) {
				//If domain is singleton, we can color the square in this color
				view.colorSquare(d.getVariable().getRow(), d.getVariable().getColumn(), view.availableColors.get(domain.get(0)));
			} else {
				view.colorSquare(d.getVariable().getRow(), d.getVariable().getColumn(), Color.WHITE);
			}
		}
	}
	
	/**
	 * Initialize root node and performs initial domain filtering
	 * @param domains
	 */
	public void initializeSearchState(ArrayList<Domain> domains) {
		//Initialize root node
		SearchNode initialNode = new SearchNode(domains);
		initialNode.setCost(0);
		initialNode.setHeuristics(model.evaluateHeuristic(initialNode));
		initialNode.setScore(initialNode.getHeuristics());
		//Add these variables to the GAC queue
		gac.initialization(initialNode);
		//Revise queue
		gac.domainFiltering(initialNode);
		//In case solution is found immediately
		if(model.isGoal(initialNode)) {
			outputResults(initialNode);
			updateView(initialNode);
			return;
		}
		//Generate successors from A* search
		model.addClosedNode(initialNode);
		astar.search(initialNode);
	}
	
	/**
	 * Outputs results, with number of generated and expanded nodes, and solution
	 * length
	 * @param node
	 */
	public void outputResults(SearchNode node) {
		int numberOfSearchNodes = model.getNumberOfSuccessors();
		int numberOfExpandedSearchNodes = model.getClosed().size();
		int pathLength = 1;
		SearchNode currentNode = node;
		while(currentNode.getParent()!=null) {
			currentNode = currentNode.getParent();
			pathLength++;
		}
		System.out.println("Number of search nodes generated: " + numberOfSearchNodes);
		System.out.println("Number of expanded search nodes: " + numberOfExpandedSearchNodes);
		System.out.println("Path length from root to solution node: " + pathLength);
	}
}
