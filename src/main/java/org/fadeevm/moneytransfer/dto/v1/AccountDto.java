package org.fadeevm.moneytransfer.dto.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.fadeevm.moneytransfer.models.Account;

@Data
@AllArgsConstructor
public class AccountDto {
    public AccountDto(Account account) {
        this.id = account.getId();
        this.currency = account.getCurrency().getCurrencyCode();
        this.amount = account.getAmount().getNumber().floatValue();
    }

    private String id;
    private String currency;
    private Float amount;
}
