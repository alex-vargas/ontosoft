package org.ontosoft.client.components.form.events;

import com.google.gwt.event.shared.EventHandler;

public interface PluginResponseHandler extends EventHandler {
	void onPluginResponse(PluginResponseEvent event);
}
