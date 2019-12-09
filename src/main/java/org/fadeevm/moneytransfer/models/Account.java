package org.fadeevm.moneytransfer.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.Money;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

@Slf4j
@EqualsAndHashCode
@ToString
public class Account {

    public Account() {
        id = UUID.randomUUID().toString();
        amount = Money.of(DEFAULT_AMOUNT, DEFAULT_CURRENCY_CODE);
    }

    public Account(String currencyCode) {
        id = UUID.randomUUID().toString();
        amount = Money.of(DEFAULT_AMOUNT, currencyCode);
    }

    public Account(Number initAmount, String currencyCode) {
        id = UUID.randomUUID().toString();
        amount = Money.of(initAmount, currencyCode);
    }

    @Getter
    private String id;

    private volatile MonetaryAmount amount;

    private static final AtomicReferenceFieldUpdater<Account, MonetaryAmount> casAmount =
            AtomicReferenceFieldUpdater.newUpdater(Account.class, MonetaryAmount.class, "amount");

    public MonetaryAmount getAmount() {
        return casAmount.get(this);
    }

    public boolean increaseAmount(MonetaryAmount amount){
        try {
            casAmount.getAndUpdate(this, monetaryAmount -> this.amount.add(amount));
        } catch (Exception e) {
            log.warn("can't subtract amount for account {}", getId());
            return false;
        }
        return true;
    }

    public boolean decreaseAmount(MonetaryAmount amount){
        MonetaryAmount newValue;
        try {
            newValue = casAmount.updateAndGet(this, monetaryAmount -> this.amount.subtract(amount));
        } catch (Exception e) {
            log.warn("can't subtract amount for account {}", getId());
            return false;
        }

        if (newValue.isNegative()) {
            casAmount.updateAndGet(this, monetaryAmount -> this.amount.add(amount));
            log.warn("concurrent error during decreasing reverted new value = {}", newValue);
            return false;
        }

        return true;
    }

    public CurrencyUnit getCurrency() {
        return amount.getCurrency();
    }


    public static final String DEFAULT_CURRENCY_CODE = "USD";
    public static final int DEFAULT_AMOUNT = 0;
    public static final Money DEFAULT_AMOUNT_USD = Money.of(0, DEFAULT_CURRENCY_CODE);
    public static final String UNKNOWN_ACCOUNT_ID = "UNKNOWN_ID";
    public static final Account UNKNOWN_ACCOUNT = new UnknownAccount();
    static
    {
        UNKNOWN_ACCOUNT.id = UNKNOWN_ACCOUNT_ID;
        UNKNOWN_ACCOUNT.amount = DEFAULT_AMOUNT_USD;
    }

    private static class UnknownAccount extends Account {
        @Override
        public boolean increaseAmount(MonetaryAmount amount) {
            throw new RuntimeException("increase action for Unknown account is prohibited");
        }

        @Override
        public boolean decreaseAmount(MonetaryAmount amount) {
            throw new RuntimeException("decrease action for Unknown account is prohibited");
        }
    }
}
