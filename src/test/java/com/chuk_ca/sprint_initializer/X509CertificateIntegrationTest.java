package com.chuk_ca.sprint_initializer;

import com.chuk_ca.sprint_initializer.model.X509CertificateCreateRequest;
import com.chuk_ca.sprint_initializer.model.X509CertificateEntry;
import com.chuk_ca.sprint_initializer.model.X509CertificateRequest;
import com.chuk_ca.sprint_initializer.repository.X509CertificateEntryRepository;
import com.chuk_ca.sprint_initializer.service.util.ConfigFileLoader;
import com.chuk_ca.sprint_initializer.service.x509.X509CertificateUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
class X509CertificateIntegrationTest {

    private static final String CA_COMMON_NAME = "ca.common_name",
            CA_ORGANIZATION = "ca.organization",
            CA_ORGANIZATIONAL_UNIT = "ca.organizational_unit",
            CA_LOCALITY = "ca.locality",
            CA_STATE = "ca.state",
            CA_COUNTRY = "ca.country",
            CA_CERT_PATH = "ca.cert.path";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private X509CertificateEntryRepository certificateEntryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConfigFileLoader configFileLoader;

    private static X509Certificate testCertificate;
    private X509CertificateEntry testCertificateEntry;
    private X509CertificateRequest testCertificateRequest;
    private static Properties caProperties;

    @BeforeEach
    void setup() throws IOException, CertificateException {
        // Load CA properties
        caProperties = configFileLoader.loadPropertiesFromFile("x509.properties");

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
        testCertificateRequest = new X509CertificateRequest();
        testCertificateRequest.setPemContent(pemContent);
    }

    @Test
    void testGetAllCertificateEntries() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/certificates")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
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
        createRequest.setCommonName(caProperties.getProperty(CA_COMMON_NAME));
        createRequest.setOrganization(caProperties.getProperty(CA_ORGANIZATION));
        createRequest.setOrganizationUnit(caProperties.getProperty(CA_ORGANIZATIONAL_UNIT));
        createRequest.setLocality(caProperties.getProperty(CA_LOCALITY));
        createRequest.setState(caProperties.getProperty(CA_STATE));
        createRequest.setCountry(caProperties.getProperty(CA_COUNTRY));
        createRequest.setPublicKey(X509CertificateUtils.convertPublicKeyToPEM(testCertificate.getPublicKey()));
        createRequest.setNotBefore(notBefore);
        createRequest.setNotAfter(notAfter);

        MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/certificates/create")
                        .content(objectMapper.writeValueAsString(createRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // Get the response content as a string
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
}
