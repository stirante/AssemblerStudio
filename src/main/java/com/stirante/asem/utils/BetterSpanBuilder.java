package com.stirante.asem.utils;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by stirante
 */
public class BetterSpanBuilder {

    private ArrayList<SingleStyleRange> regions = new ArrayList<>();

    public void addStyle(String style, int start, int end) {
        regions.add(new SingleStyleRange(start, end, style));
    }

    public StyleSpans<Collection<String>> createStyleSpans(String str) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        ArrayList<String> styles = new ArrayList<>();
        ArrayList<String> temp = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < str.length(); i++) {
            temp.clear();
            for (SingleStyleRange region : regions) {
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

    public ArrayList<StylizedRange> createStylizedRanges(String str) {
        ArrayList<StylizedRange> result = new ArrayList<>();
        ArrayList<String> styles = new ArrayList<>();
        ArrayList<String> temp = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < str.length(); i++) {
            temp.clear();
            for (SingleStyleRange region : regions) {
                if (region.contains(i)) temp.add(region.getStyle());
            }
            if (!styles.containsAll(temp) || !temp.containsAll(styles)) {
                result.add(new StylizedRange(styles, start, i));
                styles = new ArrayList<>();
                styles.addAll(temp);
                start = i;
            }
        }
        result.add(new StylizedRange(styles, start, str.length()));
        return result;
    }

    public class StylizedRange extends TextRange {
        public ArrayList<String> getStyles() {
            return styles;
        }

        private ArrayList<String> styles = new ArrayList<>();

        public StylizedRange(ArrayList<String> styles, int start, int end) {
            super(start, end);
            this.styles.addAll(styles);
        }
    }

    class SingleStyleRange extends TextRange {
        private String style;

        public SingleStyleRange(int start, int end, String style) {
            super(start, end);
            this.style = style;
        }

        public String getStyle() {
            return style;
        }

    }

}
