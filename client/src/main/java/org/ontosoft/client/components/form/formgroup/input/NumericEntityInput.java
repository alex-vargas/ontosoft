package org.ontosoft.client.components.form.formgroup.input;

import org.gwtbootstrap3.client.ui.DoubleBox;
import org.ontosoft.client.components.form.formgroup.input.events.EntityChangeEvent;
import org.ontosoft.client.components.form.formgroup.input.events.EntityChangeHandler;
import org.ontosoft.shared.classes.entities.Entity;
import org.ontosoft.shared.classes.entities.ModelVersion;
import org.ontosoft.shared.classes.entities.NumericEntity;
import org.ontosoft.shared.classes.entities.SoftwareVersion;
import org.ontosoft.shared.classes.vocabulary.MetadataProperty;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

public class NumericEntityInput implements IEntityInput {
	private HandlerManager handlerManager;

	NumericEntity entity;
	DoubleBox input;

	MetadataProperty property;
	Vocabulary vocabulary;

	public NumericEntityInput() {
		handlerManager = new HandlerManager(this);
	}

	@Override
	public DoubleBox asWidget() {
		return input;
	}

	@Override
	public void createWidget(Entity e, MetadataProperty prop, Vocabulary vocab) {
		init(prop, vocab);
		this.entity = (NumericEntity) e;
		input.setPlaceholder(prop.getLabel());
		input.addValidator(Validators.DOUBLE);
		input.addValueChangeHandler(new ValueChangeHandler<Double>() {
			@Override
			public void onValueChange(ValueChangeEvent<Double> event) {
				entity.setValue(event.getValue());
				fireEvent(new EntityChangeEvent(entity));
			}
		});

		this.setValue(e);
	}

	protected void init(MetadataProperty prop, Vocabulary vocabulary) {
		this.property = prop;
		this.vocabulary = vocabulary;
	}

	@Override
	public Entity getValue() {
		entity.setValue(input.getValue());
		return entity;
	}

	@Override
	public void setValue(Entity entity) {
		NumericEntity ne = (NumericEntity) entity;
		if (entity.getValue() != null)
			input.setValue((Double) entity.getValue());
		this.entity = ne;
	}

	@Override
	public void clearValue() {
		if (input.getValue() != null) {
			input.setValue(null);
			fireEvent(new EntityChangeEvent(getValue()));
		}
	}

	@Override
	public boolean validate(boolean show) {
		return input.validate(show);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		handlerManager.fireEvent(event);
	}

	@Override
	public HandlerRegistration addEntityChangeHandler(EntityChangeHandler handler) {
		return handlerManager.addHandler(EntityChangeEvent.TYPE, handler);
	}

	@Override
	public void layout() {
	}

	@Override
	public void disable() {
		input.setEnabled(false);
	}

	@Override
	public void createWidget(Entity entity, MetadataProperty prop, Vocabulary vocabulary, SoftwareVersion version) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createWidget(Entity entity, MetadataProperty prop, Vocabulary vocabulary, ModelVersion version) {
		// TODO Auto-generated method stub

	}
}
