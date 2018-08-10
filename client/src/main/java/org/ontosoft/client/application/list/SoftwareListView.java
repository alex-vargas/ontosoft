package org.ontosoft.client.application.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.gwt.ButtonCell;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.ontosoft.client.Config;
import org.ontosoft.client.application.ParameterizedViewImpl;
import org.ontosoft.client.authentication.SessionStorage;
import org.ontosoft.client.components.form.facet.FacetedSearchPanel;
import org.ontosoft.client.components.form.facet.events.FacetSelectionEvent;
import org.ontosoft.client.place.NameTokens;
import org.ontosoft.client.rest.SoftwareREST;
import org.ontosoft.shared.classes.SoftwareSummary;
import org.ontosoft.shared.classes.entities.Software;
import org.ontosoft.shared.classes.users.UserSession;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;
import org.ontosoft.shared.utils.PermUtils;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;

public class SoftwareListView extends ParameterizedViewImpl implements SoftwareListPresenter.MyView {

	private boolean isModel = false;

	private String browsePlace, publishPlace, comparePlace;

	boolean isreguser, isadmin;

	@UiField
	Button publishbutton, cancelbutton, bigpublishbutton, clearsearch;

	@UiField
	Modal publishdialog;

	@UiField
	Button comparebutton, selectionswitch;

	boolean selectionmode = false;

	@UiField
	public VerticalPanel loading;

	@UiField
	public Row content;

	@UiField
	TextBox softwarelabel;

	@UiField
	TextBox searchbox;

	@UiField
	FacetedSearchPanel facets;

	@UiField(provided = true)
	CellTable<SoftwareSummary> table = new CellTable<SoftwareSummary>(40);

	@UiField
	SimplePager pager;

	SoftwareREST api;
	protected Map<String, SoftwareREST> apis;
	private Map<String, String> clientUrls;

	private List<SoftwareSummary> allSoftwareList;
	private HashMap<String, Boolean> filteredSoftwareIdMap = new HashMap<String, Boolean>();
	private ListDataProvider<SoftwareSummary> listProvider = new ListDataProvider<SoftwareSummary>();

	private List<SoftwareSummary> selections;

	private Comparator<SoftwareSummary> swcompare;

	public interface Binder extends UiBinder<Widget, SoftwareListView> {
	}

	@Inject
	public SoftwareListView(Binder binder) {
		initWidget(binder.createAndBindUi(this));
		initAPIs();
		initVocabulary();
		initTable();
		initList();
		initPlaces();
	}

	public void initPlaces() {
		setBrowsePlace(NameTokens.browse);
		setPublishPlace(NameTokens.publish);
		setComparePlace(NameTokens.compare);
	}

	public String getBrowsePlace() {
		return browsePlace;
	}

	public void setBrowsePlace(String browsePlace) {
		this.browsePlace = browsePlace;
	}

	public String getPublishPlace() {
		return publishPlace;
	}

	public void setPublishPlace(String publishPlace) {
		this.publishPlace = publishPlace;
	}

	public String getComparePlace() {
		return comparePlace;
	}

	public void setComparePlace(String comparePlace) {
		this.comparePlace = comparePlace;
	}

	public void setIsModel(boolean value) {
		isModel = value;
	}

	public boolean isModel() {
		return isModel;
	}

	private void initAPIs() {
		this.api = SoftwareREST.get(Config.getServerURL());

		this.apis = new HashMap<String, SoftwareREST>();
		this.apis.put(SoftwareREST.LOCAL, this.api);
		this.setClientUrls(new HashMap<String, String>());

		final List<Map<String, String>> xservers = Config.getExternalServers();
		for (Map<String, String> xserver : xservers) {
			String xname = xserver.get("name");
			String xurl = xserver.get("server");
			String xclient = xserver.get("client");
			SoftwareREST xapi = SoftwareREST.get(xurl);
			this.apis.put(xname, xapi);
			getClientUrls().put(xname, xclient);
		}
	}

	private void initVocabulary() {
		this.api.getVocabulary(new Callback<Vocabulary, Throwable>() {
			@Override
			public void onSuccess(Vocabulary vocab) {
				facets.showFacetGroups(vocab);
			}

			@Override
			public void onFailure(Throwable reason) {
				GWT.log("Error fetching Vocabulary", reason);
			}
		}, false);
	}

