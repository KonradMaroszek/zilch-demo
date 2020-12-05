package com.example.demo.model;

import com.example.demo.model.payment.transaction.PaymentTransaction;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.Data;

@Data
@Entity
@Table(name = User.TABLE_NAME)
public class User {
  public static final String TABLE_NAME = "USER";

  @Id
  @Positive
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank private String name;

  @NotNull
  @JsonIgnore
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<@NotNull PaymentTransaction> paymentTransactions = new ArrayList<>();

  public void addPaymentTransaction(@NotNull PaymentTransaction paymentTransaction) {
    paymentTransactions.add(paymentTransaction);
    paymentTransaction.setUser(this);
  }
}
