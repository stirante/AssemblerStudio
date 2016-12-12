/**
 * Copyright (c) 2016, Farrukh Obaid
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of <organization>, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL FARRUKH OBAID BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.fo.controls.fontpicker;

import com.sun.javafx.scene.control.skin.ComboBoxPopupControl;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.text.Font;

public class FontPickerSkin extends ComboBoxPopupControl<Font> {
    private final Label displayNode;
    private FontPicker fontPicker;
    private FontPickerContent fontPickerContent;

    public FontPickerSkin(final FontPicker fontPicker) {
        super(fontPicker, new FontPickerBehavior(fontPicker));
        this.fontPicker = fontPicker;
        registerChangeListener(fontPicker.valueProperty(), "VALUE");
        displayNode = new Label("");
        displayNode.getStyleClass().add("font-label");
        displayNode.setManaged(false);
        updateFont();
    }

    @Override
    protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);
        if ("VALUE".equals(p)) {
            updateFont();
        }
    }

    private void updateFont() {
        final FontPicker fontPicker = (FontPicker) getSkinnable();
        Font font = fontPicker.getValue();
        String fontText = font.getFamily() + ", " + font.getStyle() + ", " + (int) font.getSize();
        displayNode.setText(fontText);
    }

    public void syncWithAutoUpdate() {
        if (!getPopup().isShowing() && getSkinnable().isShowing()) {
            getSkinnable().hide();
        }
    }

    @Override
    protected Node getPopupContent() {
        if (fontPickerContent == null) {
            fontPickerContent = new FontPickerContent(fontPicker);
        }
        return fontPickerContent;
    }

//    @Override
//    protected TextField getEditor() {
//        return null;
//    }
//
//    @Override
//    protected StringConverter<Font> getConverter() {
//        return null;
//    }

    @Override
    public Node getDisplayNode() {
        return displayNode;
    }

}