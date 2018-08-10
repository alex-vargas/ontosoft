package org.ontosoft.client.application.model.list;

import java.util.List;

import org.ontosoft.client.application.list.SoftwareListView;
import org.ontosoft.client.place.NameTokens;
import org.ontosoft.client.rest.SoftwareREST;
import org.ontosoft.shared.classes.SoftwareSummary;

import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;

public class ModelListView extends SoftwareListView implements ModelListPresenter.MyView {

	@Inject
	public ModelListView(Binder binder) {
		super(binder);
		setIsModel(true);
	}

	@Override
	public void initPlaces() {
		setBrowsePlace(NameTokens.modelBrowse);
		setPublishPlace(NameTokens.publishModel);
		setComparePlace(NameTokens.compare);
	}

	@Override
	public void setSoftwareList(SoftwareREST sapi, Callback<List<SoftwareSummary>, Throwable> callback) {
		sapi.getModelsList(callback, false);
	}
}