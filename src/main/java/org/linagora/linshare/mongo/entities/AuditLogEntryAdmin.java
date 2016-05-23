/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2016 LINAGORA
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for
 * LinShare software by Linagora pursuant to Section 7 of the GNU Affero General
 * Public License, subsections (b), (c), and (e), pursuant to which you must
 * notably (i) retain the display of the “LinShare™” trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source
 * and free version of LinShare™, powered by Linagora © 2009–2015. Contribute to
 * Linshare R&D by subscribing to an Enterprise offer!” infobox and in the
 * e-mails sent with the Program, (ii) retain all hypertext links between
 * LinShare and linshare.org, between linagora.com and Linagora, and (iii)
 * refrain from infringing Linagora intellectual property rights over its
 * trademarks and commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for LinShare along with this program. If not,
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to LinShare software.
 */
package org.linagora.linshare.mongo.entities;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.linagora.linshare.core.domain.constants.AuditLogEntryType;
import org.linagora.linshare.core.domain.constants.LogAction;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.mongo.entities.mto.AccountMto;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = DomainAuditLogEntry.class, name = "domain_audit"),
	@Type(value = DomainPatternAuditLogEntry.class, name = "domain_pattern_audit"),
	@Type(value = LdapConnectionAuditLogEntry.class, name = "ldap_connection_audit"),
	@Type(value = FunctionalityAuditLogEntry.class, name = "ldap_connection_audit")
		})
@XmlRootElement(name = "AuditLogEntryAdmin")
@XmlSeeAlso({ DomainAuditLogEntry.class,
	DomainPatternAuditLogEntry.class,
	LdapConnectionAuditLogEntry.class,
	FunctionalityAuditLogEntry.class
	})
@Document(collection="auditLogEntry")
public abstract class AuditLogEntryAdmin {

	protected AccountMto actor;

	protected String targetDomainUuid;

	private String resourceUuid;

	protected LogAction action;

	protected AuditLogEntryType type;

	protected Date creationDate;

	public AuditLogEntryAdmin() {
	}

	public AuditLogEntryAdmin(Account actor, String targetDomainUuid, LogAction action, AuditLogEntryType type, String resourceUuid) {
		this.actor = new AccountMto(actor);
		this.targetDomainUuid = targetDomainUuid;
		this.type = type;
		this.action = action;
		this.creationDate = new Date();
		this.resourceUuid = resourceUuid;
	}

	public AccountMto getActor() {
		return actor;
	}

	public void setActor(AccountMto actor) {
		this.actor = actor;
	}

	public String getTargetDomainUuid() {
		return targetDomainUuid;
	}

	public void setTargetDomainUuid(String targetDomainUuid) {
		this.targetDomainUuid = targetDomainUuid;
	}

	public LogAction getAction() {
		return action;
	}

	public void setAction(LogAction action) {
		this.action = action;
	}

	public AuditLogEntryType getType() {
		return type;
	}

	public void setType(AuditLogEntryType type) {
		this.type = type;
	}

	public String getResourceUuid() {
		return resourceUuid;
	}

	public void setResourceUuid(String resourceUuid) {
		this.resourceUuid = resourceUuid;
	}
}
