package com.example.demo.model.payment.status;

import java.util.Optional;
import java.util.stream.Stream;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import javax.validation.constraints.NotNull;
import org.springframework.lang.Nullable;

@SuppressWarnings("unused")
@Converter(autoApply = true)
public class PaymentStatusConverter implements AttributeConverter<PaymentStatus, String> {

  @Override
  public @NotNull String convertToDatabaseColumn(@Nullable PaymentStatus category) {
    return Optional.ofNullable(category)
        .map(PaymentStatus::getCode)
        .orElseThrow(IllegalArgumentException::new);
  }

  @Override
  public @NotNull PaymentStatus convertToEntityAttribute(@Nullable String code) {
    return Optional.ofNullable(code)
        .map(
            cd ->
                Stream.of(PaymentStatus.values())
                    .filter(c -> c.getCode().equals(cd))
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new))
        .orElseThrow(IllegalArgumentException::new);
  }
}
