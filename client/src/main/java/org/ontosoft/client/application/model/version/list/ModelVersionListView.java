package org.ontosoft.client.application.model.version.list;

import java.util.List;

import org.ontosoft.client.application.version.list.SoftwareVersionListView;
import org.ontosoft.client.place.NameTokens;
import org.ontosoft.client.rest.SoftwareREST;
import org.ontosoft.shared.classes.SoftwareVersionSummary;

import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;

public class ModelVersionListView extends SoftwareVersionListView implements ModelVersionListPresenter.MyView {

	@Inject
	public ModelVersionListView(Binder binder) {
		super(binder);
		setIsModel(true);
	}

	@Override
	public void initPlaces() {
		setBrowsePlace(NameTokens.modelBrowse);
		setCompareVersionPlace(NameTokens.compareModelVersion);
		setPublishPlace(NameTokens.publishModel);
		setPublishVersionPlace(NameTokens.publishModelVersion);
		setVersionPlace(NameTokens.modelVersionBrowse);
	}

	@Override
	public void setSoftwareVersionList(SoftwareREST sapi, Callback<List<SoftwareVersionSummary>, Throwable> callback) {
		sapi.getModelVersionList(getSoftwarename(), callback, false);
	}
//TODO in el paso:
	//Test if list of model versions can be retrieved:
	//probably some adjustments will be needed like: publishModelVersions
}
