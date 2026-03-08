package com.loopers.application.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserFacadeConcurrencyTest {

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("동일한 loginId로 동시에 여러 회원가입 요청 시, 정확히 1건만 성공하고 나머지는 CONFLICT로 실패한다.")
    @Test
    void signup_concurrently_onlyOneSucceeds() throws InterruptedException {
        // arrange
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        // act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    userFacade.signup("testUser1", "test1234!", "홍길동", LocalDate.of(1990, 1, 1), "test@loopers.im");
                    successCount.incrementAndGet();
                } catch (CoreException e) {
                    if (e.getErrorType() == ErrorType.CONFLICT) {
                        conflictCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        // assert
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(threadCount - 1);
    }
}
