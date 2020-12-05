package com.example.demo.service;

import static com.example.demo.model.payment.status.PaymentStatus.PAYED;
import static com.example.demo.model.payment.status.PaymentStatus.SECOND_PART_PAYED;
import static com.example.demo.model.payment.status.PaymentStatus.THIRD_PART_PAYED;

import com.example.demo.exception.AlreadyPayedException;
import com.example.demo.exception.WrongPaymentTransactionStatus;
import com.example.demo.model.User;
import com.example.demo.model.payment.status.PaymentStatus;
import com.example.demo.model.payment.transaction.PaymentTransaction;
import com.example.demo.repository.PaymentTransactionRepository;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentTransactionService {
  private final PaymentTransactionRepository paymentTransactionRepository;
  private final UserService userService;

  public PaymentTransactionService(
      PaymentTransactionRepository paymentTransactionRepository, UserService userService) {
    this.paymentTransactionRepository = paymentTransactionRepository;
    this.userService = userService;
  }

  @Transactional
  @Retryable(DataAccessException.class)
  public @NotNull PaymentTransaction firstPayment(@NotNull Long userId) {
    User user = userService.getUser(userId);

    PaymentTransaction paymentTransaction = new PaymentTransaction();
    paymentTransaction.setPaymentStatus(PaymentStatus.FIRST_PART_PAYED);
    paymentTransaction.setUser(user);

    user.addPaymentTransaction(paymentTransaction);

    return saveTransaction(paymentTransaction);
  }

  @Transactional
  @Retryable(DataAccessException.class)
  public @NotNull PaymentTransaction nextPayment(
      @NotNull Long userId, @NotNull Long transactionId) {
    PaymentTransaction paymentTransaction = userService.getUserTransaction(userId, transactionId);

    switch (paymentTransaction.getPaymentStatus()) {
      case FIRST_PART_PAYED:
        paymentTransaction.setPaymentStatus(SECOND_PART_PAYED);
        break;
      case SECOND_PART_PAYED:
        paymentTransaction.setPaymentStatus(THIRD_PART_PAYED);
        break;
      case THIRD_PART_PAYED:
        paymentTransaction.setPaymentStatus(PAYED);
        break;
      case PAYED:
        throw new AlreadyPayedException(
            "Transaction with id " + transactionId + " was paid already");
      default:
        throw new WrongPaymentTransactionStatus("Wrong payment transaction status");
    }

    return saveTransaction(paymentTransaction);
  }

  @Transactional
  @Retryable(DataAccessException.class)
  public @NotNull PaymentTransaction saveTransaction(
      @NotNull PaymentTransaction paymentTransaction) {
    return paymentTransactionRepository.save(paymentTransaction);
  }

  @Recover
  @SuppressWarnings("unused")
  private @NotNull PaymentTransaction recovery(
      RuntimeException error, @NotNull PaymentTransaction paymentTransaction) {
    logger.error("Informing external systems about failure.");
    throw error;
  }

  @Recover
  @SuppressWarnings("unused")
  private @NotNull PaymentTransaction recovery(
      RuntimeException error, @NotNull Long userId, @NotNull Long transactionId) {
    logger.error("Informing external systems about failure.");
    throw error;
  }
}
