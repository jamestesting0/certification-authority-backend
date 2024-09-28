package com.chuk_ca.sprint_initializer;

import com.chuk_ca.sprint_initializer.config.BouncyCastleConfig;
import com.chuk_ca.sprint_initializer.config.HazelcastConfig;
import com.chuk_ca.sprint_initializer.model.EmailRequest;
import com.chuk_ca.sprint_initializer.model.X509CertificateCreateRequest;
import com.chuk_ca.sprint_initializer.model.X509CertificateEntry;
import com.chuk_ca.sprint_initializer.model.X509CertificateRequest;
import com.chuk_ca.sprint_initializer.repository.X509CertificateEntryRepository;
import com.chuk_ca.sprint_initializer.service.X509CertificateService;
import com.chuk_ca.sprint_initializer.service.X509CertificateServiceImpl;
import com.chuk_ca.sprint_initializer.service.util.ConfigFileLoader;
import com.chuk_ca.sprint_initializer.service.util.EmailSender;
import com.chuk_ca.sprint_initializer.service.util.EmailSenderImpl;
import com.chuk_ca.sprint_initializer.service.x509.X509CertificateUtils;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;

import static com.chuk_ca.sprint_initializer.model.CAConfigPaths.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@ContextConfiguration(classes = {HazelcastConfig.class, BouncyCastleConfig.class})
@ComponentScan(basePackages = "com.chuk_ca.sprint_initializer")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
class X509CertificateServiceTest {
    @Autowired
    private X509CertificateService certificateService;

    @Autowired
    private X509CertificateEntryRepository certificateEntryRepository;

    @Autowired
    private ConfigFileLoader configFileLoader;

    private static X509Certificate testCertificate;
    private static X509CertificateEntry testCertificateEntry;
    private static Properties caProperties;

