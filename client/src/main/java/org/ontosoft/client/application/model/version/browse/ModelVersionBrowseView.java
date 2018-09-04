package org.ontosoft.client.application.model.version.browse;

import org.ontosoft.client.application.version.browse.VersionBrowseView;
import org.ontosoft.client.place.NameTokens;
import org.ontosoft.shared.classes.util.KBConstants;

import com.google.inject.Inject;

public class ModelVersionBrowseView extends VersionBrowseView 
	implements ModelVersionBrowsePresenter.MyView {


	@Inject
	public ModelVersionBrowseView(Binder binder) {
		super(binder);
		setIsModel(true);
	}

	@Override
	public void initPlaces() {
		setBrowsePlace(NameTokens.modelBrowse);
		setPublishVersionPlace(NameTokens.publishModelVersion);
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
