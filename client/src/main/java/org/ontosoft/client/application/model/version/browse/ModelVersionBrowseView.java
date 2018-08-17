package org.ontosoft.client.application.model.version.browse;

import java.util.List;

import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.constants.DeviceSize;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.PanelType;
import org.ontosoft.client.application.version.browse.VersionBrowseView;
import org.ontosoft.client.authentication.SessionStorage;
import org.ontosoft.client.components.browse.EntityBrowser;
import org.ontosoft.client.components.chart.CategoryBarChart;
import org.ontosoft.client.components.chart.CategoryPieChart;
import org.ontosoft.client.place.NameTokens;
import org.ontosoft.shared.classes.entities.Entity;
import org.ontosoft.shared.classes.entities.ModelVersion;
import org.ontosoft.shared.classes.users.UserSession;
import org.ontosoft.shared.classes.util.KBConstants;
import org.ontosoft.shared.classes.vocabulary.MetadataCategory;
import org.ontosoft.shared.classes.vocabulary.MetadataProperty;
import org.ontosoft.shared.classes.vocabulary.MetadataType;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;

import com.github.gwtd3.api.D3;
import com.github.gwtd3.api.core.Value;
import com.github.gwtd3.api.functions.DatumFunction;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ModelVersionBrowseView extends VersionBrowseView implements ModelVersionBrowsePresenter.MyView {


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
