package org.ontosoft.client.components.form.events;

import org.ontosoft.shared.classes.entities.Model;
import com.google.gwt.event.shared.GwtEvent;

public class ModelSaveEvent extends GwtEvent<ModelSaveHandler> {

  public static Type<ModelSaveHandler> TYPE = new Type<ModelSaveHandler>();
  
  private final Model model;
  
  public ModelSaveEvent(Model model) {
    this.model = model;
  }
  
  @Override
  public Type<ModelSaveHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(ModelSaveHandler handler) {
    handler.onSave(this);
  }

  public Model getModel() {
    return model;
  }
}
