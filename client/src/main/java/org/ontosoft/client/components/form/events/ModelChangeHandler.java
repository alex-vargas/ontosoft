package org.ontosoft.client.components.form.events;

import com.google.gwt.event.shared.EventHandler;

public interface ModelChangeHandler extends EventHandler {
  void onModelChange(ModelChangeEvent event);
}