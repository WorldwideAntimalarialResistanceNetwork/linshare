package org.linagora.linshare.core.facade.impl;

import java.util.ArrayList;
import java.util.List;

import org.linagora.linshare.core.domain.entities.AbstractDomain;
import org.linagora.linshare.core.domain.entities.MailingList;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.domain.vo.MailingListVo;
import org.linagora.linshare.core.domain.vo.UserVo;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.facade.MailingListFacade;
import org.linagora.linshare.core.service.AbstractDomainService;
import org.linagora.linshare.core.service.MailingListService;
import org.linagora.linshare.core.service.UserAndDomainMultiService;

public class MailingListFacadeImpl implements MailingListFacade {

	private final MailingListService mailingListService;
    private final UserAndDomainMultiService userAndDomainMultiService;
    private final AbstractDomainService abstractDomainService;
    
    public MailingListFacadeImpl(MailingListService mailingListService, UserAndDomainMultiService userAndDomainMultiService,AbstractDomainService abstractDomainService) {
        
    	super();
        this.mailingListService = mailingListService;
        this.userAndDomainMultiService = userAndDomainMultiService;
        this.abstractDomainService=abstractDomainService;
    }
    
    @Override
    public MailingListVo retrieveMailingList(long persistenceId) {
    	MailingList mailingList =mailingListService.retrieveMailingList(persistenceId);
    	return new MailingListVo(mailingList);
    }
    
    @Override
    public MailingListVo createMailingList(MailingListVo mailingListVo) throws BusinessException {
    	MailingList mailingList=new MailingList(mailingListVo);
        User actor = userAndDomainMultiService.findOrCreateUser(mailingListVo.getOwner().getMail(),mailingListVo.getOwner().getDomainIdentifier());
    	mailingList.setOwner(actor);
  	  	AbstractDomain domain = abstractDomainService.retrieveDomain(mailingListVo.getDomain().getIdentifier());
  	  	mailingList.setDomain(domain);
        mailingListService.createMailingList(mailingList);
    	return mailingListVo ;
    }
    
    @Override
    public List<MailingListVo> findAllMailingList() {
    	List<MailingListVo> list =new ArrayList<MailingListVo>();
    	for(MailingList current : mailingListService.findAllMailingList()) {
    		list.add(new MailingListVo(current));
    	}
    	return list;
    }
    
    @Override
    public List<MailingListVo> findAllMailingListByUser(UserVo actorVo) throws BusinessException {
    	List<MailingListVo> list =new ArrayList<MailingListVo>();
        User actor = userAndDomainMultiService.findOrCreateUser(actorVo.getMail(),actorVo.getDomainIdentifier());
    	for(MailingList current : mailingListService.findAllMailingListByUser(actor)) {
    		list.add(new MailingListVo(current));
    	}
    	return list;
    }
    
    @Override
    public void deleteMailingList(long persistenceId) throws BusinessException {
    	mailingListService.deleteMailingList(persistenceId);
    }
    
    @Override
    public void updateMailingList(MailingListVo mailingListVo) throws BusinessException {
    	mailingListService.updateMailingList(new MailingList(mailingListVo));
    }
    
    @Override
    public List<MailingListVo> findAllMailingListByIdentifier(String identifier, UserVo actorVo) throws BusinessException {
    	List<MailingListVo> list = new ArrayList<MailingListVo>();
    	List<MailingListVo> listByUser = new ArrayList<MailingListVo>();
    	listByUser = findAllMailingListByUser(actorVo);
    	for(MailingListVo current : listByUser) {
    		if(current.getIdentifier().equals(identifier)) {
    			list.add(current);
    		}
    	}
    	
    	return list;	
    }
    
   @Override
   public boolean mailingListIdentifierUnicity(MailingListVo toCreate,UserVo actorVo) throws BusinessException {
   		List<MailingListVo> list = findAllMailingListByIdentifier(toCreate.getIdentifier(), actorVo);
   		return list.isEmpty();
   	}
    
}
