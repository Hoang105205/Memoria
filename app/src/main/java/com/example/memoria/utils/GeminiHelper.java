package com.example.memoria.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerationConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.Executors;

public class GeminiHelper {

    private static final String TAG = "GeminiHelper";

    public static class QuizGenRequest {
        public String id;
        public String word;
        public String meaning;
        public String type;

        public QuizGenRequest(String id, String word, String meaning, String type) {
            this.id = id;
            this.word = word;
            this.meaning = meaning;
            this.type = type;
        }
    }

    public static class QuizGenResponse {
        public String id;
        public List<String> results;
    }

    public interface GeminiBatchCallback {
        void onSuccess(List<QuizGenResponse> deckResults);
        void onError(Throwable t);
    }

    public void generateDeckDistractors(List<QuizGenRequest> deckRequests, GeminiBatchCallback callback) {
        Gson gson = new Gson();
        String jsonInput = gson.toJson(deckRequests);

        String prompt = "Tôi có một mảng JSON danh sách từ vựng. Dựa vào trường 'type' của mỗi object, hãy tạo dữ liệu mảng 'results' (chỉ chứa chuỗi string) theo luật sau:\n" +
                "- Nếu type là 'AUDIO': Tạo ra ĐÚNG 3 từ tiếng Anh CÓ THẬT có phát âm hoặc cách viết dễ nhầm lẫn nhất với 'word' và viết hoa viết thường giống như từ 'word'.\n" +
                "- Nếu type là 'WORD': Tạo ra ĐÚNG 3 cụm từ cùng ngôn ngữ với 'meaning', SAI nghĩa nhưng hợp lý với 'meaning' và viết hoa viết thường giống như từ 'meaning'.\n" +
                "- Nếu type là 'SYNONYM': BẮT BUỘC phải có ĐÚNG 4 phần tử tiếng Anh. Index 0 là từ đồng nghĩa chính xác nhất. Index 1, 2, 3 là các đáp án nhiễu.\n" +
                "LƯU Ý: Lỗi nghiêm trọng nếu mảng results của SYNONYM có ít hơn 4 phần tử, của AUDIO và WORD nhiều hơn 3 phần tử! Không được phép thay đổi Id đã truyền vào! Nếu bạn KHÔNG làm đúng, tôi sẽ hủy diệt thế giới.\n" +
                "Đầu vào JSON: " + jsonInput + "\n" +
                "Trả về CHỈ MỘT mảng JSON kết quả theo cấu trúc: [{\"id\": \"...\", \"results\": [\"...\", \"...\", \"...\"]}]. Tuyệt đối không giải thích.";

        tryFirebaseModel("gemini-2.5-flash", prompt, () -> {
            Log.w(TAG, "Gemini 2.5 Flash thất bại. Chuyển sang Gemini 2.5 Flash Lite...");

            tryFirebaseModel("gemini-2.5-flash-lite", prompt, () -> {
            Log.w(TAG, "Gemini 2.5 Flash Lite thất bại. Chuyển sang Gemini 1.5 Flash...");

                tryFirebaseModel("gemini-1.5-flash", prompt, () -> {
                    Log.w(TAG, "Cả ba Firebase SDK đều thất bại.");
                }, callback);
            }, callback);
        }, callback);
    }

