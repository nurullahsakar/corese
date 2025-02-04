package fr.inria.corese.core;

import fr.inria.corese.core.edge.*;
import fr.inria.corese.core.edge.binary.*;
import fr.inria.corese.core.edge.internal.*;
import fr.inria.corese.core.edge.rule.*;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.logic.Entailment;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.NSManager;
import java.util.List;
import fr.inria.corese.kgram.api.core.Edge;
import java.util.ArrayList;

/*
 * Factory creates Edges
 * Specific Named Graph have specific Edge Class
 * where graph node and/or predicate node are not replicated in each Edge
 * @author Olivier Corby, INRIA 2011-2016
 *
 */
public class EdgeFactory {
    public static int std = 0, def = 0, rul = 0, ent = 0, typ = 0, sub = 0, fst = 0, rst = 0;
    static final String METADATA = NSManager.USER + "metadata";
    public static boolean trace = false;
    Graph graph;
    QueryProcess exec;
    boolean isOptim = false,
            isTag = false,
            isGraph = false;
    int count = 0;
    String key;
    private boolean optimize = true;

    public EdgeFactory(Graph g) {
        graph = g;
        key = hashCode() + ".";
    }

    static public void trace(){
        System.out.println("Typ: " + typ);
        System.out.println("Sub: " + sub); 
        System.out.println("Fst: " + fst); 
        System.out.println("Rst: " + rst); 
        System.out.println();
        System.out.println("Def: " + def);
        System.out.println("Rul: " + rul);
        System.out.println("Ent: " + ent);
        System.out.println("Std: " + std);
        System.out.println("Tot: " + (std+def+rul+ent));        
    }   

    public Edge create(Node source, Node subject, Node predicate, Node object) {
        if (graph.isTuple()){
             return EdgeImpl.create(source, subject, predicate, object);
        }
        else if (graph.isMetadata()) {
             return EdgeImpl.createMetadata(source, subject, predicate, object, metadata());
        }
        else if (optimize) {
            return genCreate(source, subject, predicate, object);
        }
        else {
            return createGeneric(source, subject, predicate, object);
        }
    } 
    
    Node metadata() {
        if (exec == null) {
            exec = QueryProcess.create(Graph.create());
        }
        IDatatype dt = null;
        try {
            dt = exec.funcall(METADATA, new IDatatype[0]);
        } catch (EngineException ex) {
        }
        if (dt == null) {
            dt = defaultMetadata();
        }
        return graph.getNode(dt, true, false);
    }
    
    IDatatype defaultMetadata() {
        return DatatypeMap.newInstance(count++);
    }
         
    public Edge internal(Edge ent){
        if (ent.nbNode() > 2) {
            return ent;
        }
        Edge edge = ent;
        switch (ent.getGraph().getIndex()){
            // rule edge must store an index
            case Graph.RULE_INDEX: return ent;
            
            case Graph.ENTAIL_INDEX:
                edge = EdgeInternalEntail.create(ent);
                break;
                
            case Graph.DEFAULT_INDEX:
                edge = EdgeInternalDefault.create(ent);
                break;

            default: 
                edge =  EdgeInternal.create(ent);
                break;
        }
        edge.setLevel(ent.getLevel());
        return edge;
    }
       
    public EdgeTop createDuplicate(Edge ent) {
        if (ent.nbNode() == 2) {
            return new EdgeGeneric();
        } else {
            EdgeImpl ee = new EdgeImpl();
            ee.setMetadata(graph.isMetadata());
            return ee;
        }
    }
    
    public Edge createGeneric(Node source, Node subject, Node predicate, Node value) {
        return EdgeGeneric.create(source, subject, predicate, value);
    }
                
    public Edge compact(Edge ent){
        switch (ent.getGraph().getIndex()){
            case Graph.RULE_INDEX: 
                Edge edge = EdgeInternalRule.create(ent.getNode(0), ent.getNode(1));
                edge.setLevel(ent.getLevel());
                return edge;
            default: return ent;
        }
    }
    
    /**
     * Specific named graph and specific properties have specific Edge class
     */
    public Edge genCreate(Node source, Node subject, Node predicate, Node value) {
        switch (source.getIndex()) {
            case Graph.RULE_INDEX:
                rul++;
                return ruleCreate(source, subject, predicate, value);
             case Graph.ENTAIL_INDEX:
                ent++;
                return entailCreate(source, subject, predicate, value);    
            case Graph.DEFAULT_INDEX:
                def++;
                return defaultCreate(source, subject, predicate, value);
            default:
                std++;
                return quad(source, subject, predicate, value);
        }
    }
    
