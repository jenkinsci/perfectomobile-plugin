package com.perfectomobile.jenkins;

import hudson.model.AutoCompletionCandidates;

import java.util.Random;

import javax.servlet.ServletException;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.perfectomobile.jenkins.miscel.RepositoryAccessFailureException;
import com.perfectomobile.jenkins.services.MobileCloudServicesFactory;

/**
 * every step builder, of this plugin, primary or not, should extend from this class because it contains common operations that 
 * all steps of this plugin require
 * Note however that getRandomId and obtainBuilder... are only used by the script execution but potentially they may be used
 * by any step whose .jelly performs ajax calls such as the getParameters performed by teh script execution .jelly
 * @author ronik
 *
 */
public abstract class MobileCloudStepConfiguration extends MobileCloudGlobalConfiguration {
	
	@Override
	public String getConfigPage() {
		reset();
        return super.getConfigPage();
    }
	private void reset() {
		//dbg("\n*** reset");
		MobileCloudServicesFactory.getInstance().resetGetters();
	}

	
	public int getRandomID() {
        
        return Math.abs(new Random().nextInt());
    }
	
	/**
	 * @param req
	 * @param targetClass
	 * @param json
	 * @param id
	 * @return
	 * @throws ServletException 
	 */
	protected JSONObject obtainBuilderForThisStep(StaplerRequest req, String targetClass) throws ServletException {
				
		JSONObject builder = null;
		
		JSONObject json = req.getSubmittedForm();
        String id = (((String[]) req.getParameterMap().get("id"))[0]).substring(5); //get id parameter and remove the "param" prefix
        
        JSON jsonB = (JSON) json.get("builder");
        if(jsonB.isArray()) {
            JSONArray arr = (JSONArray) jsonB;
            for(Object i : arr) {
                JSONObject ji = (JSONObject) i;
                if(targetClass.equals(ji.get("stapler-class"))) {
                	ScriptExecutionBuilder pbBuilder = req.bindJSON(ScriptExecutionBuilder.class, ji);
                	if(pbBuilder.getId().equals(id)){
                		builder = ji;
                	}
                	
                }
            }
        } else {
        	builder = (JSONObject) jsonB;
        	
        }
		return builder;
	}
	
	protected AutoCompletionCandidates doAutoCompleteRepositoryItems( @QueryParameter String value, String repositoryItemType) {
		AutoCompletionCandidates candidates = new AutoCompletionCandidates();

		String[] items = null;
		try {
			items = MobileCloudServicesFactory.getInstance().getRepositoryItemsGetter().obtainRepositoryItemsOf(repositoryItemType, getHostUrl(), 
					getAccessId(), getSecretKey()); //username, password);
			value = value.toLowerCase();
			for (String item : items)
				if (item.toLowerCase().startsWith(value))
					candidates.add( Constants.SCRIPTS_REPOSITORY.equals(repositoryItemType) 
						? item.substring(0, item.length() -  Constants.SCRIPT_FILE_EXT.length()) 
						: item);
		} catch (RepositoryAccessFailureException rafe) {
			if (rafe.isAccessDenied())
				candidates.add(Messages.UiError_CantGetItems(repositoryItemType));
		}
		return candidates;
	}

	public AutoCompletionCandidates doAutoCompleteAutoMedia( @QueryParameter String value) {
		return doAutoCompleteRepositoryItems(value,Constants.MEDIA_REPOSITORY);
	}
	
	//public FormValidation doCheckName(@QueryParameter String value)
	//		throws IOException, ServletException {
	//	return FormValidation.ok();
	//}
	
	// /**
	// * Get clouds names. Might be more than one cloud in the future.
	// * 
	// * @return Cloud Items
	// */
	//public ListBoxModel doFillPerfectoCloudItems() {
	//	ListBoxModel items = new ListBoxModel();
	//
	//	items.add(getLogicalName());
	//	return items;
	//}
}