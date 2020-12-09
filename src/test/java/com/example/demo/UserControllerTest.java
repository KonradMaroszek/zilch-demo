package com.example.demo;

import static com.example.demo.constant.Constants.X_TRACE_ID_HEADER;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.configuration.AppConfig;
import com.example.demo.controller.UserController;
import com.example.demo.exception.handler.GlobalDefaultExceptionHandler;
import com.example.demo.exception.handler.UserExceptionHandler;
import com.example.demo.interceptor.RequestFilter;
import com.example.demo.model.User;
import com.example.demo.model.error.ErrorResponse;
import com.example.demo.model.payment.status.PaymentStatus;
import com.example.demo.model.payment.transaction.PaymentTransaction;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({UserController.class})
@ContextConfiguration(
    classes = {AppConfig.class, GlobalDefaultExceptionHandler.class, UserExceptionHandler.class})
class UserControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean UserRepository userRepository;
  @SpyBean UserService userService;

  @SpyBean
  @SuppressWarnings("unused")
  private RequestFilter requestFilter;

  @Test
  void shouldGetCorrectUser() throws Exception {
    User userToReturn = new User();
    userToReturn.setId(1L);
    userToReturn.setName("USER 1");

    when(userRepository.findById(1L)).thenReturn(Optional.of(userToReturn));

    String responseString =
        mockMvc
            .perform(get("/users/1"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(UTF_8);

    User response = new Gson().fromJson(responseString, User.class);

    assertEquals(userToReturn, response);
    verify(userService, times(1)).getUser(1L);
    verify(userService, times(0)).getUsers();
  }

  @Test
  void shouldGetCorrectUsers() throws Exception {
    User userToReturn = new User();
    userToReturn.setId(1L);
    userToReturn.setName("USER 1");

    User secondUSerToReturn = new User();
    userToReturn.setId(2L);
    userToReturn.setName("USER 2");

    when(userRepository.findAll()).thenReturn(List.of(userToReturn, secondUSerToReturn));

    String responseString =
        mockMvc
            .perform(get("/users"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(UTF_8);

    Type listType = new TypeToken<ArrayList<User>>() {}.getType();
    List<User> response = new Gson().fromJson(responseString, listType);

    assertEquals(List.of(userToReturn, secondUSerToReturn), response);
    verify(userService, times(1)).getUsers();
    verify(userService, times(0)).getUser(1L);
    verify(userService, times(0)).getUser(2L);
  }

  @Test
  void shouldGetCorrectTransaction() throws Exception {
    User userToReturn = new User();
    userToReturn.setId(1L);
    userToReturn.setName("USER 1");

    PaymentTransaction paymentTransactionToReturn1 = new PaymentTransaction();
    paymentTransactionToReturn1.setPaymentStatus(PaymentStatus.PAYED);
    paymentTransactionToReturn1.setId(11L);

    PaymentTransaction paymentTransactionToReturn2 = new PaymentTransaction();
    paymentTransactionToReturn2.setPaymentStatus(PaymentStatus.PAYED);
    paymentTransactionToReturn2.setId(12L);

    userToReturn.setPaymentTransactions(
        List.of(paymentTransactionToReturn1, paymentTransactionToReturn2));

    when(userRepository.findById(1L)).thenReturn(Optional.of(userToReturn));

    String responseString =
        mockMvc
            .perform(get("/users/1/payment-transactions/12"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(UTF_8);

    PaymentTransaction response = new Gson().fromJson(responseString, PaymentTransaction.class);

    assertEquals(paymentTransactionToReturn2, response);
    verify(userService, times(1)).getUser(1L);
    verify(userService, times(0)).getUsers();
  }

  @Test
  void shouldGetCorrectTransactions() throws Exception {
    User userToReturn = new User();
    userToReturn.setId(1L);
    userToReturn.setName("USER 1");

    PaymentTransaction paymentTransactionToReturn1 = new PaymentTransaction();
    paymentTransactionToReturn1.setPaymentStatus(PaymentStatus.PAYED);
    paymentTransactionToReturn1.setId(11L);

    PaymentTransaction paymentTransactionToReturn2 = new PaymentTransaction();
    paymentTransactionToReturn2.setPaymentStatus(PaymentStatus.PAYED);
    paymentTransactionToReturn2.setId(12L);

    userToReturn.setPaymentTransactions(
        List.of(paymentTransactionToReturn1, paymentTransactionToReturn2));

    when(userRepository.findById(1L)).thenReturn(Optional.of(userToReturn));

    String responseString =
        mockMvc
            .perform(get("/users/1/payment-transactions"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(UTF_8);

    Type listType = new TypeToken<ArrayList<PaymentTransaction>>() {}.getType();
    List<PaymentTransaction> response = new Gson().fromJson(responseString, listType);

    assertEquals(List.of(paymentTransactionToReturn1, paymentTransactionToReturn2), response);
    verify(userService, times(1)).getUser(1L);
    verify(userService, times(0)).getUsers();
  }

  @Test
  void failRequestWhenUserNotFound() throws Exception {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    String responseString =
        mockMvc
            .perform(get("/users/1").header(X_TRACE_ID_HEADER, X_TRACE_ID_HEADER))
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse()
            .getContentAsString(UTF_8);

    ErrorResponse response = new Gson().fromJson(responseString, ErrorResponse.class);

    ErrorResponse expectedErrorResponse =
        ErrorResponse.builder()
            .code(404)
            .message("User with id 1 not found.")
            .origin("DEMO_APP")
            .traceId("x-trace-id")
            .build();

    assertEquals(expectedErrorResponse, response);
    verify(userService, times(1)).getUser(1L);
    verify(userService, times(0)).getUsers();
  }

  @Test
  void failRequestWhenTransactionNotFound() throws Exception {
    User userToReturn = new User();
    userToReturn.setId(1L);
    userToReturn.setName("USER 1");

    PaymentTransaction paymentTransactionToReturn1 = new PaymentTransaction();
    paymentTransactionToReturn1.setPaymentStatus(PaymentStatus.PAYED);
    paymentTransactionToReturn1.setId(11L);

    PaymentTransaction paymentTransactionToReturn2 = new PaymentTransaction();
    paymentTransactionToReturn2.setPaymentStatus(PaymentStatus.PAYED);
    paymentTransactionToReturn2.setId(12L);

    userToReturn.setPaymentTransactions(
        List.of(paymentTransactionToReturn1, paymentTransactionToReturn2));

    when(userRepository.findById(1L)).thenReturn(Optional.of(userToReturn));

    String responseString =
        mockMvc
            .perform(
                get("/users/1/payment-transactions/13")
                    .header(X_TRACE_ID_HEADER, X_TRACE_ID_HEADER))
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse()
            .getContentAsString(UTF_8);

    ErrorResponse response = new Gson().fromJson(responseString, ErrorResponse.class);

    ErrorResponse expectedErrorResponse =
        ErrorResponse.builder()
            .code(404)
            .message("Transaction with id 13 not found for user 1")
            .origin("DEMO_APP")
            .traceId("x-trace-id")
            .build();

    assertEquals(expectedErrorResponse, response);
    verify(userService, times(1)).getUser(1L);
    verify(userService, times(0)).getUsers();
  }
}
