/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework4;

import java.beans.Statement;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.toCollection;

/**
 *
 * @author prati
 */
class KnowledgeBase implements Cloneable{
    ArrayList<Sentence> sentences = new ArrayList<>();
    Sentence query;
    LinkedHashMap<Sentence, ArrayList<Sentence>> solution_set = new LinkedHashMap<>();
    boolean deived_sentence_equal_query = false;
    public void add_sentence(Sentence new_sentence){
        sentences.add(new_sentence);
    }
    
    public void add_sentence(int i, Sentence new_sentence){
        sentences.add(i,new_sentence);
    }
    public void set_query(Sentence given_query){
        query = given_query;
    }
    public void print(){
        for(Sentence s : sentences){
            System.out.print(s.used_for_resolution + " : " );
            Iterator<HashMap.Entry<String, Predicate>> entries = s.predicate_list.entrySet().iterator();
            while (entries.hasNext()) {
                HashMap.Entry<String, Predicate> entry = entries.next();
//                if(entry.getValue().not_operator) System.out.print("~");
//                System.out.print(entry.getKey());
                System.out.print(entry.getKey());
                entry.getValue().print_args();
                System.out.print(entry.getValue().const_arg_num_resolved);
                if(entries.hasNext())
                    System.out.print(" | ");
            }
            System.out.println("");
        }
    }
    
    public void add_inference_step(Sentence s1, Sentence s2, Sentence s3){
        ArrayList<Sentence> as = new ArrayList<>();
        as.add(s1);
        as.add(s2);
        solution_set.put(s3, as);
    } 
    
