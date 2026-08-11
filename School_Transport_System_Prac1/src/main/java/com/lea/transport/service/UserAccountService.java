package com.lea.transport.service;

import com.lea.transport.auth.UserAccount;
import com.lea.transport.exception.AuthenticationException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Stores user accounts and authenticates login attempts - only ever answers "who are you". */
public class UserAccountService {
    private final Map<String, UserAccount> accounts = new LinkedHashMap<>();

    public void addAccount(UserAccount account) { accounts.put(account.getUsername(), account); }

    public UserAccount authenticate(String username, String password) throws AuthenticationException {
        UserAccount account = accounts.get(username);
        if (account == null || !account.checkPassword(password)) {
            throw new AuthenticationException();
        }
        return account;
    }

    public UserAccount getAccount(String username) { return accounts.get(username); }
    public Iterable<UserAccount> allAccounts() { return accounts.values(); }
}
