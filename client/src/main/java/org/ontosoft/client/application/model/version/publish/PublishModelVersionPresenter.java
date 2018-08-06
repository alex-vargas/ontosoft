package org.ontosoft.client.application.model.version.publish;

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

public class PublishModelVersionPresenter extends
    Presenter<PublishModelVersionPresenter.MyView, PublishModelVersionPresenter.MyProxy> {
  
  @ProxyCodeSplit
  @NameToken(NameTokens.publishModelVersion)
  public interface MyProxy extends ProxyPlace<PublishModelVersionPresenter> { }

  public interface MyView extends ParameterizedView { }
  
  @Inject
  public PublishModelVersionPresenter(EventBus eventBus, MyView view, final MyProxy proxy,
      final PlaceManager placemanager) {
    super(eventBus, view, proxy, ApplicationPresenter.CONTENT_SLOT);
  }
}
