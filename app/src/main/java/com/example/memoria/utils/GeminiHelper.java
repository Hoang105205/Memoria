package com.example.memoria.utils;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.type.GenerationConfig;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.Executors;

public class GeminiHelper {

    // 1. DTO (Data Transfer Object) để gửi lên
    public static class QuizGenRequest {
        public String id;
        public String word;
        public String meaning;
        public String type; // "AUDIO", "WORD", "SYNONYM"

        public QuizGenRequest(String id, String word, String meaning, String type) {
            this.id = id;
            this.word = word;
            this.meaning = meaning;
            this.type = type;
        }
    }

    // 2. DTO để nhận kết quả về
    public static class QuizGenResponse {
        public String id;
        public List<String> results;
    }

    public interface GeminiBatchCallback {
        void onSuccess(List<QuizGenResponse> deckResults);
        void onError(Throwable t);
    }

    public void generateDeckDistractors(List<QuizGenRequest> deckRequests, GeminiBatchCallback callback) {
        GenerationConfig config = new GenerationConfig.Builder()
                .setResponseMimeType("application/json")
                .build();

        GenerativeModel gm = FirebaseAI.getInstance()
                .generativeModel("gemini-2.5-flash", config);

        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Gson gson = new Gson();
        String jsonInput = gson.toJson(deckRequests);

        String prompt = "Tôi có một mảng JSON danh sách từ vựng. Dựa vào trường 'type' của mỗi object, hãy tạo dữ liệu mảng 'results' (chỉ chứa chuỗi string) theo luật sau:\n" +
                "- Nếu type là 'AUDIO': Tạo 3 từ tiếng Anh CÓ THẬT có phát âm hoặc cách viết dễ nhầm lẫn nhất với 'word'.\n" +
                "- Nếu type là 'WORD': Tạo 3 cụm từ tiếng Việt sai nghĩa nhưng hợp lý và cùng từ loại với 'meaning'.\n" +
                "- Nếu type là 'SYNONYM': Mảng 'results' BẮT BUỘC phải có đúng 4 phần tử tiếng Anh. Phần tử đầu tiên (index 0) là từ đồng nghĩa chính xác nhất với 'word'. 3 phần tử còn lại là các từ gần nghĩa nhưng sai ngữ cảnh để làm đáp án nhiễu.\n" +
                "Đầu vào JSON: " + jsonInput + "\n" +
                "Trả về CHỈ MỘT mảng JSON kết quả theo cấu trúc: [{\"id\": \"...\", \"results\": [\"...\", \"...\", \"...\"]}]. Tuyệt đối không giải thích.";

        Content content = new Content.Builder().addText(prompt).build();
        ListenableFuture<GenerateContentResponse> responseFuture = model.generateContent(content);

        Futures.addCallback(responseFuture, new FutureCallback<>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                if (result != null && result.getText() != null) {
                    try {
                        String jsonResponse = result.getText().trim();

                        Type listType = new TypeToken<List<QuizGenResponse>>() {
                        }.getType();
                        List<QuizGenResponse> mappedResults = gson.fromJson(jsonResponse, listType);

                        new Handler(Looper.getMainLooper()).post(() ->
                                callback.onSuccess(mappedResults)
                        );
                    } catch (Exception e) {
                        new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
                    }
                } else {
                    new Handler(Looper.getMainLooper()).post(() ->
                            callback.onError(new Exception("AI Response is empty"))
                    );
                }
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(t));
            }
        }, Executors.newSingleThreadExecutor());
    }
}