package org.apereo.cas.services;

import org.apereo.cas.config.JpaServiceRegistryConfiguration;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.services.OAuthCallbackAuthorizeService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;


/**
 * Handles tests for {@link JpaServiceRegistryDaoImpl}
 *
 * @author battags
 * @since 3.1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {RefreshAutoConfiguration.class, JpaServiceRegistryConfiguration.class},
        initializers = ConfigFileApplicationContextInitializer.class)
public class JpaServiceRegistryDaoImplTests {

    @Autowired
    @Qualifier("jpaServiceRegistryDao")
    private ServiceRegistryDao dao;

    @Before
    public void setUp() {
        final List<RegisteredService> services = this.dao.load();
        for (final RegisteredService service : services) {
            this.dao.delete(service);
        }
    }

    @Test
    public void verifySaveMethodWithNonExistentServiceAndNoAttributes() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setName("test");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setPublicKey(new RegisteredServicePublicKeyImpl("classpath:/test.pub", "RSA"));

        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());

        assertEquals(r, r2);
        assertEquals(r2, r3);
    }

    @Test
    public void verifySaveAttributeReleasePolicy() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setName("test");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());

        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());

        assertEquals(r, r2);
        assertEquals(r2, r3);
        assertNotNull(r3.getAttributeReleasePolicy());
        assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
    }

    @Test
    public void verifySaveMethodWithExistingServiceNoAttribute() {

        final RegexRegisteredService r = new RegexRegisteredService();
        r.setName("test");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");

        this.dao.save(r);

        final List<RegisteredService> services = this.dao.load();

        final RegisteredService r2 = services.get(0);

        r.setId(r2.getId());
        this.dao.save(r);

        final RegisteredService r3 = this.dao.findServiceById(r.getId());

        assertEquals(r, r2);
        assertEquals(r.getTheme(), r3.getTheme());
    }

    @Test
    public void verifyRegisteredServiceProperties() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setName("test");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");

        final Map<String, RegisteredServiceProperty> propertyMap = new HashMap<>();

        final DefaultRegisteredServiceProperty property = new DefaultRegisteredServiceProperty();
        final Set<String> values = new HashSet<>();
        values.add("value1");
        values.add("value2");
        property.setValues(values);
        propertyMap.put("field1", property);

        final DefaultRegisteredServiceProperty property2 = new DefaultRegisteredServiceProperty();

        final Set<String> values2 = new HashSet<>();
        values2.add("value1");
        values2.add("value2");
        property2.setValues(values2);
        propertyMap.put("field2", property2);

        r.setProperties(propertyMap);

        this.dao.save(r);

        final RegisteredService r2 = this.dao.load().get(0);
        assertEquals(r2.getProperties().size(), 2);
    }

    @Test
    public void verifyOAuthServices() {
        final OAuthRegisteredService r = new OAuthRegisteredService();
        r.setName("test456");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        r.setClientId("testoauthservice");
        r.setClientSecret("anothertest");
        r.setBypassApprovalPrompt(true);
        final RegisteredService r2 = this.dao.save(r);
        assertEquals(r, r2);
    }

    @Test
    public void verifyOAuthServicesCallback() {
        final OAuthCallbackAuthorizeService r = new OAuthCallbackAuthorizeService();
        r.setName("test345");
        r.setServiceId(OAuthConstants.CALLBACK_AUTHORIZE_URL_DEFINITION);
        r.setTheme("theme");
        r.setDescription("description");
        r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        final RegisteredService r2 = this.dao.save(r);
        assertEquals(r, r2);
    }

    @Test
    public void verifyOAuthRegisteredServicesCallback() {
        final OAuthCallbackAuthorizeService r = new OAuthCallbackAuthorizeService();
        r.setName("testoauth");
        r.setServiceId(OAuthConstants.CALLBACK_AUTHORIZE_URL_DEFINITION);
        r.setTheme("theme");
        r.setDescription("description");
        r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        final RegisteredService r2 = this.dao.save(r);
        assertEquals(r, r2);
    }

}
