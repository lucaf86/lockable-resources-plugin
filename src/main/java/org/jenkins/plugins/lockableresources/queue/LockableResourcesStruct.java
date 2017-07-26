/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2013, Aki Asikainen. All rights reserved.             *
 *                                                                     *
 * This file is part of the Jenkins Lockable Resources Plugin and is   *
 * published under the MIT license.                                    *
 *                                                                     *
 * See the "LICENSE.txt" file for more information.                    *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.jenkins.plugins.lockableresources.queue;

import hudson.EnvVars;
import java.io.IOException;
import java.io.ObjectOutputStream;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;

import org.jenkins.plugins.lockableresources.LockableResource;
import org.jenkins.plugins.lockableresources.LockableResourcesManager;
import org.jenkins.plugins.lockableresources.RequiredResourcesProperty;
import org.jenkins.plugins.lockableresources.util.SerializableSecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;

import edu.umd.cs.findbugs.annotations.Nullable;

public class LockableResourcesStruct implements Serializable {

	/** A list containing the global resources required */
	public List<LockableResource> required;
	/** The labels associated with the required resources */
	public String label;
	/** The name of the variable that will hold the required resources names */
	public String requiredVar;
	/** The number of resources required */
	public String requiredNumber;
        
        @CheckForNull
        private final SerializableSecureGroovyScript serializableResourceMatchScript;
        
	@CheckForNull
	private transient SecureGroovyScript resourceMatchScript;

	public LockableResourcesStruct(RequiredResourcesProperty property,
			EnvVars env) {
		required = new ArrayList<LockableResource>();
		for (String name : property.getResources()) {
			LockableResource r = LockableResourcesManager.get().fromName(
				env.expand(name));
			if (r != null) {
				this.required.add(r);
			}
		}

		label = env.expand(property.getLabelName());
		if (label == null)
			label = "";

		resourceMatchScript = property.getResourceMatchScript();
                serializableResourceMatchScript = new SerializableSecureGroovyScript(resourceMatchScript);
                
		requiredVar = property.getResourceNamesVar();

		requiredNumber = property.getResourceNumber();
		if (requiredNumber != null && requiredNumber.equals("0"))
			requiredNumber = null;
	}

        /**
         * Light-weight constructor for declaring a resource only.
         * @param resources Resources to be required
         */
	public LockableResourcesStruct(@Nullable List<String> resources) {
		this(resources, null, 0);
	}

	public LockableResourcesStruct(@Nullable List<String> resources, @Nullable String label, int quantity) {
		required = new ArrayList<LockableResource>();
		if (resources != null) {
			for (String resource : resources) {
				LockableResource r = LockableResourcesManager.get().fromName(resource);
				if (r != null) {
					this.required.add(r);
				}
			}
		}

		this.label = label;
		if (this.label == null) {
		    this.label = "";
		}

		this.requiredNumber = null;
		if (quantity > 0) {
			this.requiredNumber = String.valueOf(quantity);
		}
                
                // We do not support 
                this.serializableResourceMatchScript = null;
                this.resourceMatchScript = null;
	}
        
        /**
         * Gets a system Groovy script to be executed in order to determine if the {@link LockableResource} matches the condition.
         * @return System Groovy Script if defined
         * @since TODO
         * @see LockableResource#scriptMatches(org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript, java.util.Map) 
         */
        @CheckForNull
        public SecureGroovyScript getResourceMatchScript() {
            if (resourceMatchScript == null && serializableResourceMatchScript != null) {
                resourceMatchScript = serializableResourceMatchScript.rehydrate();
            }
            return resourceMatchScript;
        }

        @Override
	public String toString() {
		return "Required resources: " + this.required +
			", Required label: " + this.label +
			", Required label script: " + (this.resourceMatchScript != null ? this.resourceMatchScript.getScript() : "") +
			", Variable name: " + this.requiredVar +
			", Number of resources: " + this.requiredNumber;
	}

	private static final long serialVersionUID = 1L;
}