    /**
     * Edge for default graph kg:default
     * Named Graph is not stored in Edge
     * Several properties have specific class: rdf:type, rdf:first, rdf:rest, rdfs:subClassOf
     */
    public Edge defaultCreate(Node source, Node subject, Node predicate, Node value) {
        switch (predicate.getIndex()){
            case Graph.TYPE_INDEX: 
                typ++;
                return EdgeBinaryType.create(source, subject, predicate, value);
             case Graph.SUBCLASS_INDEX: 
                sub++;
                return EdgeBinarySubclass.create(source, subject, predicate, value);
            case Graph.LABEL_INDEX: 
                return EdgeBinaryLabel.create(source, subject, predicate, value);
            case Graph.FIRST_INDEX:
                fst++;
                return EdgeBinaryFirst.create(source, subject, predicate, value); 
             case Graph.REST_INDEX:
                rst++;
                return EdgeBinaryRest.create(source, subject, predicate, value);              
             default:
                 return EdgeDefault.create(source, subject, predicate, value);       
        }       
    }
    
    /**
     * Edge for RDFS entailment graph kg:entail
     */
    public Edge entailCreate(Node source, Node subject, Node predicate, Node value) {
        ent++;
        return EdgeEntail.create(source, subject, predicate, value);
    }

    /**
     * Edge for rule graph kg:rule, store an index and a provenance
     * Named Graph is not stored in Edge
     * Several properties have specific class: rdf:type, rdfs:subClassOf
     */    
    public Edge ruleCreate(Node source, Node subject, Node predicate, Node value) {
        switch (predicate.getIndex()){
            case Graph.TYPE_INDEX: 
                typ++;
                return EdgeRuleType.create(source, subject, predicate, value);
             case Graph.SUBCLASS_INDEX: 
                sub++;
                return EdgeRuleSubclass.create(source, subject, predicate, value); 
             default:
                return EdgeRule.create(source, subject, predicate, value);    
        }       
    }

    /**
     * Edge for user named graph
     */
    public Edge quad(Node source, Node subject, Node predicate, Node value) {
            return EdgeQuad.create(source, subject, predicate, value);
   }
    
    public Edge create(Node source, Node predicate, List<Node> list) {
        EdgeImpl ee = EdgeImpl.create(source, predicate, list);
        ee.setMetadata(graph.isMetadata());
        return ee;
    }
    
    public Edge create(Node source, Node subject, Node predicate, Node object, Node node) {
        ArrayList<Node> list = new ArrayList<>();
        list.add(subject);list.add(object); list.add(node);
        return create(source,  predicate, list);
   }
    
    public Edge name(Edge edge) {
        return name(edge, graph.addTripleName());
    }
    
    public Edge name(Edge edge, Node name) {
        if (edge.nbNode() == 3) {
            edge.setNode(2, name);
            return edge;
        }
        return name(edge, edge.getEdgeNode(), name);
    }
    
     public Edge name(Edge edge, Node predicate, Node name) {
        if (edge.nbNode() == 3) {
            edge.setNode(2, name);
            return edge;
        }
        return create(edge.getGraph(), edge.getNode(0), predicate, edge.getNode(1), name);
    }
    
    public Edge copy(Node node, Node pred, Edge ent) {
        Edge edge;
        if (ent instanceof EdgeImpl) {
            edge = ((EdgeImpl)ent).copy();
            edge.setGraph(node);
        }  
        else {
            edge = create(node, ent.getNode(0), pred,  ent.getNode(1));
        }
        edge.setLevel(ent.getLevel());
        return edge;
    }
    
    public Edge copy(Edge ent){
        return copy(ent.getGraph(), ent.getEdgeNode(), ent);
    }
    
    
    public Edge queryEdge(Edge ent){
        if (graph.isMetadata()) {
            return createGeneric(ent.getGraph(), ent.getNode(0), ent.getEdgeNode(), ent.getNode(1));
        }
        return copy(ent.getGraph(), ent.getEdgeNode(), ent);
    }

    /**
     * Piece of code specific to EdgeImpl
     */
    public void setGraph(Edge ent, Node g) {
        ent.setGraph(g);
//        if (ent instanceof EdgeTop) {
//            EdgeTop e = (EdgeTop) ent;
//            e.setGraph(g);
//        }
 
    }
    
    Edge tag(Edge ent) {
        if (ent instanceof EdgeImpl) {
            EdgeImpl ee = (EdgeImpl) ent;
            ee.setTag(graph.tag());
        }
        return ent;
    }

    public Edge createDelete(Node source, Node subject, Node predicate, Node value) {
        return EdgeQuad.create(source, subject, predicate, value);
    }

    /**
     * Tuple
     */
    public Edge createDelete(Node source, Node predicate, List<Node> list) {
        return create(source, predicate, list);
    }
    
    
    boolean hasTag() {
        return graph.hasTag();
    }

    boolean hasTime() {
        return false;
    }

    // not with rules
    public void setGraph(boolean b) {
        isGraph = b;
    }

    Entailment proxy() {
        return graph.getProxy();
    }

    // with time stamp
    public Edge timeCreate(Node source, Node subject, Node predicate, Node value) {
        Node time = graph.getNode(DatatypeMap.newDate(), true, true);
        Edge edge = new EdgeImpl(source, predicate, subject, value, time);
        return edge;
    }
}
