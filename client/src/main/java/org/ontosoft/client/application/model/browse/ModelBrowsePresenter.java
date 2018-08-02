package org.ontosoft.client.application.model.browse;

import org.ontosoft.client.application.ApplicationPresenter;
import org.ontosoft.client.application.ParameterizedView;
import org.ontosoft.client.place.NameTokens;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class ModelBrowsePresenter extends
	Presenter<ModelBrowsePresenter.MyView, ModelBrowsePresenter.MyProxy> {

	@ProxyCodeSplit
	@NameToken(NameTokens.modelbrowse)
	public interface MyProxy extends ProxyPlace<ModelBrowsePresenter> { }
	
	public interface MyView extends ParameterizedView { }
	
	@Inject
	public ModelBrowsePresenter(EventBus eventBus, MyView view, MyProxy proxy) {
		super(eventBus, view, proxy, ApplicationPresenter.CONTENT_SLOT);
	}
}
