package org.ontosoft.client.components.form.events;

import org.ontosoft.shared.classes.entities.ModelVersion;

import com.google.gwt.event.shared.GwtEvent;

public class ModelVersionChangeEvent extends GwtEvent<ModelVersionChangeHandler> {

  public static Type<ModelVersionChangeHandler> TYPE = new Type<ModelVersionChangeHandler>();
  
  private ModelVersion version;
  
  public ModelVersionChangeEvent(ModelVersion version) {
    this.version = version;
  }
  
  @Override
  public Type<ModelVersionChangeHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(ModelVersionChangeHandler handler) {
    handler.onModelVersionChange(this);
  }

  public ModelVersion getModelVersion() {
    return version;
  }
}