package org.ontosoft.client.application.model.version.compare;

import org.ontosoft.client.application.ApplicationPresenter;
import org.ontosoft.client.application.ParameterizedView;
import org.ontosoft.client.place.NameTokens;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class CompareModelVersionPresenter
		extends Presenter<CompareModelVersionPresenter.MyView, CompareModelVersionPresenter.MyProxy> {

	@ProxyCodeSplit
	@NameToken(NameTokens.compareversion)
	public interface MyProxy extends ProxyPlace<CompareModelVersionPresenter> {
	}

	public interface MyView extends ParameterizedView {
	}

	@Inject
	public CompareModelVersionPresenter(EventBus eventBus, MyView view, MyProxy proxy) {
		super(eventBus, view, proxy, ApplicationPresenter.CONTENT_SLOT);
	}
}
