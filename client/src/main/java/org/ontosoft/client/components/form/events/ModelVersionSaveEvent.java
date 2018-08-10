package org.ontosoft.client.components.form.events;

import org.ontosoft.shared.classes.entities.ModelVersion;

import com.google.gwt.event.shared.GwtEvent;

public class ModelVersionSaveEvent extends GwtEvent<ModelVersionSaveHandler> {

	public static Type<ModelVersionSaveHandler> TYPE = new Type<ModelVersionSaveHandler>();

	private final ModelVersion version;

	public ModelVersionSaveEvent(ModelVersion model) {
		this.version = model;
	}

	@Override
	public Type<ModelVersionSaveHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(ModelVersionSaveHandler handler) {
		handler.onSave(this);
	}

	public ModelVersion getModelVersion() {
		return version;
	}
}
