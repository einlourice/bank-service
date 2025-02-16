package com.rabobank.bankservice.context;

import com.rabobank.bankservice.entity.Account;
import com.rabobank.bankservice.entity.User;
import com.rabobank.bankservice.error.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserContext {

    private final HttpServletRequest request;

    @Autowired
    UserContext(HttpServletRequest request) {
        this.request = request;
    }

    public User getCurrentUser() {
        User user = (User) request.getAttribute("currentUser");
        if (user == null) {
            throw new UnauthorizedException("User not found in context");
        }
        return user;
    }

    public void isAuthorized(Account account) {
        User user = this.getCurrentUser();
        if (!user.getId().equals(account.getUser().getId())) {
            throw new UnauthorizedException("User id and account ID mismatch");
        }
    }
}
