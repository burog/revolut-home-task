package org.fadeevm.moneytransfer.models;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.Money;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import java.util.UUID;

@Getter
@Slf4j
@EqualsAndHashCode
public class Account {
    public Account() {
        this.id = UUID.randomUUID().toString();
    }

    private static final Money DEFAULT_AMOUNT_USD = Money.of(0, "USD");

    private String id;

    @Setter
    private MonetaryAmount amount;

    public CurrencyUnit getCurrency() {
        return amount.getCurrency();
    }


    public static final String UNKNOWN_ACCOUNT_ID = "UNKNOWN_ID";
    public static final Account UNKNOWN_ACCOUNT = new Account()
    {
        @Override
        public String getId() {
            return UNKNOWN_ACCOUNT_ID;
        }

        @Override
        public MonetaryAmount getAmount() {
            return DEFAULT_AMOUNT_USD;
        }

        @Override
        public void setAmount(MonetaryAmount amount) {
        }

        @Override
        public CurrencyUnit getCurrency() {
            return DEFAULT_AMOUNT_USD.getCurrency();
        }
    };
}
