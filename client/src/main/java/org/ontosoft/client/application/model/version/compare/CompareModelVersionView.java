package org.ontosoft.client.application.model.version.compare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.PageHeader;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.ontosoft.client.Config;
import org.ontosoft.client.application.ParameterizedViewImpl;
import org.ontosoft.client.components.browse.EntityBrowser;
import org.ontosoft.client.rest.SoftwareREST;
import org.ontosoft.shared.classes.entities.ModelVersion;
import org.ontosoft.shared.classes.entities.SoftwareVersion;
import org.ontosoft.shared.classes.util.KBConstants;
import org.ontosoft.shared.classes.vocabulary.MetadataCategory;
import org.ontosoft.shared.classes.vocabulary.MetadataProperty;
import org.ontosoft.shared.classes.vocabulary.MetadataType;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CompareModelVersionView extends ParameterizedViewImpl implements CompareModelVersionPresenter.MyView {

	@UiField
	FlexTable table;

	@UiField
	PageHeader heading;

	@UiField
	Panel matrixpanel;

	@UiField
	VerticalPanel loading;

	SoftwareREST api;
	Map<String, SoftwareREST> apis;

	Vocabulary vocabulary;
	List<ModelVersion> models;
	boolean swloaded;

	interface Binder extends UiBinder<Widget, CompareModelVersionView> {
	}

	@Inject
	public CompareModelVersionView(Binder binder) {
		initWidget(binder.createAndBindUi(this));
		initAPIs();
		initVocabulary();
		models = new ArrayList<ModelVersion>();
	}

	private void initAPIs() {
		this.api = SoftwareREST.get(Config.getServerURL());

		this.apis = new HashMap<String, SoftwareREST>();
		this.apis.put(SoftwareREST.LOCAL, this.api);

		final List<Map<String, String>> xservers = Config.getExternalServers();
		for (Map<String, String> xserver : xservers) {
			String xname = xserver.get("name");
			String xurl = xserver.get("server");
			SoftwareREST xapi = SoftwareREST.get(xurl);
			this.apis.put(xname, xapi);
		}
	}

	@Override
	public void initializeParameters(String[] params) {
		reset();
		if (params.length > 0) {
			String[] swnames = params[0].split("\\s*,\\s*");
			initModels(swnames);
		}
	}

	private void reset() {
		swloaded = false;
		models.clear();
		table.removeAllRows();
	}

	private void initVocabulary() {
		this.api.getVocabulary(new Callback<Vocabulary, Throwable>() {
			@Override
			public void onSuccess(Vocabulary vocab) {
				vocabulary = vocab;
				if (swloaded)
					showComparisonMatrix();
			}

			@Override
			public void onFailure(Throwable reason) {
				GWT.log("Error fetching Vocabulary", reason);
			}
		}, false);
	}

	private void initModels(final String[] swnames) {
		loading.setVisible(true);
		matrixpanel.setVisible(false);
		for (int i = 0; i < swnames.length; i++) {
			String swname = swnames[i];
			// String[] nsname = swname.split(":");
			String[] urlname = swname.split(":");
			SoftwareREST api = this.api;
			// if(nsname.length > 1) {
			// api = this.apis.get(nsname[0]);
			// swname = nsname[1];
			// }

			// alex commented here
			// api.getModelVersion(urlname[0],urlname[1], new Callback<ModelVersion,
			// Throwable>() {
			// @Override
			// public void onSuccess(ModelVersion f) {
			// models.add(f);
			// if(models.size() == swnames.length) {
			// swloaded = true;
			// if(vocabulary != null)
			// showComparisonMatrix();
			// }
			// }
			// @Override
			// public void onFailure(Throwable exception) {
			// GWT.log("Error fetching model", exception);
			// }
			// }, false);
		}
	}

	private void showComparisonMatrix() {
		if (models == null || models.size() < 2)
			return;

		Collections.sort(models, new Comparator<ModelVersion>() {
			@Override
			public int compare(ModelVersion sw1, ModelVersion sw2) {
				return sw1.getLabel().compareToIgnoreCase(sw2.getLabel());
			}
		});

		String txt = "";
		int i = 0;
		for (ModelVersion sw : models) {
			if (i > 0)
				txt += ", ";
			txt += sw.getLabel();
			i++;
		}
		heading.setSubText(txt);

		addHeading();

		EntityBrowser browser = new EntityBrowser(vocabulary);
		MetadataType type = vocabulary.getType(models.get(0).getType());
		List<MetadataProperty> swprops = vocabulary.getPropertiesForType(type);

		MetadataCategory topcat = vocabulary.getCategory(KBConstants.CATNS() + "MetadataCategory");
		topcat = vocabulary.orderChildCategories(topcat);
		for (String lvl1catid : topcat.getChildren()) {
			MetadataCategory lvl1cat = vocabulary.getCategory(lvl1catid);
			lvl1cat = vocabulary.orderChildCategories(lvl1cat);
			for (String lvl2catid : lvl1cat.getChildren()) {
				MetadataCategory lvl2cat = vocabulary.getCategory(lvl2catid);
				List<MetadataProperty> catprops = vocabulary.getPropertiesInCategory(lvl2cat);
				catprops.retainAll(swprops);
				catprops = vocabulary.orderProperties(catprops);
				// First add required properties
				for (MetadataProperty prop : catprops) {
					if (prop.isRequired())
						addRow(prop, browser);
				}
				// Then add not optional properties
				for (MetadataProperty prop : catprops) {
					if (!prop.isRequired())
						addRow(prop, browser);
				}
			}
		}

		loading.setVisible(false);
		matrixpanel.setVisible(true);
	}

	private void addHeading() {
		int i = 0;
		FlexCellFormatter cellFormatter = table.getFlexCellFormatter();
		for (ModelVersion sw : models) {
			cellFormatter.addStyleName(0, i, "header-cell wrap-long-words");
			cellFormatter.setWidth(0, i, 100 / models.size() + "%");
			table.setWidget(0, i++, new Heading(HeadingSize.H4, sw.getLabel()));
		}
		cellFormatter.addStyleName(0, i - 1, "no-border-cell");
	}

	private void addRow(MetadataProperty prop, EntityBrowser browser) {
		if (prop.getId() != KBConstants.ONTNS() + "hasFunction"
				&& prop.getId() != KBConstants.ONTNS() + "hasImplementationLanguage"
				&& prop.getId() != KBConstants.ONTNS() + "supportsOperatingSystem"
				&& prop.getId() != KBConstants.ONTNS() + "hasKnownIssue") {
			return;
		}
		int numRows = table.getRowCount();
		boolean novalue = true;
		for (ModelVersion sw : models) {
			if (sw.getPropertyValues(prop.getId()).size() > 0) {
				novalue = false;
				break;
			}
		}
		if (novalue)
			return;

		FlexCellFormatter cellFormatter = table.getFlexCellFormatter();
		int i = 0;
		String prophtml = browser.getPropertyHTML(prop, false, false);
		table.setHTML(numRows, 0, prophtml);
		cellFormatter.addStyleName(numRows, 0, "no-border-cell no-padding-cell");
		cellFormatter.setColSpan(numRows, 0, models.size());
		for (ModelVersion sw : models) {
			if (sw.getPropertyValues(prop.getId()).size() == 0)
				cellFormatter.addStyleName(numRows + 1, i, "empty-cell");
			String html = browser.getEntityValuesHTMLCompareVersion(prop, sw.getPropertyValues(prop.getId()), true);
			table.setHTML(numRows + 1, i++, html);
		}
		cellFormatter.addStyleName(numRows + 1, i - 1, "no-border-cell");
	}

}
