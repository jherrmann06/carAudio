package com.jlh.bt.gui;

import org.controlsfx.control.Notifications;

import javafx.application.Platform;

/**
 * Utility class to easily send notifications. 
 */
public class NotificationSender {

    public static void sendInfoNotification(String title, String text) {
        Platform.runLater(() ->
            Notifications.create()
                .hideCloseButton()
                .title(title)
                .text(text)
                .darkStyle()
                .showInformation()
        );
    }

    public static void sendWarningNotification(String title, String text) {
        Platform.runLater(() ->
            Notifications.create()
                .hideCloseButton()
                .title(title)
                .text(text)
                .darkStyle()
                .showWarning()
        );
    }
}
