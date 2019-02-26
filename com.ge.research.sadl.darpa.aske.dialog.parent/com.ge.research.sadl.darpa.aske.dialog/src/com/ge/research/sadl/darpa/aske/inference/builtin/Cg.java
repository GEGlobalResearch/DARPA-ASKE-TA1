package com.ge.research.sadl.darpa.aske.inference.builtin;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.research.sadl.darpa.aske.processing.DialogConstants;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.rulesys.BuiltinException;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;
import com.hp.hpl.jena.reasoner.rulesys.builtins.BaseBuiltin;

public class Cg extends BaseBuiltin  {
	private static final Logger _logger = LoggerFactory.getLogger (Cg.class) ;

	@Override
	public String getName() {
		return "cg";
	}

    /**
     * This method is invoked when the builtin is called in a rule body.
     * @param args the array of argument values for the builtin, this is an array 
     * of Nodes, some of which may be Node_RuleVariables.
     * @param length the length of the argument list, may be less than the length of the args array
     * for some rule engines
     * @param context an execution context giving access to other relevant data
     * @return return true if the buildin predicate is deemed to have succeeded in
     * the current environment
     */
    public boolean bodyCall(Node[] args, int length, RuleContext context) {
    	if (length < 1) {
    		throw new BuiltinException(this, context, "At a minimum cg must have an argument which is the model to compute");
    	}
    	    	
    	// first argument is the model to execute
    	// subsequent arguments are the inputs to the model
    	
    	// need URL of service
    	Properties prop = new Properties();
    	String serviceUrl = prop.getProperty(DialogConstants.CG_SERVICE_BASE_URL);
    	if (serviceUrl != null) {
    		serviceUrl += "/eval";
    	}
    	
    	System.out.println("cg called with args:");
    	for (Node n : args) {
    		System.out.println("  arg: " + n.toString());
    	}
    	return true;
    }
}
