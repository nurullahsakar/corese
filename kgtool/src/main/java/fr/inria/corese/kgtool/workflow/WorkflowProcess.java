/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.Dataset;
import static fr.inria.corese.kgtool.workflow.WorkflowParser.DEBUG;
import static fr.inria.corese.kgtool.workflow.WorkflowParser.DISPLAY;
import static fr.inria.corese.kgtool.workflow.WorkflowParser.MODE;
import static fr.inria.corese.kgtool.workflow.WorkflowParser.NAME;
import static fr.inria.corese.kgtool.workflow.WorkflowParser.RESULT;
import static fr.inria.corese.kgtool.workflow.WorkflowParser.COLLECT;
import fr.inria.edelweiss.kgraph.core.Graph;
import java.util.List;

/**
 * Root class
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class WorkflowProcess implements AbstractProcess {
    private Context context;
    private Dataset dataset;
    private Data data;
    private Graph graph;
    // Direct Embedding SemanticWorkflow
    private SemanticWorkflow workflow;
    private WorkflowVisitor visitor;
    private boolean debug = false;
    // true means return input graph (use case: select where and return graph as is)
    private boolean probe = false;
    private boolean display = false;
    private boolean collect = false;
    private boolean visit = false;
    private boolean log = false; 
    private String result, uri, name;
    private IDatatype mode;
    String path;
    
    @Override
    public String toString(){
        return getClass().getName();
    }
    
     
    public Data compute(Data d) throws EngineException {
        before(d);
        start(d);
        Data res = run(d);
        finish(res);
        after(d, res);
        return res;
    } 
     
    public Data run(Data d) throws EngineException {
        return d;
    }
       
     
    private void before(Data data) {
        initContextData(data);
        beforeDebug(data);
        if (recVisitor() != null){
            recVisitor().before(this, data);
        }
    }
    
    void start(Data d){
       
    }
    
    private void after(Data in, Data data) {
        if (in.getVisitor() != null && data.getVisitor() == null){
            data.setVisitor(in.getVisitor());
        }
        afterDebug(data);
        if (recVisitor() != null){
            recVisitor().after(this, data);
        }
    }
    
    void finish(Data d){
        
    }
    
    public boolean isVisitable(boolean b){
         if (isVisit() == b){
            setVisit(!b);
            return true;
        }
        return false;
    }
    
    public void init(boolean b){
        if (isVisitable(b)){
            initialize();
        }
    }
    
    /**
     * Performed recursively before running process()
     * May initialize Context ...
     * */
    public void initialize(){
       
    }
    
    /**
     * Context contain input Data
     * @param data 
     */
    void initContextData(Data data){
        if (getContext() == null){
            setContext(new Context());
        }
        data.initContext(getContext());
    }
    

    void beforeDebug(Data data) {
        if (isRecDebug()) {
            System.out.println("SW: " + getURI() + " " + getClass().getName());           
        } 
    }

    void afterDebug(Data data) {
        if (isRecDebug()) {
            System.out.println(data);
        }
    }
     
    public List<WorkflowProcess> getProcessList(){
        return null;
    }

    @Override
    public void subscribe(SemanticWorkflow w) {
        setWorkflow(w);
    }

    @Override
    public String stringValue(Data data) {
        return "Data";
    }
    
    /**
     * @return the context
     */
    public Context getContext() {
        return context;
    }
    
    Context getCreateContext(){
        if (getContext() == null){
            setContext(new Context());
        }
        return getContext();
    }

    /**
     * @param context the context to set
     */
    @Override
    public void setContext(Context context) {
        this.context = context;
    }
    
    // Process inherit workflow Context and Dataset (if any)
    void inherit(WorkflowProcess p) {
       inherit(p.getContext());
       inherit(p.getDataset());
    }
    
    @Override
    public void inherit(Context context) {
        if (context != null) {
            if (getContext() == null) {
                setContext(context);
            } else {
                // inherit exported properties
                getContext().complete(context);
            }
        }
    }
    
    @Override
    public void inherit(Dataset dataset) {
        if (dataset != null) {
            setDataset(dataset);
        }
    }


    /**
     * @return the data
     */
    public Data getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(Data data) {
        this.data = data;
    }
    
    void collect(Data data){
        if (isRecCollect()){
            setData(data);
        }
    }
    
    boolean isRecCollect(){
        return isCollect() || (pgetWorkflow().isCollect());
    }
     
    boolean isRecDisplay(){
        return isDisplay() || pgetWorkflow().isDisplay() ;
    }   
    
    boolean isRecDebug(){
        return isDebug() || pgetWorkflow().isDebug();
    }
    
    WorkflowVisitor recVisitor(){
        return pgetWorkflow().getVisitor();
    }

     
    WorkflowProcess pgetWorkflow() {
        if (workflow != null){
            return workflow;
        }
        return this;
    } 

    /**
     * @return the workflow
     */
    public SemanticWorkflow getWorkflow() {
        return workflow;
    }

    /**
     * @param workflow the workflow to set
     */
    public void setWorkflow(SemanticWorkflow workflow) {
        this.workflow = workflow;
    }

    /**
     * @return the debug
     */
    @Override
    public boolean isDebug() {
        return debug;
    }
    
    /**
     * @param debug the debug to set
     */
    @Override
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * @return the dataset
     */
    public Dataset getDataset() {
        return dataset;
    }

    /**
     * @param dataset the dataset to set
     */
    @Override
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }
    
    @Override
    public boolean isTransformation() {
        return false;
    }
    
    boolean isTemplate() {
        return false;
    }
    
    boolean isShape(){
        return true;
    }
    
    boolean isModify(){
        return ! isTransformation();
    }

    /**
     * @return the probe
     */
    public boolean isProbe() {
        return probe;
    }

    /**
     * @param probe the probe to set
     */
    @Override
    public void setProbe(boolean probe) {
        this.probe = probe;
    }

    /**
     * @return the display
     */
    @Override
    public boolean isDisplay() {
        return display;
    }

    /**
     * @param display the display to set
     */
    @Override
    public void setDisplay(boolean display) {
        this.display = display;
    }
    
     public void setRecDisplay(boolean display) {
        this.display = display;
    }

    @Override
    public void setResult(String r) {
        result = r;
    }

    /**
     * @return the result
     */
    public String getResult() {
        return result;
    }

    /**
     * @return the uri
     */
    public String getURI() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    @Override
    public void setURI(String uri) {
        this.uri = uri;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the graph
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * @param graph the graph to set
     */
    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    /**
     * @return the mode
     */
    public IDatatype getMode() {
        return mode;
    }
    
    public String getModeString(){
        if (mode == null){
            return null;
        }
        return mode.getLabel();
    }

    /**
     * @param mode the mode to set
     */
    public void setMode(IDatatype mode) {
        this.mode = mode;
    }
    
    
    void set(String name, IDatatype dt){
        if (name.equals(NAME)){
            setName(dt.stringValue());
        }
        else if (name.equals(DEBUG)){
            setDebug(dt.booleanValue());        
        }
        else  if (name.equals(DISPLAY)){
            setDisplay(dt.booleanValue());        
        }
        else if (name.equals(RESULT)){
            setResult(dt.getLabel());        
        }
        else if (name.equals(MODE)){
            setMode(dt);        
        } 
        else if (name.equals(COLLECT)){
            setCollect(dt.booleanValue());
        }
    }

    /**
     * @return the collect
     */
    public boolean isCollect() {
        return collect;
    }

    /**
     * @param collect the collect to set
     */
    public void setCollect(boolean collect) {
        this.collect = collect;
    }

    /**
     * @return the visit
     */
    public boolean isVisit() {
        return visit;
    }

    /**
     * @param visit the visit to set
     */
    public void setVisit(boolean visit) {
        this.visit = visit;
    }
    
     /**
     * @return the visitor
     */
    public WorkflowVisitor getVisitor() {
        return visitor;
    }

    /**
     * @param visitor the visitor to set
     */
    public void setVisitor(WorkflowVisitor visitor) {
        this.visitor = visitor;
    }  
    
       /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getTransformation(){
        return null;
    }

    /**
     * @return the log
     */
    public boolean isLog() {
        return log;
    }

    /**
     * @param log the log to set
     */
    public void setLog(boolean log) {
        this.log = log;
    }

}