    public void print_inference(){
        solution_set.forEach((k,v) -> {
            for(Sentence s : v){
               s.print();
               System.out.print("        ");
            }
            System.out.print(" ----> ");
            k.print();
        });
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        KnowledgeBase kb_new = new KnowledgeBase();
        for(Sentence s : this.sentences){
            Sentence new_sen = (Sentence)s.clone();
            Sentence new_query = null;
            if(new_sen.query){
                new_query = new_sen;
                kb_new.set_query(new_query);
            }
            kb_new.add_sentence(new_sen);
        }
        LinkedHashMap<Sentence, ArrayList<Sentence>> new_solution_set = new LinkedHashMap<>();
        solution_set.forEach((k,v) -> {
            ArrayList<Sentence> ar_s = new ArrayList<>();
            for(Sentence s : v){
                try {
                    ar_s.add((Sentence)s.clone());
                } catch (CloneNotSupportedException ex) {
                    Logger.getLogger(KnowledgeBase.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                new_solution_set.put((Sentence)k.clone(), ar_s);
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(KnowledgeBase.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        return kb_new;
    }
}

class Sentence implements Cloneable{
    HashMap<String, Predicate> predicate_list;
    boolean used_for_resolution;
    boolean query; 
    public Sentence(){
        this.predicate_list = new HashMap<>();
        used_for_resolution = false;
        query =  false;
    }
    public void add_pred(Predicate given_pr){
        this.predicate_list.put(given_pr.print_name(), given_pr) ;
    }
    public boolean has_predicate(Predicate query_pr){
        return predicate_list.containsKey(query_pr.print_name());
    }
    
    public Sentence negate() throws CloneNotSupportedException{
        Sentence new_sentence = new Sentence();
        Iterator<HashMap.Entry<String, Predicate>> entries = this.predicate_list.entrySet().iterator();
        while (entries.hasNext()) {
            HashMap.Entry<String, Predicate> entry = entries.next();
            Predicate new_p = (Predicate)((Predicate)entry.getValue()).clone();
            new_p.not_operator = !new_p.not_operator;
            new_p.print_name = new_p.print_name();
            new_sentence.add_pred(new_p);
        }
        return new_sentence;
    }
    public void print(){
        Iterator<HashMap.Entry<String, Predicate>> entries = predicate_list.entrySet().iterator();
        while (entries.hasNext()) {
            HashMap.Entry<String, Predicate> entry = entries.next();
//                if(entry.getValue().not_operator) System.out.print("~");
//                System.out.print(entry.getKey());
//                entry.getValue().print_args();
            System.out.print(entry.getKey());
            entry.getValue().print_args();
            System.out.print(entry.getValue().const_arg_num_resolved);
            if(entries.hasNext())
                System.out.print(" | ");
        }
//        System.out.println("");
    }
    public boolean checkEquality(Sentence target){
        if(this.predicate_list.size() != target.predicate_list.size()){
            return false;
        }
        if(!this.predicate_list.keySet().containsAll(target.predicate_list.keySet())){
            return false;
        }
        Predicate[] preds  = this.predicate_list.values().toArray(new Predicate[this.predicate_list.size()]);
        Predicate[] target_preds  = target.predicate_list.values().toArray(new Predicate[this.predicate_list.size()]);
        for(int i = 0; i < preds.length ; i++ ){
            Predicate new_pred = preds[i];
            Predicate new_pred_target = target_preds[i];
            for(int j = 0; j< new_pred.arguments.size();j++){
//                System.out.println(new_pred.arguments.get(j).name);
//                System.out.println(new_pred_target.arguments.get(j).name);
                if( new_pred.arguments.get(j).getClass().getName().equals("homework4.Constant") && !new_pred.arguments.get(j).name.equals(new_pred_target.arguments.get(j).name)){
                    
                    return false;
                    
                }
            }
        }
        return true;
        
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        Sentence se_new = (Sentence)super.clone();
//        se_new.used_for_resolution = false;
        return se_new;
    }
}

class Predicate implements Cloneable{
    boolean not_operator;
    String name;
    ArrayList<Argument> arguments;
    int const_arg_num_resolved;
    ArrayList<Argument> subtstitute_arguments;
    String print_name;
    public Predicate(String given_name, boolean is_not, ArrayList<Argument> argument_list) throws CloneNotSupportedException{
        not_operator = is_not;
        name = given_name;
        arguments = new ArrayList<>(argument_list);
        subtstitute_arguments = new ArrayList<>();
        for(Argument a : argument_list){
            subtstitute_arguments.add((Argument)a.clone());
        }
        print_name = this.print_name();
        const_arg_num_resolved = 0;
    }
    public String print_name(){
        StringBuilder sb = new StringBuilder("");
        if(this.not_operator) sb.append("~");
        sb.append(name);
//        sb.append("(");
//        Iterator<Argument> argIter = this.arguments.iterator();
//        while(argIter.hasNext()){
//            sb.append(argIter.next().name);
//            if(argIter.hasNext()){
//                sb.append(",");
//            }
//        }
//        sb.append(")");
        return sb.toString();
    }
    public String negated_print_name(){
        String negated_print_name = print_name.charAt(0) == '~' ? print_name.substring(1): "~"+ print_name;
        return negated_print_name;
    }
    public void print_args(){
        System.out.print("(");
        Iterator<Argument> argIter = this.arguments.iterator();
        while(argIter.hasNext()){
            System.out.print(argIter.next().name);
            if(argIter.hasNext()){
                System.out.print(", ");
            }
        }
        System.out.print(")");
    }
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        Predicate pr_new = (Predicate)super.clone();
        ArrayList<Argument> args_new = new ArrayList<>();
        for(Argument a : pr_new.arguments){
            args_new.add((Argument)a.clone());
        }
        pr_new.arguments = args_new;
        
        ArrayList<Argument> args_subst_new = new ArrayList<>();
        for(Argument a : pr_new.subtstitute_arguments){
            args_subst_new.add((Argument)a.clone());
        }
        pr_new.subtstitute_arguments = args_subst_new;
//        pr_new.arguments = (ArrayList<Argument>) arguments.clone();
//        System.out.println("ARGS"+ pr_new.arguments.get(0).hashCode() +" " + arguments.get(0).hashCode() );
        return pr_new;
    }


}

abstract class Argument implements Cloneable{
    String name;
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        Argument pr_new = (Argument)super.clone();
//        System.out.println("ARGS"+ pr_new.arguments.get(0).hashCode() +" " + this.arguments.get(0).hashCode() );
        return pr_new;
    }
}

class Variable extends Argument{
//    String name;
    String value; 
    public Variable(String given_name){
        name = given_name;
    }
}

class Constant extends Argument{
//    String name;
    public Constant(String given_value){
        name = given_value;
    }
}

class ResolutionSet implements Cloneable{
    HashMap<String, Argument> resolution_set = new HashMap<>();
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        HashMap<String, Argument> new_resolution_set = new HashMap<>();
        ResolutionSet rs_new = (ResolutionSet)super.clone();
        Iterator<HashMap.Entry<String, Argument>> entries = this.resolution_set.entrySet().iterator();
        while (entries.hasNext()) {
            HashMap.Entry<String, Argument> entry = entries.next();
            new_resolution_set.put(entry.getKey(), (Argument)entry.getValue().clone());
        }
//        System.out.println("ARGS"+ pr_new.arguments.get(0).hashCode() +" " + this.arguments.get(0).hashCode() );
        return rs_new;
    }
} 

public class Homework4 {

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, CloneNotSupportedException, ClassNotFoundException {
        // TODO code application logic here
        KnowledgeBase KB = new KnowledgeBase();
        KnowledgeBase QB = new KnowledgeBase();
        BufferedReader br =  new BufferedReader(new FileReader("input.txt"));
        HashMap<String, Constant> constants = new HashMap<>();
        HashMap<String, Constant> predicates = new HashMap<>();
        HashMap<String, Argument> argument_table = new HashMap<>();
        int number_of_query = Integer.parseInt(br.readLine());
        for (int i = 0; i < number_of_query; i++) {
            String query = br.readLine();
            Sentence new_sentence = new Sentence();
            new_sentence = add_sentences_to_database(query,argument_table);
            QB.add_sentence(new_sentence);
        }
        int number_of_facts = Integer.parseInt(br.readLine());
        for (int i = 0; i < number_of_facts; i++) {
            String facts = br.readLine();
            Sentence new_sentence = new Sentence();
            new_sentence = add_sentences_to_database(facts,argument_table);
            KB.add_sentence(new_sentence);
        }
        KB.print();
//        QB.print();
        StringBuffer result = new StringBuffer("");
        for(Sentence query : QB.sentences){
            System.out.println("RESOLVE ----------------------------------- FOR : ");
            query.print();
            System.out.println("");
            Sentence negated_query = query.negate();
            negated_query.query = true;
            KnowledgeBase new_KB = (KnowledgeBase)KB.clone();
            new_KB.add_sentence(0,negated_query);
            new_KB.set_query(negated_query);
//            System.out.println(query.hashCode() + " " + negated_query.hashCode() );
            ResolutionSet RS = new ResolutionSet();
            Boolean can_be_infered = resolveQuery(new_KB,RS);
            new_KB.print();
            System.out.println("Inference : ");
            new_KB.print_inference();
            result.append(can_be_infered);
            if(can_be_infered){
                KB.add_sentence(query);
            }
            result.append("\n");
            System.out.println("RESOLVE ----------------END-------------------");
        }
        System.out.println("RESULTSSSSSS : ");
        System.out.println(result);
//        KB.print();
//        argument_table.forEach((k,v) -> System.out.println(k));
    }
    
    public static boolean resolveQuery(KnowledgeBase KB, ResolutionSet RS) throws ClassNotFoundException, CloneNotSupportedException{
//        if there exists some false or sentences checked or some negations
        System.out.println("CALLLLLLLLLLLLLLLLLLLLLLLLLLL TO RESOLVEEEEEE : "+ KB.hashCode());
        
        for(Sentence source : KB.sentences){
//            if(!source.query){
//                if(source.checkEquality(KB.query)){
//                    KB.deived_sentence_equal_query = true;
//                    return true;
//                }
//            }
            for(Sentence resolve_statement : KB.sentences){
                if(source != resolve_statement){
                    if(source.negate().checkEquality(resolve_statement) && KB.query.used_for_resolution){
                        RS = new ResolutionSet();
                        Sentence derived_statement = canBeResolved(source, resolve_statement, RS);
                        if(source == derived_statement){
                            return false;
                        }
                        System.out.println("TRUE FROM EQUALITY");
                        source.print();
                        resolve_statement.print();
                        
                        KB.print_inference();
                        return true;
                    }
                }
            }
        }
        
        
        for(int i =0 ;i< KB.sentences.size();i++){
            Sentence source = KB.sentences.get(i);
            for(int j = 0;j< KB.sentences.size();j++){
                Sentence resolve_statement = KB.sentences.get(j);

                if(source != resolve_statement && !resolve_statement.used_for_resolution && !source.used_for_resolution){
//                    System.out.println("LOOOOP IN RESOVE : "+ KB.hashCode());
                    
                    Sentence derived_statement = new Sentence();

//                    System.out.println("TRYING TO RESOLVE : ");
//                    source.print();
//                    System.out.println("");
//                    resolve_statement.print();
//                    System.out.println("ENDDDDDDDDDDDDDDDDD");

                    RS = new ResolutionSet();
                    derived_statement = canBeResolved(source, resolve_statement, RS);
                    if(source != derived_statement){

//                        System.out.println("SENTENCE : ");
//                        source.print() ;
//                        System.out.print("CAN BE RESOLVED WITH : "); 
//                        resolve_statement.print();
//                        System.out.println("");

                        source.used_for_resolution = true;
                        resolve_statement.used_for_resolution = true;
                        KB.add_inference_step(source, resolve_statement, derived_statement);
//                        if(derived_statement.checkEquality(KB.query)){
//                            KB.deived_sentence_equal_query = true;
//                            return false;
//                        }
                        if(derived_statement.predicate_list.isEmpty() && KB.query.used_for_resolution){
                            System.out.println("TRUE FROM EMPTY");
                            return true;
                        }
                        else{
                            KnowledgeBase new_kb = new KnowledgeBase();
                            Sentence new_query =  null;
                            for(Sentence s : KB.sentences){
                                Sentence new_sen = (Sentence)s.clone();
                                if(new_sen.query){
                                    new_query = new_sen;
                                    new_kb.set_query(new_query);
                                }
                                new_kb.add_sentence(new_sen);
                            }
                            
                            new_kb.add_sentence(0,derived_statement);
                            
                            System.out.println("KB TO CALL :");
                            new_kb.print();
                            
//                            RS = new ResolutionSet();
                            if(resolveQuery(new_kb, RS)){
                                new_kb.print_inference();
//                                KB = (KnowledgeBase)new_kb.clone();
//                                KB.deived_sentence_equal_query = new_kb.deived_sentence_equal_query;
                                System.out.println("TRUE FROM RECURSION");
//                                System.out.println("KB TO CALL  FROM RETURN :");
//                                new_kb.print();
                                return true;
                            }else{
//                                if(new_kb.deived_sentence_equal_query){
//                                    return false;
//                                }
                                source.used_for_resolution = false;
                                resolve_statement.used_for_resolution = false;
                                KB.solution_set.remove(derived_statement);
                            }    
                        }
                    }
                }
            }
            return false;
        }
        System.out.println("FALSE FROM ENDDDDDDDDDD");
        return false;
    }
    
    public static Sentence canBeResolved(Sentence source,Sentence resolve_statement,ResolutionSet RS) throws ClassNotFoundException, CloneNotSupportedException{
        // COMBINE LIST LATERRRRRRRRRRRRRRRR
        boolean resolved = false;
        Sentence new_sentence = new Sentence();
        Predicate pre_to_remove = null;
        Predicate pre_to_remove_res = null;
        final Predicate pred_r, pred_re_s;
//        int var_count_s_p  = 0;
        int var_count_r_s  = 0;
        for(Predicate s_p : source.predicate_list.values()){
            if(resolve_statement.predicate_list.containsKey(s_p.negated_print_name())){
                pre_to_remove = source.predicate_list.get(s_p.print_name);
                pre_to_remove_res = resolve_statement.predicate_list.get(s_p.negated_print_name());
                
                // Check for similar constants ahead A(x,y) and ~A(z,x) it should not unify. 
                for(int i = 0; i < s_p.arguments.size(); i++){
                    Argument s_p_arg = s_p.arguments.get(i); 
                    for(int j = i + 1; j < s_p.arguments.size(); j++){
                        Argument r_s_arg = resolve_statement.predicate_list.get(s_p.negated_print_name()).arguments.get(j); 
                        if(s_p_arg.name.equals(r_s_arg.name)){
                            System.out.println(" FALSEEEE NOT RESOLVE FROM AHEAD");
                            return source;
                        }
                    }
                }
                
                
                for(int i = 0; i < s_p.arguments.size(); i++){
                    Argument s_p_arg = s_p.arguments.get(i); 
                    Argument r_s_arg = resolve_statement.predicate_list.get(s_p.negated_print_name()).arguments.get(i); 
                    
                    // FALSE FOR BOTH CONSTANTS IF DIFFRENT
                    if((r_s_arg.getClass().getName().equals("homework4.Constant")) && (s_p_arg.getClass().equals(r_s_arg.getClass())) && (!s_p_arg.name.equals(r_s_arg.name)))
                    {
                        return source;
                    }
                    
                   //BOTH ARE VARIABLES AND  
                    if((r_s_arg.getClass().getName().equals("homework4.Variable")) && 
                       (s_p_arg.getClass().equals(r_s_arg.getClass()))){ 
//                        var_count_r_s += 1;
//                        if(true || ( !s_p_arg.name.equals(r_s_arg.name)))
//                        {
                            RS.resolution_set.put(s_p_arg.name, r_s_arg);
                            resolved = true;
//                        }
                    }
                    else if((!s_p_arg.getClass().equals(r_s_arg.getClass()))||( s_p_arg.name.equals(r_s_arg.name))){
                        String key = r_s_arg.getClass().getName().equals("homework4.Variable") ? r_s_arg.name : s_p_arg.name;
                        Argument value = r_s_arg.getClass().getName().equals("homework4.Constant") ? r_s_arg : s_p_arg;
                        
                        source.print() ;
                        System.out.print("  CAN BE RESOLVED WITH : FOR " + s_p_arg.name + " & " + r_s_arg.name); 
                        resolve_statement.print();
                        System.out.println("");

                        RS.resolution_set.put(key, value);
                        resolved = true;
                    }
                    
                }
                
//                if(var_count_r_s == pre_to_remove_res.arguments.size()){
//                    return source;
//                }
            }
        }
        pred_r = pre_to_remove;
        pred_re_s = pre_to_remove_res;
        source.predicate_list.forEach((k,v) -> {
            try {
                if(v != pred_r)
                    new_sentence.predicate_list.put(k,(Predicate)v.clone());
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(Homework4.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        resolve_statement.predicate_list.forEach((k,v) -> {
            try {
                if(v != pred_re_s)
                    new_sentence.predicate_list.put(k,(Predicate)v.clone());
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(Homework4.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        if(resolved){
            new_sentence.predicate_list.forEach((k,v) -> {
                ArrayList<Argument> new_args =  new ArrayList<>();
                v.arguments.forEach((arg) -> {
                    if(RS.resolution_set.containsKey(arg.name)){
                        new_args.add(RS.resolution_set.get(arg.name));
                    }
                    else{
                        new_args.add(arg);
                    }
                });
                v.arguments = new_args;
            });
            
            
            source.predicate_list.forEach((k,v) -> {
                v.arguments.forEach((arg) -> {
                    if(RS.resolution_set.containsKey(arg.name)){
                       Argument ar = RS.resolution_set.get(arg.name);
                       if(ar.getClass().getName().equals("homework4.Constant")){
                           v.const_arg_num_resolved += 1; 
                       }else{
                           if(arg.getClass().getName().equals("homework4.Constant")){
                               v.const_arg_num_resolved += 1;
                           }
                       }
                    }
                });
            });
            resolve_statement.predicate_list.forEach((k,v) -> {
                v.arguments.forEach((arg) -> {
                    if(RS.resolution_set.containsKey(arg.name)){
                        Argument ar = RS.resolution_set.get(arg.name);
                       if(ar.getClass().getName().equals("homework4.Constant")){
                           v.const_arg_num_resolved += 1; 
                       }else{
                           if(arg.getClass().getName().equals("homework4.Constant")){
                               v.const_arg_num_resolved += 1;
                           }
                       }
                       
                    }
                });
            });


            System.out.println("NEW SENTENCE : " );
            new_sentence.print();
            System.out.println("");
            System.out.println("-----------------------");
            return new_sentence;
        }
        else{
            System.out.println("RETURNNNNNN FALSEEEEEEEEEEEEEEEEEE");
            return source;
        }
    }
    
    public static Sentence add_sentences_to_database(String input_line, HashMap<String, Argument> argument_table) throws CloneNotSupportedException{
        String query = input_line;
        String[] atomic_sentences = query.split("\\|");
        Sentence new_sentence = new Sentence();
        
        for(String predi : atomic_sentences){
            predi = predi.trim();
            ArrayList<Argument> arg_list = new ArrayList<>();
            String[] components = predi.split("\\(");
            for(String s: components){
//                System.out.println("COMPNONT " + s);
            }
            boolean predicate_type = components[0].substring(0,1).equals("~");
            String predicate_name = predicate_type ? components[0].substring(1) : components[0];
//            System.out.println("PRED :" + components[0]);
            String[] argument_list =  components[1].replace(")", "").trim().split(",");
//                if(!predicates.containsKey(predicate_name)){
//                    Predicate new_pred =   new Predicate(predicate_name, predicate_type);
//                    predicates.put(predicate_name,new_pred);
//                }
            for(String arg : argument_list){
                if(argument_table.containsKey(arg)){
                    arg_list.add(argument_table.get(arg));
                }
                else{
                    if(arg.length() == 1 && Character.isLowerCase(arg.charAt(0))){
                        Variable new_var = new Variable(arg);
                        argument_table.put(arg, new_var);
                        arg_list.add(new_var);
                    }
                    else{
                        Constant new_const = new Constant(arg);
                        argument_table.put(arg, new_const);
                        arg_list.add(new_const);
                    }
                }    
            }
            Predicate new_pred =   new Predicate(predicate_name, predicate_type, arg_list);
            new_sentence.add_pred(new_pred);
        }//For Each Predicate
        return  new_sentence;
    }
    
    static boolean check_validity_of_solution(KnowledgeBase KB){
        for(Sentence s : KB.sentences){
            if(s.used_for_resolution){
                Iterator<HashMap.Entry<String, Predicate>> entries = s.predicate_list.entrySet().iterator();
                while (entries.hasNext()) {
                    HashMap.Entry<String, Predicate> entry = entries.next();
//                    for(Argument a : entry.getValue().subtstitute_arguments){
//                        if(!a.getClass().getName().equals("homework4.Constant")){
//                            System.out.println("NOT CONSTANT : " + a.name);
//                            return false;
//                        }
//                    }
//                    if(entry.getValue().arguments.size() != entry.getValue().const_arg_num_resolved){
//                        return false;
//                    }
                }
            }
        }
        
        return true;
    }
    
}