	public void initList() {
		final List<String> loaded = new ArrayList<String>();

		for (final String sname : apis.keySet()) {
			SoftwareREST sapi = apis.get(sname);
			setAllSoftwareList(new ArrayList<SoftwareSummary>());
			Callback<List<SoftwareSummary>, Throwable> callback = new Callback<List<SoftwareSummary>, Throwable>() {
				@Override
				public void onSuccess(List<SoftwareSummary> list) {
					for (SoftwareSummary sum : list) {
						sum.setExternalRepositoryId(sname);
						sum.setExternalRepositoryUrl(getClientUrls().get(sname));
					}
					getAllSoftwareList().addAll(list);
					Collections.sort(getAllSoftwareList(), getSwcompare());
					for (SoftwareSummary sum : list)
						getFilteredSoftwareIdMap().put(sum.getId(), true);

					loaded.add(sname);
					if (loaded.size() == apis.size()) {
						getListProvider().getList().clear();
						getListProvider().getList().addAll(getAllSoftwareList());
						getListProvider().flush();
						initMaterial();
						getLoading().setVisible(false);
						getContent().setVisible(true);
						Window.scrollTo(0, 0);
					}
				}

				@Override
				public void onFailure(Throwable reason) {
				}
			};
			setSoftwareList(sapi, callback);
		}
	}

	public void setSoftwareList(SoftwareREST sapi, Callback<List<SoftwareSummary>, Throwable> callback) {
		sapi.getSoftwareList(callback, false);
	}

	@Override
	public void initializeParameters(String[] params) {
		UserSession session = SessionStorage.getSession();
		this.isreguser = false;
		this.isadmin = false;
		if (session != null) {
			this.isreguser = true;
			if (session.getRoles().contains("admin"))
				this.isadmin = true;
		}
		this.updateList();
		this.redrawControls();
	}

	public void redrawControls() {
		table.removeStyleName("edit-col");
		table.removeStyleName("delete-col");

		bigpublishbutton.getParent().setVisible(false);

		if (this.isadmin || this.isreguser) {
			bigpublishbutton.getParent().setVisible(true);
			table.addStyleName("edit-col");
			table.addStyleName("delete-col");
			table.redraw();
		}
	}

	private void initTable() {
		ListHandler<SoftwareSummary> sortHandler = new ListHandler<SoftwareSummary>(getListProvider().getList());

		selections = new ArrayList<SoftwareSummary>();

		table.addColumnSortHandler(sortHandler);
		table.setEmptyTableWidget(new Label("No Software found.."));

		this.setSwcompare(new Comparator<SoftwareSummary>() {
			@Override
			public int compare(SoftwareSummary sw1, SoftwareSummary sw2) {
				if (sw1.getLabel() != null && sw2.getLabel() != null)
					return sw1.getLabel().compareToIgnoreCase(sw2.getLabel());
				return 0;
			}
		});

		/*
		 * SafeHtmlRenderer<String> anchorRenderer = new
		 * AbstractSafeHtmlRenderer<String>() {
		 * 
		 * @Override public SafeHtml render(String object) { SafeHtmlBuilder sb = new
		 * SafeHtmlBuilder();
		 * sb.appendHtmlConstant("<a href=\"javascript:;\">").appendEscaped(object)
		 * .appendHtmlConstant("</a>"); return sb.toSafeHtml(); } };
		 */

		// Name Column
		// TODO: Add extra rows for associated Software Versions too (indented)
		final SafeHtmlCell progressCell = new SafeHtmlCell();
		Column<SoftwareSummary, SafeHtml> namecol = new Column<SoftwareSummary, SafeHtml>(progressCell) {
			@Override
			public SafeHtml getValue(SoftwareSummary summary) {
				SafeHtmlBuilder sb = new SafeHtmlBuilder();

				String link = "#" + browsePlace + "/" + summary.getName();
				String extralabel = "";

				if (!summary.getExternalRepositoryId().equals(SoftwareREST.LOCAL)) {
					link = summary.getExternalRepositoryUrl() + link;
					extralabel = "<div class='external-repo'>" + summary.getExternalRepositoryId() + "</div>";
				}

				sb.appendHtmlConstant("<div class='software-list-item'>");
				sb.appendHtmlConstant("<div class='software-name'>");
				sb.appendHtmlConstant(extralabel);
				if (summary.getSoftwareName() != null)
					sb.appendHtmlConstant("<a href='" + link + "'>" + summary.getSoftwareName() + "</a>");
				else
					sb.appendHtmlConstant("<a href='" + link + "'>" + summary.getLabel() + "</a>");
				sb.appendHtmlConstant("</div>");

				if (summary.getDescription() != null)
					sb.appendHtmlConstant("<div class='wrap-pre wrap-long-words software-description'>"
							+ summary.getDescription() + "</div>");

				if (summary.getAuthors() != null) {
					if (summary.getAuthors().size() == 1) {
						sb.appendHtmlConstant(
								"<div class='software-meta'>Author: " + summary.getAuthors().get(0) + " </div>");
					} else {
						String authors = "";
						for (int i = 0; i < summary.getAuthors().size(); i++) {
							if (i > 0)
								authors += ", ";
							authors += summary.getAuthors().get(i);
						}
						sb.appendHtmlConstant("<div class='software-meta'>Authors: " + authors + " </div>");
					}
				}

				if (summary.getUser() != null) {
					String poststring = "<div class='software-meta'>Posted by: " + summary.getUser();
					if (summary.getTime() > 0) {
						DateTimeFormat fmt = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);
						String datestr = fmt.format(new Date(summary.getTime()));
						poststring += " at " + datestr;
					}
					poststring += "</div>";
					sb.appendHtmlConstant(poststring);
				}
				sb.appendHtmlConstant("</div>");
				return sb.toSafeHtml();
			}
		};

