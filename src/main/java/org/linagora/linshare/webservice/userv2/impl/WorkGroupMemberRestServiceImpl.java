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

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.facade.webservice.common.dto.WorkGroupMemberDto;
import org.linagora.linshare.core.facade.webservice.user.WorkGroupMemberFacade;
import org.linagora.linshare.webservice.WebserviceBase;
import org.linagora.linshare.webservice.userv2.WorkGroupMemberRestService;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("/work_groups/{workGroupUuid}/members")
@Api(value = "/rest/user/work_groups/{workGroupUuid}/members", basePath = "/rest/work_groups/{workGroupUuid}/members",
	description = "work group members service.",
	produces = "application/json,application/xml", consumes = "application/json,application/xml")
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class WorkGroupMemberRestServiceImpl extends WebserviceBase implements
		WorkGroupMemberRestService {

	private final WorkGroupMemberFacade workGroupMemberFacade;

	public WorkGroupMemberRestServiceImpl(final WorkGroupMemberFacade workGroupMemberFacade) {
		this.workGroupMemberFacade = workGroupMemberFacade;
	}

	@Path("/")
	@POST
	@ApiOperation(value = "Create a workgroup member.", response = WorkGroupMemberDto.class)
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have the delegation role."),
					@ApiResponse(code = 404, message = "Member not found."),
					@ApiResponse(code = 400, message = "Bad request : missing required fields."),
					@ApiResponse(code = 500, message = "Internal server error."),
					})
	@Override
	public WorkGroupMemberDto create(
			@ApiParam(value = "The workgroup uuid.", required = true) @PathParam("workGroupUuid") String workGroupUuid,
			@ApiParam(value = "The user domain identifier.", required = true) WorkGroupMemberDto workGroupMember)
					throws BusinessException {
		return workGroupMemberFacade.create(workGroupUuid, workGroupMember.getUserDomainId(), workGroupMember.getUserMail(), workGroupMember.isReadonly(), workGroupMember.isAdmin());
	}

	@Path("/")
	@GET
	@ApiOperation(value = "Get all workgroup members.", response = WorkGroupMemberDto.class, responseContainer = "Set")
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have the delegation role.") ,
					@ApiResponse(code = 404, message = "Member not found."),
					@ApiResponse(code = 400, message = "Bad request : missing required fields."),
					@ApiResponse(code = 500, message = "Internal server error."),
					})
	@Override
	public List<WorkGroupMemberDto> findAll(
			@ApiParam(value = "The workgroup uuid.", required = true) @PathParam("workGroupUuid") String workGroupUuid)
				throws BusinessException {
		return workGroupMemberFacade.findAll(workGroupUuid);
	}

	@Path("/{userUuid}")
	@GET
	@ApiOperation(value = "Get a workgroup member.", response = WorkGroupMemberDto.class)
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have the delegation role.") ,
					@ApiResponse(code = 404, message = "Member not found."),
					@ApiResponse(code = 400, message = "Bad request : missing required fields."),
					@ApiResponse(code = 500, message = "Internal server error."),
					})
	@Override
	public WorkGroupMemberDto find(
			@ApiParam(value = "The workgroup uuid.", required = true) @PathParam("workGroupUuid") String workGroupUuid,
			@ApiParam(value = "The user uuid.", required = true) @PathParam("userUuid") String userUuid)
			throws BusinessException {
		return workGroupMemberFacade.find(workGroupUuid, userUuid);
	}

	@Path("/")
	@PUT
	@ApiOperation(value = "Update a workgroup member.", response = WorkGroupMemberDto.class)
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have the delegation role.") ,
					@ApiResponse(code = 404, message = "Member or workgroup not found."),
					@ApiResponse(code = 400, message = "Bad request : missing required fields."),
					@ApiResponse(code = 500, message = "Internal server error."),
					})
	@Override
	public WorkGroupMemberDto update(
			@ApiParam(value = "The workgroup uuid.", required = true) @PathParam("workGroupUuid") String workGroupUuid,
			@ApiParam(value = "The workgroup member to update.", required = true) WorkGroupMemberDto workGroupMember)
					throws BusinessException {
		return workGroupMemberFacade.update(workGroupUuid, workGroupMember);
	}

	@Path("/")
	@DELETE
	@ApiOperation(value = "Delete a workgroup member.", response = WorkGroupMemberDto.class)
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have the delegation role.") ,
					@ApiResponse(code = 404, message = "Member or workgroup member not found."),
					@ApiResponse(code = 400, message = "Bad request : missing required fields."),
					@ApiResponse(code = 500, message = "Internal server error."),
					})
	@Override
	public WorkGroupMemberDto delete(
			@ApiParam(value = "The workgroup uuid.", required = true) @PathParam("workGroupUuid") String workGroupUuid,
			@ApiParam(value = "The workgroup member to delete.", required = true) WorkGroupMemberDto workGroupMember)
					throws BusinessException {
		return workGroupMemberFacade.delete(workGroupUuid, workGroupMember.getUserUuid());
	}

	@Path("/{uuid}")
	@DELETE
	@ApiOperation(value = "Delete a workgroup member.", response = WorkGroupMemberDto.class)
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have the delegation role.") ,
					@ApiResponse(code = 404, message = "Member or workgroup member not found."),
					@ApiResponse(code = 400, message = "Bad request : missing required fields."),
					@ApiResponse(code = 500, message = "Internal server error."),
					})
	@Override
	public WorkGroupMemberDto delete(
			@ApiParam(value = "The workgroup uuid.", required = true) @PathParam("workGroupUuid") String workGroupUuid,
			@ApiParam(value = "The user uuid.", required = true) @PathParam("uuid") String userUuid)
					throws BusinessException {
		return workGroupMemberFacade.delete(workGroupUuid, userUuid);
	}
}
