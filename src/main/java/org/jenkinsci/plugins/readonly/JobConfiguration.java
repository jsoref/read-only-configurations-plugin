package org.jenkinsci.plugins.readonly;

import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.ProminentProjectAction;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import jenkins.model.Jenkins;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Script;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.jelly.DefaultScriptInvoker;      
import org.kohsuke.stapler.jelly.HTMLWriterOutput;
import org.kohsuke.stapler.jelly.JellyClassLoaderTearOff;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Display Job configuration page in read-only form
 * 
 * @author Lucie Votypkova
 */
public class JobConfiguration implements Action {

    private AbstractProject<?, ?> project;

    public JobConfiguration(AbstractProject<?, ?> project) {
        this.project = project;
        
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
            source.setSystemId(JenkinsConfiguration.class.getResource("JobConfiguration").toString());
            result = context.compileScript(source);
        } catch (Exception ex) {
            Logger.getLogger(JobConfiguration.class.getName()).log(Level.WARNING, "Read-only configuration plugin failed to compile script", ex);
        }
        return result;
    }

    public String getIconFileName() {
        return "search.png";
    }

    public String getDisplayName() {
        return "Read-only job configuration";
    }

    public String getUrlName() {
        return "configure-readonly";
    }
    
    public boolean isAvailable() throws IOException, ServletException{
        return ReadOnlyUtil.isAvailableJobConfiguration(project);
    }
    
    public String getConfigContent(){
        try{
       URL url = Job.class.getResource("Job/configure.jelly");
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
            return outputConfig.replace("it.EXTENDED_READ", "it.READ"); //change permission 
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Read-only configuration plugin failed to load configuration script", e);
            return null;
        }
    }
    
    /**
    * Transformation of html code which modify all formular's items to read-only
    * 
    */
    public void transformToReadOnly() throws IOException, JellyException, SAXException, ParserConfigurationException, TransformerConfigurationException, TransformerException {
            DefaultScriptInvoker invoker = new DefaultScriptInvoker();
            StaplerRequest request = Stapler.getCurrentRequest();
            StaplerResponse response = Stapler.getCurrentResponse();
            MetaClass c = WebApp.get(request.getServletContext()).getMetaClass(project.getClass());
            ByteArrayOutputStream out = new ByteArrayOutputStream(); 
            HTMLWriterOutput xmlOutput = HTMLWriterOutput.create(out);
            Script script = compileScript();           
            xmlOutput.useHTML(true);          
            invoker.invokeScript(request, response, script, project, xmlOutput);
            String charset = Charset.defaultCharset().name();
            String page = ReadOnlyUtil.transformInputsToReadOnly(out.toString(charset));          
            response.getOutputStream().write(page.getBytes());
    }
    
}
