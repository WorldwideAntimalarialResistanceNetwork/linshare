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
package org.linagora.linshare.mongo.entities.logs;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.linagora.linshare.core.domain.constants.AuditLogEntryType;
import org.linagora.linshare.core.domain.constants.LogAction;
import org.linagora.linshare.core.domain.constants.LogActionCause;
import org.linagora.linshare.mongo.entities.mto.AccountMto;

public class AuditLogEntry {

	protected AccountMto actor;

	protected String resourceUuid;

	protected LogAction action;

	protected LogActionCause cause;

	protected String fromResourceUuid;

	protected AuditLogEntryType type;

	protected Date creationDate;

	@JsonIgnore
	protected String technicalComment;

	public AuditLogEntry() {
		super();
	}

	public AccountMto getActor() {
		return actor;
	}

	public void setActor(AccountMto actor) {
		this.actor = actor;
	}

	public String getResourceUuid() {
		return resourceUuid;
	}

	public void setResourceUuid(String resourceUuid) {
		this.resourceUuid = resourceUuid;
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

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getRepresentation(AuditLogEntryUser log) {
		return "action : " + log.getAction().name() + ", type : " + log.getType().name();
	}

	public String getTechnicalComment() {
		return technicalComment;
	}

	public void setTechnicalComment(String technicalComment) {
		this.technicalComment = technicalComment;
	}

	public LogActionCause getCause() {
		return cause;
	}

	public void setCause(LogActionCause cause) {
		this.cause = cause;
	}

	public String getFromResourceUuid() {
		return fromResourceUuid;
	}

	public void setFromResourceUuid(String fromResourceUuid) {
		this.fromResourceUuid = fromResourceUuid;
	}

	@Override
	public String toString() {
		return "AuditLogEntry [actor=" + actor + ", resourceUuid=" + resourceUuid + ", action=" + action + ", type="
				+ type + ", creationDate=" + creationDate + "]";
	}
}