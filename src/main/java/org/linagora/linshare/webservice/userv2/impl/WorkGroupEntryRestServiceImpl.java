/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2015 LINAGORA
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

package org.linagora.linshare.webservice.userv2.impl;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.Validate;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.linagora.linshare.core.domain.constants.AsyncTaskType;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.facade.webservice.common.dto.AccountDto;
import org.linagora.linshare.core.facade.webservice.common.dto.AsyncTaskDto;
import org.linagora.linshare.core.facade.webservice.common.dto.WorkGroupEntryDto;
import org.linagora.linshare.core.facade.webservice.user.AsyncTaskFacade;
import org.linagora.linshare.core.facade.webservice.user.ThreadEntryAsyncFacade;
import org.linagora.linshare.core.facade.webservice.user.WorkGroupEntryFacade;
import org.linagora.linshare.webservice.WebserviceBase;
import org.linagora.linshare.webservice.user.task.ThreadEntryUploadAsyncTask;
import org.linagora.linshare.webservice.user.task.context.ThreadEntryTaskContext;
import org.linagora.linshare.webservice.userv2.WorkGroupEntryRestService;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("/work_groups/{workGroupUuid}/entries")
@Api(value = "/rest/user/work_groups/{workGroupUuid}/entries", basePath = "/rest/work_groups/{workGroupUuid}/entries",
	description = "workgroup entries service.",
	produces = "application/json,application/xml", consumes = "application/json,application/xml")
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class WorkGroupEntryRestServiceImpl extends WebserviceBase implements
	WorkGroupEntryRestService {

	private final WorkGroupEntryFacade workGroupEntryFacade;

	private final ThreadEntryAsyncFacade threadEntryAsyncFacade ;

	private final AsyncTaskFacade asyncTaskFacade;

	private org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor taskExecutor;

	private boolean sizeValidation;

	public WorkGroupEntryRestServiceImpl(WorkGroupEntryFacade workGroupEntryFacade,
			ThreadEntryAsyncFacade threadEntryAsyncFacade,
			AsyncTaskFacade asyncTaskFacade,
			ThreadPoolTaskExecutor taskExecutor,
			boolean sizeValidation) {
		super();
		this.workGroupEntryFacade = workGroupEntryFacade;
		this.threadEntryAsyncFacade = threadEntryAsyncFacade;
		this.asyncTaskFacade = asyncTaskFacade;
		this.taskExecutor = taskExecutor;
		this.sizeValidation = sizeValidation;
	}

	@Path("/")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Create a workgroup entry which will contain the uploaded file.", response = WorkGroupEntryDto.class)
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have the delegation role."),
					@ApiResponse(code = 404, message = "Workgroup entry not found."),
					@ApiResponse(code = 400, message = "Bad request : missing required fields."),
					@ApiResponse(code = 500, message = "Internal server error."),
					})
	@Override
	public WorkGroupEntryDto create(
			@ApiParam(value = "The workgroup uuid.", required = true) @PathParam("workGroupUuid") String workGroupUuid,
			@ApiParam(value = "The workgroup folder uuid.", required = false) @QueryParam("workGroupUuid") String workGroupFolderUuid,
			@ApiParam(value = "File stream.", required = true) InputStream file,
			@ApiParam(value = "An optional description of a workgroup entry.") @Multipart(value = "description", required = false) String description,
			@ApiParam(value = "The given file name of the uploaded file.", required = true) @Multipart(value = "filename", required = false) String givenFileName,
			@ApiParam(value = "True to enable asynchronous upload processing.", required = false) @QueryParam("async") Boolean async,
			@HeaderParam("Content-Length") Long contentLength,
			@ApiParam(value = "file size (size validation purpose).", required = false) @Multipart(value = "filesize", required = false)  Long fileSize,
			MultipartBody body)
					throws BusinessException {
		Long transfertDuration = getTransfertDuration();
		if (file == null) {
			logger.error("Missing file (check parameter file)");
			throw giveRestException(HttpStatus.SC_BAD_REQUEST, "Missing file (check parameter file)");
		}
		String fileName = getFileName(givenFileName, body);
		// Default mode. No user input.
		if (async == null) {
			async = false;
		}
		File tempFile = getTempFile(file, "rest-userv2-thread-entries", fileName);
		long currSize = tempFile.length();
		if (sizeValidation) {
			checkSizeValidation(contentLength, fileSize, currSize);
		}
		if (async) {
			logger.debug("Async mode is used");
			// Asynchronous mode
			AccountDto actorDto = workGroupEntryFacade.getAuthenticatedAccountDto();
			AsyncTaskDto asyncTask = null;
			try {
				asyncTask = asyncTaskFacade.create(currSize, transfertDuration, fileName, null, AsyncTaskType.THREAD_ENTRY_UPLOAD);
				ThreadEntryTaskContext threadEntryTaskContext = new ThreadEntryTaskContext(actorDto, actorDto.getUuid(), workGroupUuid, tempFile, fileName, workGroupFolderUuid);
				ThreadEntryUploadAsyncTask task = new ThreadEntryUploadAsyncTask(threadEntryAsyncFacade, threadEntryTaskContext, asyncTask);
				taskExecutor.execute(task);
				return new WorkGroupEntryDto(asyncTask, threadEntryTaskContext);
			} catch (Exception e) {
				logAsyncFailure(asyncTask, e);
				throw e;
			} finally {
				deleteTempFile(tempFile);
			}
		} else {
			// TODO : manage transfertDuration
			// Synchronous mode
			try {
				logger.debug("Async mode is not used");
				return workGroupEntryFacade.create(null, workGroupUuid, workGroupFolderUuid, tempFile, fileName);
			} finally {
				deleteTempFile(tempFile);
			}
		}
	}

	@Path("/copy/{entryUuid}")
	@POST
	@ApiOperation(value = "Create a threworkgroupry which will contain the uploaded file.", response = WorkGroupEntryDto.class)
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have the delegation role."),
					@ApiResponse(code = 404, message = "Workgroup entry not found."),
					@ApiResponse(code = 400, message = "Bad request : missing required fields."),
					@ApiResponse(code = 500, message = "Internal server error."),
					})
	@Override
	public WorkGroupEntryDto copy(
			@ApiParam(value = "The workgroup uuid.", required = true) @PathParam("workGroupUuid") String workGroupUuid,
			@ApiParam(value = "The document entry uuid.", required = true) @PathParam("entryUuid")  String entryUuid)
					throws BusinessException {
		return workGroupEntryFacade.copy(null, workGroupUuid, entryUuid);
	}

	@Path("/{uuid}")
	@GET
	@ApiOperation(value = "Get a workgroup entry.", response = WorkGroupEntryDto.class)
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have the delegation role.") ,
					@ApiResponse(code = 404, message = "Workgroup entry not found."),
					@ApiResponse(code = 400, message = "Bad request : missing required fields."),
					@ApiResponse(code = 500, message = "Internal server error."),
					})
	@Override
	public WorkGroupEntryDto find(
			@ApiParam(value = "The workgroup uuid.", required = true) @PathParam("workGroupUuid") String workGroupUuid,
			@ApiParam(value = "The workgroup entry uuid.", required = true) @PathParam("uuid") String uuid)
					throws BusinessException {
		return workGroupEntryFacade.find(null, workGroupUuid, uuid);
	}

	@Path("/{uuid}")
	@HEAD
	@ApiOperation(value = "Get a workgroup entry.")
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have the delegation role.") ,
					@ApiResponse(code = 404, message = "Workgroup entry not found."),
					@ApiResponse(code = 400, message = "Bad request : missing required fields."),
					@ApiResponse(code = 500, message = "Internal server error."),
					})
	@Override
	public void head(String workGroupUuid, String uuid) throws BusinessException {
		workGroupEntryFacade.find(null, workGroupUuid, uuid);
	}

	@Path("/")
	@GET
	@ApiOperation(value = "Get all workgroup entries.", response = WorkGroupEntryDto.class, responseContainer = "Set")
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have the delegation role.") ,
					@ApiResponse(code = 404, message = "Workgroup entry not found."),
					@ApiResponse(code = 400, message = "Bad request : missing required fields."),
					@ApiResponse(code = 500, message = "Internal server error."),
					})
	@Override
	public List<WorkGroupEntryDto> findAll(
			@ApiParam(value = "The workgroup uuid.", required = true) @PathParam("workGroupUuid") String workGroupUuid)
					throws BusinessException {
		return workGroupEntryFacade.findAll(null, workGroupUuid);
	}

	@Path("/")
	@DELETE
	@ApiOperation(value = "Delete a workgroup entry.", response = WorkGroupEntryDto.class)
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have the delegation role.") ,
					@ApiResponse(code = 404, message = "Workgroup entry or workgroup entry not found."),
					@ApiResponse(code = 400, message = "Bad request : missing required fields."),
					@ApiResponse(code = 500, message = "Internal server error."),
					})
	@Override
	public WorkGroupEntryDto delete(
			@ApiParam(value = "The workgroup uuid.", required = true) @PathParam("workGroupUuid") String workGroupUuid,
			@ApiParam(value = "The workgroup entry to delete.", required = true) WorkGroupEntryDto workGroupEntry)
					throws BusinessException {
		return workGroupEntryFacade.delete(null, workGroupUuid, workGroupEntry);
	}

	@Path("/{uuid}")
	@DELETE
	@ApiOperation(value = "Delete a workgroup entry.", response = WorkGroupEntryDto.class)
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have the delegation role.") ,
					@ApiResponse(code = 404, message = "Workgroup entry or workgroup entry not found."),
					@ApiResponse(code = 400, message = "Bad request : missing required fields."),
					@ApiResponse(code = 500, message = "Internal server error."),
					})
	@Override
	public WorkGroupEntryDto delete(
			@ApiParam(value = "The workgroup uuid.", required = true) @PathParam("workGroupUuid") String workGroupUuid,
			@ApiParam(value = "The workgroup entry uuid to delete.", required = true) @PathParam("uuid") String uuid)
					throws BusinessException {
		return workGroupEntryFacade.delete(null, workGroupUuid, uuid);
	}

	@Path("/{uuid}/download")
	@GET
	@ApiOperation(value = "Download a file.")
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have the delegation role."),
					@ApiResponse(code = 404, message = "Workgroup entry not found."),
					@ApiResponse(code = 400, message = "Bad request : missing required fields."),
					@ApiResponse(code = 500, message = "Internal server error."),
					})
	@Override
	public Response download(
			@ApiParam(value = "The workgroup uuid.", required = true) @PathParam("workGroupUuid") String workGroupUuid,
			@ApiParam(value = "The workgroup entry uuid.", required = true) @PathParam("uuid") String uuid)
					throws BusinessException {
		return workGroupEntryFacade.download(null, workGroupUuid, uuid);
	}

	@Path("/{uuid}/thumbnail")
	@GET
	@ApiOperation(value = "Download the thumbnail of a file.")
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have the delegation role."),
					@ApiResponse(code = 404, message = "Workgroup entry not found."),
					@ApiResponse(code = 400, message = "Bad request : missing required fields."),
					@ApiResponse(code = 500, message = "Internal server error."),
					})
	@Override
	public Response thumbnail(
			@ApiParam(value = "The workgroup uuid.", required = true) @PathParam("workGroupUuid") String workGroupUuid,
			@ApiParam(value = "The document uuid.", required = true) @PathParam("uuid") String uuid,
			@ApiParam(value = "True to get an encoded base 64 response", required = false) @QueryParam("base64") @DefaultValue("false") boolean base64)
					throws BusinessException {
		return workGroupEntryFacade.thumbnail(null, workGroupUuid, uuid, base64);
	}

	@Path("/{uuid}")
	@PUT
	@ApiOperation(value = "Update a workgroup entry.")
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have the delegation role.") ,
					@ApiResponse(code = 404, message = "Workgroup entry or workgroup entry not found."),
					@ApiResponse(code = 400, message = "Bad request : missing required fields."),
					@ApiResponse(code = 500, message = "Internal server error."),
					})
	@Override
	public WorkGroupEntryDto update(
			@ApiParam(value = "The workgroup uuid.", required = true) @PathParam("workGroupUuid") String workGroupUuid,
			@ApiParam(value = "The workgroup uuid.", required = true) @PathParam("uuid") String workGroupEntryUuid,
			WorkGroupEntryDto workGroupEntryDto) throws BusinessException {

		return workGroupEntryFacade.update(null, workGroupUuid, workGroupEntryUuid,
				workGroupEntryDto);
	}

	@Path("/{uuid}/async")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Override
	public AsyncTaskDto findAsync(@PathParam("uuid") String uuid)
			throws BusinessException {
		Validate.notEmpty(uuid, "Missing uuid");
		return asyncTaskFacade.find(uuid);
	}

	protected void logAsyncFailure(AsyncTaskDto asyncTask, Exception e) {
		logger.error(e.getMessage());
		logger.debug("Exception : ", e);
		if (asyncTask != null) {
			asyncTaskFacade.fail(asyncTask, e);
		}
	}
}