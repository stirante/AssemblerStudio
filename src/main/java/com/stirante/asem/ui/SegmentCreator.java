package com.stirante.asem.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.Optional;

import static com.stirante.asem.ui.SegmentCreator.SegmentType.*;

/**
 * Created by stirante
 */
public class SegmentCreator {
    private static final double THICKNESS = 20;
    private static final double LENGTH = 100;
    private static final double OFFSET_X = 30;
    private static final double OFFSET_Y = 20;
    private static Segment[] segments;

    static {
        segments = new Segment[8];
        segments[0] = new Segment(THICKNESS / 2, 0, HORIZONTAL, "a");
        segments[1] = new Segment(100, THICKNESS / 2, VERTICAL, "b");
        segments[2] = new Segment(100, 100 + THICKNESS / 2, VERTICAL, "c");
        segments[3] = new Segment(THICKNESS / 2, 200, HORIZONTAL, "d");
        segments[4] = new Segment(0, 100 + THICKNESS / 2, VERTICAL, "e");
        segments[5] = new Segment(0, THICKNESS / 2, VERTICAL, "f");
        segments[6] = new Segment(THICKNESS / 2, 100, HORIZONTAL, "g");
        segments[7] = new Segment(95 + THICKNESS / 2, 195 + THICKNESS / 2, SQUARE, "P");
    }

    public static String create() {
        for (Segment segment : segments) {
            segment.set = false;
        }
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Pixel creator");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(SegmentCreator.class.getResource("/style.css").toExternalForm());
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        VBox box = new VBox();
        Canvas canvas = new Canvas(200, 250);
        box.getChildren().add(canvas);
        for (Segment segment : segments) {
            segment.render(canvas.getGraphicsContext2D());
        }
        Label l = new Label(constructByte());
        canvas.setOnMouseClicked(event -> {
            for (Segment segment : segments) {
                if (segment.contains(event.getX(), event.getY())) {
                    segment.set = !segment.set;
                    break;
                }
            }
            canvas.getGraphicsContext2D().clearRect(0, 0, 150, 250);
            for (Segment segment1 : segments) {
                segment1.render(canvas.getGraphicsContext2D());
            }
            l.setText(constructByte());
        });

        box.getChildren().add(l);

        dialog.getDialogPane().setContent(box);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                return constructByte();
            }
            return "";
        });

        Optional<String> result = dialog.showAndWait();
        return result.isPresent() ? result.get() : "";
    }

    private static String constructByte() {
        StringBuilder sb = new StringBuilder("#");
        for (int i = 7; i >= 0; i--) {
            sb.append(segments[i].set ? "1" : "0");
        }
        sb.append("b");
        return sb.toString();
    }

    enum SegmentType {
        HORIZONTAL, VERTICAL, SQUARE
    }

    private static class Segment {
        private final String name;
        private double x;
        private double y;
        private SegmentType type;
        private boolean set = false;

        Segment(double x, double y, SegmentType type, String name) {
            this.x = x + OFFSET_X;
            this.y = y + OFFSET_Y;
            this.type = type;
            this.name = name;
        }

        void render(GraphicsContext c) {
            c.setLineWidth(2);
            c.setStroke(Color.GREEN);
            c.setFill(Color.GREEN);
            switch (type) {
                case HORIZONTAL:
                    c.beginPath();
                    c.moveTo(x, y + THICKNESS / 2);
                    c.lineTo(x + THICKNESS / 2, y);
                    c.lineTo(x + LENGTH - THICKNESS / 2, y);
                    c.lineTo(x + LENGTH, y + THICKNESS / 2);
                    c.lineTo(x + LENGTH - THICKNESS / 2, y + THICKNESS);
                    c.lineTo(x + THICKNESS / 2, y + THICKNESS);
                    c.lineTo(x, y + THICKNESS / 2);
                    c.closePath();
                    if (set) c.fill();
                    c.stroke();
                    c.setFill(Color.WHITE);
                    c.fillText(name, x + LENGTH / 2, y + THICKNESS / 1.5);
                    break;
                case VERTICAL:
                    c.beginPath();
                    c.moveTo(x + THICKNESS / 2, y);
                    c.lineTo(x, y + THICKNESS / 2);
                    c.lineTo(x, y + LENGTH - THICKNESS / 2);
                    c.lineTo(x + THICKNESS / 2, y + LENGTH);
                    c.lineTo(x + THICKNESS, y + LENGTH - THICKNESS / 2);
                    c.lineTo(x + THICKNESS, y + THICKNESS / 2);
                    c.lineTo(x + THICKNESS / 2, y);
                    c.closePath();
                    if (set) c.fill();
                    c.stroke();
                    c.setFill(Color.WHITE);
                    c.fillText(name, x + THICKNESS / 3, y + LENGTH / 2);
                    break;
                case SQUARE:
                    c.beginPath();
                    c.moveTo(x + THICKNESS / 2, y);
                    c.lineTo(x, y + THICKNESS / 2);
                    c.lineTo(x, y + THICKNESS / 2);
                    c.lineTo(x + THICKNESS / 2, y + THICKNESS);
                    c.lineTo(x + THICKNESS, y + THICKNESS / 2);
                    c.lineTo(x + THICKNESS, y + THICKNESS / 2);
                    c.lineTo(x + THICKNESS / 2, y);
                    c.closePath();
                    if (set) c.fill();
                    c.stroke();
                    c.setFill(Color.WHITE);
                    c.fillText(name, x + THICKNESS / 3, y + THICKNESS / 1.3);
                    break;
            }
        }

        boolean contains(double x, double y) {
            switch (type) {
                case HORIZONTAL:
                    return this.x < x && this.y < y && this.x + LENGTH > x && this.y + THICKNESS > y;
                case VERTICAL:
                    return this.x < x && this.y < y && this.x + THICKNESS > x && this.y + LENGTH > y;
                case SQUARE:
                    return this.x < x && this.y < y && this.x + THICKNESS > x && this.y + THICKNESS > y;
            }
            return false;
        }

    }

}
