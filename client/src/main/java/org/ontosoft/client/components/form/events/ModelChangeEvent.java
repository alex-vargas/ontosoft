package org.ontosoft.client.components.form.events;

import org.ontosoft.shared.classes.entities.Model;

import com.google.gwt.event.shared.GwtEvent;

public class ModelChangeEvent extends GwtEvent<ModelChangeHandler> {

	public static Type<ModelChangeHandler> TYPE = new Type<ModelChangeHandler>();

	private Model model;

	public ModelChangeEvent(Model model) {
		this.model = model;
	}

	@Override
	public Type<ModelChangeHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(ModelChangeHandler handler) {
		handler.onModelChange(this);
	}

	public Model getModel() {
		return model;
	}
}