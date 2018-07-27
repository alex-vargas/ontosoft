package org.ontosoft.shared.classes;

import org.ontosoft.shared.classes.entities.ModelConfiguration;

public class ModelConfigurationSummary extends ModelSummary {

	ModelSummary modelSummary;

	public ModelConfigurationSummary() {
		super();
	}
	
	public ModelConfigurationSummary(ModelConfiguration modelConfiguration) {
		super(modelConfiguration);
	}

	public ModelSummary getModelSummary() {
		return modelSummary;
	}

	public void setModelSummary(ModelSummary modelSummary) {
		this.modelSummary = modelSummary;
	}
}
