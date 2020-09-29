package fr.inria.corese.kgram.core;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Matcher;
import fr.inria.corese.kgram.api.query.Producer;
import java.util.List;

/**
 *
 * @author corby
 */
public class EvalGraph {
    
    Eval eval;
    
    EvalGraph(Eval e) {
        eval = e;
    }
    
    
    int namedGraph(Producer p, Node gNode, Exp exp, Mappings data, Stack stack, int n) throws SparqlException {
        int backtrack = n - 1;
        Node graphNode = exp.getGraphName();
        Node queryNode = eval.getQuery().getGraphNode();
        Node graph     = eval.getNode(p, graphNode);
        Mappings res;

        if (graph == null) {
            res = graphNodes(p, exp, data, n);
        } else {
            res = graph(p, graph, exp, data, n);
        }

        if (res == null) {
            return backtrack;
        }
        Memory env = eval.getMemory();

        for (Mapping m : res) {
//            if (stop) {
//                return STOP;
//            }            
            
            Node namedGraph = null;
            if (graphNode.isVariable()) {
                namedGraph = m.getNode(graphNode);
                if (namedGraph != null && ! namedGraph.equals(m.getNamedGraph())) {
                    // graph ?g { s p o optional { o q ?g }}
                    // variable ?g bound by exp not equal to named graph variable ?g
                    continue;
                }
            }
            
            if (env.push(m, n)) {
                boolean pop = false;                               
                if (queryNode != null) {
                    env.pop(queryNode);
                }
                if (env.push(graphNode, m.getNamedGraph())) {
                    pop = true;
                } else {
                    env.pop(m);
                    continue;
                }

                backtrack = eval.eval(p, gNode, stack, n + 1);
                env.pop(m);
                if (pop) {
                    env.pop(graphNode);
                }
                if (backtrack < n) {
                    return backtrack;
                }
            }
        }

        return backtrack;
    }

    private Mappings graphNodes(Producer p, Exp exp, Mappings map, int n) throws SparqlException {
        Memory env = eval.getMemory();
        Query qq = eval.getQuery();
        Matcher mm = eval.getMatcher();
        int backtrack = n - 1;
        Node name = exp.getGraphName();
        Mappings res = null;

        Iterable<Node> graphNodes = null;
        if (map != null && map.inScope(name)) {
            List<Node> list = map.aggregate(name);
            if (!list.isEmpty()) {
                graphNodes = list;
            }
        }
        if (graphNodes == null) {
            graphNodes = p.getGraphNodes(name, qq.getFrom(name), env);
        }

        for (Node graph : graphNodes) {
            if (mm.match(name, graph, env)
                    && env.push(name, graph, n)
                    ) {
                Mappings m = graph(p, graph, exp, map, n);
                if (res == null) {
                    res = m;
                } else {
                    res.add(m);
                }
                env.pop(name);
            }
        }
        return res;
    }

    private Mappings graph(Producer p, Node graph, Exp exp, Mappings map, int n) throws SparqlException {
        int backtrack = n - 1;
        boolean external = false;
        Node graphNode = exp.getGraphName();
        Node queryNode = eval.getQuery().getGraphNode();
        Producer np = p;
        if (graph != null && p.isProducer(graph)) {
            // graph ?g { }
            // named graph in GraphStore 
            np = p.getProducer(graph, eval.getMemory());
            np.setGraphNode(graphNode);  // the new gNode
            // use graphNode variable (instead of queryNode ?_kgram_0)
            // use case: graph $path { ... }
            queryNode = null;
        }

        Exp main = exp;
        Exp body = exp.rest();
        Mappings res;
              
        if (eval.isFederate(exp)) {
            res = eval.subEval(np, graphNode, queryNode, body, main, map);
        } 
        else {
            // exp += values(var, map)
            Exp ee = body;
            if (graph.getPath() == null) {
                ee = eval.complete(body, map);
            }
            res = eval.subEval(np, graphNode, queryNode, ee, main);
        }
        res.setNamedGraph(graph);

        eval.getVisitor().graph(eval, graph, exp, res);
        return res;
    }

    
}
