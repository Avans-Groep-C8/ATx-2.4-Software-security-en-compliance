/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.audit;

import org.junit.Assert;
import org.junit.Test;

public class AuditLogServiceTest {

    @Test
    public void safe_shouldReturnDashForNull() {
        AuditLogService service = new AuditLogService();

        Assert.assertEquals("-", service.safe(null));
    }

    @Test
    public void safe_shouldReturnDashForBlankValue() {
        AuditLogService service = new AuditLogService();

        Assert.assertEquals("-", service.safe("   "));
    }

    @Test
    public void safe_shouldRemoveNewlinesTabsAndCarriageReturns() {
        AuditLogService service = new AuditLogService();

        String result = service.safe("abc\n123\r456\txyz");

        Assert.assertEquals("abc_123_456_xyz", result);
    }

    @Test
    public void buildAuditMessage_shouldContainRequiredAuditFieldsForSuccessfulPatientAction() {
        AuditLogService service = new AuditLogService();

        String message = service.buildAuditMessage(
                "PATIENT_ACCESS",
                "SUCCESS",
                "user-123",
                "Patient",
                "patient-456",
                "CREATE"
        );

        Assert.assertTrue(message.contains("event=PATIENT_ACCESS"));
        Assert.assertTrue(message.contains("outcome=SUCCESS"));
        Assert.assertTrue(message.contains("userId=user-123"));
        Assert.assertTrue(message.contains("resourceType=Patient"));
        Assert.assertTrue(message.contains("resourceUuid=patient-456"));
        Assert.assertTrue(message.contains("action=CREATE"));
        Assert.assertTrue(message.contains("timestamp="));
    }

    @Test
    public void buildAuditMessage_shouldContainFailureOutcomeForFailedPatientAction() {
        AuditLogService service = new AuditLogService();

        String message = service.buildAuditMessage(
                "PATIENT_ACCESS",
                "FAILURE",
                "user-123",
                "Patient",
                "patient-456",
                "UPDATE"
        );

        Assert.assertTrue(message.contains("event=PATIENT_ACCESS"));
        Assert.assertTrue(message.contains("outcome=FAILURE"));
        Assert.assertTrue(message.contains("action=UPDATE"));
    }

    @Test
    public void buildAuditMessage_shouldSanitizeAllUserControlledFields() {
        AuditLogService service = new AuditLogService();

        String message = service.buildAuditMessage(
                "PATIENT\nACCESS",
                "SUCCESS\r",
                "user\t123",
                "Patient\n",
                "patient\r456",
                "CREATE\t"
        );

        Assert.assertFalse(message.contains("\n"));
        Assert.assertFalse(message.contains("\r"));
        Assert.assertFalse(message.contains("\t"));

        Assert.assertTrue(message.contains("event=PATIENT_ACCESS"));
        Assert.assertTrue(message.contains("outcome=SUCCESS_"));
        Assert.assertTrue(message.contains("userId=user_123"));
        Assert.assertTrue(message.contains("resourceType=Patient_"));
        Assert.assertTrue(message.contains("resourceUuid=patient_456"));
        Assert.assertTrue(message.contains("action=CREATE_"));
    }

    @Test
    public void buildAuditMessage_shouldUseDashForMissingOptionalValues() {
        AuditLogService service = new AuditLogService();

        String message = service.buildAuditMessage(
                "PATIENT_ACCESS",
                "SUCCESS",
                null,
                "Patient",
                null,
                "DELETE"
        );

        Assert.assertTrue(message.contains("userId=-"));
        Assert.assertTrue(message.contains("resourceUuid=-"));
    }

    @Test
    public void buildAuditMessage_shouldNotContainSensitivePatientDataWhenOnlyMetadataIsProvided() {
        AuditLogService service = new AuditLogService();

        String message = service.buildAuditMessage(
                "PATIENT_ACCESS",
                "SUCCESS",
                "user-123",
                "Patient",
                "patient-456",
                "UPDATE"
        );

        Assert.assertFalse(message.contains("BSN"));
        Assert.assertFalse(message.contains("password"));
        Assert.assertFalse(message.contains("Authorization"));
        Assert.assertFalse(message.contains("JSESSIONID"));
        Assert.assertFalse(message.contains("diagnose"));
        Assert.assertFalse(message.contains("medicatie"));
        Assert.assertFalse(message.contains("requestBody"));
        Assert.assertFalse(message.contains("responseBody"));
    }

    @Test
    public void buildAuditMessage_shouldSupportAccessDeniedEvent() {
        AuditLogService service = new AuditLogService();

        String message = service.buildAuditMessage(
                "ACCESS_DENIED",
                "FAILURE",
                "user-123",
                "Patient",
                "patient-456",
                "READ"
        );

        Assert.assertTrue(message.contains("event=ACCESS_DENIED"));
        Assert.assertTrue(message.contains("outcome=FAILURE"));
        Assert.assertTrue(message.contains("resourceType=Patient"));
        Assert.assertTrue(message.contains("resourceUuid=patient-456"));
        Assert.assertTrue(message.contains("action=READ"));
    }
}