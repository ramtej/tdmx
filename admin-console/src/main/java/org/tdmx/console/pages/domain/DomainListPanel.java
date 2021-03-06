package org.tdmx.console.pages.domain;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.console.AdminApplication;
import org.tdmx.console.domain.Domain;
import org.tdmx.console.service.UIService;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapBookmarkablePageLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons.Type;
import de.agilecoders.wicket.core.markup.html.bootstrap.image.IconType;

public class DomainListPanel extends Panel {

	private static Logger log = LoggerFactory.getLogger(DomainListPanel.class);
		
	private transient UIService searchService = AdminApplication.getUIService();
	
	public DomainListPanel(String id) {
		super(id);
		setOutputMarkupId(true);
		createComponents();
	}

	private void createComponents() {
		IModel<List<Domain>> domainListModel = new LoadableDetachableModel<List<Domain>>() {
	        @Override
	        protected List<Domain> load() {
	        	return searchService.listDomains();
	        }
	    };
		
		
		ListView<Domain> listview = new ListView<Domain>("listview",  domainListModel) {
			  protected void populateItem(final ListItem<Domain> item) {
			        item.add(new Label("title", item.getModelObject().getTitle()));
			        
			        PageParameters params = new PageParameters();
			        params.set("domain", item.getModelObject().getTitle());
			        BootstrapBookmarkablePageLink<Domain> domainDetailsLink = new BootstrapBookmarkablePageLink<Domain>(
			        															"domainDetailsLink", DomainDetailsPage.class, params, Type.Default
			        															);
			        domainDetailsLink.setIconType(IconType.wrench);
			        domainDetailsLink.setLabel(Model.of("Modify"));
			        
			        item.add(domainDetailsLink);
			    }
		};
		
		add(listview);
		
	}
	

}
