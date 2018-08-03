package org.ontosoft.client.components.form.events;

import com.google.gwt.event.shared.EventHandler;

public interface ModelSaveHandler extends EventHandler {
  void onSave(ModelSaveEvent event);
}
