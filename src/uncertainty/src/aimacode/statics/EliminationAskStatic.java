package aimacode.statics;

import aima.core.probability.CategoricalDistribution;
import aima.core.probability.Factor;
import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.FiniteNode;
import aima.core.probability.bayes.Node;
import aima.core.probability.bayes.exact.EliminationAsk;
import aima.core.probability.bayes.impl.BayesNet;
import aima.core.probability.proposition.AbstractTermProposition;
import aima.core.probability.proposition.AssignmentProposition;
import aima.core.probability.util.ProbabilityTable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Estensione della Variable Elimination con, in aggiunta, l'implementazione di diversi ordinamenti
 */
public class EliminationAskStatic extends EliminationAsk {
    private static final ProbabilityTable _identity = new ProbabilityTable(new double[]{1.0});
    final public static String TOPOLOGICAL = "topological";
    final public static String MIN_DEGREE = "mindegree";
    final public static String MIN_FILL = "minfill";

    final private String ordering;

    public EliminationAskStatic(String ordering) {
        this.ordering = ordering;
    }

    /**
     * Esegue l'algoritmo di Variable Elimination prestando attenzione all'ordine con cui si effettuano le sumOut
     */
    public CategoricalDistribution eliminationAsk(final RandomVariable[] X,
                                                  final AssignmentProposition[] e, final BayesianNetwork bn) {

        Set<RandomVariable> hidden = new HashSet<>();
        List<RandomVariable> VARS = new ArrayList<>();
        calculateVariables(X, e, bn, hidden, VARS);

        List<RandomVariable> evidenceVars = Arrays.stream(e).map(AbstractTermProposition::getTermVariable).collect(Collectors.toList());
        List<RandomVariable> queryVars = Arrays.stream(X).collect(Collectors.toList());
        List<RandomVariable> toSumOut = new ArrayList<>();

        // factors <- []
        List<Factor> factors = new ArrayList<>();
        for (RandomVariable var : order(bn, VARS)) {
            // factors <- [MAKE-FACTOR(var, e) | factors]
            Factor factor = makeFactor(var, e, bn);
            factors.add(0, factor);
            // se non è variabile di query o di evidenza
            if (!queryVars.contains(var) && !evidenceVars.contains(var)) {
                toSumOut.add(var);
            }
        }

        for (RandomVariable var : toSumOut) {
            factors = sumOut(var, factors, bn);
        }

        // return NORMALIZE(POINTWISE-PRODUCT(factors))
        Factor product = pointwiseProduct(factors);
        // Note: Want to ensure the order of the product matches the
        // query variables
        return ((ProbabilityTable) product.pointwiseProductPOS(_identity, X)).normalize();
    }

    /**
     * Ordina una collezione di variabili secondo l'ordinamento specificato in "ordering"
     *
     * @param bn   Rete Bayesiana di appartenenza
     * @param vars Variabili da ordinare
     * @return varibili ordinate
     */
    @Override
    protected List<RandomVariable> order(BayesianNetwork bn, Collection<RandomVariable> vars) {
        if (ordering.equals(TOPOLOGICAL)) {
            List<RandomVariable> order = new ArrayList<>(vars);
            Collections.reverse(order);
            return order;
        }

        List<RandomVariable> ordered = new ArrayList<>();
        Set<Node> nodes = vars.stream().map(bn::getNode).collect(Collectors.toSet());
        InteractionGraph interGraph = new InteractionGraph(nodes);
        int size = vars.size();

        switch (ordering) {
            case MIN_DEGREE:
                for (int i = 0; i < size; i++) {
                    RandomVariable var = interGraph.findMinDegreeVariable();
                    interGraph.updateEdges(var);
                    interGraph.delete(var);
                    ordered.add(var);
                }
                break;
            case MIN_FILL:
                for (int i = 0; i < size; i++) {
                    RandomVariable var = interGraph.findMinFillVariable();
                    interGraph.updateEdges(var);
                    interGraph.delete(var);
                    ordered.add(var);
                }
                break;
        }
        return ordered;
    }

    /*
     *
     * Original Code
     *
     */

    private Factor makeFactor(RandomVariable var, AssignmentProposition[] e,
                              BayesianNetwork bn) {

        Node n = bn.getNode(var);
        if (!(n instanceof FiniteNode)) {
            throw new IllegalArgumentException(
                    "Elimination-Ask only works with finite Nodes.");
        }
        FiniteNode fn = (FiniteNode) n;
        List<AssignmentProposition> evidence = new ArrayList<AssignmentProposition>();
        for (AssignmentProposition ap : e) {
            if (fn.getCPT().contains(ap.getTermVariable())) {
                evidence.add(ap);
            }
        }

        return fn.getCPT().getFactorFor(
                evidence.toArray(new AssignmentProposition[evidence.size()]));
    }

    private List<Factor> sumOut(RandomVariable var, List<Factor> factors,
                                BayesianNetwork bn) {
        List<Factor> summedOutFactors = new ArrayList<Factor>();
        List<Factor> toMultiply = new ArrayList<Factor>();
        for (Factor f : factors) {
            if (f.contains(var)) {
                toMultiply.add(f);
            } else {
                // This factor does not contain the variable
                // so no need to sum out - see AIMA3e pg. 527.
                summedOutFactors.add(f);
            }
        }

        summedOutFactors.add(pointwiseProduct(toMultiply).sumOut(var));

        return summedOutFactors;
    }

    private Factor pointwiseProduct(List<Factor> factors) {

        Factor product = factors.get(0);
        for (int i = 1; i < factors.size(); i++) {
            product = product.pointwiseProduct(factors.get(i));
        }

        return product;
    }
}
