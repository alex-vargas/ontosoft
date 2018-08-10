package org.ontosoft.client.application.model.browse;

import org.ontosoft.client.application.browse.BrowseView;
import org.ontosoft.client.place.NameTokens;
import org.ontosoft.shared.classes.util.KBConstants;

import com.google.inject.Inject;

public class ModelBrowseView extends BrowseView implements ModelBrowsePresenter.MyView {

	@Inject
	public ModelBrowseView(Binder binder) {
		super(binder);

	}

	@Override
	public void initPlaces() {
		setPublishPlace(NameTokens.publishModel);
		setVersionsPlace(NameTokens.modelVersion);
		setPublishVersionsPlace(NameTokens.publishModelVersion);
	}

	/**
	 * Returns the namespace for "hasCreator" using OntoSoft Ontology NameSpace
	 * 
	 * @return
	 */
	@Override
	public String getHasCreatorNameSpace() {
		return KBConstants.ONTNS() + "hasModelCreator";
	}

	/**
	 * Returns the namespace for "hasName" using OntoSoft Ontology NameSpace
	 * 
	 * @return
	 */
	@Override
	public String getHasNameNameSpace() {
		return KBConstants.ONTNS() + "hasModelName";
	}
}