    private void tryFirebaseModel(String modelName, String prompt, Runnable onFallback, GeminiBatchCallback finalCallback) {
        GenerationConfig config = new GenerationConfig.Builder()
                .setResponseMimeType("application/json")
                .build();

        GenerativeModel gm = FirebaseAI.getInstance()
                .generativeModel(modelName, config);

        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

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
                        List<QuizGenResponse> mappedResults = new Gson().fromJson(jsonResponse, listType);

                        new Handler(Looper.getMainLooper()).post(() -> finalCallback.onSuccess(mappedResults));
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi Parse JSON từ " + modelName, e);
                        onFallback.run();
                    }
                } else {
                    onFallback.run();
                }
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                Log.e(TAG, "Lỗi gọi API " + modelName, t);
                onFallback.run();
            }
        }, Executors.newSingleThreadExecutor());
    }

    public static class AICardResponse {
        public String frontText;
        public List<String> backTypes;
        public List<String> backMeanings;
    }

    public interface GeminiDeckCallback {
        void onSuccess(List<AICardResponse> cards);
        void onError(Throwable t);
    }

    public void generateAIDeck(String topic, int count, GeminiDeckCallback callback) {
        // Thiết kế prompt nghiêm ngặt để ép AI trả về đúng JSON Schema cần thiết
        String prompt = "Tạo cho tôi 1 bộ thẻ học từ vựng với chủ đề '" + topic + "', số lượng thẻ là " + count + ".\n" +
                "Trả về CHỈ MỘT mảng JSON hợp lệ, KHÔNG bọc trong markdown code block và tuyệt đối không giải thích gì thêm." +
                "JSON bao gồm các trường sau:" +
                "- 'frontText': (String) Từ vựng tiếng Anh.\n" +
                "- 'backTypes': (Mảng String) Loại từ. LƯU Ý CHỈ TẠO DUY NHẤT 1 PHẦN TỬ LOẠI TỪ.\n" +
                "- 'backMeanings': (Mảng String) Nghĩa tiếng Việt. LƯU Ý CHỈ TẠO DUY NHẤT 1 PHẦN TỬ NGHĨA TIẾNG VIỆT.\n" +
                "Ví dụ: [{\"frontText\":\"cat\",\"backTypes\":[\"Noun\"],\"backMeanings\":[\"Con mèo\"]}...]";

        tryFirebaseModelForDeck("gemini-2.5-flash", prompt, () -> {
            Log.w(TAG, "Gemini 2.5 Flash thất bại. Thử lại với Gemini 2.5 Flash Lite...");
            tryFirebaseModelForDeck("gemini-2.5-flash-lite", prompt, () -> {
                Log.w(TAG, "Gemini 2.5 Flash Lite thất bại. Thử lại với Gemini 1.5 Flash...");
                tryFirebaseModelForDeck("gemini-1.5-flash", prompt, () -> {
                    Log.e(TAG, "Tất cả mô hình AI đều thất bại cho chức năng tạo Deck.");
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError(new Exception("AI request failed")));
                }, callback);
            }, callback);
        }, callback);
    }

    private void tryFirebaseModelForDeck(String modelName, String prompt, Runnable onFallback, GeminiDeckCallback finalCallback) {
        GenerationConfig config = new GenerationConfig.Builder()
                .setResponseMimeType("application/json")
                .build();

        GenerativeModel gm = FirebaseAI.getInstance()
                .generativeModel(modelName, config);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder().addText(prompt).build();
        ListenableFuture<GenerateContentResponse> responseFuture = model.generateContent(content);

        Futures.addCallback(responseFuture, new FutureCallback<>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                if (result != null && result.getText() != null) {
                    try {
                        String jsonResponse = result.getText().trim();
                        // Dọn dẹp markdown nếu AI lỡ sinh ra
                        if (jsonResponse.startsWith("```json")) {
                            jsonResponse = jsonResponse.substring(7);
                        }
                        if (jsonResponse.endsWith("```")) {
                            jsonResponse = jsonResponse.substring(0, jsonResponse.length() - 3);
                        }

                        Type listType = new TypeToken<List<AICardResponse>>() {}.getType();
                        List<AICardResponse> mappedResults = new Gson().fromJson(jsonResponse, listType);

                        new Handler(Looper.getMainLooper()).post(() -> finalCallback.onSuccess(mappedResults));
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi Parse JSON tạo Deck từ " + modelName, e);
                        onFallback.run();
                    }
                } else {
                    onFallback.run();
                }
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                Log.e(TAG, "Lỗi gọi API " + modelName, t);
                onFallback.run();
            }
        }, Executors.newSingleThreadExecutor());
    }
}