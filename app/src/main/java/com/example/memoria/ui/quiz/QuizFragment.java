package com.example.memoria.ui.quiz;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.memoria.R;
import com.example.memoria.data.model.Card;
import com.example.memoria.data.model.QuizHistory;
import com.example.memoria.utils.GeminiHelper;
import com.example.memoria.utils.PronunciationManager;
import com.example.memoria.utils.SoundManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class QuizFragment extends Fragment {
    private UUID deckId;
    private String deckName;

    private QuizViewModel viewModel;
    private GeminiHelper geminiHelper;

    private LinearLayout layoutLoading;
    private ConstraintLayout layoutQuestion;

    private TextView tvTimer;
    private ProgressBar progressBar;

    private TextView tvInstruction, tvQuestionWord, tvQuestionWordSynonym;
    private ImageButton btnPlayLarge;
    private LinearLayout layoutWordAndAudio;
    private ConstraintLayout layoutWord;

    private LinearLayout layoutOptionsGrid, layoutOptionsVertical;
    private final Button[] gridButtons = new Button[4];
    private final Button[] vertButtons = new Button[4];
    private Button btnCheck;

    private int elapsedSeconds = 0;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private PronunciationManager pronunciationManager;
    private final List<QuizQuestion> quizList = new ArrayList<>();
    private final ArrayList<String> wrongWordsList = new ArrayList<>();
    private int currentIndex = 0;
    private int currentScore = 0;

    private boolean isInteractionLocked = false;
    private Button selectedButton = null;

    @Inject
    SoundManager soundManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            deckId = (UUID) getArguments().getSerializable("DECK_ID");
            deckName = getArguments().getString("DECK_NAME");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_base_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);

        viewModel = new ViewModelProvider(this).get(QuizViewModel.class);
        geminiHelper = new GeminiHelper();
        pronunciationManager = new PronunciationManager(requireContext());

        if (deckId == null) {
            Toast.makeText(getContext(), R.string.quiz_null_deck, Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return;
        }

        viewModel.getCardsByDeckId(deckId).observe(getViewLifecycleOwner(), cards -> {
            if (cards != null && !cards.isEmpty() && quizList.isEmpty()) {
                setupQuizData(cards);
            } else {
                Toast.makeText(getContext(), R.string.quiz_empty_deck, Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
            }
        });
    }

    private void initViews(View view) {
        layoutLoading = view.findViewById(R.id.layoutLoading);
        layoutQuestion = view.findViewById(R.id.layoutQuestion);
        layoutLoading.setVisibility(View.VISIBLE);
        layoutQuestion.setVisibility(View.GONE);

        tvTimer = view.findViewById(R.id.tvTimer);
        progressBar = view.findViewById(R.id.bgProgressBar);
        ImageButton btnCloseQuiz = view.findViewById(R.id.btnCloseQuiz);
        btnCheck = view.findViewById(R.id.btnCheck);

        if (btnCloseQuiz != null) {
            btnCloseQuiz.setOnClickListener(v -> requireActivity().onBackPressed());
        }

        tvInstruction = view.findViewById(R.id.tvInstruction);
        tvQuestionWord = view.findViewById(R.id.tvQuestionWord);
        tvQuestionWordSynonym = view.findViewById(R.id.tvQuestionWordSynonym);
        btnPlayLarge = view.findViewById(R.id.btnPlayLarge);
        ImageButton btnPlaySmall = view.findViewById(R.id.btnPlaySmall);
        layoutWordAndAudio = view.findViewById(R.id.layoutWordAndAudio);
        layoutWord = view.findViewById(R.id.layoutWord);

        layoutOptionsGrid = view.findViewById(R.id.layoutOptionsGrid);
        layoutOptionsVertical = view.findViewById(R.id.layoutOptionsVertical);

        gridButtons[0] = view.findViewById(R.id.btnGridA);
        gridButtons[1] = view.findViewById(R.id.btnGridB);
        gridButtons[2] = view.findViewById(R.id.btnGridC);
        gridButtons[3] = view.findViewById(R.id.btnGridD);

        vertButtons[0] = view.findViewById(R.id.btnVertA);
        vertButtons[1] = view.findViewById(R.id.btnVertB);
        vertButtons[2] = view.findViewById(R.id.btnVertC);
        vertButtons[3] = view.findViewById(R.id.btnVertD);

        View.OnClickListener answerClickListener = v -> {
            if (isInteractionLocked) return;

            resetButtonStyles();

            selectedButton = (Button) v;
            selectedButton.setBackgroundResource(R.drawable.bg_choice_selected);
            selectedButton.setTextColor(Color.parseColor("#4A90E2"));
            selectedButton.setTypeface(selectedButton.getTypeface(), Typeface.BOLD);
        };

        for (int i = 0; i < 4; i++) {
            gridButtons[i].setOnClickListener(answerClickListener);
            vertButtons[i].setOnClickListener(answerClickListener);
        }

        btnPlayLarge.setOnClickListener(v -> playCurrentAudio());
        btnPlaySmall.setOnClickListener(v -> playCurrentAudio());

        btnCheck.setOnClickListener(v -> {
            if (isInteractionLocked) return;

            if (selectedButton == null) {
                Toast.makeText(getContext(), R.string.quiz_null_selected, Toast.LENGTH_SHORT).show();
                return;
            }

            checkAnswer();
        });
    }

    private void playCurrentAudio() {
        if (quizList.size() > currentIndex && pronunciationManager != null) {
            pronunciationManager.playSound(quizList.get(currentIndex).word, null, 1.0f);
        }
    }

    public void setupQuizData(List<Card> cards) {
        List<GeminiHelper.QuizGenRequest> requests = new ArrayList<>();
        Random random = new Random();
        quizList.clear();

        for (Card card : cards) {
            String word = card.getFrontText();
            String meaning = (card.getBackMeanings() != null && !card.getBackMeanings().isEmpty())
                    ? card.getBackMeanings().get(random.nextInt(card.getBackMeanings().size()))
                    : "";
            QuizQuestion.Type typeEnum = QuizQuestion.Type.values()[random.nextInt(QuizQuestion.Type.values().length)];

            quizList.add(new QuizQuestion(card.getCardId(), word, meaning, typeEnum));

            requests.add(new GeminiHelper.QuizGenRequest(
                    card.getCardId().toString(),
                    word,
                    meaning,
                    typeEnum.name()
            ));
        }

        geminiHelper.generateDeckDistractors(requests, new GeminiHelper.GeminiBatchCallback() {
            @Override
            public void onSuccess(List<GeminiHelper.QuizGenResponse> deckResults) {
                buildQuizList(deckResults);

                layoutLoading.setVisibility(View.GONE);
                layoutQuestion.setVisibility(View.VISIBLE);
                btnCheck.setVisibility(View.VISIBLE);

                if (!quizList.isEmpty()) {
                    loadQuestionToUI(quizList.get(0));
                    startStopwatch();
                }
            }

            @Override
            public void onError(Throwable t) {
                layoutLoading.setVisibility(View.GONE);

                Log.e("QuizFragment", "Lỗi Gemini API: " + t.getMessage());
                Toast.makeText(getContext(), R.string.quiz_load_error, Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
            }
        });
    }

    private void buildQuizList(List<GeminiHelper.QuizGenResponse> aiResponses) {
        for (int i = 0; i < quizList.size(); i++) {
            var card = quizList.get(i);
            GeminiHelper.QuizGenResponse aiResult = findResponseById(aiResponses, card.id.toString());

            if (aiResult == null || aiResult.results == null || aiResult.results.isEmpty()) continue;

            List<String> options = new ArrayList<>();

            Log.d("Init data", card.questionType.toString());

            switch (card.questionType) {
                case AUDIO:
                    card.correctAnswer = card.word;
                    options.addAll(aiResult.results.subList(0, Math.min(3, aiResult.results.size())));
                    options.add(card.correctAnswer);
                    break;

                case WORD:
                    card.correctAnswer = card.meaning;
                    options.addAll(aiResult.results.subList(0, Math.min(3, aiResult.results.size())));
                    options.add(card.correctAnswer);
                    break;

                case SYNONYM:
                default:
                    if (!aiResult.results.isEmpty()) {
                        if (aiResult.results.size() < 4) {
                            card.correctAnswer = card.word;
                            options.add(card.word);
                        } else {
                            card.correctAnswer = aiResult.results.get(0);
                        }
                        options.addAll(aiResult.results.subList(0, Math.min(4, aiResult.results.size())));
                    } else {
                        card.correctAnswer = card.word;
                        options.add(card.correctAnswer);
                    }

                    if (options.size() < 4) {
                        for (QuizQuestion randomCard : quizList) {
                            if (options.size() >= 4) break;

                            if (!options.contains(randomCard.word) && !randomCard.word.equals(card.correctAnswer)) {
                                options.add(randomCard.word);
                            }
                        }

                        while (options.size() < 4) {
                            options.add("Unknown Word " + options.size());
                        }
                    }
                    break;
            }

            Collections.shuffle(options);
            card.options = options;
        }
    }

    private GeminiHelper.QuizGenResponse findResponseById(List<GeminiHelper.QuizGenResponse> list, String id) {
        for (GeminiHelper.QuizGenResponse res : list) {
            if (res.id.equals(id)) return res;
        }
        return null;
    }

    private void loadQuestionToUI(QuizQuestion question) {
        isInteractionLocked = false;
        selectedButton = null;
        resetButtonStyles();

        Button[] activeButtons;

        switch (question.questionType) {
            case AUDIO:
                tvInstruction.setText(R.string.quiz_audio_instruction);
                tvInstruction.setGravity(Gravity.CENTER);

                layoutWordAndAudio.setVisibility(View.GONE);
                layoutWord.setVisibility(View.GONE);
                btnPlayLarge.setVisibility(View.VISIBLE);

                layoutOptionsGrid.setVisibility(View.VISIBLE);
                layoutOptionsVertical.setVisibility(View.GONE);
                activeButtons = gridButtons;

                playCurrentAudio();
                break;

            case WORD:
                tvInstruction.setText(R.string.quiz_word_instruction);
                tvInstruction.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                tvQuestionWord.setText(question.word);

                layoutWordAndAudio.setVisibility(View.VISIBLE);
                layoutWord.setVisibility(View.GONE);
                btnPlayLarge.setVisibility(View.GONE);

                layoutOptionsGrid.setVisibility(View.GONE);
                layoutOptionsVertical.setVisibility(View.VISIBLE);
                activeButtons = vertButtons;
                break;

            case SYNONYM:
            default:
                tvInstruction.setText(R.string.quiz_synonym_instruction);
                tvInstruction.setGravity(Gravity.CENTER);
                tvQuestionWordSynonym.setText(question.word);

                layoutWordAndAudio.setVisibility(View.GONE);
                layoutWord.setVisibility(View.VISIBLE);
                btnPlayLarge.setVisibility(View.GONE);

                layoutOptionsGrid.setVisibility(View.GONE);
                layoutOptionsVertical.setVisibility(View.VISIBLE);
                activeButtons = vertButtons;
                break;
        }

        for (int i = 0; i < 4; i++) {
            if (question.options != null && i < question.options.size()) {
                activeButtons[i].setText(question.options.get(i));
                activeButtons[i].setVisibility(View.VISIBLE);
            } else {
                activeButtons[i].setVisibility(View.INVISIBLE);
            }
        }

        progressBar.setMax(quizList.size());
        progressBar.setProgress(currentIndex + 1, true);
    }

    private void checkAnswer() {
        isInteractionLocked = true;

        QuizQuestion currentQuestion = quizList.get(currentIndex);
        String selectedAnswer = selectedButton.getText().toString();

        if (selectedAnswer.equals(currentQuestion.correctAnswer)) {
            soundManager.playSound(SoundManager.SoundEvent.CORRECT_ANSWER);

            selectedButton.setBackgroundResource(R.drawable.bg_choice_correct);
            selectedButton.setTextColor(Color.parseColor("#4FBC53"));
            selectedButton.setTypeface(selectedButton.getTypeface(), Typeface.BOLD);

            currentScore++;
        } else {
            soundManager.playSound(SoundManager.SoundEvent.WRONG_ANSWER);

            selectedButton.setBackgroundResource(R.drawable.bg_choice_incorrect);
            selectedButton.setTextColor(Color.parseColor("#F64F43"));
            selectedButton.setTypeface(selectedButton.getTypeface(), Typeface.BOLD);

            String wrongWord = currentQuestion.word;
            if (!wrongWordsList.contains(wrongWord)) {
                wrongWordsList.add(wrongWord);
            }

            highlightCorrectAnswer(currentQuestion.correctAnswer, currentQuestion.questionType);
        }

        moveToNextQuestionWithDelay();
    }

    private void highlightCorrectAnswer(String correctAnswer, QuizQuestion.Type type) {
        Button[] targetButtons = (type == QuizQuestion.Type.AUDIO) ? gridButtons : vertButtons;
        for (Button btn : targetButtons) {
            if (btn.getText().toString().equals(correctAnswer)) {
                btn.setBackgroundResource(R.drawable.bg_choice_correct);
                btn.setTextColor(Color.parseColor("#4FBC53"));
                btn.setTypeface(btn.getTypeface(), Typeface.BOLD);

                break;
            }
        }
    }

    private void startStopwatch() {
        timerHandler.removeCallbacks(timerRunnable);

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                elapsedSeconds++;
                int minutes = elapsedSeconds / 60;
                int seconds = elapsedSeconds % 60;

                String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                tvTimer.setText(timeFormatted);

                timerHandler.postDelayed(this, 1000);
            }
        };

        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void moveToNextQuestionWithDelay() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            currentIndex++;
            if (currentIndex < quizList.size()) {
                loadQuestionToUI(quizList.get(currentIndex));
            } else {
                timerHandler.removeCallbacks(timerRunnable);
                QuizHistory history = new QuizHistory();
                history.setResultId(UUID.randomUUID());
                history.setDeckId(this.deckId);
                history.setCorrectCount(currentScore);
                history.setTotalQuestions(quizList.size());
                history.setTakenAt(new Date());
                history.setTimeTaken(elapsedSeconds);

                viewModel.saveQuizHistory(history, isSuccess -> {
                    if (isSuccess) {
                        if (!isAdded() || getActivity() == null) return;

                        Bundle bundle = new Bundle();
                        bundle.putInt("SCORE", currentScore);
                        bundle.putInt("TOTAL_QUESTIONS", quizList.size());
                        bundle.putStringArrayList("WRONG_WORDS", wrongWordsList);
                        bundle.putSerializable("DECK_ID", deckId);
                        bundle.putString("DECK_NAME", deckName);

                        androidx.navigation.Navigation.findNavController(requireView())
                                .navigate(R.id.action_quiz_to_result, bundle);

                    } else {
                        Toast.makeText(requireContext(), R.string.quiz_save_error, Toast.LENGTH_SHORT).show();
                        requireActivity().onBackPressed();
                    }
                });
            }
        }, 1500);
    }

    private void resetButtonStyles() {
        for (int i = 0; i < 4; i++) {
            gridButtons[i].setBackgroundResource(R.drawable.bg_choice_normal);
            gridButtons[i].setTextColor(Color.parseColor("#424242"));
            gridButtons[i].setTypeface(null, Typeface.NORMAL);

            vertButtons[i].setBackgroundResource(R.drawable.bg_choice_normal);
            vertButtons[i].setTextColor(Color.parseColor("#424242"));
            vertButtons[i].setTypeface(null, Typeface.NORMAL);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timerRunnable != null) timerHandler.removeCallbacks(timerRunnable);
        if (pronunciationManager != null) pronunciationManager.releaseResources();
    }
}