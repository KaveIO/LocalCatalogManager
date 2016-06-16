/*
 * Copyright 2016 KPMG N.V. (unless otherwise stated).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.kpmg.lcm.ui.view;

import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import javax.annotation.PostConstruct;
import nl.kpmg.lcm.server.rest.client.version0.types.StoragesRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.TaskDescriptionsRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.TaskScheduleRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.UserGroupsRepresentation;
import nl.kpmg.lcm.server.rest.client.version0.types.UsersRepresentation;
import nl.kpmg.lcm.ui.rest.AuthenticationException;
import nl.kpmg.lcm.ui.rest.RestClientService;
import nl.kpmg.lcm.ui.view.administration.StoragePanel;
import nl.kpmg.lcm.ui.view.administration.TasksPanel;
import nl.kpmg.lcm.ui.view.administration.UsersPanel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.xpoft.vaadin.VaadinView;

/**
 *
 * @author mhoekstra
 */
@Component
@VaadinView(AdministrationViewImpl.VIEW_NAME)
public class AdministrationViewImpl extends VerticalLayout implements AdministrationView {

    /**
     * The linkable name of this view.
     */
    public static final String VIEW_NAME = "administration";

    /**
     * The service for interacting with the backend.
     */
    @Autowired
    private RestClientService restClientService;

    /**
     * The auto wired main UI component.
     */
    @Autowired
    private UI ui;

    private final TabSheet tabsheet = new TabSheet();

    private UserGroupsRepresentation userGroups;
    private UsersRepresentation users;
    private TaskScheduleRepresentation taskSchedule;
    private TaskDescriptionsRepresentation tasks;
    private StoragesRepresentation storage;

    private TasksPanel tasksPanel;
    private StoragePanel storagePanel;
    private UsersPanel usersPanel;

    /**
     * Builds the interface.
     */
    @PostConstruct
    public final void init() {
        HorizontalLayout root = new HorizontalLayout();
        root.setWidth("100%");
        root.setMargin(true);
        root.setSpacing(true);

        storagePanel = new StoragePanel();
        tabsheet.addTab(storagePanel, "Storage");

        tasksPanel = new TasksPanel();
        tabsheet.addTab(tasksPanel, "Tasks");

        usersPanel = new UsersPanel();
        tabsheet.addTab(usersPanel, "Users");

        root.addComponent(tabsheet);

        addComponent(root);
    }

    /**
     * Loads the data on presentation.
     *
     * @param event fired when the view is entered.
     */
    @Override
    public final void enter(final ViewChangeListener.ViewChangeEvent event) {
        try {
            storage = restClientService.getStorage();
            tasks = restClientService.getTasks();
            taskSchedule = restClientService.getTaskSchedule();
            users = restClientService.getUsers();
            userGroups = restClientService.getUserGroups();

            storagePanel.setStorages(storage);

            tasksPanel.setTasks(tasks);
            tasksPanel.setTaskSchedule(taskSchedule);

            usersPanel.setUsers(users);
            usersPanel.setUserGroups(userGroups);
        } catch (AuthenticationException ex) {
            getUI().getNavigator().navigateTo("");
        }
    }
}
