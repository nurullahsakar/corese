package fr.inria.corese.sparql.api;

import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;
import fr.inria.corese.kgram.api.core.DatatypeValue;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.Pointerable;
import fr.inria.corese.kgram.api.core.Loopable;
import fr.inria.corese.kgram.api.core.PointerType;
import fr.inria.corese.kgram.api.core.TripleStore;
import fr.inria.corese.sparql.storage.api.IStorage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Interface for Corese XSD datatypes
 * 
 * @author Olivier Corby & Olivier Savoie & Virginie Bottollier
 */
public interface IDatatype
        extends Iterable<IDatatype>,  Node, Loopable, DatatypeValue, Comparable {

    static final int VALUE = -1;
    static final int RESULT = -2;
    // use case: cast
    static final int UNDEFINED = -1;
    static final int LITERAL = 0;
    static final int STRING = 1;
    static final int XMLLITERAL = 2;
    static final int NUMBER = 3;
    static final int DATE = 4;
    static final int BOOLEAN = 5;
    static final int STRINGABLE = 6;
    static final int URI = 7;
    static final int UNDEF = 8;
    static final int BLANK = 9;
    static final int DOUBLE = 11;
    static final int FLOAT = 12;
    static final int DECIMAL = 13;
    static final int LONG = 14;
    static final int INTEGER = 15;
    static final int URI_LITERAL = 16;

    // Pseudo codes (target is Integer or String ...)
    static final int DAY = 21;
    static final int MONTH = 22;
    static final int YEAR = 23;
    static final int DURATION = 24;
    static final int DATETIME = 25;
    static final int GENERIC_INTEGER = 26;

    static final String KGRAM = ExpType.KGRAM;
    public static final String RULE = KGRAM + "Rule";
    public static final String QUERY = KGRAM + "Query";
    public static final String GRAPH = KGRAM + "Graph";
    public static final String MAPPINGS = KGRAM + "Mappings";

    public static final String ENTITY_DATATYPE = ExpType.DT + "entity";
    public static final String RESOURCE_DATATYPE = ExpType.DT + "resource";
    public static final String URI_DATATYPE = ExpType.DT + "uri";
    public static final String BNODE_DATATYPE = ExpType.DT + "bnode";
    public static final String LITERAL_DATATYPE = ExpType.DT + "literal";
    public static final String STANDARD_DATATYPE = ExpType.DT + "standard";
    public static final String EXTENDED_DATATYPE = ExpType.DT + "extended";
    public static final String ERROR_DATATYPE = ExpType.DT + "error";

    public static final String GRAPH_DATATYPE    = ExpType.DT + "graph";    // same as PointerType.GRAPH.name; 
    public static final String MAPPINGS_DATATYPE = ExpType.DT + "mappings"; // same as PointerType.MAPPINGS.name;  

    public static final String ITERATE_DATATYPE = ExpType.DT + "iterate";
    public static final String MAP_DATATYPE     = ExpType.DT + "map";
    public static final String LIST_DATATYPE = ExpType.DT + "list";
    public static final String JSON_DATATYPE = ExpType.DT + "json";
    public static final String XML_DATATYPE = ExpType.DT + "xml";
    public static final String SYSTEM = ExpType.DT + "system";

    IDatatype copy();
    
    boolean isSkolem();

    boolean isXMLLiteral();

    @Override
    boolean isUndefined();

    boolean isGeneralized(); // isExtension or isUndefined

    boolean isArray();

    boolean isList();

    boolean isMap();

    boolean isJSON();

    default boolean isXML() {
        return false;
    }

    boolean isLoop();

    default boolean isDateElement() {
        return false;
    }

    List<IDatatype> getValues();

    @Override
    List<IDatatype> getValueList();

    @Override
    IDatatype getValue(String var, int n);

    IDatatype toList();

    IDatatypeList getList();

    default Map<IDatatype, IDatatype> getMap() {
        return null;
    }

    default IDatatype member(IDatatype elem) {
        return null;
    }

    @Override
    Iterable getLoop();

    IDatatype has(IDatatype dt);

    IDatatype get(int n);

    IDatatype get(IDatatype name);
    
    // xml to json
    default IDatatype json() {
        return null;
    }
    
    // json pointer
    default IDatatype path(IDatatype list) {
        return null;
    }
    
    default IDatatype path(IDatatype list, int n) {
        return null;
    }

    IDatatype set(IDatatype name, IDatatype value);

    @Override
    int size();

    IDatatype length();

    @Override
    boolean isBlank();

    @Override
    boolean isLiteral();

    @Override
    boolean isURI();

    boolean conform(IDatatype dt);

    IDatatype isWellFormed();

    IDatatype isBlankNode();

    IDatatype isLiteralNode();

    IDatatype isURINode();

    @Override
    boolean isFuture();

    boolean isPointer();

    @Override
    boolean isExtension();

    @Override
    PointerType pointerType();

    @Override
    Pointerable getPointerObject();
    
    void setTripleStore(TripleStore store);

    /**
     * Compare 2 datatypes
     *
     * @param dt2 another datatype
     * @return 0 if they are equals, an int > 0 if the datatype is greater than
     * dt2, an int < 0 if the datatype is lesser
     */
    int compareTo(IDatatype dt);

    // for TreeMap
    int mapCompareTo(IDatatype dt);

    int compare(IDatatype dt) throws CoreseDatatypeException;

    /**
     * Cast a value
     *
     * @param datatype ex: xsd:integer
     * @return the datatype casted
     */
    //IDatatype cast(IDatatype target, IDatatype javaType);
    IDatatype cast(IDatatype datatype);

    IDatatype cast(String datatype);

    /**
     * @return the lang as a datatype
     */
    IDatatype getDataLang();

    /**
     * @return the Sparql form of the datatype
     */
    String toSparql();

    String toSparql(boolean prefix);

    String toSparql(boolean prefix, boolean xsd);
    
    default String trace() {
        return String.format("trace: %s code: %s datatype: %s label: %s", 
                this,
                getCode(),
                getDatatypeURI(),
                getLabel());
    }

    // Used by XMLLiteral to store a XML DOM 
    @Override
    void setObject(Object obj);

    @Override
    Object getObject();

    IDatatype getPublicDatatypeValue();

    IDatatype setPublicDatatypeValue(IDatatype dt);

    String getContent();

    IDatatype display();

    void setVariable(boolean b);

    void setValue(int n);

    void setValue(BigDecimal n);

    /**
     * ************************************************************************
     */
    /**
     * test if this.getLowerCaseLabel() contains iod.getLowerCaseLabel()
     *
     * @param iod the instance to be tested with
     * @return this.getLowerCaseLabel() contains iod.getLowerCaseLabel()
     */
    boolean contains(IDatatype iod);

    /**
     * test if this.getLowerCaseLabel() starts with iod.getLowerCaseLabel()
     *
     * @param iod the instance to be tested with
     * @return this.getLowerCaseLabel() starts with iod.getLowerCaseLabel()
     */
    boolean startsWith(IDatatype iod);

    /**
     * test the equality (by value) between two instances of datatype class
     *
     * @param iod the instance to be tested with this
     * @return true if the param has the same runtime class and if values are
     * equals, else false note: equals correponds to the SPARQL equals, with
     * type checking
     */
    boolean equalsWE(IDatatype iod) throws CoreseDatatypeException;

    /**
     * test the equality (by value) between two instances of datatype class
     *
     * @param iod the instance to be tested with this
     * @return true if the param has the same runtime class and if values are
     * equals, else false
     */
    boolean sameTerm(IDatatype iod);

    /**
     *
     * @param iod
     * @return iod.getValue() < this.getValue() @throws Core seDatatypeException
     */
    boolean less(IDatatype iod) throws CoreseDatatypeException;

    /**
     *
     * @param iod
     * @return iod.getValue() <= to this.getValue() @throws CoreseDa
     * tatypeException
     */
    boolean lessOrEqual(IDatatype iod)
            throws CoreseDatatypeException;

    /**
     *
     * @param iod
     * @return iod.getValue() > this.getValue()
     * @throws CoreseDatatypeException
     */
    boolean greater(IDatatype iod) throws CoreseDatatypeException;

    /**
     *
     * @param iod
     * @return iod.getValue() >= to this.getValue()
     * @throws CoreseDatatypeException
     */
    boolean greaterOrEqual(IDatatype iod)
            throws CoreseDatatypeException;

    IDatatype eq(IDatatype dt);

    IDatatype ne(IDatatype dt);

    IDatatype ge(IDatatype dt);

    IDatatype gt(IDatatype dt);

    IDatatype lt(IDatatype dt);

    IDatatype le(IDatatype dt);

    IDatatype neq(IDatatype dt);

    IDatatype plus(IDatatype dt);

    IDatatype minus(IDatatype dt);

    IDatatype mult(IDatatype dt);

    IDatatype divis(IDatatype dt);

    IDatatype div(IDatatype dt);

    IDatatype minus(long val);

    /**
     * ************************************************************************
     */
    /**
     * @return the datatype of this
     */
    IDatatype datatype();

    IDatatype getDatatype();

    // IDatatype value of Pointer Object (eg XML TEXT Node as xsd:string)
    IDatatype getObjectDatatypeValue();

    // same as getDatatype but URI return rdfs:Resource
    IDatatype getIDatatype();

    /**
     * @return the lang of this ('fr', 'en',...)
     */
    @Override
    String getLang();

    /**
     * @return the datatype of this as a URI
     */
    @Override
    String getDatatypeURI();

    /**
     * @return the string depending on the datatype
     * <br>representing the value of this
     */
    @Override
    String getLabel();

    String getID();

    StringBuilder getStringBuilder();

    void setStringBuilder(StringBuilder s);

    /**
     * @return true if this instance class is a number
     */
    @Override
    boolean isNumber();

    boolean isDecimalInteger();

    boolean isDate();

    @Override
    boolean isBoolean();

    Class getJavaClass();

    /**
     * ************************************************
     */
    @Deprecated
    double getDoubleValue();

    @Deprecated
    int getIntegerValue();

    @Deprecated
    int getiValue();

    @Deprecated
    double getdValue();

    @Deprecated
    String getNormalizedLabel();

    @Deprecated
    IDatatype getExtDatatype();

    @Deprecated
    String getLowerCaseLabel();

    int getCode();

    boolean semiEquals(IDatatype iod); // get rid of @ lang

    boolean hasLang();

    boolean isTrue() throws CoreseDatatypeException;

    boolean isTrueAble();

    void setBlank(boolean b);

    void setDatatype(String uri);

    void setValue(String str) throws CoreseDatatypeException;

    void setValue(String str, int id, IStorage pmgr);

    void setValue(IDatatype dt);

    void setLang(String str);

    long getlValue();
}
