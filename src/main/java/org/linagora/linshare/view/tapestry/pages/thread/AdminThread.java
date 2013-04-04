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
package org.linagora.linshare.view.tapestry.pages.thread;

import java.util.List;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.linagora.linshare.core.domain.vo.ThreadMemberVo;
import org.linagora.linshare.core.domain.vo.ThreadVo;
import org.linagora.linshare.core.domain.vo.UserVo;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.facade.ThreadEntryFacade;
import org.linagora.linshare.view.tapestry.beans.ShareSessionObjects;
import org.linagora.linshare.view.tapestry.components.ConfirmPopup;
import org.linagora.linshare.view.tapestry.components.ThreadEditForm;
import org.linagora.linshare.view.tapestry.components.WindowWithEffects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminThread {

	private static final Logger logger = LoggerFactory.getLogger(AdminThread.class);
	
    @SessionState
    @Property
    private ShareSessionObjects shareSessionObjects;

    @SessionState
    @Property
    private UserVo userVo;
    
    @Property
    @Persist
    private ThreadVo currentThread;
    
    @Component(parameters = {"style=bluelighting", "show=false", "width=520", "height=180"})
    private WindowWithEffects memberEditWindow;

    @Component(parameters = {"style=bluelighting", "show=false", "width=520", "height=180"})
    private WindowWithEffects threadEditWindow;
    
    @InjectComponent
    private ConfirmPopup confirmPopup;
    
    @InjectComponent
    private Zone memberEditTemplateZone;
    
    @InjectComponent
    private Zone threadEditTemplateZone;
    
	@Property
	@Persist
	private List<ThreadMemberVo> members;
	
	@Property
	private ThreadMemberVo member;
	
	@Persist
	private ThreadMemberVo toDelete;
	
    @Inject
    private Messages messages;
    
    @Inject
    private ThreadEntryFacade threadEntryFacade;

    @Persist
    @Property
	private String selectedMemberId;
    
	@Inject
	private Block adminBlock, userBlock, restrictedUserBlock;
    
    /*
     * Assuming currentThread isn't be null
     */
    @SetupRender
    public void init() {
    	try {
    		members = threadEntryFacade.getThreadMembers(currentThread);
		} catch (BusinessException e) {
    		logger.error(e.getMessage());
    		logger.debug(e.toString());
		}
    }
    
    public Object onActivate() {
    	if (currentThread == null) {
    		return Index.class;
    	}
    	try {
			if (!threadEntryFacade.userIsAdmin(userVo, currentThread)) {
				logger.info("Unauthorized");
				return ThreadContent.class;
			}
		} catch (BusinessException e) {
			e.printStackTrace();
			return ThreadContent.class;
		}
    	return null;
    }
    
	/*
	 * Handle page layout with Tapestry Blocks
	 */
	public Object getType() {
		return (member.isAdmin() ? adminBlock : member.isCanUpload() ? userBlock : restrictedUserBlock);
	}
    
    /*
     * Called externally before calling the page.
     * Refer to Tapestry Documentation about passing data from page to page
     * Setup render will fail if this is not called (currentThread would be null)
     */
    public void setSelectedCurrentThread(ThreadVo currentThread) {
    	this.currentThread = currentThread;
    }
    
    public Zone onActionFromEditMember(String identifier) {
    	logger.info("Trying to edit member with identifier : " + identifier);
    	selectedMemberId = identifier;
    	return memberEditTemplateZone;
    }
    
    public Zone onActionFromEditThread() {
    	logger.info("Trying to edit current thread ");
    	return threadEditTemplateZone;
    }
    
    public void onActionFromDeleteMember(String identifier) {
    	logger.info("Trying to delete a member.");
    	for (ThreadMemberVo m : members) {
    		logger.debug(m.getLsUuid() + " compared to parameter " + identifier);
			if (m.getLsUuid().equals(identifier)) {
				selectedMemberId = identifier;
				toDelete = m;
				logger.info("Trying to delete " + toDelete.getLsUuid());
			}
		}
    }
    
	@OnEvent(value="deleteThreadPopupEvent")
	public void deleteCurrentThread() {
		try {
			threadEntryFacade.deleteThread(userVo, currentThread);
			currentThread = null;
		} catch (BusinessException e) {
			logger.error(e.getMessage());
		}
	}

	@OnEvent(value="deleteMemberPopupEvent")
	public void deleteMember() {
		threadEntryFacade.deleteMember(currentThread, userVo, toDelete);
	
		// refresh list
		try {
			List<ThreadMemberVo> tmp = threadEntryFacade.getThreadMembers(currentThread);
			members = tmp;
		} catch (BusinessException e) {
    		logger.error(e.getMessage());
    		logger.debug(e.toString());
		}
	}
}
