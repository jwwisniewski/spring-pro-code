package accounts.web;

import accounts.AccountManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import rewards.internal.account.Account;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// TODO-07: Replace @ExtendWith(SpringExtension.class) with the following annotation
// - @WebMvcTest(AccountController.class) // includes @ExtendWith(SpringExtension.class)
@WebMvcTest(AccountController.class)
public class AccountControllerBootTests {

    // TODO-08: Autowire MockMvc bean
    @Autowired
    MockMvc mockMvc;

    // TODO-09: Create AccountManager mock bean using @MockBean annotation
    @MockBean
    AccountManager accountManager;

    // TODO-10: Write positive test for GET request for an account
    // - Uncomment the code and run the test and verify it succeeds
    @Test
    public void accountDetails() throws Exception {

        given(accountManager.getAccount(0L))
                .willReturn(new Account("1234567890", "John Doe"));
//
        mockMvc.perform(get("/accounts/0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name").value("John Doe"))
                .andExpect(jsonPath("number").value("1234567890"));
//
        verify(accountManager).getAccount(0L);

    }

    // TODO-11: Write negative test for GET request for a non-existent account
    // - Uncomment the "given" and "verify" statements
    // - Write code between the "given" and "verify" statements
    // - Run the test and verify it succeeds
    @Test
    public void accountDetailsFail() throws Exception {

        given(accountManager.getAccount(any(Long.class)))
                .willThrow(new IllegalArgumentException("No such account with id " + 0L));

        // (Write code here)
        // - Use mockMvc to perform HTTP Get operation using "/accounts/9999"
        //   as a non-existent account URL
        // - Verify that the HTTP response status is 404
        mockMvc.perform(get("/accounts/9999"))
                .andExpect(status().isNotFound());

        verify(accountManager).getAccount(any(Long.class));
    }

    // TODO-12: Write test for `POST` request for an account
    // - Uncomment Java code below
    // - Write code between the "given" and "verify" statements
    // - Run the test and verify it succeeds
    @Test
    public void createAccount() throws Exception {

        Account testAccount = new Account("1234512345", "Mary Jones");
        testAccount.setEntityId(21L);

        given(accountManager.save(any(Account.class)))
                .willReturn(testAccount);

        // (Write code here)
        // Use mockMvc to perform HTTP Post operation to "/accounts"
        // - Set the request content type to APPLICATION_JSON
        // - Set the request content with Json string of the "testAccount"
        //   (Use "asJsonString" method below to convert the "testAccount"
        //   object into Json string)
        // - Verify that the response status is 201
        // - Verify that the response "Location" header contains "http://localhost/accounts/21"
        mockMvc
            .perform(
                    post("/accounts").contentType(MediaType.APPLICATION_JSON).content(asJsonString(testAccount))
            )
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "http://localhost/accounts/21"))
        ;

        verify(accountManager).save(any(Account.class));
    }

    // Utility class for converting an object into JSON string
    protected static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final String jsonContent = mapper.writeValueAsString(obj);
            return jsonContent;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // TODO-13 (Optional): Experiment with @MockBean vs @Mock
    // - Change `@MockBean` to `@Mock` for the `AccountManager dependency above
    // - Run the test and observe a test failure
    // - Change it back to `@MockBean`

    @Test
    public void teatGetAllAccounts() throws Exception {
        List<Account> allAccounts = new ArrayList<>();
        allAccounts.add(new Account("1", "One"));
        allAccounts.add(new Account("2", "Two"));
        given(accountManager.getAllAccounts())
                .willReturn(
                        allAccounts
                );

        mockMvc
                .perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(asJsonString(allAccounts)))
        ;

        verify(accountManager).getAllAccounts();
    }

    @Test
    public void testGetBeneficiaryNotFound() throws Exception {

        Account account = new Account("1234567890", "John Doe");
        account.setEntityId(1L);

        given(accountManager.getAccount(1L))
                .willReturn(account);

        mockMvc
                .perform(get("/accounts/1/beneficiaries/Fred"))
                .andExpect(status().isNotFound())
        ;
        verify(accountManager).getAccount(1L);
    }

    @Test
    public void testDeleteBeneficiaryNotFound() throws Exception {

        Account account = new Account("1234567890", "John Doe");
        account.setEntityId(1L);

        given(accountManager.getAccount(1L))
                .willReturn(account);

        mockMvc
                .perform(delete("/accounts/1/beneficiaries/Fred"))
                .andExpect(status().isNotFound())
        ;
        verify(accountManager).getAccount(1L);
    }

    @Test
    public void testGetBeneficiaryFound() throws Exception {

        Account account = new Account("1234567890", "John Doe");
        account.setEntityId(1L);
        account.addBeneficiary("Fred");

        given(accountManager.getAccount(1L))
                .willReturn(account);

        mockMvc
                .perform(get("/accounts/1/beneficiaries/Fred"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name").value("Fred"))
        ;
        verify(accountManager).getAccount(1L);
    }

    @Test
    public void testDeleteBeneficiaryFound() throws Exception {

        Account account = new Account("1234567890", "John Doe");
        account.setEntityId(1L);
        account.addBeneficiary("Fred");

        given(accountManager.getAccount(1L))
                .willReturn(account);

        mockMvc
                .perform(delete("/accounts/1/beneficiaries/Fred"))
                .andExpect(status().isNoContent())
        ;
        verify(accountManager).getAccount(1L);
    }

    @Test
    public void testAddBeneficiary() throws Exception {
        Account account = new Account("1234567890", "John Doe");
        account.setEntityId(1L);

        given(accountManager.getAccount(1L))
                .willReturn(account);

        mockMvc
                .perform(post("/accounts/1/beneficiaries/").contentType(MediaType.APPLICATION_JSON).content("Fred"))
                .andExpect(status().isCreated())
        ;
        verify(accountManager).addBeneficiary(1L, "Fred");
    }
}
