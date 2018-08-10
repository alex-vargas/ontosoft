package org.ontosoft.client.components.form.events;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasModelHandlers extends HasHandlers {
	HandlerRegistration addModelSaveHandler(ModelSaveHandler handler);

	HandlerRegistration addModelChangeHandler(ModelChangeHandler handler);
}