		table.addColumn(namecol);
		namecol.setSortable(true);
		sortHandler.setComparator(namecol, this.getSwcompare());
		table.getColumnSortList().push(namecol);

		// Delete Button Column
		Column<SoftwareSummary, String> deletecolumn = new Column<SoftwareSummary, String>(
				new ButtonCell(IconType.REMOVE, ButtonType.DANGER, ButtonSize.EXTRA_SMALL)) {
			@Override
			public String getValue(SoftwareSummary details) {
				return "Delete";
			}

			@Override
			public String getCellStyleNames(Context context, SoftwareSummary summary) {
				UserSession session = SessionStorage.getSession();
				String style = "hidden-cell";
				if (isadmin
						|| (isreguser && PermUtils.hasOwnerAccess(summary.getPermission(), session.getUsername()))) {
					if (summary.getExternalRepositoryId().equals(SoftwareREST.LOCAL))
						style = "delete-cell";
				}
				return style;
			}
		};
		deletecolumn.setFieldUpdater(new FieldUpdater<SoftwareSummary, String>() {
			@Override
			public void update(int index, SoftwareSummary summary, String value) {
				deleteSoftware(summary);
			}
		});
		table.addColumn(deletecolumn);

		// Edit Button Column
		final Column<SoftwareSummary, String> editcol = new Column<SoftwareSummary, String>(
				new ButtonCell(IconType.EDIT, ButtonType.INFO, ButtonSize.EXTRA_SMALL)) {
			@Override
			public String getValue(SoftwareSummary details) {
				return "Edit";
			}

			@Override
			public String getCellStyleNames(Context context, SoftwareSummary summary) {
				UserSession session = SessionStorage.getSession();
				String style = "hidden-cell";
				if (isadmin || (isreguser && (PermUtils
						.getAccessLevelForUser(summary.getPermission(), session.getUsername(), summary.getId())
						.equals("Write"))))
					if (summary.getExternalRepositoryId().equals(SoftwareREST.LOCAL))
						style = "edit-cell";
				return style;
			}
		};

		editcol.setFieldUpdater(new FieldUpdater<SoftwareSummary, String>() {
			@Override
			public void update(int index, SoftwareSummary summary, String value) {
				String swname = summary.getName();
				History.newItem(publishPlace + "/" + swname);
			}
		});
		table.addColumn(editcol);

