package org.ontosoft.client.components.form.events;

import com.google.gwt.event.shared.EventHandler;

public interface ModelVersionChangeHandler extends EventHandler {
  void onModelVersionChange(ModelVersionChangeEvent event);
}
