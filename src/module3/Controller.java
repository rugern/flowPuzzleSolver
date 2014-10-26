package module3;

import java.util.ArrayList;

import javafx.scene.paint.Color;

public class Controller {
	
	private View view;
	private Model model;
	private GAC gac;
	private AStar astar;

	public Controller(String fileName, View view) {
		this.view = view;
		model = new Model();
		ArrayList<Domain> domains = model.readFileAndAnalyze(fileName);
		model.generateConstraints();
		view.initializeStage(domains);
		
		gac = new GAC();
		astar = new AStar(model);
		initializeSearchState(domains);
		new Thread() {
			public void run() {
				while(!model.openIsEmpty()) {
					SearchNode nextNode = model.popFromOpen();
					model.addClosedNode(nextNode);
					updateView(nextNode);
					gac.reRun(nextNode);
					if(model.isGoal(nextNode)) {
						System.out.println("Mål");
						updateView(nextNode);
						return;
					}
					if(model.isPossibleSolution(nextNode)) {
						astar.search(nextNode);
					}
				}
				System.out.println("Found no solutions");
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
	
	public void initializeSearchState(ArrayList<Domain> domains) {
		SearchNode initialNode = new SearchNode(domains);
		initialNode.setCost(0);
		initialNode.setHeuristics(model.evaluateHeuristic(initialNode));
		initialNode.setScore(initialNode.getHeuristics());
		gac.initialization(initialNode);
		gac.domainFiltering(initialNode);
		model.addClosedNode(initialNode);
		astar.search(initialNode);
	}
}
