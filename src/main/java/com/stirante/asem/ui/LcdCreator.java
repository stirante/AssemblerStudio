package com.stirante.asem.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.Optional;

/**
 * Created by stirante
 */
public class LcdCreator {
    private static final double SIZE = 20;
    private static final double PADDING = 5;
    private static final double OFFSET_X = 30;
    private static final double OFFSET_Y = 20;
    private static Pixel[][] pixels;

    static {
        pixels = new Pixel[8][5];
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 5; x++) {
                pixels[y][x] = new Pixel(x * (SIZE + PADDING), y * (SIZE + PADDING));
            }
        }
    }

    public static String create() {
        for (Pixel[] pixels1 : pixels) {
            for (Pixel pixel : pixels1) {
                pixel.set = false;
            }
        }
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Pixel creator");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(LcdCreator.class.getResource("/style.css").toExternalForm());
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        VBox box = new VBox();
        Canvas canvas = new Canvas(200, 250);
        box.getChildren().add(canvas);
        for (Pixel[] pixels1 : pixels) {
            for (Pixel pixel : pixels1) {
                pixel.render(canvas.getGraphicsContext2D());
            }
        }
        canvas.setOnMouseClicked(event -> {
            for (Pixel[] pixels1 : pixels) {
                for (Pixel pixel : pixels1) {
                    if (pixel.contains(event.getX(), event.getY())) {
                        pixel.set = !pixel.set;
                        break;
                    }
                }
            }
            for (Pixel[] pixels1 : pixels) {
                for (Pixel pixel : pixels1) {
                    pixel.render(canvas.getGraphicsContext2D());
                }
            }
        });

        dialog.getDialogPane().setContent(box);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                return constructDb();
            }
            return "";
        });

        Optional<String> result = dialog.showAndWait();
        return result.isPresent() ? result.get() : "";
    }

    private static String constructDb() {
        StringBuilder sb = new StringBuilder("name:\n");
        for (int y = 0; y < 8; y++) {
            sb.append("\tDB\t000");
            for (int x = 0; x < 5; x++) {
                sb.append(pixels[y][x].set ? "1" : "0");
            }
            sb.append("b\n");
        }
        return sb.toString();
    }

    private static class Pixel {
        private double x;
        private double y;
        private boolean set = false;

        Pixel(double x, double y) {
            this.x = x + OFFSET_X;
            this.y = y + OFFSET_Y;
        }

        void render(GraphicsContext c) {
            c.clearRect(x, y, SIZE, SIZE);
            c.setLineWidth(2);
            c.setStroke(Color.GREEN);
            c.setFill(Color.GREEN);
            if (set) c.fillRect(x, y, SIZE, SIZE);
            c.strokeRect(x, y, SIZE, SIZE);
        }

        boolean contains(double x, double y) {
            return this.x < x && this.y < y && this.x + SIZE > x && this.y + SIZE > y;
        }

    }

}
