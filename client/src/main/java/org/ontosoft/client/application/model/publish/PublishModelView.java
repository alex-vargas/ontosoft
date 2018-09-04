package org.ontosoft.client.application.model.publish;

import org.ontosoft.client.application.publish.PublishView;
import org.ontosoft.client.place.NameTokens;

import com.google.inject.Inject;

public class PublishModelView extends PublishView 
	implements PublishModelPresenter.MyView {

	@Inject
	public PublishModelView(Binder binder) {
		super(binder);

	}

	@Override
	public void initPlaces() {
		setBrowsePlace(NameTokens.modelBrowse);
		setPublishPlace(NameTokens.publishModel);
	}
}
