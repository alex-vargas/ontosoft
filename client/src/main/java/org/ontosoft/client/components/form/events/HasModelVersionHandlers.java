package org.ontosoft.client.components.form.events;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasModelVersionHandlers extends HasHandlers {
  HandlerRegistration addModelVersionSaveHandler(ModelVersionSaveHandler handler);
  HandlerRegistration addModelVersionChangeHandler(ModelVersionChangeHandler handler);
}
