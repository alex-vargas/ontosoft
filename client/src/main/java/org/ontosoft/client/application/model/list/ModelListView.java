package org.ontosoft.client.application.model.list;

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
import org.ontosoft.shared.classes.ModelSummary;
import org.ontosoft.shared.classes.entities.Model;
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

public class ModelListView extends ParameterizedViewImpl 
  implements ModelListPresenter.MyView {
  
  boolean isreguser, isadmin;
  
  @UiField
  Button publishbutton, cancelbutton, bigpublishbutton, clearsearch;

  @UiField
  Modal publishdialog;
  
  @UiField
  Button comparebutton, selectionswitch;

  boolean selectionmode = false;

  @UiField
  VerticalPanel loading;
  
  @UiField
  Row content;
  
  @UiField
  TextBox modellabel, searchbox;
  
  @UiField
  FacetedSearchPanel facets;
  
  @UiField(provided = true)
  CellTable<ModelSummary> table = new CellTable<ModelSummary>(40);

  @UiField
  SimplePager pager;
  
  SoftwareREST api;
  Map<String, SoftwareREST> apis;
  Map<String, String> clientUrls;

  private List<ModelSummary> allModelList;
  private HashMap<String, Boolean> filteredModelIdMap =
	      new HashMap<String, Boolean>();
  private ListDataProvider<ModelSummary> modelListProvider = 
	      new ListDataProvider<ModelSummary>();

  private List<ModelSummary> selections;

  private Comparator<ModelSummary> modelCompare;
  
  interface Binder extends UiBinder<Widget, ModelListView> {
  }

  @Inject
  public ModelListView(Binder binder) {
    initWidget(binder.createAndBindUi(this));
    initAPIs();
    initVocabulary();
    initTable();
    initList();
  }
  
  private void initAPIs() {
    this.api = SoftwareREST.get(Config.getServerURL());
    
    this.apis = new HashMap<String, SoftwareREST>();
    this.apis.put(SoftwareREST.LOCAL, this.api);
    this.clientUrls = new HashMap<String, String>();
    
    final List<Map<String, String>> xservers = Config.getExternalServers();
    for(Map<String, String> xserver : xservers) {
      String xname = xserver.get("name");
      String xurl = xserver.get("server");
      String xclient = xserver.get("client");
      SoftwareREST xapi = SoftwareREST.get(xurl);
      this.apis.put(xname, xapi);
      clientUrls.put(xname, xclient);
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
    
    for(final String sname : apis.keySet()) {
      SoftwareREST sapi = apis.get(sname);
      allModelList = new ArrayList<ModelSummary>();
      sapi.getModelsList(new Callback<List<ModelSummary>, Throwable>() {
        @Override
        public void onSuccess(List<ModelSummary> list) {
          for(ModelSummary sum : list) {
            sum.setExternalRepositoryId(sname);
            sum.setExternalRepositoryUrl(clientUrls.get(sname));
          }
          allModelList.addAll(list);
          Collections.sort(allModelList, modelCompare);
          for(ModelSummary sum : list)
        	  filteredModelIdMap.put(sum.getId(), true);
          
          loaded.add(sname);
          if(loaded.size() == apis.size()) {
            modelListProvider.getList().clear();
            modelListProvider.getList().addAll(allModelList);
            modelListProvider.flush();
            initMaterial();
            loading.setVisible(false);
            content.setVisible(true);
            Window.scrollTo(0, 0);              
          }
        }
        @Override
        public void onFailure(Throwable reason) { }
      }, false);       
    }   
  }

  
  @Override
  public void initializeParameters(String[] params) {
    UserSession session = SessionStorage.getSession();
    this.isreguser = false;
    this.isadmin = false;
    if( session != null) {
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
    
    if(this.isadmin || this.isreguser) {
      bigpublishbutton.getParent().setVisible(true);
      table.addStyleName("edit-col");
      table.addStyleName("delete-col");
      table.redraw();
    }
  }
  
  private void initTable() {
    ListHandler<ModelSummary> sortHandler =
        new ListHandler<ModelSummary>(modelListProvider.getList());
    
    selections = new ArrayList<ModelSummary>();
    
    table.addColumnSortHandler(sortHandler);
    table.setEmptyTableWidget(new Label("No Models found.."));
    
    this.modelCompare = new Comparator<ModelSummary>() {
      @Override
      public int compare(ModelSummary model1, ModelSummary model2) {
        if(model1.getLabel() != null && model2.getLabel() != null)
          return model1.getLabel().compareToIgnoreCase(model2.getLabel());
        return 0;
      }
    };
    
    /*SafeHtmlRenderer<String> anchorRenderer = new AbstractSafeHtmlRenderer<String>() {
      @Override
      public SafeHtml render(String object) {
        SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendHtmlConstant("<a href=\"javascript:;\">").appendEscaped(object)
            .appendHtmlConstant("</a>");
        return sb.toSafeHtml();
      }
    };*/
    
    // Name Column
    // TODO: Add extra rows for associated Software Versions too (indented)
    final SafeHtmlCell progressCell = new SafeHtmlCell();
    Column<ModelSummary, SafeHtml> namecol = new Column<ModelSummary, SafeHtml>(progressCell) {
        @Override
        public SafeHtml getValue(ModelSummary summary) {
            SafeHtmlBuilder sb = new SafeHtmlBuilder();
            
            String link = "#" + NameTokens.browse + "/" + summary.getName();
            String extralabel = "";
            
            if(!summary.getExternalRepositoryId().equals(SoftwareREST.LOCAL)) {
              link = summary.getExternalRepositoryUrl() + link;
              extralabel = "<div class='external-repo'>" 
                  + summary.getExternalRepositoryId()+"</div>";
            }
            
            sb.appendHtmlConstant("<div class='software-list-item'>");
            sb.appendHtmlConstant("<div class='software-name'>");
            sb.appendHtmlConstant(extralabel);
            if (summary.getModelName() != null)
              sb.appendHtmlConstant("<a href='" + link + "'>" + summary.getModelName() + "</a>");
            else
              sb.appendHtmlConstant("<a href='" + link + "'>" + summary.getLabel() + "</a>");
            sb.appendHtmlConstant("</div>");
            
            if(summary.getDescription() != null)
              sb.appendHtmlConstant("<div class='wrap-pre wrap-long-words software-description'>" + 
                  summary.getDescription() + "</div>");
            
            if(summary.getAuthors() != null) {
              if(summary.getAuthors().size() == 1) {
                sb.appendHtmlConstant("<div class='software-meta'>Author: " +
                    summary.getAuthors().get(0)+" </div>");
              }
              else {
                String authors = "";
                for(int i=0; i<summary.getAuthors().size(); i++) {
                  if(i > 0) authors += ", ";
                  authors += summary.getAuthors().get(i);
                }
                sb.appendHtmlConstant("<div class='software-meta'>Authors: " +
                    authors+" </div>");
              }
            }
            
            if(summary.getUser() != null) {
              String poststring = "<div class='software-meta'>Posted by: " + summary.getUser();
              if(summary.getTime() > 0) {
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
    sortHandler.setComparator(namecol, this.modelCompare);
    table.getColumnSortList().push(namecol);
    
    // Delete Button Column
    Column<ModelSummary, String> deletecolumn = 
        new Column<ModelSummary, String>(
            new ButtonCell(IconType.REMOVE, ButtonType.DANGER, ButtonSize.EXTRA_SMALL)) {
      @Override
      public String getValue(ModelSummary details) {
        return "Delete";
      }
      @Override
      public String getCellStyleNames(Context context,
    		  ModelSummary summary) {
        UserSession session = SessionStorage.getSession();
        String style = "hidden-cell";
        if (isadmin || 
            (isreguser && 
                PermUtils.hasOwnerAccess(summary.getPermission(), session.getUsername()))) {
          if(summary.getExternalRepositoryId().equals(SoftwareREST.LOCAL))
            style = "delete-cell";
        }
        return style;
      }
    };
    deletecolumn.setFieldUpdater(new FieldUpdater<ModelSummary, String>() {
      @Override
      public void update(int index, ModelSummary summary, String value) {
        deleteModel(summary);
      }
    });
    table.addColumn(deletecolumn);

    // Edit Button Column
    final Column<ModelSummary, String> editcol = 
        new Column<ModelSummary, String>(
            new ButtonCell(IconType.EDIT, ButtonType.INFO, ButtonSize.EXTRA_SMALL)) {
        @Override
        public String getValue(ModelSummary details) {
          return "Edit";
        }
        @Override
        public String getCellStyleNames(Context context,
        		ModelSummary summary) {
          UserSession session = SessionStorage.getSession();
          String style = "hidden-cell";
          if (isadmin ||
              (isreguser && 
                  (PermUtils.getAccessLevelForUser(
                  summary.getPermission(), session.getUsername(), summary.getId()) 
                  .equals("Write"))))
            if(summary.getExternalRepositoryId().equals(SoftwareREST.LOCAL))
              style = "edit-cell";
          return style;
        }     
    };
    
    editcol.setFieldUpdater(new FieldUpdater<ModelSummary, String>() {
      @Override
      public void update(int index, ModelSummary summary, String value) {
        String swname = summary.getName();
        History.newItem(NameTokens.publish + "/" + swname);
      }
    });
    table.addColumn(editcol);
    
    // Checkbox, software selection column (to select software to compare)
    Column<ModelSummary, Boolean> checkboxcolumn = 
        new Column<ModelSummary, Boolean>(new CheckboxCell(true, true)) {
        @Override
        public Boolean getValue(ModelSummary summary) {
          return selections.contains(summary);
        }
    };
    checkboxcolumn.setFieldUpdater(new FieldUpdater<ModelSummary, Boolean>() {
        @Override
        public void update(int index, ModelSummary summary, Boolean value) {
          if(value) 
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
    table.setRowStyles(new RowStyles<ModelSummary>() {
      @Override
      public String getStyleNames(ModelSummary summary, int rowIndex) {
       if(selections.contains(summary))
         return "selected-row";
       return "";
      }
    });
    
    
    // Bind list & pager to table
    pager.setDisplay(table);
    modelListProvider.addDataDisplay(table);
  }
  
  private void deleteModel(final ModelSummary model) {
    if (Window.confirm("Are you sure you want to delete the model ?")) {
      this.api.deleteModel(model.getName(),
          new Callback<Void, Throwable>() {
            @Override
            public void onSuccess(Void v) {
              removeFromList(model);
              updateList();
            }
            @Override
            public void onFailure(Throwable exception) { }
          }
      );
    }
  }
  
  @UiHandler("publishdialog")
  void onShowWindow(ModalShownEvent event) {
    modellabel.setFocus(true);
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
  
  @UiHandler("modellabel")
  void onSoftwareEnter(KeyPressEvent event) {
    if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
      submitPublishForm();
    }
  }
  
  @UiHandler("cancelbutton")
  void onCancelPublish(ClickEvent event) {
	  modellabel.setValue(null);
  }
  
  private void submitPublishForm() {
    String label = modellabel.getValue();
    if(modellabel.validate(true)) {
      Model tmpModel = new Model();
      tmpModel.setLabel(label);
      this.api.publishModel(tmpModel, new Callback<Model, Throwable>() {
        public void onSuccess(Model model) {
          // Add item to list
          ModelSummary newModel = new ModelSummary(model);
          newModel.setExternalRepositoryId(SoftwareREST.LOCAL);
          addToList(newModel);
          updateList();
          
          // Go to the new item
          History.newItem(NameTokens.publish + "/" + model.getName());
          
          publishdialog.hide();
          modellabel.setValue(null);
        }
        @Override
        public void onFailure(Throwable exception) { }
      });
    } 
  }
  
  private void addToList(ModelSummary summary) {
    boolean contains = false;
    for(ModelSummary sum : allModelList) {
      if(sum.getId().equals(summary.getId())) {
        contains = true;
        break;
      }
    }
    if(!contains)
      allModelList.add(summary);
    filteredModelIdMap.put(summary.getId(), true);
    Collections.sort(allModelList, modelCompare);    
  }
  
  private void removeFromList(ModelSummary summary) {
    filteredModelIdMap.remove(summary);
    allModelList.remove(summary);
  }
  
  @UiHandler("searchbox")
  void onSearch(KeyUpEvent event) {
    updateList();
  }
  
  @UiHandler("clearsearch")
  void onClearSearch(ClickEvent event) {
    searchbox.setValue("");
    updateList();
  }
  
  void updateList() {
    modelListProvider.getList().clear();
    String value = searchbox.getValue();
    for(ModelSummary summary : allModelList) {
      if(filteredModelIdMap.containsKey(summary.getId())) {
        if(value == null || value.equals("") ||
            summary.getLabel().toLowerCase().contains(value.toLowerCase()))
        modelListProvider.getList().add(summary);
      }
    }
    this.modelListProvider.flush();    
  }
  
  void updateCompareButton() {
    String text = "Compare";
    if(selections.size() > 0)
      text += " ("+selections.size()+")";
    comparebutton.setText(text);
  }
  
  @UiHandler("facets")
  void onFacetSelection(FacetSelectionEvent event) {
    final List<String> loaded = new ArrayList<String>();
    
    final List<ModelSummary> facetList = new ArrayList<ModelSummary>();
    for(final String sname : apis.keySet()) {
      SoftwareREST sapi = apis.get(sname);
      sapi.getModelListFaceted(facets.getFacets(),
          new Callback<List<ModelSummary>, Throwable>() {
        @Override
        public void onSuccess(List<ModelSummary> list) {
          facetList.addAll(list);
          loaded.add(sname);
          if(loaded.size() == apis.size()) {
            filteredModelIdMap.clear();
            for(ModelSummary flist: facetList)
              filteredModelIdMap.put(flist.getId(), true);
            updateList();
          }
        }
        @Override
        public void onFailure(Throwable reason) { }
      });
    }
  }
  
  @UiHandler("selectionswitch")
  void onSelectionModeToggle(ClickEvent event) {
    if(!selectionmode) {
      selectionmode = true;
      selectionswitch.setIcon(IconType.CHECK_SQUARE_O);
      table.addStyleName("selection-table");
    }
    else {
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
    if(selections.size() < 2)
      Window.alert("Select at least 2 software to compare.\n\n"
          + "Click on the checkbox next to the compare button.\n"
          + "Then select software using checkboxes in each row");
    else if(selections.size() > 10)
      Window.alert("Cannot compare more than 10 at a time");    
    else {
      int i=0;
      for(ModelSummary summary : selections) {
        if(i > 0) idtext += ",";
        if(!summary.getExternalRepositoryId().equals(SoftwareREST.LOCAL))
          idtext += summary.getExternalRepositoryId()+":";
        idtext += summary.getName();
        i++;
      }
      History.newItem(NameTokens.compare + "/" + idtext);
    }
  }

}