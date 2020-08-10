package cz.vse.fcp.demo.controller;


import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

import java.util.HashSet;
import java.util.Iterator;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;

//update start 10/8/2020
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
//update End 10/8/2020

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.search.EntitySearcher;

import uk.ac.manchester.cs.jfact.JFactFactory;

/**
 * Created by Ondřej Zamazal on 2016-2020.
 */

public class FCP {

    protected OWLOntology ontology;
    protected OWLOntologyManager manager;
    protected OWLDataFactory factory;
    Map<String,Object> data = new HashMap<>();

    //get categorization options for each ontology
    //first there is black list generation
    //then categorization options detection itself
    //onto is the URI of ontology, imports either true (including imports) or false (without imports)
    public Map getCategorizationOptions(String onto, boolean imports) {
        try {
            //init for each ontology
            this.manager = OWLManager.createOWLOntologyManager();

            //update start 10/8/2020

            this.manager.getOntologyConfigurator().setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
            this.manager.getOntologyConfigurator().setConnectionTimeout(500);
            //09-08-20, START silent missing imports handling
            //this.manager.getOntologyConfigurator().setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
            //09-08-20, END silent missing imports handling

            //update End 10/8/2020

            this.ontology=this.manager.loadOntology(IRI.create(onto));
            this.factory=manager.getOWLDataFactory();


            //System.out.println(this.ontology.getClassesInSignature().size());

            //the black list
            //black list construction strategy
            //the combination (anchor class, property, thing)
            //this is created from the cases where the filler Thing
            //the combination (anchor class, property, filler)
            //this is created from the cases where the filler is the class or instance
            //there is always the combination (anchor class, property) added to the black list for pruning
            HashSet<String> ACpropertyFillerBlist = new HashSet<String>();

            //19-12-19, creation class expressions corresponding to cases v2,v3,v4 (p1,p2,p3) for checking whether they are not already counted in v1 (p1) - kind of experiment implemention
            HashSet<OWLClassExpression> all_created_class_expressions = null;

            //use the reasoner
            this.factory= manager.getOWLDataFactory();
            //since 2019 I use JFact reasoner
            OWLReasonerFactory reasonerFactory = new JFactFactory();
            OWLReasonerConfiguration config = new SimpleConfiguration(5000);
            OWLReasoner reasoner = reasonerFactory.createReasoner(ontology, config);
            //System.out.println("to precompute inferences...");
            reasoner.precomputeInferences();

            //1. black list generation
            //iteration over all classes either with imports Imports.INCLUDED or without imports Imports.EXCLUDED
            Imports considerImport;
            if(imports)
                considerImport=Imports.INCLUDED;
            else
                considerImport=Imports.EXCLUDED;

            //update start 10/8/2020

            //for(OWLClass cls : this.ontology.getClassesInSignature(considerImport)) {
            //10-08-20, Java8 for stream
            Iterator<OWLClass> iter = this.ontology.classesInSignature(considerImport).iterator();
            while(iter.hasNext()) {
                OWLClass cls = iter.next();

            //update end 10/8/2020


                //the iteration over all partial definitions and checking of different types of anonymous expressions


                //update start 10/8/2020
                //EntitySearcher.getSuperClasses(cls, this.ontology).forEach((OWLClassExpression exp) -> {
                    //System.out.println("   "+cls+" rdfs:subClassOf:"+exp);

                //10-08-20, Java8 for stream
                Iterator<OWLClassExpression> iter1 = EntitySearcher.getSuperClasses(cls, this.ontology).iterator();

                while(iter1.hasNext()) {
                    OWLClassExpression exp = iter1.next();



                    //update end 10/8/2020





                    if(exp.isAnonymous()) {
                        if (exp instanceof OWLObjectSomeValuesFrom) {
                            String p = ((OWLObjectSomeValuesFrom) exp).getProperty().toString();

                            ACpropertyFillerBlist.add(cls.toString()+";"+p.toString()+";"+((OWLObjectSomeValuesFrom) exp).getFiller());
                            ACpropertyFillerBlist.add(cls.toString()+";"+p.toString());

                            //start..................................

                            //for(Node<OWLClass> n : reasoner.getSubClasses(cls, false).getNodes()) {
                            Iterator<Node<OWLClass>> it1 = reasoner.getSubClasses(cls, false).nodes().iterator();

                            while(it1.hasNext()) {
                                Node<OWLClass> n = it1.next();

                            //end...........................................

                                for(OWLClass cls1 : n.getEntitiesMinusTop()) {
                                    //the propagation to the subclasses
                                    if(!cls1.isBottomEntity()) {
                                        ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString()+";"+((OWLObjectSomeValuesFrom) exp).getFiller());
                                        ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString());
                                    }
                                }
                            }
                        }

                        if (exp instanceof OWLDataSomeValuesFrom) {
                            String p = ((OWLDataSomeValuesFrom) exp).getProperty().toString();
                            ACpropertyFillerBlist.add(cls.toString()+";"+p.toString());

                            //start.........................................
                            //for(Node<OWLClass> n : reasoner.getSubClasses(cls, false).getNodes()) {
                            Iterator<Node<OWLClass>> it2 = reasoner.getSubClasses(cls, false).nodes().iterator();

                            while(it2.hasNext()) {
                                Node<OWLClass> n = it2.next();

                            //end.................................................


                                for(OWLClass cls1 : n.getEntitiesMinusTop()) {
                                    //the propagation to the subclasses
                                    if(!cls1.isBottomEntity()) {
                                        ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString());
                                    }
                                }
                            }
                        }

                        if (exp instanceof OWLObjectHasValue) {
                            String p = ((OWLObjectHasValue) exp).getProperty().toString();
                            ACpropertyFillerBlist.add(cls.toString()+";"+p.toString()+";"+((OWLObjectHasValue) exp).getFiller());
                            ACpropertyFillerBlist.add(cls.toString()+";"+p.toString());

                            //start.............................................................

                            //for(Node<OWLClass> n : reasoner.getSubClasses(cls, false).getNodes()) {
                            Iterator<Node<OWLClass>> it3 = reasoner.getSubClasses(cls, false).nodes().iterator();

                            while(it3.hasNext()) {
                                Node<OWLClass> n = it3.next();


                            //end..................................................................

                                for(OWLClass cls1 : n.getEntitiesMinusTop()) {
                                    //the propagation to the subclasses
                                    if(!cls1.isBottomEntity()) {
                                        ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString()+";"+((OWLObjectHasValue) exp).getFiller());
                                        ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString());
                                    }
                                }
                            }
                        }

