package com.loopers.application.like;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.vo.Price;
import com.loopers.domain.product.vo.Stock;
import com.loopers.domain.user.User;
import com.loopers.domain.user.vo.BirthDate;
import com.loopers.domain.user.vo.Email;
import com.loopers.domain.user.vo.LoginId;
import com.loopers.domain.user.vo.Name;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LikeFacadeConcurrencyTest {

    @Autowired
    private LikeFacade likeFacade;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private BrandJpaRepository brandJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private Long productId;

    @BeforeEach
    void setUp() {
        Brand brand = brandJpaRepository.save(Brand.of("나이키", "스포츠 브랜드"));
        Product product = productJpaRepository.save(
                Product.of("에어맥스", "운동화", Stock.from(100), Price.from(150000), brand.getId())
        );
        productId = product.getId();
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("서로 다른 N명이 동시에 같은 상품에 좋아요를 누르면, likeCount 가 정확히 N이어야 한다.")
    @Test
    void like_concurrently_byMultipleUsers_likeCountIsAccurate() throws InterruptedException {
        // arrange
        int threadCount = 5;
        List<Long> userIds = createUsers(threadCount);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        // act
        for (int i = 0; i < threadCount; i++) {
            Long userId = userIds.get(i);
            executor.submit(() -> {
                try {
                    startLatch.await();
                    likeFacade.like(userId, productId);
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
        Product product = productJpaRepository.findById(productId).orElseThrow();
        assertThat(product.likeCount()).isEqualTo(threadCount);
    }

    @DisplayName("동일 사용자가 같은 상품에 동시에 N번 좋아요를 눌러도, likeCount 는 1이어야 한다.")
    @Test
    void like_concurrently_bySameUser_likeCountIsOne() throws InterruptedException {
        // arrange
        int threadCount = 5;
        User user = userJpaRepository.save(User.create(
                LoginId.from("testUser1"),
                "encodedPassword",
                Name.from("홍길동"),
                BirthDate.from(LocalDate.of(1990, 1, 1)),
                Email.from("test@loopers.im")
        ));
        Long userId = user.getId();

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        // act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    likeFacade.like(userId, productId);
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
        Product product = productJpaRepository.findById(productId).orElseThrow();
        assertThat(product.likeCount()).isEqualTo(1);
    }

    private List<Long> createUsers(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> userJpaRepository.save(User.create(
                        LoginId.from("testUser" + i),
                        "encodedPassword",
                        Name.from("홍길동"),
                        BirthDate.from(LocalDate.of(1990, 1, 1)),
                        Email.from("test" + i + "@loopers.im")
                )).getId())
                .toList();
    }
}
