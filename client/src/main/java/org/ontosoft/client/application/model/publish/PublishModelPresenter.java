package org.ontosoft.client.application.model.publish;

import org.ontosoft.client.application.ApplicationPresenter;
import org.ontosoft.client.application.ParameterizedView;
import org.ontosoft.client.place.NameTokens;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class PublishModelPresenter extends Presenter<PublishModelPresenter.MyView, PublishModelPresenter.MyProxy> {

	@ProxyCodeSplit
	@NameToken(NameTokens.publishModel)
	public interface MyProxy extends ProxyPlace<PublishModelPresenter> {
	}

	public interface MyView extends ParameterizedView {
	}

	@Inject
	public PublishModelPresenter(EventBus eventBus, MyView view, final MyProxy proxy, final PlaceManager placemanager) {
		super(eventBus, view, proxy, ApplicationPresenter.CONTENT_SLOT);
	}
}
