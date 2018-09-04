package org.ontosoft.client.application.model.version.publish;

import org.ontosoft.client.application.version.publish.PublishVersionView;
import org.ontosoft.client.place.NameTokens;

import com.google.inject.Inject;

public class PublishModelVersionView extends 
	PublishVersionView implements PublishModelVersionPresenter.MyView {


	@Inject
	public PublishModelVersionView(Binder binder) {
		super(binder);
	}
	
	@Override
	public void initPlaces() {
		setPublishVersionPlace(NameTokens.publishModelVersion);
		setPublishPlace(NameTokens.publishModel);
		setVersionsPlace(NameTokens.modelVersions);
	}
	
}
