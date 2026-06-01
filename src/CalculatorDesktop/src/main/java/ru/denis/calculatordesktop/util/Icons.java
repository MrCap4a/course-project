package ru.denis.calculatordesktop.util;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;

/**
 * Icon buttons using inline Material Design SVG paths (24×24 coordinate space).
 */
public final class Icons {

    // pencil / edit — Material Design "mode_edit"
    private static final String EDIT_PATH =
        "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25z" +
        "M20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34" +
        "c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z";

    // trash / delete — Material Design "delete"
    private static final String DELETE_PATH =
        "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12z" +
        "M19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z";

    private Icons() {}

    public static Button editButton() {
        return make(EDIT_PATH, "#3b82f6", "Редактировать");
    }

    public static Button deleteButton() {
        return make(DELETE_PATH, "#ef4444", "Удалить");
    }

    private static Button make(String pathData, String hexColor, String tooltipText) {
        SVGPath icon = new SVGPath();
        icon.setContent(pathData);
        icon.setFill(Color.web(hexColor));
        // scale from 24-unit coordinate space to ~16 px
        icon.getTransforms().add(new Scale(0.68, 0.68, 0, 0));

        Button btn = new Button("", icon);
        btn.setTooltip(new Tooltip(tooltipText));
        btn.getStyleClass().add("icon-btn");
        return btn;
    }
}
