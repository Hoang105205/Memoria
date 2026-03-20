package com.example.memoria.service;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.memoria.data.repository.FavRepository;
// import com.example.memoria.data.repository.CardRepository;
// import com.example.memoria.data.repository.DeckRepository;
// import com.example.memoria.data.repository.QuizRepository;

import java.util.concurrent.CountDownLatch;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class SyncWorker extends Worker {

    private final FavRepository favRepository;
    // private final CardRepository cardRepository;
    // private final DeckRepository deckRepository;
    // private final QuizRepository quizRepository;

    @AssistedInject
    public SyncWorker(@Assisted @NonNull Context context,
                      @Assisted @NonNull WorkerParameters workerParams,
                      FavRepository favRepository
                      // , CardRepository cardRepository
                      // , DeckRepository deckRepository
                      // , QuizRepository quizRepository
                      ) {
        super(context, workerParams);
        this.favRepository = favRepository;
        // this.cardRepository = cardRepository;
        // this.deckRepository = deckRepository;
        // this.quizRepository = quizRepository;
    }

    @NonNull
    @Override
    public Result doWork() {
        // Lấy userId được truyền vào khi gọi Worker
        String userId = getInputData().getString("USER_ID");
        if (userId == null || userId.isEmpty()) {
            return Result.failure();
        }

        // ToDo: Sau này sẽ phải tiêm thêm các repository khác lên, thay CountDownLatch thành số repo cần đẩy và thực hiện
        CountDownLatch latch = new CountDownLatch(1); // Thay bằng dòng này CountDownLatch latch = new CountDownLatch(4);

        final boolean[] isSuccess = {false}; // Thay bằng dòng này final boolean[] isSuccess = {true};

        // Gọi hàm đồng bộ từ Repository
        favRepository.syncDataToCloud(userId, success -> {
            isSuccess[0] = success; // Thay dòng này thành if (!success) isSuccess[0] = false;
            latch.countDown(); // Nhả khóa khi Firebase chạy xong (Dù thành công hay thất bại)
        });

        // 2. Gọi hàm đồng bộ từ CardRepository
        // cardRepository.syncDataToCloud(userId, success -> {
        //     if (!success) isSuccess[0] = false;
        //     latch.countDown(); // Nhả khóa 2
        // });

        // 3. Gọi hàm đồng bộ từ DeckRepository
        // deckRepository.syncDataToCloud(userId, success -> {
        //     if (!success) isSuccess[0] = false;
        //     latch.countDown(); // Nhả khóa 3
        // });

        // 4. Gọi hàm đồng bộ từ QuizRepository
        // quizRepository.syncDataToCloud(userId, success -> {
        //     if (!success) isSuccess[0] = false;
        //     latch.countDown(); // Nhả khóa 4
        // });

        try {
            // Bắt buộc hệ thống chờ ở đây cho đến khi 4 thằng Firebase phản hồi hết
            latch.await();
        } catch (InterruptedException e) {
            return Result.retry(); // Nếu bị lỗi hệ thống ngắt giữa chừng, yêu cầu chạy lại sau
        }

        // Trả về kết quả cho Android OS biết
        return isSuccess[0] ? Result.success() : Result.retry();
    }
}
