package org.ontosoft.client.components.chart.events;

import com.google.gwt.event.shared.HandlerRegistration;

public interface HasCategoryChartHandlers {
	public HandlerRegistration addCategorySelectionHandler(CategorySelectionHandler handler);
}
