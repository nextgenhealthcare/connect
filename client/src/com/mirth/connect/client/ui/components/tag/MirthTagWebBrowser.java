/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.tag;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirth.connect.client.ui.PlatformUI;

import netscape.javascript.JSObject;

public class MirthTagWebBrowser extends Region {

    private WebView webView;
    private WebEngine webEngine;
    private JSObject tokenField;

    private MirthTagWebController webController;

    private AutoCompletionPopupWindow popupWindow;

    public MirthTagWebBrowser(AutoCompletionPopupWindow popupWindow, List<Map<String, String>> userTags, Map<String, Map<String, String>> attributeMap, boolean channelContext) throws Exception {
        this.popupWindow = popupWindow;

        setHeight(24);
        setPrefWidth(200);

        initComponents(userTags, attributeMap, channelContext);

        getChildren().add(webView);
    }

    private void initComponents(List<Map<String, String>> userTags, Map<String, Map<String, String>> attributeMap, final boolean channelContext) throws Exception {
        webView = new WebView();
        webView.setContextMenuEnabled(false);

        webEngine = webView.getEngine();

        final String tagData = convertToJSON(userTags);
        final String attributeData = convertToJSON(attributeMap);
        final String context = convertToJSON(channelContext);

//        webEngine.load(getClass().getResource("bootstrap.min.css").toExternalForm());
//        webEngine.load(getClass().getResource("bootstrap-tokenfield.css").toExternalForm());
//        webEngine.load(getClass().getResource("tokenfield-typeahead.css").toExternalForm());
//        webEngine.load(getClass().getResource("bootstrap-tokenfield.js").toExternalForm());
//        webEngine.load(getClass().getResource("jquery-ui.min.js").toExternalForm());
//        webEngine.load(getClass().getResource("jquery.min.js").toExternalForm());
//        webEngine.load(getClass().getResource("MirthTagField.html").toExternalForm());

        /*
         * This is required because of https://bugs.openjdk.java.net/browse/JDK-8136529. In
         * 8u60-8u71 linking to resources packaged in the JAR doesn't work when launched via
         * webstart. So instead the HTML is loaded directly and links are manually replaced.
         */
        String html = IOUtils.toString(getClass().getResource("MirthTagField.html"), "UTF-8");
        html = html.replace("bootstrap.min.css", getClass().getResource("bootstrap.min.css").toURI().toString());
        html = html.replace("bootstrap-tokenfield.css", getClass().getResource("bootstrap-tokenfield.css").toURI().toString());
        html = html.replace("tokenfield-typeahead.css", getClass().getResource("tokenfield-typeahead.css").toURI().toString());
        html = html.replace("bootstrap-tokenfield.js", getClass().getResource("bootstrap-tokenfield.js").toURI().toString());
        html = html.replace("jquery-ui.min.js", getClass().getResource("jquery-ui.min.js").toURI().toString());
        html = html.replace("jquery.min.js", getClass().getResource("jquery.min.js").toURI().toString());
        webEngine.loadContent(html);

        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
                if (newState == State.SUCCEEDED) {
                    JSObject init = (JSObject) webEngine.executeScript("window");
                    init.setMember("clickController", webController);
                    init.call("updateTags", attributeData, context);
                    init.call("setUserTags", tagData);
                }
            }
        });

        tokenField = (JSObject) webEngine.executeScript("window");
        webController = new MirthTagWebController(popupWindow);
        popupWindow.setWebEngine(webEngine);
    }

    public void updateTags(Map<String, Map<String, String>> tagAtrributeMap, boolean channelContext) {
        doCall("updateTags", convertToJSON(tagAtrributeMap), convertToJSON(channelContext));
    }

    public void setFocus(boolean focus) {
        doCall("setFocus", convertToJSON(focus));
    }

    public void setEnabled(boolean enable) {
        doCall("setEnabled", convertToJSON(enable));
    }

    public void setUserTags(List<Map<String, String>> tags) {
        doCall("setUserTags", convertToJSON(tags));
    }

    public void insertTag(String tagName) {
        doCall("insertTag", tagName);
    }

    public String getTags() {
        return webController.getTags();
    }

    public Map<String, Color> getTagColors() {
        return webController.getTagColors();
    }

    public void clear() {
        setUserTags(new ArrayList<Map<String, String>>());
        webController.setTags("");
    }

    private String convertToJSON(Object object) {
        String jsonData = "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            jsonData = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            PlatformUI.MIRTH_FRAME.alertThrowable(PlatformUI.MIRTH_FRAME, e.getCause(), "Error converting to JSON");
        }

        return jsonData;
    }

    private void doCall(final String method, final Object... args) {
        try {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    tokenField.call(method, args);
                }
            });
        } catch (Exception e) {
            PlatformUI.MIRTH_FRAME.alertThrowable(PlatformUI.MIRTH_FRAME, e.getCause(), "Error in tagfield");
        }
    }
}