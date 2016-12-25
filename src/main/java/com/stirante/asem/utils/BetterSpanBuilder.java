package com.stirante.asem.utils;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by stirante
 */
public class BetterSpanBuilder {

    private ArrayList<Region> regions = new ArrayList<>();

    public void addStyle(String style, int start, int end) {
        regions.add(new Region(start, end, style));
    }

    public StyleSpans<Collection<String>> create(String str) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        ArrayList<String> styles = new ArrayList<>();
        ArrayList<String> temp = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < str.length(); i++) {
            temp.clear();
            for (Region region : regions) {
                if (region.contains(i)) temp.add(region.style);
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

    private int clamp(int i) {
        return i < 0 ? 0 : i;
    }

    class Region {
        int start;
        int end;
        String style;

        public Region(int start, int end, String style) {
            this.start = start;
            this.end = end;
            this.style = style;
        }

        boolean contains(int index) {
            return index >= start && index < end;
        }
    }

}
