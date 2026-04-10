package com.ra.base_spring_boot.dto.Registration;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SepayWebhookDTO {
    private String id;

    @JsonAlias({"amount"})
    private BigDecimal transferAmount;

    @JsonAlias({"content", "description"})
    private String transferContent;

    private String transactionDate;
}
