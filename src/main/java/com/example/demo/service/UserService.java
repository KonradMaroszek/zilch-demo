package com.example.demo.service;

import com.example.demo.exception.TransactionNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.User;
import com.example.demo.model.payment.transaction.PaymentTransaction;
import com.example.demo.repository.UserRepository;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public @NotNull List<@NotNull User> getUsers() {
    return userRepository.findAll();
  }

  public @NotNull User getUser(@NotNull Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found."));
  }

  public @NotNull List<@NotNull PaymentTransaction> getUserTransactions(@NotNull Long userId) {
    return getUser(userId).getPaymentTransactions();
  }

  public @NotNull PaymentTransaction getUserTransaction(
      @NotNull Long userId, @NotNull Long transactionId) {
    return getUser(userId).getPaymentTransactions().stream()
        .filter(paymentTransaction -> paymentTransaction.getId().equals(transactionId))
        .findFirst()
        .orElseThrow(
            () ->
                new TransactionNotFoundException(
                    "Transaction with id " + transactionId + " not found in user " + userId));
  }
}
