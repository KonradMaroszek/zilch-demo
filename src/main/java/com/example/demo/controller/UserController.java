package com.example.demo.controller;

import static org.springframework.http.HttpStatus.OK;

import com.example.demo.model.User;
import com.example.demo.model.payment.transaction.PaymentTransaction;
import com.example.demo.service.UserService;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@CrossOrigin
@RestController
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @ResponseBody
  @ResponseStatus(OK)
  @GetMapping(path = "/users/{userId}")
  public @NotNull User getUser(@PathVariable Long userId) {
    return userService.getUser(userId);
  }

  @ResponseBody
  @ResponseStatus(OK)
  @GetMapping(path = "/users")
  public @NotNull List<@NotNull User> getUsers() {
    return userService.getUsers();
  }

  @ResponseBody
  @ResponseStatus(OK)
  @GetMapping(path = "/users/{userId}/payment-transactions/{transactionId}")
  public @NotNull PaymentTransaction getUserTransaction(
      @PathVariable Long userId, @PathVariable Long transactionId) {
    return userService.getUserTransaction(userId, transactionId);
  }

  @ResponseBody
  @ResponseStatus(OK)
  @GetMapping(path = "/users/{userId}/payment-transactions")
  public @NotNull List<@NotNull PaymentTransaction> getUserTransactions(@PathVariable Long userId) {
    return userService.getUserTransactions(userId);
  }
}
