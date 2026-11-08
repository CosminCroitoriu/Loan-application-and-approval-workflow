package com.loanworkflow.loan;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Shared helpers for the loan API tests. Each subclass sets up its own Spring test. */
abstract class LoanApiTestBase {

    static final String GOOD_PROFILE =
            "{\"monthlyIncome\":5000,\"monthlyDebt\":500,\"yearsEmployed\":5,\"missedPayments\":0}";
    static final String BAD_PROFILE =
            "{\"monthlyIncome\":5000,\"monthlyDebt\":3000,\"yearsEmployed\":1,\"missedPayments\":2}";

    @Autowired
    protected MockMvc mvc;

    protected ResultActions postJson(String path, String json) throws Exception {
        return mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    protected ResultActions postEmpty(String path) throws Exception {
        return mvc.perform(post(path));
    }

    /** Creates a loan for "carol" and returns its path, for example /loans/1234. */
    protected String createLoan(String amount) throws Exception {
        return postJson("/loans", "{\"applicant\":\"carol\",\"amount\":" + amount + "}")
                .andReturn().getResponse().getHeader("Location");
    }
}
