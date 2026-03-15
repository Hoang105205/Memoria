package com.example.memoria.ui.quiz;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.memoria.R;
import com.example.memoria.ui.library.CardViewModel;
import com.example.memoria.utils.PronunciationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class QuizFragment extends Fragment {
    private UUID deckId;
    private String deckName;

    private CardViewModel viewModel;

    private List<QuizQuestion> questionList;

    private TextView tvTimer;
    private ProgressBar progressBar;
    private ImageButton btnCloseQuiz;

    private TextView tvInstruction, tvQuestionWord;
    private ImageButton btnPlayLarge, btnPlaySmall;
    private LinearLayout layoutWordAndAudio;

    private LinearLayout layoutOptionsGrid, layoutOptionsVertical;
    private final Button[] gridButtons = new Button[4];
    private final Button[] vertButtons = new Button[4];

    private CountDownTimer countDownTimer;
    private PronunciationManager pronunciationManager;
    private final List<QuizQuestion> quizList = new ArrayList<>();
    private int currentIndex = 0;
    private int currentScore = 0;
    private boolean isInteractionLocked = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTimer = view.findViewById(R.id.tv_timer);
        progressBar = view.findViewById(R.id.bg_progress_bar);
        btnCloseQuiz = view.findViewById(R.id.btn_close_quiz);

        if (btnCloseQuiz != null) {
            btnCloseQuiz.setOnClickListener(v -> requireActivity().onBackPressed());
        }

        tvInstruction = view.findViewById(R.id.tvInstruction);
        tvQuestionWord = view.findViewById(R.id.tvQuestionWord);
        btnPlayLarge = view.findViewById(R.id.btnPlayLarge);
        btnPlaySmall = view.findViewById(R.id.btnPlaySmall);
        layoutWordAndAudio = view.findViewById(R.id.layoutWordAndAudio);

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

        // 4. GẮN SỰ KIỆN CLICK CHO ĐÁP ÁN
        View.OnClickListener answerClickListener = v -> {
            if (!isInteractionLocked) {
                checkAnswer((Button) v);
            }
        };

        for (int i = 0; i < 4; i++) {
            gridButtons[i].setOnClickListener(answerClickListener);
            vertButtons[i].setOnClickListener(answerClickListener);
        }

        viewModel = new ViewModelProvider(this).get(CardViewModel.class);


        // 5. KHỞI TẠO TTS
        pronunciationManager = new PronunciationManager(requireContext());
    }

    private void loadQuestionToUI(QuizQuestion question) {
        // TODO: Load quiz question
    }

    // TASK 1 & 2: Chuẩn bị dữ liệu và tạo list câu hỏi
    public void setupQuizData() {
        // TODO: Setup dữ liệu quiz
    }

    private void checkAnswer(Button selectedBtn) {
        // TODO: Check câu trả lời
    }

    private void handleTimeUp() {
        // TODO: Xử lý khi hết giờ
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) countDownTimer.cancel();
        if (pronunciationManager != null) pronunciationManager.releaseResources();
    }
}