package com.stirante.asem.utils;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by stirante
 */
public class BetterSpanBuilder {

    private ArrayList<StyledRange> regions = new ArrayList<>();

    public void addStyle(String style, int start, int end) {
        regions.add(new StyledRange(start, end, style));
    }

    public StyleSpans<Collection<String>> create(String str) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        ArrayList<String> styles = new ArrayList<>();
        ArrayList<String> temp = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < str.length(); i++) {
            temp.clear();
            for (StyledRange region : regions) {
                if (region.contains(i)) temp.add(region.getStyle());
            }
            if (!styles.containsAll(temp) || !temp.containsAll(styles)) {
                spansBuilder.add(styles, i - start);
                styles = new ArrayList<>();
                styles.addAll(temp);
                start = i;
            }
        }
        spansBuilder.add(styles, str.length() - start);
        return spansBuilder.create();
    }

    class StyledRange extends TextRange {
        private String style;

        public StyledRange(int start, int end, String style) {
            super(start, end);
            this.style = style;
        }

        public String getStyle() {
            return style;
        }

    }

}
