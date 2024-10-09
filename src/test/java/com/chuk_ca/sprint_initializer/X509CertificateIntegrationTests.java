package com.chuk_ca.sprint_initializer;

import com.chuk_ca.sprint_initializer.model.*;
import com.chuk_ca.sprint_initializer.repository.X509CertificateEntryRepository;
import com.chuk_ca.sprint_initializer.service.util.CSRUtils;
import com.chuk_ca.sprint_initializer.service.util.ConfigFileLoader;
import com.chuk_ca.sprint_initializer.service.x509.X509CertificateUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import static com.chuk_ca.sprint_initializer.model.CAConfigPaths.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
class X509CertificateIntegrationTests {

    private static X509Certificate testCertificate;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private X509CertificateEntryRepository certificateEntryRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ConfigFileLoader configFileLoader;
    @MockBean
    private JavaMailSender mailSender;
    private X509CertificateEntry testCertificateEntry;
    private X509CertificateViewRequest testCertificateRequest;

    @BeforeEach
    void setup() throws IOException, CertificateException {
        // Load CA properties
        Properties caProperties = configFileLoader.loadPropertiesFromFile("ca.properties");

        // Generate a test certificate
        String caCertPath = caProperties.getProperty(CA_CERT_PATH);
        testCertificate = X509CertificateUtils.readCertificateFromFile(caCertPath);
        String pemContent = X509CertificateUtils.convertCertificateToPEM(testCertificate);

        // Set up the test certificate entry with a public key
        testCertificateEntry = new X509CertificateEntry();
        testCertificateEntry.setCertificate(pemContent);
        testCertificateEntry.setIssuanceDate("2024-09-26");
        testCertificateEntry.setIssuancePerson("Test Issuer");

        // Save to repository
        certificateEntryRepository.save(testCertificateEntry);

        // Prepare test certificate request object
        testCertificateRequest = new X509CertificateViewRequest();
        testCertificateRequest.setPemContent(pemContent);
    }

    @Test
    void testGetAllCertificateEntries() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/certificates")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].issuancePerson", is(testCertificateEntry.getIssuancePerson())));
    }

    @Test
    void testGetCertificateEntryById() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/certificates/{id}", testCertificateEntry.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.issuancePerson", is(testCertificateEntry.getIssuancePerson())))
                .andExpect(jsonPath("$.issuanceDate", is(testCertificateEntry.getIssuanceDate())));
    }

    @Test
    void testDeleteCertificateEntry() throws Exception {
        mvc.perform(MockMvcRequestBuilders.delete("/certificates/{id}", testCertificateEntry.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void testSaveCertificateEntry() throws Exception {
        X509CertificateEntry newEntry = new X509CertificateEntry();
        newEntry.setCertificate(testCertificateEntry.getCertificate());
        newEntry.setIssuanceDate("2024-09-26");
        newEntry.setIssuancePerson("Another Issuer");

        mvc.perform(MockMvcRequestBuilders.post("/certificates")
                        .content(objectMapper.writeValueAsString(newEntry))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.issuancePerson", is(newEntry.getIssuancePerson())))
                .andExpect(jsonPath("$.issuanceDate", is(newEntry.getIssuanceDate())));
    }

    @Test
    void testCreateCertificateAsPEM() throws Exception {
        Date notBefore = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        Date notAfter = calendar.getTime();

        X509CertificateCreateRequest createRequest = new X509CertificateCreateRequest();
        createRequest.setCsr(CSRUtils.readCSRFromFile("./csr_example.pem"));
        createRequest.setNotBefore(notBefore);
        createRequest.setNotAfter(notAfter);

        MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/certificates/create")
                        .content(objectMapper.writeValueAsString(createRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();

        assertNotNull(responseContent);
        assertTrue(responseContent.contains("-----BEGIN CERTIFICATE-----"));
    }

    @Test
    void testValidateCertificate() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/certificates/validate")
                        .content(objectMapper.writeValueAsString(testCertificateRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("The certificate is valid."));
    }

    @Test
    void testViewCertificateAsJSON() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/certificates/view-json")
                        .content(objectMapper.writeValueAsString(testCertificateRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject", is(testCertificate.getSubjectX500Principal().getName())))
                .andExpect(jsonPath("$.issuer", is(testCertificate.getIssuerX500Principal().getName())));
    }

    @Test
    void testGenerateKeys() throws Exception {
        MimeMessage mimeMessage = Mockito.mock(MimeMessage.class);
        Mockito.when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        Mockito.doNothing().when(mailSender).send(Mockito.any(MimeMessage.class));

        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setAddress(CA_EMAIL_ADDRESS);

        mvc.perform(MockMvcRequestBuilders.post("/certificates/generate-keys")
                        .content(objectMapper.writeValueAsString(emailRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pemContent").exists())
                .andReturn();

        // Optionally verify if the email was "sent"
        Mockito.verify(mailSender, Mockito.times(1)).send(Mockito.any(MimeMessage.class));
    }

    @Test
    void testGetCAPublicKey() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/certificates/ca-public-key")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();

        assertNotNull(responseContent);
        assertTrue(responseContent.contains("-----BEGIN PUBLIC KEY-----"));
    }

}
