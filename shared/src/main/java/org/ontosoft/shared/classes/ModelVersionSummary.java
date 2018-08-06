package org.ontosoft.shared.classes;

import org.ontosoft.shared.classes.entities.ModelVersion;

public class ModelVersionSummary extends ModelSummary {

	ModelSummary modelSummary;

	public ModelVersionSummary() {
		super();
	}
	
	public ModelVersionSummary(ModelVersion model) {
		super(model);
	}

	public ModelSummary getModelSummary() {
		return modelSummary;
	}

	public void setModelSummary(ModelSummary modelSummary) {
		this.modelSummary = modelSummary;
	}
}
