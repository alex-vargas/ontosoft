package org.ontosoft.client.components.form.formgroup.input;

import org.ontosoft.client.components.form.formgroup.input.events.HasEntityHandlers;
import org.ontosoft.shared.classes.entities.Entity;
import org.ontosoft.shared.classes.entities.ModelVersion;
import org.ontosoft.shared.classes.entities.SoftwareVersion;
import org.ontosoft.shared.classes.vocabulary.MetadataProperty;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;

import com.google.gwt.user.client.ui.IsWidget;

public interface IEntityInput extends IsWidget, HasEntityHandlers {

	public void createWidget(Entity entity, MetadataProperty prop, Vocabulary vocabulary);

	public void createWidget(Entity entity, MetadataProperty prop, Vocabulary vocabulary, SoftwareVersion version);

	public void createWidget(Entity entity, MetadataProperty prop, Vocabulary vocabulary, ModelVersion version);

	public Entity getValue();

	public void setValue(Entity entity);

	public void clearValue();

	public boolean validate(boolean show);

	public void layout();

	public void disable();

}
