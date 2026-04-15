package com.example.memoria.ui.search;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;
import com.example.memoria.data.model.dto.HistoryListItem;
import com.example.memoria.data.model.entity.SearchHistory;
import com.example.memoria.data.repository.SearchHistoryRepository;
import com.example.memoria.ui.adapter.SearchHistoryAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SearchHistoryDialog extends DialogFragment {

    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null) {
            d.setCanceledOnTouchOutside(true);
        }
    }

    public interface Listener {
        void onSelectWord(String word);
        void onChanged();
    }

    private final SearchHistoryRepository repo;
    private final Listener listener;

    private SearchHistoryAdapter adapter;
    private EditText edtFilter;

    private List<SearchHistory> all = new ArrayList<>();

    public SearchHistoryDialog(SearchHistoryRepository repo, Listener listener) {
        this.repo = repo;
        this.listener = listener;
    }

    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_search_history, null, false);

        // click outside EditText -> hide keyboard
        View root = v.findViewById(R.id.historyDialogRoot);
        if (root != null) {
            root.setOnTouchListener((vv, event) -> {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    if (edtFilter != null) {
                        int[] loc = new int[2];
                        edtFilter.getLocationOnScreen(loc);
                        float x = event.getRawX();
                        float y = event.getRawY();

                        int left = loc[0];
                        int top = loc[1];
                        int right = left + edtFilter.getWidth();
                        int bottom = top + edtFilter.getHeight();

                        boolean touchInsideEdit =
                                x >= left && x <= right && y >= top && y <= bottom;

                        if (!touchInsideEdit) {
                            hideKeyboardAndClearFocus();
                        }
                    } else {
                        hideKeyboardAndClearFocus();
                    }
                }
                return false;
            });
        }

        RecyclerView rv = v.findViewById(R.id.rvHistory);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setOnTouchListener((vv, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                hideKeyboardAndClearFocus();
            }
            return false;
        });

        adapter = new SearchHistoryAdapter(new SearchHistoryAdapter.Listener() {
            @Override
            public void onClickWord(String word) {
                if (listener != null) listener.onSelectWord(word);
                dismiss();
            }

            @Override
            public void onDeleteWord(String word) {
                showConfirmDeleteWord(word);
            }
        });
        rv.setAdapter(adapter);

        edtFilter = v.findViewById(R.id.edtFilter);
        edtFilter.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { render(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        ImageView ivDeleteAll = v.findViewById(R.id.ivDeleteAll);
        ivDeleteAll.setOnClickListener(x -> showConfirmDeleteAll());

        load();

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(v)
                .create();

        dialog.setCanceledOnTouchOutside(true);
        setCancelable(true);

        return dialog;
    }

    private void load() {
        repo.getAllSearchHistories(histories -> {
            if (!isAdded()) return;
            all = histories != null ? histories : new ArrayList<>();
            requireActivity().runOnUiThread(this::render);
        });
    }

    private void render() {
        String q = edtFilter != null && edtFilter.getText() != null
                ? edtFilter.getText().toString().trim().toLowerCase(Locale.ROOT)
                : "";

        List<SearchHistory> filtered = new ArrayList<>();
        for (SearchHistory h : all) {
            if (h == null) continue;
            String w = h.getWordText() == null ? "" : h.getWordText().trim();
            if (q.isEmpty() || w.toLowerCase(Locale.ROOT).contains(q)) filtered.add(h);
        }

        adapter.submit(buildSectionedItems(filtered));
    }

    private List<HistoryListItem> buildSectionedItems(List<SearchHistory> list) {
        Map<Long, List<SearchHistory>> map = new LinkedHashMap<>();
        list.sort((a, b) -> b.getSearchedAt().compareTo(a.getSearchedAt()));
        for (SearchHistory h : list) {
            Date d = h.getSearchedAt();
            if (d == null) continue;
            long dayStart = startOfDay(d).getTime();
            map.computeIfAbsent(dayStart, k -> new ArrayList<>()).add(h);
        }

        List<HistoryListItem> out = new ArrayList<>();

        Date todayStart = startOfDay(new Date());
        Date yesterdayStart = addDays(todayStart, -1);

        SimpleDateFormat dowMonthDayYear = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.ENGLISH);

        for (Map.Entry<Long, List<SearchHistory>> e : map.entrySet()) {
            Date dayStart = new Date(e.getKey());

            String header;
            if (sameDay(dayStart, todayStart)) {
                header = "Recent"; // hôm nay (2026-04-09)
            } else if (sameDay(dayStart, yesterdayStart)) {
                header = "Yesterday - " + dowMonthDayYear.format(dayStart);
            } else {
                header = dowMonthDayYear.format(dayStart);
            }

            out.add(new HistoryListItem.Header(header));

            for (SearchHistory h : e.getValue()) out.add(new HistoryListItem.Row(h));
        }
        return out;
    }

    private void showConfirmDeleteWord(String word) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.action_delete)
                .setMessage(R.string.dialog_message_delete_word_from_history)
                .setNegativeButton(R.string.action_cancel, (d, w) -> d.dismiss())
                .setPositiveButton(R.string.action_delete, (d, w) -> {
                    repo.deleteByWord(word);
                    if (listener != null) listener.onChanged();
                    load();
                })
                .show();
    }

    private void showConfirmDeleteAll() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_all)
                .setMessage(R.string.message_delete_all_history)
                .setNegativeButton(R.string.action_cancel, (d, w) -> d.dismiss())
                .setPositiveButton(R.string.delete_all, (d, w) -> {
                    repo.deleteAll();
                    if (listener != null) listener.onChanged();
                    load();
                })
                .show();
    }

    // ---- Date helpers ----
    private static Date startOfDay(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    private static Date addDays(Date d, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(Calendar.DATE, days);
        return c.getTime();
    }

    private static boolean sameDay(Date a, Date b) {
        return startOfDay(a).getTime() == startOfDay(b).getTime();
    }

    private void hideKeyboardAndClearFocus() {
        if (!isAdded()) return;

        View focused = requireActivity().getCurrentFocus();
        if (focused == null) focused = edtFilter;

        edtFilter.clearFocus();

        InputMethodManager imm =
                (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
        }
    }
}
