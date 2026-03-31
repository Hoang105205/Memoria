package com.example.memoria.ui.quiz;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.memoria.data.model.entity.Card;
import com.example.memoria.data.model.entity.QuizHistory;
import com.example.memoria.data.repository.CardRepository;
import com.example.memoria.data.repository.QuizRepository;

import com.example.memoria.utils.SyncHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

@HiltViewModel
public class QuizViewModel extends ViewModel {

    private final CardRepository cardRepository;
    private final QuizRepository quizRepository;

    private final Context context;

    @Inject
    public QuizViewModel(CardRepository cardRepository, QuizRepository quizRepository, @ApplicationContext Context context) {
        this.context = context;
        this.cardRepository = cardRepository;
        this.quizRepository = quizRepository;
    }

    public LiveData<List<Card>> getCardsByDeckId(UUID deckId) {
        return cardRepository.getCardsByDeckId(deckId);
    }

    public void saveQuizHistory(QuizHistory history, CardRepository.DataCallback<Boolean> callback) {
        quizRepository.addQuizResult(history, isSuccess -> {
            if (isSuccess) {
                triggerSync();
            }
            if (callback != null) {
                callback.onDataLoaded(isSuccess);
            }
        });
    }

    public void updateQuizHistory(QuizHistory history, CardRepository.DataCallback<Boolean> callback) {
        quizRepository.updateQuizResult(history, isSuccess -> {
            if (isSuccess) {
                triggerSync();
            }
            if (callback != null) {
                callback.onDataLoaded(isSuccess);
            }
        });
    }

    private void triggerSync() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            SyncHelper.triggerImmediateSync(context, user.getUid());
        }
    }
}