/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.readonly;

import hudson.model.Action;
import hudson.model.Computer;
import hudson.security.Permission;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.Script;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.jelly.DefaultScriptInvoker;
import org.kohsuke.stapler.jelly.HTMLWriterOutput;
import org.kohsuke.stapler.jelly.JellyClassLoaderTearOff;
import org.xml.sax.InputSource;

/**
 * Read-only configuration for computers (@link Computer)
 * 
 * @author Lucie Votypkova
 */
public class SlaveConfiguration implements Action{
    
    private Computer computer;
    
    public SlaveConfiguration(Computer computer){
        this.computer=computer;
    }

    public String getIconFileName() {
        return "search.png";
    }

    public String getDisplayName() {
        return "Slave read-only configuration";
    }

    public String getUrlName() {
        return "configure-readonly";
    }
    
    /**
     * Determine if the read-only configuration is available for current user
     * 
     * @return true if the current user does not have permission to configure computers (@link Computer)
     */
    public boolean isAvailable(){
        return !computer.hasPermission(Permission.CONFIGURE);
    }
    
    /**
     * Compile script with a context for Job class
     * 
     * @return compiled script
     */
    public Script compileScript() {
        Script result = null;
        try {
            MetaClass c = WebApp.getCurrent().getMetaClass(Jenkins.getInstance().getClass());
            JellyContext context = new JellyClassLoaderTearOff(c.classLoader).createContext();
            StringReader buffer = new StringReader(getConfigContent());
            InputSource source = new InputSource(buffer);
            source.setSystemId("org.jenkinsci.plugins.readonly.SlaveConfiguration");
            result = context.compileScript(source);
        } catch (Exception ex) {
            Logger.getLogger(JobConfiguration.class.getName()).log(Level.WARNING, "Read-only configuration plugin failed to compile script", ex);
        }
        return result;
    }
    
    public String getConfigContent(){
        try{
       URL url = Computer.class.getResource("Computer/configure.jelly");
       InputStream input = url.openConnection().getInputStream();
       ByteArrayOutputStream output = new ByteArrayOutputStream();
       int b = 0;
            while (b != -1) {
                b = input.read();
                if (b != -1) {
                    output.write(b);
                }
            }
            String outputConfig = output.toString();
            return outputConfig.replace("it.CONFIGURE", "it.READ"); //change permission 
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Read-only configuration plugin failed to load configuration script", e);
            return null;
        }
    }
    
    public void doIndex(StaplerRequest request, StaplerResponse response) throws IOException {
        transformToReadOnly(request, response);        
    }
    
    public void transformToReadOnly(StaplerRequest request, StaplerResponse response) throws IOException {
        try{
            DefaultScriptInvoker invoker = new DefaultScriptInvoker();
            ByteArrayOutputStream out = new ByteArrayOutputStream(); 
            HTMLWriterOutput xmlOutput = HTMLWriterOutput.create(out);
            Script script = compileScript();           
            xmlOutput.useHTML(true);          
            invoker.invokeScript(request, response, script, computer, xmlOutput);
            String charset = Charset.defaultCharset().name();
            String page = ReadOnlyUtil.transformInputsToReadOnly(out.toString(charset));          
            OutputStream output = response.getCompressedOutputStream(request);
            output.write(page.getBytes());
            output.close();
        }
        catch(Exception ex){
            ex.printStackTrace(new PrintStream(response.getOutputStream()));
        }
    }
    
    public void getDynamic(String token, StaplerRequest request, StaplerResponse response) throws ServletException, IOException{
        //actions on the page have relative path, so redirect it on correct url wihtou read-only configuration part
        response.sendRedirect("/" + computer.getUrl() + token);
    }
    
}