                        if (exp instanceof OWLDataHasValue) {
                            String p = ((OWLDataHasValue) exp).getProperty().toString();
                            ACpropertyFillerBlist.add(cls.toString()+";"+p.toString());

                            //start...................................

                            //for(Node<OWLClass> n : reasoner.getSubClasses(cls, false).getNodes()) {
                            Iterator<Node<OWLClass>> it4 = reasoner.getSubClasses(cls, false).nodes().iterator();

                            while(it4.hasNext()) {

                                Node<OWLClass> n = it4.next();


                            //end.............................................


                                for(OWLClass cls1 : n.getEntitiesMinusTop()) {
                                    //the propagation to the subclasses
                                    if(!cls1.isBottomEntity()) {
                                        ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString());
                                    }
                                }
                            }
                        }

                        if (exp instanceof OWLObjectHasSelf) {
                            String p = ((OWLObjectHasSelf) exp).getProperty().toString();
                            ACpropertyFillerBlist.add(cls.toString()+";"+p.toString());

                            //start...............................................

                            //for(Node<OWLClass> n : reasoner.getSubClasses(cls, false).getNodes()) {

                            Iterator<Node<OWLClass>> it5 = reasoner.getSubClasses(cls, false).nodes().iterator();

                            while(it5.hasNext()) {
                                Node<OWLClass> n = it5.next();

                            //end..................................................

                                for(OWLClass cls1 : n.getEntitiesMinusTop()) {
                                    //the propagation to the subclasses
                                    if(!cls1.isBottomEntity()) {
                                        ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString());
                                    }
                                }
                            }
                        }

                        if (exp instanceof OWLObjectMinCardinality) {
                            if(((OWLObjectMinCardinality) exp).getCardinality()>=0) {
                                String p = ((OWLObjectMinCardinality) exp).getProperty().toString();
                                ACpropertyFillerBlist.add(cls.toString()+";"+p.toString()+";"+((OWLObjectMinCardinality) exp).getFiller());
                                ACpropertyFillerBlist.add(cls.toString()+";"+p.toString());

                                //start..............................

                                //for(Node<OWLClass> n : reasoner.getSubClasses(cls, false).getNodes()) {

                                Iterator<Node<OWLClass>> it6 = reasoner.getSubClasses(cls, false).nodes().iterator();

                                while(it6.hasNext()) {
                                    Node<OWLClass> n = it6.next();


                                //end
                                    for(OWLClass cls1 : n.getEntitiesMinusTop()) {
                                        //the propagation to the subclasses
                                        if(!cls1.isBottomEntity()) {
                                            ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString()+";"+((OWLObjectMinCardinality) exp).getFiller());
                                            ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString());
                                        }
                                    }
                                }
                            }
                        }

                        if (exp instanceof OWLDataMinCardinality) {
                            if(((OWLDataMinCardinality) exp).getCardinality()>=0) {
                                String p = ((OWLDataMinCardinality) exp).getProperty().toString();
                                ACpropertyFillerBlist.add(cls.toString()+";"+p.toString());

                                //start...................................

                                //for(Node<OWLClass> n : reasoner.getSubClasses(cls, false).getNodes()) {
                                Iterator<Node<OWLClass>> it7 = reasoner.getSubClasses(cls, false).nodes().iterator();

                                while(it7.hasNext()) {
                                    Node<OWLClass> n = it7.next();


                                //end.......................................

                                    for(OWLClass cls1 : n.getEntitiesMinusTop()) {
                                        //the propagation to the subclasses
                                        if(!cls1.isBottomEntity()) {
                                            ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString());
                                        }
                                    }
                                }
                            }
                        }

                        if (exp instanceof OWLObjectMaxCardinality) {
                            if(((OWLObjectMaxCardinality) exp).getCardinality()>=0) {
                                String p = ((OWLObjectMaxCardinality) exp).getProperty().toString();
                                ACpropertyFillerBlist.add(cls.toString()+";"+p.toString()+";"+((OWLObjectMaxCardinality) exp).getFiller());
                                ACpropertyFillerBlist.add(cls.toString()+";"+p.toString());

                                //sart....................................
                                //for(Node<OWLClass> n : reasoner.getSubClasses(cls, false).getNodes()) {

                                Iterator<Node<OWLClass>> it8 = reasoner.getSubClasses(cls, false).nodes().iterator();

                                while(it8.hasNext()) {
                                    Node<OWLClass> n = it8.next();


                                //end...............................................
                                    for(OWLClass cls1 : n.getEntitiesMinusTop()) {
                                        //the propagation to the subclasses
                                        if(!cls1.isBottomEntity()) {
                                            ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString()+";"+((OWLObjectMaxCardinality) exp).getFiller());
                                            ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString());
                                        }
                                    }
                                }
                            }
                        }

                        if (exp instanceof OWLDataMaxCardinality) {
                            if(((OWLDataMaxCardinality) exp).getCardinality()>=0) {
                                String p = ((OWLDataMaxCardinality) exp).getProperty().toString();
                                ACpropertyFillerBlist.add(cls.toString()+";"+p.toString());

                                //start.................................
                                //for(Node<OWLClass> n : reasoner.getSubClasses(cls, false).getNodes()) {
                                Iterator<Node<OWLClass>> it9 = reasoner.getSubClasses(cls, false).nodes().iterator();

                                while(it9.hasNext()) {
                                    Node<OWLClass> n = it9.next();


                                    //end............................................

                                    for(OWLClass cls1 : n.getEntitiesMinusTop()) {
                                        //the propagation to the subclasses
                                        if(!cls1.isBottomEntity()) {
                                            ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString());
                                        }
                                    }
                                }
                            }
                        }

                        if (exp instanceof OWLObjectExactCardinality) {
                            if(((OWLObjectExactCardinality) exp).getCardinality()>=0) {
                                String p = ((OWLObjectExactCardinality) exp).getProperty().toString();
                                ACpropertyFillerBlist.add(cls.toString()+";"+p.toString()+";"+((OWLObjectExactCardinality) exp).getFiller());
                                ACpropertyFillerBlist.add(cls.toString()+";"+p.toString());

                                //start.............................
                                //for(Node<OWLClass> n : reasoner.getSubClasses(cls, false).getNodes()) {
                                Iterator<Node<OWLClass>> it10 = reasoner.getSubClasses(cls, false).nodes().iterator();

                                while(it10.hasNext()) {
                                    Node<OWLClass> n = it10.next();

                                //end.................................

                                    for(OWLClass cls1 : n.getEntitiesMinusTop()) {
                                        //the propagation to the subclasses
                                        if(!cls1.isBottomEntity()) {
                                            ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString()+";"+((OWLObjectExactCardinality) exp).getFiller());
                                            ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString());
                                        }
                                    }
                                }
                            }
                        }

                        if (exp instanceof OWLDataExactCardinality) {
                            if(((OWLDataExactCardinality) exp).getCardinality()>=0) {
                                String p = ((OWLDataExactCardinality) exp).getProperty().toString();
                                ACpropertyFillerBlist.add(cls.toString()+";"+p.toString());

                                //start........................
                                //for(Node<OWLClass> n : reasoner.getSubClasses(cls, false).getNodes()) {
                                Iterator<Node<OWLClass>> it11 = reasoner.getSubClasses(cls, false).nodes().iterator();

                                while(it11.hasNext()) {
                                    Node<OWLClass> n = it11.next();


                                    //end......................


                                    for(OWLClass cls1 : n.getEntitiesMinusTop()) {
                                        //the propagation to the subclasses
                                        if(!cls1.isBottomEntity()) {
                                            ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString());
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
                //the iteration over all complete definitions
//                for(OWLClassExpression exp : EntitySearcher.getEquivalentClasses(cls, this.ontology)) {

                //start........................


                //EntitySearcher.getEquivalentClasses(cls, this.ontology).forEach((OWLClassExpression exp) -> {
                Iterator<OWLClassExpression> iter2 = EntitySearcher.getEquivalentClasses(cls, this.ontology).iterator();

                while(iter2.hasNext()) {
                    OWLClassExpression exp = iter2.next();




                    //end.............................

                    if(exp.isAnonymous()) {

                        if (exp instanceof OWLObjectSomeValuesFrom) {
                            String p = ((OWLObjectSomeValuesFrom) exp).getProperty().toString();
                            ACpropertyFillerBlist.add(cls.toString()+";"+p.toString()+";"+((OWLObjectSomeValuesFrom) exp).getFiller());
                            ACpropertyFillerBlist.add(cls.toString()+";"+p.toString());

                            //start.............................

                            //for(Node<OWLClass> n : reasoner.getSubClasses(cls, false).getNodes()) {

                            Iterator<Node<OWLClass>> it1 = reasoner.getSubClasses(cls, false).nodes().iterator();

                            while(it1.hasNext()) {
                                Node<OWLClass> n = it1.next();

                            //end............................


                                for(OWLClass cls1 : n.getEntitiesMinusTop()) {
                                    //the propagation to the subclasses
                                    if(!cls1.isBottomEntity()) {
                                        ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString()+";"+((OWLObjectSomeValuesFrom) exp).getFiller());
                                        ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString());
                                    }
                                }
                            }
                        }

                        if (exp instanceof OWLDataSomeValuesFrom) {
                            String p = ((OWLDataSomeValuesFrom) exp).getProperty().toString();
                            ACpropertyFillerBlist.add(cls.toString()+";"+p.toString());

                            //start......................
                            //for(Node<OWLClass> n : reasoner.getSubClasses(cls, false).getNodes()) {

                            Iterator<Node<OWLClass>> it2 = reasoner.getSubClasses(cls, false).nodes().iterator();

                            while(it2.hasNext()) {
                                Node<OWLClass> n = it2.next();

                            //end................................
                                for(OWLClass cls1 : n.getEntitiesMinusTop()) {
                                    //the propagation to the subclasses
                                    if(!cls1.isBottomEntity()) {
                                        ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString());
                                    }
                                }
                            }
                        }

                        if (exp instanceof OWLObjectHasValue) {
                            String p = ((OWLObjectHasValue) exp).getProperty().toString();
                            ACpropertyFillerBlist.add(cls.toString()+";"+p.toString()+";"+((OWLObjectHasValue) exp).getFiller());
                            ACpropertyFillerBlist.add(cls.toString()+";"+p.toString());

                            //start....................

                            //for(Node<OWLClass> n : reasoner.getSubClasses(cls, false).getNodes()) {
                            Iterator<Node<OWLClass>> it3 = reasoner.getSubClasses(cls, false).nodes().iterator();

                            while(it3.hasNext()) {
                                Node<OWLClass> n = it3.next();


                            //end..............................

                                for(OWLClass cls1 : n.getEntitiesMinusTop()) {
                                    //the propagation to the subclasses
                                    if(!cls1.isBottomEntity()) {
                                        ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString()+";"+((OWLObjectHasValue) exp).getFiller());
                                        ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString());
                                    }
                                }
                            }
                        }

                        if (exp instanceof OWLDataHasValue) {
                            String p = ((OWLDataHasValue) exp).getProperty().toString();
                            ACpropertyFillerBlist.add(cls.toString()+";"+p.toString());

                            //start.......................................

                            //for(Node<OWLClass> n : reasoner.getSubClasses(cls, false).getNodes()) {
                            Iterator<Node<OWLClass>> it4 = reasoner.getSubClasses(cls, false).nodes().iterator();

                            while(it4.hasNext()) {
                                Node<OWLClass> n = it4.next();

                            //end..................................................

                                for(OWLClass cls1 : n.getEntitiesMinusTop()) {
                                    //the propagation to the subclasses
                                    if(!cls1.isBottomEntity()) {
                                        ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString());
                                    }
                                }
                            }
                        }

                        if (exp instanceof OWLObjectHasSelf) {
                            String p = ((OWLObjectHasSelf) exp).getProperty().toString();
                            ACpropertyFillerBlist.add(cls.toString()+";"+p.toString());

                            //start.................

                            //for(Node<OWLClass> n : reasoner.getSubClasses(cls, false).getNodes()) {
                            Iterator<Node<OWLClass>> it5 = reasoner.getSubClasses(cls, false).nodes().iterator();

                            while(it5.hasNext()) {
                                Node<OWLClass> n = it5.next();

                            //end........................

                                for(OWLClass cls1 : n.getEntitiesMinusTop()) {
                                    //the propagation to the subclasses
                                    if(!cls1.isBottomEntity()) {
                                        ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString());
                                    }
                                }
                            }
                        }

                        if (exp instanceof OWLObjectMinCardinality) {
                            if(((OWLObjectMinCardinality) exp).getCardinality()>=0) {
                                String p = ((OWLObjectMinCardinality) exp).getProperty().toString();
                                ACpropertyFillerBlist.add(cls.toString()+";"+p.toString()+";"+((OWLObjectMinCardinality) exp).getFiller());
                                ACpropertyFillerBlist.add(cls.toString()+";"+p.toString());

                                //start..................................

                                //for(Node<OWLClass> n : reasoner.getSubClasses(cls, false).getNodes()) {
                                Iterator<Node<OWLClass>> it6 = reasoner.getSubClasses(cls, false).nodes().iterator();

                                while(it6.hasNext()) {
                                    Node<OWLClass> n = it6.next();

                                //end....................................

                                    for(OWLClass cls1 : n.getEntitiesMinusTop()) {
                                        //the propagation to the subclasses
                                        if(!cls1.isBottomEntity()) {
                                            ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString()+";"+((OWLObjectMinCardinality) exp).getFiller());
                                            ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString());
                                        }
                                    }
                                }
                            }
                        }

                        if (exp instanceof OWLDataMinCardinality) {
                            if(((OWLDataMinCardinality) exp).getCardinality()>=0) {
                                String p = ((OWLDataMinCardinality) exp).getProperty().toString();
                                ACpropertyFillerBlist.add(cls.toString()+";"+p.toString());

                                //start..............................

                                //for(Node<OWLClass> n : reasoner.getSubClasses(cls, false).getNodes()) {

                                Iterator<Node<OWLClass>> it7 = reasoner.getSubClasses(cls, false).nodes().iterator();

                                while(it7.hasNext()) {
                                    Node<OWLClass> n = it7.next();


                                //end................................

                                    for(OWLClass cls1 : n.getEntitiesMinusTop()) {
                                        //the propagation to the subclasses
                                        if(!cls1.isBottomEntity()) {
                                            ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString());
                                        }
                                    }
                                }
                            }
                        }

                        if (exp instanceof OWLObjectMaxCardinality) {
                            if(((OWLObjectMaxCardinality) exp).getCardinality()>=0) {
                                String p = ((OWLObjectMaxCardinality) exp).getProperty().toString();
                                ACpropertyFillerBlist.add(cls.toString()+";"+p.toString()+";"+((OWLObjectMaxCardinality) exp).getFiller());
                                ACpropertyFillerBlist.add(cls.toString()+";"+p.toString());

                                //start.................................

                                //for(Node<OWLClass> n : reasoner.getSubClasses(cls, false).getNodes()) {

                                Iterator<Node<OWLClass>> it8 = reasoner.getSubClasses(cls, false).nodes().iterator();

                                while(it8.hasNext()) {
                                    Node<OWLClass> n = it8.next();


                                //end.................................

                                    for(OWLClass cls1 : n.getEntitiesMinusTop()) {
                                        //the propagation to the subclasses
                                        if(!cls1.isBottomEntity()) {
                                            ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString()+";"+((OWLObjectMaxCardinality) exp).getFiller());
                                            ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString());
                                        }
                                    }
                                }
                            }
                        }

                        if (exp instanceof OWLDataMaxCardinality) {
                            if(((OWLDataMaxCardinality) exp).getCardinality()>=0) {
                                String p = ((OWLDataMaxCardinality) exp).getProperty().toString();
                                ACpropertyFillerBlist.add(cls.toString()+";"+p.toString());

                                //start...........................

                                //for(Node<OWLClass> n : reasoner.getSubClasses(cls, false).getNodes()) {
                                Iterator<Node<OWLClass>> it9 = reasoner.getSubClasses(cls, false).nodes().iterator();

                                while(it9.hasNext()) {
                                    Node<OWLClass> n = it9.next();



                                    //end........................................

                                    for(OWLClass cls1 : n.getEntitiesMinusTop()) {
                                        //the propagation to the subclasses
                                        if(!cls1.isBottomEntity()) {
                                            ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString());
                                        }
                                    }
                                }
                            }
                        }

                        if (exp instanceof OWLObjectExactCardinality) {
                            if(((OWLObjectExactCardinality) exp).getCardinality()>=0) {
                                String p = ((OWLObjectExactCardinality) exp).getProperty().toString();
                                ACpropertyFillerBlist.add(cls.toString()+";"+p.toString()+";"+((OWLObjectExactCardinality) exp).getFiller());
                                ACpropertyFillerBlist.add(cls.toString()+";"+p.toString());


                                //start.................................
                                //for(Node<OWLClass> n : reasoner.getSubClasses(cls, false).getNodes()) {

                                Iterator<Node<OWLClass>> it10 = reasoner.getSubClasses(cls, false).nodes().iterator();

                                while(it10.hasNext()) {
                                    Node<OWLClass> n = it10.next();

                                //end....................................


                                    for(OWLClass cls1 : n.getEntitiesMinusTop()) {
                                        //the propagation to the subclasses
                                        if(!cls1.isBottomEntity()) {
                                            ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString()+";"+((OWLObjectExactCardinality) exp).getFiller());
                                            ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString());
                                        }
                                    }
                                }
                            }
                        }

                        if (exp instanceof OWLDataExactCardinality) {
                            if(((OWLDataExactCardinality) exp).getCardinality()>=0) {
                                String p = ((OWLDataExactCardinality) exp).getProperty().toString();
                                ACpropertyFillerBlist.add(cls.toString()+";"+p.toString());

                                //start..............................

                                //for(Node<OWLClass> n : reasoner.getSubClasses(cls, false).getNodes()) {

                                Iterator<Node<OWLClass>> it11 = reasoner.getSubClasses(cls, false).nodes().iterator();

                                while(it11.hasNext()) {
                                    Node<OWLClass> n = it11.next();

                                //end..........................
                                    for(OWLClass cls1 : n.getEntitiesMinusTop()) {
                                        //the propagation to the subclasses
                                        if(!cls1.isBottomEntity()) {
                                            ACpropertyFillerBlist.add(cls1.toString()+";"+p.toString());
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
            }
            String writePath = System.getProperty("catalina.base") + "/temp/";
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                writePath = "";
            }
            //black list generation end
            System.out.println("blist:");
            System.out.println(ACpropertyFillerBlist);
            //control output to the text file
            PrintWriter toFileBL = new PrintWriter(new FileWriter(writePath + "blackList", true));
            toFileBL.println(onto);
            for(String s : ACpropertyFillerBlist) {
                toFileBL.println(s);
            }
            toFileBL.println();
            toFileBL.close();

            //2. categorization options detection itself - variants v1, v2, v3, v4 (v5 is skipped for now)
            PrintWriter toFile = new PrintWriter(new FileWriter(writePath + "a.txt", true));
            toFile.println(onto);

            //19-12-19
            //all_created_class_expressions - work in progress
            all_created_class_expressions = new HashSet<OWLClassExpression>();

            //fist go for variants v2, v3, v4
            List<String> list2=new ArrayList<>();
            List<String> list3=new ArrayList<>();
            List<String> list4=new ArrayList<>();

            //start.........................................
            //for(OWLObjectProperty op : this.ontology.getObjectPropertiesInSignature(considerImport)) {
              //  for(Node<OWLClass> n : reasoner.getObjectPropertyDomains(op, false).getNodes()) {
            Iterator<OWLObjectProperty> iter3 = this.ontology.objectPropertiesInSignature(considerImport).iterator();

            while(iter3.hasNext()) {

                OWLObjectProperty op = iter3.next();

                Iterator<Node<OWLClass>> it1 = reasoner.getObjectPropertyDomains(op, false).nodes().iterator();

                while(it1.hasNext()) {
                    Node<OWLClass> n = it1.next();
            //end..................................................
                    //it does not consider Nothing
                    if(n.isBottomNode()) break;
                    for(OWLClass cls1 : n.getEntitiesMinusTop()) {
                        //v2
                        if(ACpropertyFillerBlist.contains(cls1.toString()+";"+op.toString())) {
                            System.out.println("blist applied:"+cls1.toString()+";"+op.toString());
                            continue; //pruning
                        }
                        System.out.println("$v2 | exists | "+op+".owl:Thing | rdfs:subClassOf | "+cls1);
                        list2.add("$v2 | exists | "+op+".owl:Thing | rdfs:subClassOf | "+cls1);
                        toFile.println("$v2 | exists | "+op+".owl:Thing | rdfs:subClassOf | "+cls1);

                        //19-12-19, creation corresponding class expression
                        OWLClassExpression clsExpr1 = this.factory.getOWLObjectSomeValuesFrom(op, factory.getOWLThing());
                        all_created_class_expressions.add(clsExpr1);
                        OWLClassExpression clsExpr3 = this.factory.getOWLObjectSomeValuesFrom(op, factory.getOWLThing());
                        OWLClassExpression clsExpr4 = factory.getOWLObjectIntersectionOf(cls1, clsExpr3);
                        if (reasoner.isSatisfiable(clsExpr4)==false) {
                            System.out.println(reasoner.isConsistent());
                            System.out.println(clsExpr4);
                            System.out.println("Found CE which is not satisfiable");
                            System.exit(1);
                        }

                        System.out.println("");

                        //start v3
                        HashSet<String> subclasses = new HashSet<String>();
                        HashSet<String> individuals = new HashSet<String>();

                        //start......................
                        //EntitySearcher.getRanges(op, this.ontology).forEach((OWLClassExpression exp) -> {
                        Iterator<OWLClassExpression> iter4 = EntitySearcher.getRanges(op, this.ontology).iterator();

                        while(iter4.hasNext()) {
                            OWLClassExpression exp = iter4.next();

                        //end..........................


                            if(exp instanceof OWLClass) {
                                OWLClass cls2 = (OWLClass) exp;

                                //start............................
                                //for(Node<OWLClass> n2 : reasoner.getSubClasses(cls2, false).getNodes()) {
                                Iterator<Node<OWLClass>> it2 = reasoner.getSubClasses(cls2, false).nodes().iterator();

                                while(it2.hasNext()) {
                                    Node<OWLClass> n2 = it2.next();

                                //end......................................


                                    for(OWLClass cls3 : n2.getEntitiesMinusBottom()) {
                                        if(ACpropertyFillerBlist.contains(cls1.toString()+";"+op.toString()+";"+cls3.toString())) {
                                            System.out.println("blist applied:"+cls1.toString()+";"+op.toString()+";"+cls3.toString());
                                            continue; //pruning
                                        }
                                        subclasses.add(cls3.toString());
                                    }
                                }
                                //start v4


                                //start........................

                                //for(Node<OWLNamedIndividual> n2 : reasoner.getInstances(cls2, false).getNodes()) {
                                  //  for(OWLNamedIndividual ind : n2.getEntities()) {
                                Iterator<Node<OWLNamedIndividual>> it3 = reasoner.getInstances(cls2, false).nodes().iterator();

                                while(it3.hasNext()) {
                                    Node<OWLNamedIndividual> n2 = it3.next();

                                    Iterator<OWLNamedIndividual> it4 = n2.entities().iterator();

                                    while(it4.hasNext()) {
                                        OWLNamedIndividual ind = it4.next();

                                //end....................................

                                        if(ACpropertyFillerBlist.contains(cls1.toString()+";"+op.toString()+";"+ind.toString())) {
                                            System.out.println("blist applied:"+cls1.toString()+";"+op.toString()+";"+ind.toString());
                                            continue; //pruning
                                        }
                                        individuals.add(ind.toString());
                                    }
                                }
                                //end v4
                            }
                        });
                        if(!subclasses.isEmpty()) {
                            //System.out.println("$v3 "+subclasses+" classification option (via op "+op+") for anchor class:"+cls1);;
                            for(String x : subclasses) {
                                System.out.println("$v3 | exists | "+op+"."+x+" | rdfs:subClassOf | "+cls1);

                                list3.add("$v3 | exists | "+op+"."+x+" | rdfs:subClassOf | "+cls1);
                                toFile.println("$v3 | exists | "+op+"."+x+" | rdfs:subClassOf | "+cls1);
                                toFile.println("");
                                System.out.println("");
                                //19-12-19, start, corresponding class expression
                                clsExpr1 = this.factory.getOWLObjectSomeValuesFrom(op, this.factory.getOWLClass(IRI.create(x.replaceAll("<", "").replaceAll(">", ""))));
                                all_created_class_expressions.add(clsExpr1);
                                clsExpr3 = this.factory.getOWLObjectSomeValuesFrom(op, this.factory.getOWLClass(IRI.create(x.replaceAll("<", "").replaceAll(">", ""))));
                                clsExpr4 = factory.getOWLObjectIntersectionOf(cls1, clsExpr3);
                                if (reasoner.isSatisfiable(clsExpr4)==false) {
                                    System.out.println(reasoner.isConsistent());
                                    System.out.println(clsExpr4);
                                    System.out.println("Found CE which is not satisfiable");
                                    System.exit(1);
                                }
                            }

                        }
                        if(!individuals.isEmpty())
                            for(String x : individuals) {
                                System.out.println("$v4 | exists | "+op+".{"+x+"} | rdfs:subClassOf | "+cls1);
                                System.out.println("");

                                list4.add("$v4 | exists | "+op+".{"+x+"} | rdfs:subClassOf | "+cls1);
                                toFile.println("$v4 | exists | "+op+".{"+x+"} | rdfs:subClassOf | "+cls1);
                                toFile.println("");
                                //19-12-19, start, corresponding class expression
                                clsExpr1 = this.factory.getOWLObjectSomeValuesFrom(op, (this.factory.getOWLObjectOneOf(this.factory.getOWLNamedIndividual(IRI.create(x.replaceAll("<", "").replaceAll(">", ""))))));
                                all_created_class_expressions.add(clsExpr1);
                                clsExpr3 = this.factory.getOWLObjectSomeValuesFrom(op, (this.factory.getOWLObjectOneOf(this.factory.getOWLNamedIndividual(IRI.create(x.replaceAll("<", "").replaceAll(">", ""))))));
                                clsExpr4 = factory.getOWLObjectIntersectionOf(cls1, clsExpr3);
                                if (reasoner.isSatisfiable(clsExpr4)==false) {
                                    System.out.println(reasoner.isConsistent());
                                    System.out.println(clsExpr4);
                                    System.out.println("Found CE which is not satisfiable");
                                    System.exit(1);
                                }
                            }
                        //end v3
                    }
                }
            }
            data.put("v2", list2);
            data.put("v3", list3);
            data.put("v4", list4);

            //for dp compute v2

            //start.............................

            //for(OWLDataProperty dp : this.ontology.getDataPropertiesInSignature(considerImport)) {
              //  for(Node<OWLClass> n : reasoner.getDataPropertyDomains(dp, false).getNodes()) {

            Iterator<OWLDataProperty> iter5 = this.ontology.dataPropertiesInSignature(considerImport).iterator();

            while(iter5.hasNext()) {

                OWLDataProperty dp = iter5.next();

                Iterator<Node<OWLClass>> it1 = reasoner.getDataPropertyDomains(dp, false).nodes().iterator();

                while(it1.hasNext()) {
                    Node<OWLClass> n = it1.next();

            //end....................................................

                    //if domain is Nothing not considering this
                    if(n.isBottomNode()) break;
                    for(OWLClass cls1 : n.getEntitiesMinusTop()) {
                        if(ACpropertyFillerBlist.contains(cls1.toString()+";"+dp.toString())) {
                            System.out.println("blist applied:"+cls1.toString()+";"+dp.toString());
                            continue; //pruning
                        }
                        System.out.println("$v2 | (dp) exists | "+dp+".owl:Thing | rdfs:subClassOf | "+cls1);
                        list2.add("$v2 | (dp) exists | "+dp+".owl:Thing | rdfs:subClassOf | "+cls1);
                        System.out.println("");
                        toFile.println("$v2 | (dp) exists | "+dp+".owl:Thing | rdfs:subClassOf | "+cls1);
                        toFile.println("");
                        //19-12-19, start, corresponding odpovidajici class expression for dp owl:Thing not possible
                        //end dp v2
                    }
                }

            }
            data.put("v2", list2);
            //end categorization options detection itself v2, v3, v4 but v1 is in the following iteration

            //3. compute v1
            List<String> list1=new ArrayList<>();

            //start......................
            //for(OWLClass cls : this.ontology.getClassesInSignature(considerImport)) {

            Iterator<OWLClass> iter6 = this.ontology.classesInSignature(considerImport).iterator();

            while(iter6.hasNext()) {

                OWLClass cls = iter6.next();

            //end.............................

                if(cls.isTopEntity())
                    continue;
                //compute and print v1
                HashSet<String> subclasses = new HashSet<String>();
                HashSet<OWLClass> subclasses1 = new HashSet<OWLClass>();

                //start.......................................

                //for(Node<OWLClass> n : reasoner.getSubClasses(cls, false).getNodes()) {
                Iterator<Node<OWLClass>> it1 = reasoner.getSubClasses(cls, false).nodes().iterator();

                while(it1.hasNext()) {
                    Node<OWLClass> n = it1.next();

                    //end..........................................

                    if(n.getEntitiesMinusBottom().size()>0) {
                        subclasses.add(n.getEntitiesMinusBottom().toString());
                        //19-12-19, because of finding CE already as equivalence for subclasses)
                        for(OWLClass cls1 : n.getEntitiesMinusBottom()) {
                            subclasses1.add(cls1);
                        }
                        //19-12-19, end
                    }
                }
                if(subclasses.size()>0) {
                    System.out.println("$v1 | "+cls+" | "+subclasses);
                    list1.add("$v1 | "+cls+" | "+subclasses);
                    toFile.println("$v1 | "+cls+" | "+subclasses);
                    //19-12-19, checking whether named subclass  is not the same as cases from v2, v3, v4
                    //if so then it is output but not counted
                    //this part is in progress
                    System.out.println(subclasses1);
                    //toFile.println(subclasses1);
                    //the following work in progress
                    for(OWLClassExpression owlExpr1 : all_created_class_expressions) {

                        //start....................

                        //if (!reasoner.getEquivalentClasses(owlExpr1).getEntities().isEmpty()) {
                          //  System.out.println(reasoner.getEquivalentClasses(owlExpr1).getEntities());
                        //}

                        //system control output
                        //Iterator<Node<OWLNamedIndividual>> it3 = reasoner.getInstances(cls2, false).nodes().iterator();
                        Iterator<OWLClass> itt = reasoner.getEquivalentClasses(owlExpr1).entities().iterator();
                        while(itt.hasNext()) {
                            System.out.println(itt.next());

                        //end...........................
                    }
                    //19-12-19, end
                }

                toFile.println();
            }

            data.put("v1", list1);
            toFile.close();

            System.out.println("done");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return data;
    }


    //ArrayList<String> ontologies = new ArrayList<String>();

}