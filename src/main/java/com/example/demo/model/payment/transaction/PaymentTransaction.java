package com.example.demo.model.payment.transaction;

import com.example.demo.model.User;
import com.example.demo.model.payment.status.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.Data;

@Data
@Entity
@Table(name = PaymentTransaction.TABLE_NAME)
public class PaymentTransaction {
  public static final String TABLE_NAME = "PAYMENT_TRANSACTION";

  @Id
  @Positive
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull private PaymentStatus paymentStatus;

  @NotNull
  @JsonBackReference
  @ManyToOne(fetch = FetchType.LAZY)
  private User user;
}
