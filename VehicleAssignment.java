package Code;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class VehicleAssignment {

    private int numPassengers;      // Nombre de passagers
    private int numVehicles;        // Nombre de véhicules
    private int[] capacities;       // Capacités des véhicules
    private IloCplex model;         // Modèle CPLEX
    private IloNumVar[][] x;        // Variables de décision pour l'affectation

    // Constructeur qui initialise les paramètres du problème
    public VehicleAssignment(int numPassengers, int numVehicles, int[] capacities) throws IloException {
        this.numPassengers = numPassengers;
        this.numVehicles = numVehicles;
        this.capacities = capacities;
        model = new IloCplex();
        
        x = new IloNumVar[numPassengers][];
        for (int i = 0; i < numPassengers; i++) {
            x[i] = new IloNumVar[numVehicles];
        }

        createModel();
    }

    // Création du modèle de programmation linéaire en nombre entier
    private void createModel() throws IloException {
        createVariables();
        createConstraints();
        createObjectiveFunction();
    }

    // Définition de la fonction objectif pour minimiser le nombre de véhicules utilisés
    private void createObjectiveFunction() throws IloException {
        IloLinearNumExpr objective = model.linearNumExpr();
        // Minimiser le nombre total de véhicules utilisés
        for (int j = 0; j < numVehicles; j++) {
            IloLinearNumExpr vehicleLoad = model.linearNumExpr();
            for (int i = 0; i < numPassengers; i++) {
                vehicleLoad.addTerm(1, x[i][j]);
            }
            // Si un véhicule a au moins un passager, il est utilisé
            IloNumVar vehicleUsed = model.boolVar("vehicleUsed_" + j);
            model.addEq(vehicleUsed, vehicleLoad);
            objective.addTerm(1, vehicleUsed);
        }
        model.addMinimize(objective);
    }

    // Définition des variables binaires pour l'affectation
    private void createVariables() throws IloException {
        for (int i = 0; i < numPassengers; i++) {
            x[i] = model.boolVarArray(numVehicles); // x[i][j] == 1 si passager i est affecté au véhicule j
        }
    }

    // Définition des contraintes
    private void createConstraints() throws IloException {
        // Chaque passager doit être affecté à exactement un véhicule
        for (int i = 0; i < numPassengers; i++) {
            IloLinearNumExpr passengerConstraint = model.linearNumExpr();
            for (int j = 0; j < numVehicles; j++) {
                passengerConstraint.addTerm(1, x[i][j]);
            }
            model.addEq(passengerConstraint, 1);
        }

        // Contrainte sur la capacité des véhicules
        for (int j = 0; j < numVehicles; j++) {
            IloLinearNumExpr vehicleCapacity = model.linearNumExpr();
            for (int i = 0; i < numPassengers; i++) {
                vehicleCapacity.addTerm(1, x[i][j]);
            }
            model.addLe(vehicleCapacity, capacities[j]);
        }
    }

    // Méthode pour résoudre le problème d'affectation
    public void solveProblem() throws IloException {
        if (model.solve()) {
            System.out.println("Solution trouvée avec un nombre total de véhicules utilisés : " + model.getObjValue());
            for (int i = 0; i < numPassengers; i++) {
                for (int j = 0; j < numVehicles; j++) {
                    if (model.getValue(x[i][j]) > 0.5) {
                        System.out.println("Passager " + (i + 1) + " est affecté au véhicule " + (j + 1));
                    }
                }
            }
        } else {
            System.err.println("Aucune solution trouvée.");
        }
    }

    // Programme principal pour tester le modèle
    public static void main(String[] args) throws IloException {
        int numPassengers = 2;  // Réduit pour tester un petit cas
        int numVehicles = 2;    // Nombre réduit de véhicules
        int[] capacities = {2, 2}; // Capacités ajustées des véhicules
        
        VehicleAssignment vehicleAssignment = new VehicleAssignment(numPassengers, numVehicles, capacities);
        vehicleAssignment.solveProblem();
    }
}
