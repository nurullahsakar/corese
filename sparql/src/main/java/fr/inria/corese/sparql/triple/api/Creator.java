package fr.inria.corese.sparql.triple.api;

import java.util.List;

import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.RDFList;

/**
* 
* Interface to create edges from Turtle parser
* @author Olivier Corby, INRIA 2012
* 
*/
 public interface Creator {
	
	 boolean accept(Atom subject, Atom predicate, Atom object);
        
         void start();
	
         void finish();
         
	 void triple(Atom subject, Atom predicate, Atom object);
	
	 void triple(Atom predicate, List<Atom> l);

	 void graph(Atom graph);
	
	 void endGraph(Atom graph);

	 void list(RDFList l);
	
	 void setRenameBlankNode(boolean b);

	 boolean isRenameBlankNode();

         void setLimit(int limit);
}
