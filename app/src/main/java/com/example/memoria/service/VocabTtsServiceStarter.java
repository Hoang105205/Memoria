package com.example.memoria.service;

import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;

public class VocabTtsServiceStarter {
    public static final String EXTRA_DECK_IDS = "extra_deck_ids";     // ArrayList<String> UUID
    public static final String EXTRA_SELECT_ALL = "extra_select_all"; // boolean

    public static void startWithDecks(Context context, ArrayList<String> deckIds, boolean selectAll) {
        Intent i = new Intent(context, VocabTtsForegroundService.class);
        i.setAction(VocabTtsForegroundService.ACTION_START);
        i.putStringArrayListExtra(EXTRA_DECK_IDS, deckIds);
        i.putExtra(EXTRA_SELECT_ALL, selectAll);
        ContextCompat.startForegroundService(context, i);
    }

    public static void stop(Context context) {
        Intent i = new Intent(context, VocabTtsForegroundService.class);
        i.setAction(VocabTtsForegroundService.ACTION_STOP);
        context.startService(i);
    }
}
