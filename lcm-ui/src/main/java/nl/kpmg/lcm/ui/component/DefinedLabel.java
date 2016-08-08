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
package nl.kpmg.lcm.ui.component;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author mhoekstra
 */
public class DefinedLabel extends CustomComponent {

    public DefinedLabel(String title, String content, String tooltip) {
        // A layout structure used for composition
        Panel panel = new Panel();
        panel.setStyleName("v-panel-borderless");
        panel.setWidth("100%");

        VerticalLayout panelContent = new VerticalLayout();
        panelContent.setMargin(true); // Very useful
        panelContent.setWidth("100%");

        panel.setContent(panelContent);

        // Compose from multiple components
        Label titleLabel = new Label(title);
        titleLabel.setStyleName("v-label-h4");
        panelContent.addComponent(titleLabel);

        Label contentLabel = new Label(content);
        panelContent.addComponent(contentLabel);

        // The composition root MUST be set
        setCompositionRoot(panel);
    }

    public DefinedLabel(String title, String content) {
        this(title, content, null);
    }
}