		// Checkbox, software selection column (to select software to compare)
		Column<SoftwareSummary, Boolean> checkboxcolumn = new Column<SoftwareSummary, Boolean>(
				new CheckboxCell(true, true)) {
			@Override
			public Boolean getValue(SoftwareSummary summary) {
				return selections.contains(summary);
			}
		};
		checkboxcolumn.setFieldUpdater(new FieldUpdater<SoftwareSummary, Boolean>() {
			@Override
			public void update(int index, SoftwareSummary summary, Boolean value) {
				if (value)
					selections.add(summary);
				else
					selections.remove(summary);
				table.redrawRow(index);
				updateCompareButton();
			}
		});

		checkboxcolumn.setCellStyleNames("selection-cell");
		table.addColumn(checkboxcolumn);

		// Set row styles
		table.setRowStyles(new RowStyles<SoftwareSummary>() {
			@Override
			public String getStyleNames(SoftwareSummary summary, int rowIndex) {
				if (selections.contains(summary))
					return "selected-row";
				return "";
			}
		});

		// Bind list & pager to table
		pager.setDisplay(table);
		getListProvider().addDataDisplay(table);
	}

	private void deleteSoftware(final SoftwareSummary sw) {
		if (Window.confirm("Are you sure you want to delete the software ?")) {
			this.api.deleteSoftware(sw.getName(), new Callback<Void, Throwable>() {
				@Override
				public void onSuccess(Void v) {
					removeFromList(sw);
					updateList();
				}

				@Override
				public void onFailure(Throwable exception) {
				}
			});
		}
	}

	@UiHandler("publishdialog")
	void onShowWindow(ModalShownEvent event) {
		softwarelabel.setFocus(true);
	}

	@UiHandler("bigpublishbutton")
	void onOpenPublishButtonClick(ClickEvent event) {
		publishdialog.show();
	}

	@UiHandler("publishbutton")
	void onPublishButtonClick(ClickEvent event) {
		submitPublishForm();
		event.stopPropagation();
	}

	@UiHandler("softwarelabel")
	void onSoftwareEnter(KeyPressEvent event) {
		if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
			submitPublishForm();
		}
	}

	@UiHandler("cancelbutton")
	void onCancelPublish(ClickEvent event) {
		softwarelabel.setValue(null);
	}

	private void submitPublishForm() {
		if (softwarelabel.validate(true)) {
			publishSoftware();
		}
	}

	public void publishSoftware() {
		Software tmpsw = new Software();
		tmpsw.setLabel(softwarelabel.getValue());
		Callback<Software, Throwable> callback = new Callback<Software, Throwable>() {
			public void onSuccess(Software sw) {
				// Add item to list
				SoftwareSummary newsw = new SoftwareSummary(sw);
				newsw.setExternalRepositoryId(SoftwareREST.LOCAL);
				addToList(newsw);
				updateList();

				// Go to the new item
				History.newItem(publishPlace + "/" + sw.getName());
				publishdialog.hide();
				softwarelabel.setValue(null);
			}

			@Override
			public void onFailure(Throwable exception) {
			}
		};

		if (isModel)
			this.api.publishSoftware(tmpsw, callback, true);
		else
			this.api.publishSoftware(tmpsw, callback, false);
	}

	private void addToList(SoftwareSummary summary) {
		boolean contains = false;
		for (SoftwareSummary sum : getAllSoftwareList()) {
			if (sum.getId().equals(summary.getId())) {
				contains = true;
				break;
			}
		}
		if (!contains)
			getAllSoftwareList().add(summary);
		getFilteredSoftwareIdMap().put(summary.getId(), true);
		Collections.sort(getAllSoftwareList(), getSwcompare());
	}

	private void removeFromList(SoftwareSummary summary) {
		getFilteredSoftwareIdMap().remove(summary);
		getAllSoftwareList().remove(summary);
	}

	@UiHandler("searchbox")
	void onSearch(KeyUpEvent event) {
		updateList();
	}

	@UiHandler("clearsearch")
	void onClearSearch(ClickEvent event) {
		getSearchbox().setValue("");
		updateList();
	}

	public void updateList() {
		getListProvider().getList().clear();
		String value = getSearchbox().getValue();
		for (SoftwareSummary summary : getAllSoftwareList()) {
			if (getFilteredSoftwareIdMap().containsKey(summary.getId())) {
				if (value == null || value.equals("") || summary.getLabel().toLowerCase().contains(value.toLowerCase()))
					getListProvider().getList().add(summary);
			}
		}
		this.getListProvider().flush();
	}

	void updateCompareButton() {
		String text = "Compare";
		if (selections.size() > 0)
			text += " (" + selections.size() + ")";
		comparebutton.setText(text);
	}

	@UiHandler("facets")
	void onFacetSelection(FacetSelectionEvent event) {
		final List<String> loaded = new ArrayList<String>();

		final List<SoftwareSummary> facetList = new ArrayList<SoftwareSummary>();
		for (final String sname : apis.keySet()) {
			SoftwareREST sapi = apis.get(sname);
			sapi.getSoftwareListFaceted(facets.getFacets(), new Callback<List<SoftwareSummary>, Throwable>() {
				@Override
				public void onSuccess(List<SoftwareSummary> list) {
					facetList.addAll(list);
					loaded.add(sname);
					if (loaded.size() == apis.size()) {
						getFilteredSoftwareIdMap().clear();
						for (SoftwareSummary flist : facetList)
							getFilteredSoftwareIdMap().put(flist.getId(), true);
						updateList();
					}
				}

				@Override
				public void onFailure(Throwable reason) {
				}
			});
		}
	}

	@UiHandler("selectionswitch")
	void onSelectionModeToggle(ClickEvent event) {
		if (!selectionmode) {
			selectionmode = true;
			selectionswitch.setIcon(IconType.CHECK_SQUARE_O);
			table.addStyleName("selection-table");
		} else {
			selectionmode = false;
			selectionswitch.setIcon(IconType.SQUARE_O);
			table.removeStyleName("selection-table");

			// Remove all selections
			selections.clear();
			table.redraw();
			updateCompareButton();
		}
	}

	@UiHandler("comparebutton")
	void onCompareButtonClick(ClickEvent event) {
		String idtext = "";
		if (selections.size() < 2)
			Window.alert(
					"Select atleast 2 software to compare.\n\n" + "Click on the checkbox next to the compare button.\n"
							+ "Then select software using checkboxes in each row");
		else if (selections.size() > 10)
			Window.alert("Cannot compare more than 10 at a time");
		else {
			int i = 0;
			for (SoftwareSummary summary : selections) {
				if (i > 0)
					idtext += ",";
				if (!summary.getExternalRepositoryId().equals(SoftwareREST.LOCAL))
					idtext += summary.getExternalRepositoryId() + ":";
				idtext += summary.getName();
				i++;
			}
			History.newItem(comparePlace + "/" + idtext);
		}
	}

	public Map<String, String> getClientUrls() {
		return clientUrls;
	}

	public void setClientUrls(Map<String, String> clientUrls) {
		this.clientUrls = clientUrls;
	}

	public Comparator<SoftwareSummary> getSwcompare() {
		return swcompare;
	}

	public void setSwcompare(Comparator<SoftwareSummary> swcompare) {
		this.swcompare = swcompare;
	}

	public VerticalPanel getLoading() {
		return loading;
	}

	public void setLoading(VerticalPanel loading) {
		this.loading = loading;
	}

	public Row getContent() {
		return content;
	}

	public void setContent(Row content) {
		this.content = content;
	}

	public TextBox getSearchbox() {
		return searchbox;
	}

	public void setSearchbox(TextBox searchbox) {
		this.searchbox = searchbox;
	}

	public HashMap<String, Boolean> getFilteredSoftwareIdMap() {
		return filteredSoftwareIdMap;
	}

	public void setFilteredSoftwareIdMap(HashMap<String, Boolean> filteredSoftwareIdMap) {
		this.filteredSoftwareIdMap = filteredSoftwareIdMap;
	}

	public List<SoftwareSummary> getAllSoftwareList() {
		return allSoftwareList;
	}

	public void setAllSoftwareList(List<SoftwareSummary> allSoftwareList) {
		this.allSoftwareList = allSoftwareList;
	}

	public ListDataProvider<SoftwareSummary> getListProvider() {
		return listProvider;
	}

	public void setListProvider(ListDataProvider<SoftwareSummary> listProvider) {
		this.listProvider = listProvider;
	}

}
