package module3;
import java.util.ArrayList;

public class AStar {
	
	private Model model;
	
	public AStar(Model model) {
		this.model = model;
	}
	
	/**
	 * Use argument node as starting point for A*-search and returns goal node.
	 * @param start
	 * @return
	 */
	public void search(SearchNode node) {
		//Generate successor SearchNodes
		ArrayList<SearchNode> successors = model.generateSuccessors(node);
		if(successors == null) {
			return;
		}
		//Check status of successor SearchNodes
		for(SearchNode child : successors) {
			//Expanded it before?
			int indexClosed = model.indexOfNodeInClosed(child);
			if(indexClosed!=-1) {
				//Throw away newly generated SearchNode and replace with our previously generated SearchNode for this state
				child = model.getFromClosed(indexClosed);
			}
			int indexOpen = model.indexOfNodeInOpen(child);
			if(indexOpen!=-1) {
				//Throw away newly generated SearchNode and replace with our previously generated SearchNode for this state
				child = model.getFromOpen(indexOpen);
			}
			//Add SearchNode to current's list of children
			node.addChild(child);
			//If we have not discovered SearchNode before
			if(indexOpen==-1 && indexClosed==-1) {
				//Set best parent and find score
				attachAndEvaluate(node, child);
				//Add to open set for later expansion
				model.addOpenNode(child);
				//Sort list by score to get correct order
				model.sortOpenByScore();
			}
			//If discovered before but new path is cheaper
			else if(node.getCost() + model.getArcCost() < child.getCost()) {
				//Set best parent and update score
				attachAndEvaluate(node, child);
				//If this node has been expanded we need to propagate changes
				if(indexClosed!=-1) {
					//Propagate score improvement to children of SearchNode, and if true we need to sort open again
					if(propagateImprovement(child)) {
						model.sortOpenByScore();
					}
				}
			}

		}
	}
	
	
	
	
	/**
	 * Checks all the children to see if their cost can be improved by changing parent to this node
	 * @param node
	 * @return
	 */
	public boolean propagateImprovement(SearchNode node) {
		boolean openNodesAffected = false;
		ArrayList<SearchNode> children = node.getChildren();
		//Temp variable for cost of potentially cheaper path, the +1 signifies arc cost from node to child
		double costToCompare = node.getCost() + model.getArcCost();
		for(SearchNode child:children) {
			//set new parent and cost if new path is cheaper
			if(child.getCost()>costToCompare) {
				attachAndEvaluate(node, child);
				if(propagateImprovement(child)) {
					openNodesAffected = true;
				}
				if(model.indexOfNodeInOpen(node)!=-1) {
					openNodesAffected = true;
				}
			}
		}
		return openNodesAffected;
	}
	
	/**
	*Set new best parent (current) as this SearchNodes new parent. Update its score accordingly
	*/
	public void attachAndEvaluate(SearchNode parent, SearchNode node) {
		//Set new parent
		node.setParent(parent);
		//Find and set cost to reach this SearchNode (+1 from parent SearchNode)
		node.setCost(parent.getCost() + model.getArcCost());
		//If heuristic not evaluated before (new node)
		if(node.getHeuristics()==-1) {
			node.setHeuristics(model.evaluateHeuristic(node));
		}
		//Evaluate SearchNode score
		node.setScore(node.getCost() + node.getHeuristics());
	}
}