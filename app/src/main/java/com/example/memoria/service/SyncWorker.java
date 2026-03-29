package com.example.memoria.service;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.memoria.data.repository.SyncRepository;

import java.util.concurrent.CountDownLatch;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class SyncWorker extends Worker {

    private final SyncRepository syncRepository;

    @AssistedInject
    public SyncWorker(@Assisted @NonNull Context context,
                      @Assisted @NonNull WorkerParameters workerParams,
                      SyncRepository syncRepository) {
        super(context, workerParams);
        this.syncRepository = syncRepository;
    }

    @NonNull
    @Override
    public Result doWork() {
        // Lấy userId được truyền vào khi gọi Worker
        String userId = getInputData().getString("USER_ID");
        if (userId == null || userId.isEmpty()) {
            return Result.failure();
        }

        // Nào làm tới Quiz thì thay thành 3
        int SYNC_TASKS_COUNT = 2;
        CountDownLatch latch = new CountDownLatch(SYNC_TASKS_COUNT);

        // Mặc định là true, nếu có bất kỳ luồng nào thất bại thì chuyển thành false
        final boolean[] isSuccess = {true};

        // Gọi hàm đồng bộ Favorite
        syncRepository.syncFavorites(userId, success -> {
            if (!success) isSuccess[0] = false;
            latch.countDown(); // Xong 1 task, giảm khóa đi 1
        });

         // Gọi hàm đồng bộ DeckCard
        syncRepository.syncDecksAndCards(userId, success -> {
            if (!success) isSuccess[0] = false;
            latch.countDown(); // Xong 1 task, giảm khóa đi 1
        });

        // Gọi hàm đồng bộ Quiz
        // syncRepository.syncQuizData(userId, success -> {
        //     if (!success) isSuccess[0] = false;
        //     latch.countDown();
        // });

        try {
            // Hệ thống sẽ đứng chờ ở đây cho đến khi biến latch giảm về 0
            // (Tức là tất cả các callback trên Firebase đều đã trả về kết quả)
            latch.await();
        } catch (InterruptedException e) {
            return Result.retry(); // Nếu bị lỗi hệ thống ngắt giữa chừng, yêu cầu chạy lại sau
        }

        // Nếu tất cả đều true -> success. Nếu có 1 cái false -> retry (chạy lại)
        return isSuccess[0] ? Result.success() : Result.retry();
    }
}
