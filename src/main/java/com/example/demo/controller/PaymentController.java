package com.example.demo.controller;

import static org.springframework.http.HttpStatus.OK;

import com.example.demo.model.payment.transaction.PaymentTransaction;
import com.example.demo.service.PaymentTransactionService;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@CrossOrigin
@RestController
public class PaymentController {
  private final PaymentTransactionService paymentTransactionService;

  public PaymentController(PaymentTransactionService paymentTransactionService) {

    this.paymentTransactionService = paymentTransactionService;
  }

  @ResponseBody
  @ResponseStatus(OK)
  @PostMapping(path = "/users/{userId}/pay")
  public @NotNull PaymentTransaction firstPayment(@PathVariable Long userId) {
    return paymentTransactionService.firstPayment(userId);
  }

  @ResponseBody
  @ResponseStatus(OK)
  @PutMapping(path = "/users/{userId}/next-payment/{transactionId}")
  public @NotNull PaymentTransaction nextPayment(
      @PathVariable Long userId, @PathVariable Long transactionId) {
    return paymentTransactionService.nextPayment(userId, transactionId);
  }
}
