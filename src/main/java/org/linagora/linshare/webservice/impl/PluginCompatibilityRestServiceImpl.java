/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2013 LINAGORA
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for
 * LinShare software by Linagora pursuant to Section 7 of the GNU Affero General
 * Public License, subsections (b), (c), and (e), pursuant to which you must
 * notably (i) retain the display of the “LinShare™” trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source
 * and free version of LinShare™, powered by Linagora © 2009–2013. Contribute to
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
package org.linagora.linshare.webservice.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.linagora.linshare.core.domain.entities.Guest;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.facade.WebServiceDocumentFacade;
import org.linagora.linshare.core.facade.WebServiceShareFacade;
import org.linagora.linshare.webservice.dto.DocumentDto;
import org.linagora.linshare.webservice.dto.SimpleStringValue;
import org.linagora.linshare.webservice.PluginCompatibilityRestService;

public class PluginCompatibilityRestServiceImpl extends WebserviceBase implements PluginCompatibilityRestService {

	private final WebServiceDocumentFacade webServiceDocumentFacade;
	private final WebServiceShareFacade webServiceShareFacade;

	public PluginCompatibilityRestServiceImpl(final WebServiceDocumentFacade webServiceDocumentFacade, final WebServiceShareFacade facade){
		this.webServiceDocumentFacade = webServiceDocumentFacade;
		this.webServiceShareFacade = facade;
	}
	
	@GET
    @Path("/plugin/information")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Override
	public SimpleStringValue getInformation() {
		return new SimpleStringValue("undefined");
	}
	
	@POST
    @Path("/share/multiplesharedocuments")
	@Override
	public void multiplesharedocuments(@FormParam("targetMail") String targetMail, @FormParam("file") List<String> uuid, @FormParam("securedShare") @DefaultValue("0") int securedShare, @FormParam("message")  @DefaultValue("") String message) {
		
		User actor;
		
		try {
			actor = webServiceShareFacade.checkAuthentication();
		} catch (BusinessException e) {
			throw analyseFaultREST(e);
		} 
 
		if ((actor instanceof Guest  && !actor.getCanUpload())) {
			throw giveRestException(HttpStatus.SC_FORBIDDEN,"You are not authorized to use this service");
		}
		
		
		//check not empty values
		List<String> uuidValues = new ArrayList<String>();
		for (String identifier : uuid) {
			if(identifier!=null && !identifier.isEmpty()) uuidValues.add(identifier);
		}
		if(uuidValues.size()==0){
			throw giveRestException(HttpStatus.SC_BAD_REQUEST, "Missing parameter file");
		}
		
 
		try {
			webServiceShareFacade.multiplesharedocuments(targetMail, uuidValues, securedShare, message);
		} catch (BusinessException e) {
			throw analyseFaultREST(e);
		}
	}
	
	/**
	 * upload a file in user's space.
	 * send file inside a form 
	 */
	@POST
	@Path("/document/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Override
	public DocumentDto uploadfile(@Multipart(value = "file") InputStream theFile, @Multipart(value = "description", required = false) String description, @Multipart(value = "filename",required = false) String givenFileName, MultipartBody body) {
		
		User actor = null;
		try {
			actor = webServiceDocumentFacade.checkAuthentication();
		} catch (BusinessException e) {
			throw analyseFaultREST(e);
		}
		
		if ((actor instanceof Guest  && !actor.getCanUpload())) {
			throw giveRestException(HttpStatus.SC_FORBIDDEN,"You are not authorized to use this service");
		}
 
		if (theFile==null) {
			throw giveRestException(HttpStatus.SC_BAD_REQUEST,"Missing file (check parameter file)");
		}	
 
		
		String filename;
		
		if(givenFileName==null || givenFileName.isEmpty()){
			//parameter givenFileName is optional
			//so need to search this information in the header of the attachement (with id file)
			filename = body.getAttachment("file").getContentDisposition().getParameter("filename"); 
		}  else {
			filename = givenFileName;
		}
		
		//comment can not be null ?
		String comment = (description == null)? "":description;
		
		try {
			return webServiceDocumentFacade.uploadfile(theFile, filename, comment);
		} catch (BusinessException e) {
			throw analyseFaultREST(e);
		}
	}
	
}