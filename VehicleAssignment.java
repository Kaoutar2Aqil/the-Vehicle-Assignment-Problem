package Code;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class VehicleAssignment {

    private int[][] costs;          // Matrice des coûts d'affectation
    private int nbTasks;            // Nombre de tâches
    private int nbVehicles;         // Nombre de véhicules
    private IloCplex model;         // Modèle CPLEX
    private IloNumVar[][] x;        // Variables de décision pour l'affectation (x[i][j] == 1 si tâche i est assignée au véhicule j)

    // Constructeur qui initialise les paramètres du problème
    public VehicleAssignment(int[][] costs, int nbVehicles) throws IloException {
        this.costs = costs;
        this.nbVehicles = nbVehicles;
        this.nbTasks = costs.length;
        model = new IloCplex();

        x = new IloNumVar[nbTasks][];
        for (int i = 0; i < nbTasks; i++) {
            x[i] = new IloNumVar[nbVehicles];
        }
        
        createModel();
    }

    // Création du modèle de programmation linéaire en nombre entier
    private void createModel() throws IloException {
        createVariables();
        createConstraints();
        createObjectiveFunction();
    }

    // Définition de la fonction objectif pour minimiser les coûts
    private void createObjectiveFunction() throws IloException {
        IloLinearNumExpr objective = model.linearNumExpr();
        for (int i = 0; i < nbTasks; i++) {
            for (int j = 0; j < nbVehicles; j++) {
                objective.addTerm(costs[i][j], x[i][j]);
            }
        }
        model.addMinimize(objective);
    }

    // Définition des variables binaires pour l'affectation
    private void createVariables() throws IloException {
        for (int i = 0; i < nbTasks; i++) {
            x[i] = model.boolVarArray(nbVehicles); // x[i][j] == 1 si tâche i est assignée au véhicule j
        }
    }

    // Définition des contraintes
    private void createConstraints() throws IloException {
        // Chaque tâche doit être affectée à exactement un véhicule
        for (int i = 0; i < nbTasks; i++) {
            IloLinearNumExpr taskConstraint = model.linearNumExpr();
            for (int j = 0; j < nbVehicles; j++) {
                taskConstraint.addTerm(1, x[i][j]);
            }
            model.addEq(taskConstraint, 1);
        }
    }

    // Méthode pour résoudre le problème d'affectation
    public void solveProblem() throws IloException {
        if (model.solve()) {
            System.out.println("Solution trouvée avec un coût total minimal : " + model.getObjValue());
            for (int i = 0; i < nbTasks; i++) {
                for (int j = 0; j < nbVehicles; j++) {
                    if (model.getValue(x[i][j]) > 0.5) {
                        System.out.println("Tâche " + (i + 1) + " est assignée au véhicule " + (j + 1));
                    }
                }
            }
        } else {
            System.err.println("Aucune solution trouvée.");
        }
    }

    // Programme principal pour tester le modèle
    public static void main(String[] args) throws IloException {
        int[][] costs = {
            {10, 20, 30},
            {15, 25, 35},
            {30, 20, 10},
            {40, 30, 20}
        };
        int nbVehicles = 3;
        
        VehicleAssignment vehicleAssignment = new VehicleAssignment(costs, nbVehicles);
        vehicleAssignment.solveProblem();
    }
}
