package module3;

import java.util.ArrayList;

import javax.script.Invocable;
import javax.script.ScriptException;

public class GAC {
	
	private ArrayList<Domain> queue = new ArrayList<Domain>();
	
	public GAC() {
	}
	
	/**
	 * Initialize queue with all constraints
	 */
	public void initialization(SearchNode node) {
		queue.addAll(node.getDomains());
	}
	
	/**
	 * Pop constraint from queue and revise constraint, repeat until queue is empty
	 */
	public void domainFiltering(SearchNode node) {
		while(!queue.isEmpty()) {
			Domain nextToRevise = queue.remove(0);
			if(!revise(nextToRevise, node)) {
				//If a domain turns up empty, we empty queue and try new node
				queue = new ArrayList<Domain>();
			}
		}
	}
	
	/**
	 * Add constraints of all variables affected by assumption made about variableWithAssumption to queue
	 * and run domainFiltering with queue
	 * @param variableDomainWithAssumption
	 */
	public void reRun(SearchNode node) {
		addAffectedDomainsToQueue(node);
		domainFiltering(node);
	}
	
	/**
	 * Adds all variables affected by changes in variable's domain to the queue
	 * to be revised
	 * @param domain
	 */
	public void addAffectedDomainsToQueue(SearchNode node) {
		Domain domain = node.getLastVariableDomainToAssumeValue();
		//Store all constraints this variable is participating in
		ArrayList<Constraint> constraints = domain.getVariable().getParticipatingInConstraints();
		//For each constraint add all participating variables
		//to an array of affected variables
		ArrayList<Variable> affectedVariables = getAffectedVariables(constraints);
		queue.addAll(getAffectedDomains(affectedVariables, node));		
	}
	
	public ArrayList<Domain> getAffectedDomains(ArrayList<Variable> variables, SearchNode node) {
		ArrayList<Domain> affectedDomains = new ArrayList<Domain>();
		ArrayList<Domain> allDomains = node.getDomains();
		//For each variable
		for(Variable v:variables) {
			affectedDomains.add(getDomainOfVariable(v, allDomains));
		}
		return affectedDomains;
	}
	
	public Domain getDomainOfVariable(Variable variable, ArrayList<Domain> domains) {
		//Go through all domains to find variable's corresponding domain
		for(Domain d:domains) {
			//If variable equals domain's variable reference
			if(variable.equals(d.getVariable())) {
				//add to array of affected domains
				return d;
			}
		}
		//Should not happen
		return null;
	}
	
	public ArrayList<Variable> getAffectedVariables(ArrayList<Constraint> constraints) {
		ArrayList<Variable> affectedVariables = new ArrayList<Variable>();
		for(Constraint constraint:constraints) {
			ArrayList<Variable> temp = new ArrayList<Variable>();
			temp.addAll(constraint.getParticipatingVariables());
			//Remove index 0 because it is the variable the assumption is made about
			temp.remove(0);
			affectedVariables.addAll(temp);
		}
		return affectedVariables;
	}
	
	/**
	 * Controls all constraints the variable participates in to see if domain values are
	 * satisfiable. If value is not satisfiable, it is removed and affected variables
	 * are added to the queue to be revised
	 * @param domain
	 */
	public boolean revise(Domain domain, SearchNode node) {
		//Get all constraints this variable participates in
		ArrayList<Constraint> participatingInConstraints = domain.getVariable().getParticipatingInConstraints();
		//Get all domains of this node
		ArrayList<Domain> domains = node.getDomains();
		//Get domain of this variable
		ArrayList<Integer> domainValues = domain.getDomainValues();
		ArrayList<Integer> domainValuesToBeRemoved = new ArrayList<Integer>();
		//Control each value in domain
		for(int domainValue:domainValues) {
			//Need to keep track of how many constraints that are satisfied
			//Endpoints only need one flow connector, the rest needs two ("one in, one out")
			int numberOfSatisfiedConstraints = 0;
			for(Constraint constraint:participatingInConstraints) {
				//Get code chunk of constraint
				Invocable function = constraint.getConstraint();
				//Get all participating variables, or arguments, of this constraint's code chunk
				ArrayList<Variable> participatingVariables = new ArrayList<Variable>();
				participatingVariables.addAll(constraint.getParticipatingVariables());
				//Get domain of participating variable
				ArrayList<Integer> participatingVariableDomain = getDomainOfVariable(participatingVariables.get(1), domains).getDomainValues();
				//Assume domainValue does not satisfy constraint
				boolean satisfiable = false;
				//Try to find a satisfiable value in participating variables domain
				for(int participatingDomainValue:participatingVariableDomain) {
					ArrayList<Integer> arguments = new ArrayList<Integer>();
					arguments.add(domainValue);
					arguments.add(participatingDomainValue);
					try {
						//Invoke the function with variables as argument
						if((boolean) function.invokeFunction("constraint", arguments)) {
							//This domain value is satisfiable, break for-loop
							satisfiable = true;
							break;
						}
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (ScriptException e) {
						e.printStackTrace();
					}
				}
				if(satisfiable) {
					numberOfSatisfiedConstraints++;
					if(numberOfSatisfiedConstraints>2 || (domain.getVariable().isEndpoint() && numberOfSatisfiedConstraints>0)) {
						break;
					}
				}
			}
			if(domain.getVariable().isEndpoint()) {
				//If variable is endpoint, we need at least one constraint to be
				//satisfiable, i.e. one neighbour with same color in domain
				if(numberOfSatisfiedConstraints==0) {
					//Not satisfiable, set domain value to be removed
					domainValuesToBeRemoved.add(domainValue);
				}
			} else {
				//If variable isn't endpoint, we need at least two constraints to be
				//satisfiable, i.e. two neighbours with same color in domain
				if(numberOfSatisfiedConstraints<2) {
					//Not satisfiable, set domain value to be removed
					domainValuesToBeRemoved.add(domainValue);
				}
			}
		}
		//If we need to reduce domain, do so and add affected variables to queue
		if(!domainValuesToBeRemoved.isEmpty()) {
			domainValues.removeAll(domainValuesToBeRemoved);
			if(domainValues.isEmpty()) {
				//Return true to indicate this is no possible solution
				return false;
			}
			//Set last domain to change value to easier find dependent variables
			//to add to domain later
			node.setLastVariableDomainToAssumeValue(domain);
			addAffectedDomainsToQueue(node);
		}
		return true;
	}

}
