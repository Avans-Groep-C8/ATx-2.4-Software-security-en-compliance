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

import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class AuditLogService {
	
	private static final Logger auditLogger = LoggerFactory.getLogger("OPENMRS_REST_AUDIT");
	
	public void logPatientAccess(String patientUuid, String action, boolean success) {
		logEvent(
		    "PATIENT_ACCESS",
		    success ? "SUCCESS" : "FAILURE",
		    "Patient",
		    patientUuid,
		    action);
	}
	
	public void logAccessDenied(String resourceType, String resourceUuid, String action) {
		logEvent(
		    "ACCESS_DENIED",
		    "FAILURE",
		    resourceType,
		    resourceUuid,
		    action);
	}
	
	public void logEvent(String event, String outcome, String resourceType, String resourceUuid, String action) {
		auditLogger.info(buildAuditMessage(
		    event,
		    outcome,
		    getCurrentUserId(),
		    resourceType,
		    resourceUuid,
		    action
		        ));
	}
	
	String buildAuditMessage(String event, String outcome, String userId, String resourceType, String resourceUuid,
	        String action) {
		return "event=" + safe(event)
		        + " outcome=" + safe(outcome)
		        + " userId=" + safe(userId)
		        + " resourceType=" + safe(resourceType)
		        + " resourceUuid=" + safe(resourceUuid)
		        + " action=" + safe(action)
		        + " timestamp=" + Instant.now();
	}
	
	private String getCurrentUserId() {
		User user = Context.getAuthenticatedUser();
		
		if (user == null) {
			return "anonymous";
		}
		
		if (user.getUuid() != null) {
			return user.getUuid();
		}
		
		return "user-" + user.getUserId();
	}
	
	String safe(String value) {
		if (value == null || value.trim().isEmpty()) {
			return "-";
		}
		
		return value
		        .replace("\n", "_")
		        .replace("\r", "_")
		        .replace("\t", "_");
	}
}
