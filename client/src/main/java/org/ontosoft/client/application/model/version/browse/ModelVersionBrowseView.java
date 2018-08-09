package org.ontosoft.client.application.model.version.browse;

import java.util.List;

import org.fusesource.restygwt.client.JsonEncoderDecoder;
import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.PageHeader;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.DeviceSize;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.PanelType;
import org.ontosoft.client.Config;
import org.ontosoft.client.application.ParameterizedViewImpl;
import org.ontosoft.client.authentication.SessionStorage;
import org.ontosoft.client.components.browse.EntityBrowser;
import org.ontosoft.client.components.chart.CategoryBarChart;
import org.ontosoft.client.components.chart.CategoryPieChart;
import org.ontosoft.client.place.NameTokens;
import org.ontosoft.client.rest.SoftwareREST;
import org.ontosoft.shared.classes.ModelVersionSummary;
import org.ontosoft.shared.classes.entities.Entity;
import org.ontosoft.shared.classes.entities.Model;
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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ModelVersionBrowseView extends ParameterizedViewImpl 
  implements ModelVersionBrowsePresenter.MyView {

  interface Binder extends UiBinder<Widget, ModelVersionBrowseView> {}
  
  @UiField
  PageHeader modelTitle;

  @UiField
  VerticalPanel modelBody;

  @UiField
  VerticalPanel loading;
  
  @UiField
  Button publishbutton, cancelbutton, bigpublishbutton;
  
  @UiField
  Modal publishdialog;
  
  @UiField
  TextBox modellabel;
  
  @UiField
  VerticalPanel modelNameVP;
  
  @UiField
  Button htmlbutton, rdfbutton, jsonbutton, editbutton;
  
  @UiField
  CategoryPieChart piechart;
  
  SoftwareREST api = SoftwareREST.get(Config.getServerURL());

  ModelVersion version;
  Model model;
  String modelNameStr;
  String modelRDF;
  String modelHTML;

  Vocabulary vocabulary;
  
  public interface SoftwareCodec extends JsonEncoderDecoder<ModelVersion> {}
  SoftwareCodec codec;
  
  @Inject
  public ModelVersionBrowseView(Binder binder) {
    initWidget(binder.createAndBindUi(this));
    initVocabulary();
    
    this.codec = GWT.create(SoftwareCodec.class);
  }
  
  public static void setBrowserWindowTitle (String newTitle) {
    if (Document.get() != null) {
        Document.get().setTitle (newTitle + " - OntoSoft Portal");
    }
  }

  // If some parameters are passed in, initialize the software and interface
  public void initializeParameters(String[] params) {    
    clear();
    if(params.length > 0) {
      this.modelNameStr = params[0];
      initModel(this.modelNameStr);
    }
    UserSession session = SessionStorage.getSession();
    if(session != null && session.getUsername() != null)
      editbutton.setVisible(true);
  }
  
  private void initVocabulary() {
    this.api.getVocabulary(new Callback<Vocabulary, Throwable>() {
      @Override
      public void onSuccess(Vocabulary vocab) {
        vocabulary = vocab;
        if(version != null)
          showModel(version);
      }
      @Override
      public void onFailure(Throwable reason) {
        GWT.log("Error fetching Vocabulary", reason);
      }
    }, false);
  }
  
  private void initModel(String modelName) {
    loading.setVisible(true);
    
    String[] swnames = modelName.split("\\s*:\\s*");

    this.api.getModelVersion(swnames[0], swnames[1], 
		new Callback<ModelVersion, Throwable>() {
		  @Override
		  public void onSuccess(ModelVersion v) {
		    version = v;
		    initModelRDF();
		    loading.setVisible(false);
		    if(vocabulary != null)
		        showModel(version);
		  }
		  @Override
		  public void onFailure(Throwable exception) {
		    GWT.log("Error fetching Software", exception);
		  }
		  }, false);
    
    this.api.getModel(swnames[0], new Callback<Model, Throwable>() {
        @Override
        public void onSuccess(Model sw) {
          model = sw;
          modelNameVP.clear();
          String softwareLink = "#" + NameTokens.modelbrowse + "/" + model.getName();

          if (model.getSoftwareName() != null)
          	softwareLink = "<a href='" + softwareLink + "'>" + model.getSoftwareName() + "</a>";
          else
          	softwareLink = "<a href='" + softwareLink + "'>" + model.getLabel() + "</a>";
          modelNameVP.add(new HTML(softwareLink + " >> "));
        }
        @Override
        public void onFailure(Throwable exception) {
          GWT.log("Error fetching Model", exception);
        }
      }, false);
  }
  
  private void initModelRDF() {
    this.api.getSoftwareRDF(version.getId(), new Callback<String, Throwable>() {
      @Override
      public void onSuccess(String rdf) {
        modelRDF = rdf;
      }
      @Override
      public void onFailure(Throwable exception) {
        GWT.log("Error fetching Software RDF", exception);
      }
    });
  }
  
  private void clear() {
    htmlbutton.getParent().setVisible(false);
    editbutton.getParent().setVisible(false);
    editbutton.setVisible(false);
    piechart.setVisible(false);
    modelBody.setVisible(false);
    modelTitle.setVisible(false);
    modelBody.clear();
    modelNameVP.clear();
    modelTitle.setText(null);
    modelTitle.setSubText(null);
    version = null;
  }

  private void initializePieChart() {
    piechart.setEventEnabled(false);
    piechart.setVisible(true);
    piechart.setVocabulary(vocabulary);
    piechart.setSoftware(version);
    if(!piechart.drawnCategories())
      piechart.drawCategories();
    piechart.fillCategories(true); 
    //piechart.setActiveCategoryId(null, false);
  }
  
  public void showModel(ModelVersion sw) {
    if(sw == null || vocabulary == null)
      return;

    initializePieChart();    
    
    Entity swName = sw.getPropertyValue(KBConstants.ONTNS()+"hasName");
    if (swName != null)
    	modelTitle.setText(swName.getValue().toString());
    else
    	modelTitle.setText(sw.getLabel());
    
    modelBody.clear();
    
    setBrowserWindowTitle(modelTitle.getText());
    
    String topcatid = KBConstants.CATNS()+"MetadataCategory";
    MetadataCategory topcat = vocabulary.getCategory(topcatid);
    topcat = vocabulary.orderChildCategories(topcat);
    
    MetadataType swtype = vocabulary.getType(sw.getType());
    
    EntityBrowser browser = new EntityBrowser(vocabulary);
    
    for(String lvl1catid: topcat.getChildren()) {
      Row catrow = new Row();
      
      // Level 1 category
      MetadataCategory lvl1cat = vocabulary.getCategory(lvl1catid);
      
      // Main panel
      Column panelcol = new Column("SM_9");
      catrow.add(panelcol);
      Panel lvl1panel = new Panel(PanelType.INFO);
      panelcol.add(lvl1panel);
      PanelHeader header1 = new PanelHeader();
      Heading heading1 = new Heading(HeadingSize.valueOf("H2"));
      heading1.setText(lvl1cat.getLabel().toUpperCase());
      header1.add(heading1);
      lvl1panel.add(header1);
      PanelBody body1 = new PanelBody();
      lvl1panel.add(body1);
      
      // Side panel
      Column sidecol = new Column("SM_3");
      sidecol.setHiddenOn(DeviceSize.XS);
      catrow.add(sidecol);
      Panel sidepanel = new Panel(PanelType.INFO);
      sidecol.add(sidepanel);
      PanelHeader sideheader = new PanelHeader();
      Heading sideheading = new Heading(HeadingSize.valueOf("H2"));
      sideheader.add(sideheading);
      sidepanel.add(sideheader);
      PanelBody sidebody = new PanelBody();
      sidepanel.add(sidebody);
      
      CategoryPieChart pchart = new CategoryPieChart(lvl1cat.getName(), 250);
      pchart.setSoftware(sw);
      pchart.setEventEnabled(false);
      pchart.setVocabulary(vocabulary);
      pchart.drawCategories();
      pchart.fillCategories(false);
      pchart.setActiveCategoryId(lvl1catid, false);
      sidebody.add(pchart);
      
      Double done = pchart.getDonePercentage(lvl1catid);
      Double optdone = pchart.getDonePercentage(lvl1catid, true);
      sideheading.setText("Done: "+done.intValue()+"% ("+optdone.intValue()+"% optional)");
      
      CategoryBarChart chart = new CategoryBarChart(lvl1cat.getName(), 250, 250);
      chart.setSoftware(sw);
      chart.setEventEnabled(false);
      chart.setVocabulary(vocabulary);
      chart.drawCategories(lvl1catid);
      chart.fillCategories(false);
      sidebody.add(chart);
      
      lvl1cat = vocabulary.orderChildCategories(lvl1cat);
      boolean hasSomeValues = false;
      
      for(String lvl2catid: lvl1cat.getChildren()) {
        MetadataCategory lvl2cat = vocabulary.getCategory(lvl2catid);
        Heading heading2 = new Heading(HeadingSize.valueOf("H4"));
        heading2.setText(lvl2cat.getLabel());
        String sublabel = lvl2cat.getSublabel();
        if(sublabel != null) {
          sublabel = Character.toUpperCase(sublabel.charAt(0)) + sublabel.substring(1);
          heading2.setSubText(" - " + sublabel);
        }
        body1.add(heading2);

        List<MetadataProperty> props = vocabulary.getPropertiesForType(swtype);
        props.retainAll(vocabulary.getPropertiesInCategory(lvl2cat));
        props = vocabulary.orderProperties(props);
        
        String html = swtype.getName() + browser.getEntitiesHTML(sw, props, false);
        for(MetadataProperty prop : props)
        	html += prop.getName() + " - " + prop.getLabel() + " - " + prop.getCategory();
        HTML lvl2html = new HTML(html);
        if(hasSomePropertyValues(props, sw)) {
          hasSomeValues = true;
        }
        else {
          heading2.addStyleName("hide-this-in-html");
          lvl2html.addStyleName("hide-this-in-html");
        }
        body1.add(lvl2html);
      }
      
      modelBody.add(catrow);
      
      if(!hasSomeValues) {
        lvl1panel.addStyleName("hide-this-in-html");
      }
    }
    htmlbutton.getParent().setVisible(true);
    editbutton.getParent().setVisible(true);
    modelBody.setVisible(true);
    modelTitle.setVisible(true);
    easeIn(htmlbutton.getParent());
    easeIn(editbutton.getParent());
    easeIn(modelBody);
    easeIn(modelTitle);
    
    initMaterial();
    Window.scrollTo(0, 0);
    
    bigpublishbutton.getParent().setVisible(true);
  }
  
  
  private boolean hasSomePropertyValues(List<MetadataProperty> props, ModelVersion sw) {
    for(MetadataProperty prop : props) {
      if(sw.getPropertyValues(prop.getId()).size() > 0) {
        return true;
      }
    }
    return false;
  }

  @UiHandler("editbutton")
  void onEditButtonClick(ClickEvent event) {
	String[] swnames = modelNameStr.split("\\s*:\\s*");
    History.newItem(NameTokens.publishModelVersion + "/" +
    		swnames[0] + ":" + version.getName());
  }
  
  @UiHandler("rdfbutton")
  void onRDFButtonClick(ClickEvent event) {
    openWindow("text/plain", modelRDF);
  }
  
  @UiHandler("jsonbutton")
  void onJsonButtonClick(ClickEvent event) {
    openWindow("application/json", codec.encode(version).toString());
  }
  
  @UiHandler("htmlbutton")
  void onHTMLButtonClick(ClickEvent event) {
    modelHTML = modelTitle.getElement().getInnerHTML();
    D3.selectAll(".col-sm-9").datum(new DatumFunction<Void>() {
      @Override
      public Void apply(Element context, Value d, int index) {
        modelHTML += context.getInnerHTML();
        return null;
      }
    });
    String styles =  "<style>\n"+
        "* { font-family: Arial, Helvetica }"+
        ".browse-label {\n"+
        "   color: #5D7BA0 !important;\n"+
        "   background-color: rgba(93,123,160,0.05) !important;\n"+
        "   border: 1px solid rgba(93,123,160,0.08) !important;\n"+
        "   border-radius: 4px;\n"+
        "   padding-left: 4px;\n"+
        "   padding-right: 4px;\n"+
        "   margin-left: -3px;\n"+
        "}\n"+
        ".browse-label {\n"+
        "   font-weight: bold;\n"+
        "   font-size: 0.85em;\n"+
        "}\n"+
        ".browse-label.error-label {\n"+
        "   background-color: rgba(212, 32, 65,0.05) !important;\n"+
        "   border: 1px solid rgba(212, 32, 65,0.08) !important;\n" +
        "   color: #D42041 !important;\n"+
        "}\n"+
        ".browse-label.error-label.optional {\n"+
        "   color: #969696 !important;\n"+
        "   background-color: #F9F9F9 !important;\n"+
        "   border: 1px solid #F0F0F0 !important;\n"  +
        "}\n"+
        ".hide-this-in-html {\n"+
        "   display: none;\n"+
        "}\n"+
        ".wrap-long-words {\n" + 
        "  -ms-word-break: break-all;\n" + 
        "  word-break: break-all;\n" + 
        "  word-break: break-word;\n" + 
        "  -webkit-hyphens: auto;\n" + 
        "  -moz-hyphens: auto;\n" + 
        "  hyphens: auto;\n" + 
        "}\n" + 
        ".wrap-pre {\n" + 
        "  width: 100%;\n" + 
        "  white-space: pre-wrap;\n" + 
        "}\n"+
        "</style>\n";    
    String html = "<html><head>" + styles + 
        "</head><body>"+ modelHTML +"</body></html>";
    openWindow("text/html", html);
  }
  
  native void openWindow(String mime, String content) /*-{
    window.open("data:"+mime+";base64,"+btoa(unescape(encodeURIComponent(content))));
  }-*/;
  
  private void easeIn(Widget w) {
    D3.select(w.getElement()).style("opacity", 0);
    D3.select(w.getElement()).transition().duration(400).style("opacity", 1);
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
	final String[] swnames = modelNameStr.split("\\s*:\\s*");
	GWT.log("here");
    String label = modellabel.getValue();
    if(modellabel.validate(true)) {
      ModelVersion tmpsw = new ModelVersion();
      tmpsw.setLabel(label);
      this.api.publishModelVersion(swnames[0], tmpsw, new Callback<ModelVersion, Throwable>() {
        public void onSuccess(ModelVersion sw) {
          // Add item to list
          ModelVersionSummary newsw = new ModelVersionSummary(sw);
          newsw.setExternalRepositoryId(SoftwareREST.LOCAL);
          // TODO: do the same to versions
          //addToList(newsw);
          //updateList();
          
          // Go to the new item
          History.newItem(NameTokens.publishModelVersion + 
        		  "/" + swnames[0] + ":" + sw.getName());
          
          publishdialog.hide();
          modellabel.setValue(null);
        }
        @Override
        public void onFailure(Throwable exception) { }
      });
    } 
  }

}
