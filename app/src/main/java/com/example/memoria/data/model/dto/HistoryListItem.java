package com.example.memoria.data.model.dto;
import com.example.memoria.data.model.entity.SearchHistory;

import java.util.Date;

public abstract class HistoryListItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ROW = 1;

    public abstract int getType();

    public static class Header extends HistoryListItem {
        public final String title;

        public Header(String title) {
            this.title = title;
        }

        @Override public int getType() { return TYPE_HEADER; }
    }

    public static class Row extends HistoryListItem {
        public final SearchHistory history;

        public Row(SearchHistory history) {
            this.history = history;
        }

        @Override public int getType() { return TYPE_ROW; }
    }
}
