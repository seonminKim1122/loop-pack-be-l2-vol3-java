package com.loopers.application.order;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.UserCoupon;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.vo.Price;
import com.loopers.domain.product.vo.Stock;
import com.loopers.domain.user.User;
import com.loopers.domain.user.vo.BirthDate;
import com.loopers.domain.user.vo.Email;
import com.loopers.domain.user.vo.LoginId;
import com.loopers.domain.user.vo.Name;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.coupon.CouponJpaRepository;
import com.loopers.infrastructure.coupon.UserCouponJpaRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderFacadeConcurrencyTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private BrandJpaRepository brandJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private CouponJpaRepository couponJpaRepository;

    @Autowired
    private UserCouponJpaRepository userCouponJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private Long userId;
    private Long productId;

    @BeforeEach
    void setUp() {
        User user = userJpaRepository.save(User.create(
                LoginId.from("testUser1"),
                "encodedPassword",
                Name.from("홍길동"),
                BirthDate.from(LocalDate.of(1990, 1, 1)),
                Email.from("test@loopers.im")
        ));
        userId = user.getId();

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

    @DisplayName("재고 100개인 상품에 5명이 동시에 5개씩 주문 시, 5건 모두 성공하고 최종 재고는 75개여야 한다.")
    @Test
    void createOrder_concurrently_stockDecreasedAccurately() throws InterruptedException {
        // arrange
        int threadCount = 5;
        int quantityPerThread = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        OrderCommand command = new OrderCommand(List.of(new OrderCommand.Item(productId, quantityPerThread)), null);

        // act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    orderFacade.createOrder(userId, command);
                    successCount.incrementAndGet();
                } catch (CoreException e) {
                    if (e.getErrorType() == ErrorType.BAD_REQUEST) {
                        failCount.incrementAndGet();
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
        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(failCount.get()).isEqualTo(0);

        Product product = productJpaRepository.findById(productId).orElseThrow();
        assertThat(product.stock().value()).isEqualTo(100 - threadCount * quantityPerThread);
    }

    @DisplayName("동일한 쿠폰으로 동시에 2건 주문하면, 1건만 성공하고 1건은 실패해야 한다.")
    @Test
    void createOrder_withSameCoupon_concurrently_onlyOneSucceeds() throws InterruptedException {
        // arrange
        Coupon coupon = couponJpaRepository.save(
                Coupon.of("10% 할인 쿠폰", "RATE", 10, ZonedDateTime.now().plusDays(30))
        );
        UserCoupon userCoupon = userCouponJpaRepository.save(UserCoupon.of(coupon, userId));
        Long userCouponId = userCoupon.getId();

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        OrderCommand command = new OrderCommand(
                List.of(new OrderCommand.Item(productId, 1)),
                userCouponId
        );

        // act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    orderFacade.createOrder(userId, command);
                    successCount.incrementAndGet();
                } catch (CoreException e) {
                    failCount.incrementAndGet();
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
        assertThat(failCount.get()).isEqualTo(1);
    }
}