    @BeforeEach
    void setup() throws IOException, CertificateException {
        // Set the CA's properties
        caProperties = configFileLoader.loadPropertiesFromFile("x509.properties");
        String caCertPath = caProperties.getProperty(CA_CERT_PATH);

        // Set the test certificate
        testCertificate = X509CertificateUtils.readCertificateFromFile(caCertPath);

        // Configure the data that will be entered
        String pemContent = X509CertificateUtils.convertCertificateToPEM(testCertificate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String issuanceDate = dateFormat.format(new Date());
        String issuancePerson = "James Chen";

        // Instantiate the X509CertificateEntry entity
        X509CertificateEntry certificateEntry = new X509CertificateEntry();
        certificateEntry.setCertificate(pemContent);
        certificateEntry.setIssuanceDate(issuanceDate);
        certificateEntry.setIssuancePerson(issuancePerson);
        testCertificateEntry = certificateEntry;
    }

    @Test
    void testSaveCertificateEntry() {
        certificateService.saveCertificateEntry(testCertificateEntry);
        Long certificateId = testCertificateEntry.getId(); // the ID is auto-generated after save

        // Check if the entity is saved correctly to the database
        Optional<X509CertificateEntry> foundCertificate = certificateEntryRepository.findById(certificateId);
        assertTrue(foundCertificate.isPresent());
        assertEquals(foundCertificate.get().getCertificate(), testCertificateEntry.getCertificate());
        assertEquals(foundCertificate.get().getIssuanceDate(), testCertificateEntry.getIssuanceDate());
        assertEquals(foundCertificate.get().getIssuancePerson(), testCertificateEntry.getIssuancePerson());
    }

    @Test
    void testDeleteCertificateEntry() {
        certificateService.saveCertificateEntry(testCertificateEntry);
        Long certificateId = testCertificateEntry.getId(); // the ID is auto-generated after save
        certificateService.deleteCertificateEntryById(certificateId);

        // Check if the entity is deleted from the database
        Optional<X509CertificateEntry> foundCertificate = certificateEntryRepository.findById(certificateId);
        assertTrue(foundCertificate.isEmpty());
    }

    @Test
    void testCreateCertificate() throws IOException, CertificateException, InvalidKeySpecException, OperatorCreationException {
        X509CertificateCreateRequest certificateCreateRequest = new X509CertificateCreateRequest();
        certificateCreateRequest.setPublicKey(X509CertificateUtils.convertPublicKeyToPEM(testCertificate.getPublicKey()));
        certificateCreateRequest.setCommonName(caProperties.getProperty(CA_COMMON_NAME));
        certificateCreateRequest.setOrganization(caProperties.getProperty(CA_ORGANIZATION));
        certificateCreateRequest.setOrganizationUnit(caProperties.getProperty(CA_ORGANIZATIONAL_UNIT));
        certificateCreateRequest.setLocality(caProperties.getProperty(CA_LOCALITY));
        certificateCreateRequest.setState(caProperties.getProperty(CA_STATE));
        certificateCreateRequest.setCountry(caProperties.getProperty(CA_COUNTRY));
        certificateCreateRequest.setNotBefore(testCertificate.getNotBefore());
        certificateCreateRequest.setNotAfter(testCertificate.getNotAfter());

        String pemContent = certificateService.createCertificateAsPEM(certificateCreateRequest);
        X509Certificate certificate = X509CertificateUtils.readCertificateFromPEM(pemContent);

        // Check if the certificate is created correctly
        assertEquals(testCertificate.getVersion(), testCertificate.getVersion());
        assertEquals(testCertificate.getIssuerX500Principal().getName(), certificate.getIssuerX500Principal().getName());
        assertEquals(testCertificate.getSubjectX500Principal().getName(), certificate.getSubjectX500Principal().getName());
        assertDoesNotThrow(() -> certificate.verify(testCertificate.getPublicKey(), BouncyCastleProvider.PROVIDER_NAME));
        assertEquals(testCertificate.getNotBefore().getTime() / 1000, certificate.getNotBefore().getTime() / 1000);
        assertEquals(testCertificate.getNotAfter().getTime() / 1000, certificate.getNotAfter().getTime() / 1000);
        assertEquals(testCertificate.getPublicKey(), certificate.getPublicKey());
    }

    @Test
    void testValidateCertificate() throws Exception {
        X509CertificateRequest certificateRequest = new X509CertificateRequest();

        // Scenario 1: Test with a valid certificate
        certificateRequest.setPemContent(X509CertificateUtils.convertCertificateToPEM(testCertificate));
        assertDoesNotThrow(() -> certificateService.validateCertificate(certificateRequest));

        // Scenario 2: Test with a random string
        String randomString = "random_invalid_certificate_string";
        certificateRequest.setPemContent(randomString);
        assertThrows(CertificateException.class, () -> {certificateService.validateCertificate(certificateRequest);});

        // Scenario 3: Test with a tampered certificate
        certificateRequest.setPemContent(tamperCertificate(X509CertificateUtils.convertCertificateToPEM(testCertificate)));
        assertThrows(SignatureException.class, () -> {certificateService.validateCertificate(certificateRequest);});

        // Creating a test X509CertificateCreateRequest
        X509CertificateCreateRequest certificateCreateRequest = new X509CertificateCreateRequest();
        certificateCreateRequest.setPublicKey(X509CertificateUtils.convertPublicKeyToPEM(testCertificate.getPublicKey()));
        certificateCreateRequest.setCommonName(caProperties.getProperty(CA_COMMON_NAME));
        certificateCreateRequest.setOrganization(caProperties.getProperty(CA_ORGANIZATION));
        certificateCreateRequest.setOrganizationUnit(caProperties.getProperty(CA_ORGANIZATIONAL_UNIT));
        certificateCreateRequest.setLocality(caProperties.getProperty(CA_LOCALITY));
        certificateCreateRequest.setState(caProperties.getProperty(CA_STATE));
        certificateCreateRequest.setCountry(caProperties.getProperty(CA_COUNTRY));

        // Scenario 4: Test with an expired certificate
        certificateCreateRequest.setNotBefore(testCertificate.getNotBefore());
        certificateCreateRequest.setNotAfter(testCertificate.getNotBefore());
        String expiredCertificatePEM = certificateService.createCertificateAsPEM(certificateCreateRequest);
        certificateRequest.setPemContent(expiredCertificatePEM);
        assertThrows(CertificateExpiredException.class, () -> {certificateService.validateCertificate(certificateRequest);});

        // Scenario 5: Test with a not yet valid certificate
        certificateCreateRequest.setNotBefore(testCertificate.getNotAfter());
        certificateCreateRequest.setNotAfter(testCertificate.getNotAfter());
        String notYetValidCertificatePEM = certificateService.createCertificateAsPEM(certificateCreateRequest);
        certificateRequest.setPemContent(notYetValidCertificatePEM);
        assertThrows(CertificateNotYetValidException.class, () -> certificateService.validateCertificate(certificateRequest));
    }

    private String tamperCertificate(String pemContent) {
        int startIndex = pemContent.indexOf("-----BEGIN CERTIFICATE-----") + "-----BEGIN CERTIFICATE-----".length();
        int endIndex = pemContent.indexOf("-----END CERTIFICATE-----");

        String base64Content = pemContent.substring(startIndex, endIndex).trim();

        int middleIndex = base64Content.length() / 2;
        String tamperedBase64Content = base64Content.substring(0, middleIndex) +
                "ZZZZZ" +
                base64Content.substring(middleIndex + 5);

        return pemContent.substring(0, startIndex) + "\n" + tamperedBase64Content + "\n" + pemContent.substring(endIndex);
    }

    @Test
    void testSendPrivateKey() throws GeneralSecurityException, IOException, MessagingException {
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setAddress(caProperties.getProperty(CA_EMAIL_ADDRESS));
        PrivateKey privateKey = X509CertificateUtils.readPrivateKeyFromFile(caProperties.getProperty(CA_PRIVATE_KEY_PATH));

        // Set up mock objects
        GreenMail greenMail = new GreenMail(ServerSetupTest.SMTP);
        greenMail.start();
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost("localhost");
        javaMailSender.setPort(3025);
        EmailSender emailSenderMock = new EmailSenderImpl(javaMailSender);
        X509CertificateService certificateServiceMock = new X509CertificateServiceImpl(null, null, emailSenderMock);

        assertDoesNotThrow(() -> certificateServiceMock.sendPrivateKey(emailRequest, privateKey));

        // Check if the Email is sent
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(1, receivedMessages.length);
        assertEquals(caProperties.getProperty(CA_EMAIL_ADDRESS), receivedMessages[0].getAllRecipients()[0].toString());
    }
}
