package org.jenkinsci.plugins.readonly;

import hudson.Extension;
import hudson.Functions;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.TransientProjectActionFactory;
import hudson.model.User;
import hudson.security.Permission;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;


/**
 *
 * @author Lucie Votypkova
 */
@Extension
public class ActionFactory extends TransientProjectActionFactory{


    @Override
    public Collection<? extends Action> createFor(@SuppressWarnings("unchecked") AbstractProject target) {
        final ArrayList<Action> actions = new ArrayList<Action>();     
        final JobConfiguration newAction = new JobConfiguration(target);
        actions.add(newAction);              
        return actions;
    }
    
   

}

