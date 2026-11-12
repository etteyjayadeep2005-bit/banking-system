package com.bank.bankapp;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Tests always use their own in-memory H2 database, never the real Neon database.
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:integrationtest;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "app.jwt.secret=integration-test-secret-key-at-least-32-characters-long",
        "app.admin.username=admin",
        "app.admin.password=Admin-test-pass-123"
})
@AutoConfigureMockMvc
class AccountApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ---------- helpers ----------

    private String newUsername() {
        return "user" + UUID.randomUUID().toString().substring(0, 8);
    }

    private ResultActions postJson(String token, String path, String json) throws Exception {
        var request = post(path).contentType(MediaType.APPLICATION_JSON).content(json);
        if (token != null) {
            request = request.header("Authorization", "Bearer " + token);
        }
        return mockMvc.perform(request);
    }

    private ResultActions getAs(String token, String path) throws Exception {
        var request = get(path);
        if (token != null) {
            request = request.header("Authorization", "Bearer " + token);
        }
        return mockMvc.perform(request);
    }

    private String credentials(String username, String password) {
        return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = postJson(null, "/api/auth/login", credentials(username, password))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    /** Registers a new customer and returns a login token. */
    private String registerAndLogin(String username) throws Exception {
        postJson(null, "/api/auth/register", credentials(username, "secret123"))
                .andExpect(status().isCreated());
        return login(username, "secret123");
    }

    private String createAccount(String token, String holder, int deposit) throws Exception {
        MvcResult result = postJson(token, "/api/accounts",
                "{\"holderName\":\"" + holder + "\",\"initialDeposit\":" + deposit + "}")
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.accountNumber");
    }

    private double balance(String token, String accountNumber) throws Exception {
        MvcResult result = getAs(token, "/api/accounts/" + accountNumber)
                .andExpect(status().isOk())
                .andReturn();
        Number value = JsonPath.read(result.getResponse().getContentAsString(), "$.balance");
        return value.doubleValue();
    }

    // ---------- authentication ----------

    @Test
    void accountsRequireLogin() throws Exception {
        getAs(null, "/api/accounts").andExpect(status().isUnauthorized());
    }

    @Test
    void loginReturnsTokenAndRole() throws Exception {
        String username = newUsername();
        postJson(null, "/api/auth/register", credentials(username, "secret123"))
                .andExpect(status().isCreated());

        postJson(null, "/api/auth/login", credentials(username, "secret123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        String username = newUsername();
        postJson(null, "/api/auth/register", credentials(username, "secret123"))
                .andExpect(status().isCreated());

        postJson(null, "/api/auth/login", credentials(username, "wrong-password"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }

    @Test
    void duplicateUsernameIsRejected() throws Exception {
        String username = newUsername();
        postJson(null, "/api/auth/register", credentials(username, "secret123"))
                .andExpect(status().isCreated());

        postJson(null, "/api/auth/register", credentials(username, "secret123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username already taken"));
    }

    // ---------- accounts, deposits, withdrawals ----------

    @Test
    void customerCanDepositAndWithdraw() throws Exception {
        String token = registerAndLogin(newUsername());
        String number = createAccount(token, "Ravi", 1000);

        postJson(token, "/api/accounts/" + number + "/deposit", "{\"amount\":500}")
                .andExpect(status().isOk());
        postJson(token, "/api/accounts/" + number + "/withdraw", "{\"amount\":200}")
                .andExpect(status().isOk());

        assertThat(balance(token, number)).isEqualTo(1300.0);

        // opening deposit + deposit + withdrawal
        getAs(token, "/api/accounts/" + number + "/transactions")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void withdrawMoreThanBalanceIsRejected() throws Exception {
        String token = registerAndLogin(newUsername());
        String number = createAccount(token, "Ravi", 100);

        postJson(token, "/api/accounts/" + number + "/withdraw", "{\"amount\":500}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient balance"));

        assertThat(balance(token, number)).isEqualTo(100.0);
    }

    @Test
    void invalidInputIsRejected() throws Exception {
        String token = registerAndLogin(newUsername());

        postJson(token, "/api/accounts", "{\"holderName\":\"Ravi\",\"initialDeposit\":-5}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("initialDeposit")));
    }

    @Test
    void unknownAccountReturnsNotFound() throws Exception {
        String token = registerAndLogin(newUsername());

        getAs(token, "/api/accounts/SB0000000000")
                .andExpect(status().isNotFound());
    }

    // ---------- access control ----------

    @Test
    void customerCannotSeeAnotherCustomersAccount() throws Exception {
        String ownerToken = registerAndLogin(newUsername());
        String otherToken = registerAndLogin(newUsername());
        String number = createAccount(ownerToken, "Owner", 1000);

        getAs(otherToken, "/api/accounts/" + number)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("You do not have access to this account"));

        postJson(otherToken, "/api/accounts/" + number + "/deposit", "{\"amount\":50}")
                .andExpect(status().isForbidden());

        assertThat(balance(ownerToken, number)).isEqualTo(1000.0);
    }

    @Test
    void transferMovesMoneyBetweenCustomers() throws Exception {
        String senderToken = registerAndLogin(newUsername());
        String receiverToken = registerAndLogin(newUsername());
        String from = createAccount(senderToken, "Sender", 1000);
        String to = createAccount(receiverToken, "Receiver", 200);

        postJson(senderToken, "/api/transfers",
                "{\"fromAccount\":\"" + from + "\",\"toAccount\":\"" + to + "\",\"amount\":300}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transfer successful"));

        assertThat(balance(senderToken, from)).isEqualTo(700.0);
        assertThat(balance(receiverToken, to)).isEqualTo(500.0);
    }

    @Test
    void cannotTransferFromSomeoneElsesAccount() throws Exception {
        String ownerToken = registerAndLogin(newUsername());
        String attackerToken = registerAndLogin(newUsername());
        String victim = createAccount(ownerToken, "Victim", 1000);
        String mine = createAccount(attackerToken, "Attacker", 10);

        postJson(attackerToken, "/api/transfers",
                "{\"fromAccount\":\"" + victim + "\",\"toAccount\":\"" + mine + "\",\"amount\":500}")
                .andExpect(status().isForbidden());

        assertThat(balance(ownerToken, victim)).isEqualTo(1000.0);
    }

    @Test
    void customerCannotUseAdminEndpoint() throws Exception {
        String token = registerAndLogin(newUsername());

        getAs(token, "/api/admin/accounts").andExpect(status().isForbidden());
    }

    @Test
    void adminCanListAndOpenAnyAccount() throws Exception {
        String customerToken = registerAndLogin(newUsername());
        String number = createAccount(customerToken, "Customer", 750);

        MvcResult result = postJson(null, "/api/auth/login", credentials("admin", "Admin-test-pass-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andReturn();
        String adminToken = JsonPath.read(result.getResponse().getContentAsString(), "$.token");

        getAs(adminToken, "/api/admin/accounts").andExpect(status().isOk());
        assertThat(balance(adminToken, number)).isEqualTo(750.0);
    }
}