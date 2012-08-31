/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.readonly;


import hudson.Extension;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 *
 * @author lucinka
 */
public class UserConfiguration extends UserProperty{
    
    private boolean displayForReadOnlyPermission;
    
    @DataBoundConstructor
    public UserConfiguration(boolean displayForReadOnlyPermission){
        this.displayForReadOnlyPermission= displayForReadOnlyPermission;
    }
    
    public boolean getDisplayForReadOnlyPermission(){
        return displayForReadOnlyPermission;
    }
   
      @Extension
        public static final class DescriptorImpl extends UserPropertyDescriptor {
            public String getDisplayName() {
                return "Read-only job configuration settings";
            }

            public UserProperty newInstance(User user) {
                return new UserConfiguration(true);
            }

            @Override
            public UserProperty newInstance(StaplerRequest req, JSONObject formData) throws FormException {
                return req.bindJSON(this.clazz, formData);
            }
        }
    
}
