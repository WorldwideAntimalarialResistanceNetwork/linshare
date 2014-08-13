/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2014 LINAGORA
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for
 * LinShare software by Linagora pursuant to Section 7 of the GNU Affero General
 * Public License, subsections (b), (c), and (e), pursuant to which you must
 * notably (i) retain the display of the “LinShare™” trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source
 * and free version of LinShare™, powered by Linagora © 2009–2014. Contribute to
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
package org.linagora.linshare.core.facade.webservice.user.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.activation.DataHandler;

import org.linagora.linshare.core.domain.entities.DocumentEntry;
import org.linagora.linshare.core.domain.entities.MimeType;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.exception.BusinessErrorCode;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.facade.webservice.user.DocumentFacade;
import org.linagora.linshare.core.service.AccountService;
import org.linagora.linshare.core.service.DocumentEntryService;
import org.linagora.linshare.core.service.MimePolicyService;
import org.linagora.linshare.webservice.dto.DocumentAttachement;
import org.linagora.linshare.webservice.dto.DocumentDto;
import org.linagora.linshare.webservice.dto.MimeTypeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class DocumentFacadeImpl extends UserGenericFacadeImp
		implements DocumentFacade {

	private static final Logger logger = LoggerFactory
			.getLogger(DocumentFacade.class);

	private final DocumentEntryService documentEntryService;

	private final MimePolicyService mimePolicyService;

	public DocumentFacadeImpl(
			final DocumentEntryService documentEntryService,
			final AccountService accountService,
			final MimePolicyService mimePolicyService) {
		super(accountService);
		this.documentEntryService = documentEntryService;
		this.mimePolicyService = mimePolicyService;
	}

	@Override
	public List<DocumentDto> getDocuments() throws BusinessException {
		User actor = checkAuthentication();
		List<DocumentEntry> docs = documentEntryService.findAll(actor, actor);
		return Lists.transform(docs, DocumentDto.toVo());
	}

	@Override
	public DocumentDto getDocument(String uuid) throws BusinessException {
		User actor = checkAuthentication();
		DocumentEntry doc = documentEntryService.find(actor, actor, uuid);
		return new DocumentDto(doc);
	}

	@Override
	public DocumentDto uploadfile(InputStream fi, String fileName,
			String description) throws BusinessException {
		User actor = checkAuthentication();
		if ((actor.isGuest() && !actor.getCanUpload()))
			throw new BusinessException(
					BusinessErrorCode.WEBSERVICE_FORBIDDEN,
					"You are not authorized to use this service");
		DocumentEntry res = documentEntryService.create(actor, actor,
				fi, fileName);

		documentEntryService.updateFileProperties(actor, actor,
				res.getUuid(), res.getName(), description);
		return new DocumentDto(res);
	}

	@Override
	public DocumentDto addDocumentXop(DocumentAttachement doca)
			throws BusinessException {
		try {
			User actor = checkAuthentication();
			DataHandler dh = doca.getDocument();
			InputStream in = dh.getInputStream();
			String fileName = doca.getFilename();
			DocumentEntry res = documentEntryService.create(actor,
					actor, in, fileName);
			// mandatory ?
			String comment = (doca.getComment() == null) ? "" : doca
					.getComment();

			documentEntryService.updateFileProperties(actor, actor,
					res.getUuid(), res.getName(), comment);
			return new DocumentDto(res);
		} catch (IOException e) {
			throw new BusinessException(BusinessErrorCode.WEBSERVICE_FAULT,
					"unable to upload", e);
		}
	}

	@Override
	public Long getUserMaxFileSize() throws BusinessException {
		User actor = checkAuthentication();
		return documentEntryService.getUserMaxFileSize(actor);
	}

	@Override
	public Long getAvailableSize() throws BusinessException {
		User actor = checkAuthentication();
		return documentEntryService.getAvailableSize(actor);
	}

	@Override
	public InputStream getDocumentStream(String docEntryUuid) throws BusinessException {
		logger.debug("downloading for document : " + docEntryUuid);
		User actor = checkAuthentication();
		return documentEntryService.getDocumentStream(actor, actor, docEntryUuid);
	}

	@Override
	public InputStream getThumbnailStream(String docEntryUuid) throws BusinessException {
		logger.debug("downloading thumbnail for document : " + docEntryUuid);
		User actor = checkAuthentication();
		return documentEntryService.getDocumentThumbnailStream(actor, actor, docEntryUuid);
	}

	@Override
	public DocumentDto deleteFile(String uuid) throws BusinessException {
		logger.debug("deleting for document : " + uuid);
		User actor = checkAuthentication();
		DocumentEntry doc = documentEntryService.find(actor, actor, uuid);
		documentEntryService.delete(actor, actor, uuid);
		return new DocumentDto(doc);
	}

	@Override
	public List<MimeTypeDto> getMimeTypes() throws BusinessException {
		User actor = checkAuthentication();
		List<MimeTypeDto> res = Lists.newArrayList();
		Set<MimeType> mimeTypes = mimePolicyService.findAllMyMimeTypes(actor);
		for (MimeType mimeType : mimeTypes) {
			if (mimeType.getEnable()) {
				res.add(new MimeTypeDto(mimeType, true));
			}
		}
		return res;
	}

	@Override
	public Boolean isEnableMimeTypes() throws BusinessException {
		User actor = checkAuthentication();
		return documentEntryService.mimeTypeFilteringStatus(actor);
	}
}
