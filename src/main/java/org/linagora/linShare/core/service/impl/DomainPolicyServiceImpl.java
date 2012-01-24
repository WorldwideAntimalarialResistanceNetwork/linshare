package org.linagora.linShare.core.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.linagora.linShare.core.domain.constants.DomainAccessRuleType;
import org.linagora.linShare.core.domain.entities.AbstractDomain;
import org.linagora.linShare.core.domain.entities.AllowDomain;
import org.linagora.linShare.core.domain.entities.DenyDomain;
import org.linagora.linShare.core.domain.entities.DomainAccessRule;
import org.linagora.linShare.core.domain.entities.DomainPolicy;
import org.linagora.linShare.core.repository.DomainPolicyRepository;
import org.linagora.linShare.core.service.DomainPolicyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomainPolicyServiceImpl implements DomainPolicyService {

	private static final Logger logger = LoggerFactory.getLogger(DomainPolicyServiceImpl.class);
	private final DomainPolicyRepository domainPolicyRepository;
	
	
	public DomainPolicyServiceImpl(DomainPolicyRepository domainPolicyRepository) {
		super();
		this.domainPolicyRepository = domainPolicyRepository;
	}

	@Override
	public DomainPolicy findById(String identifier) {
		return domainPolicyRepository.findById(identifier);
	}

	@Override
	public List<DomainPolicy> getAllDomainPolicy() {
		return domainPolicyRepository.findAll();
	}

	@Override
	public List<String> getAllDomainPolicyIdentifiers() {
		return domainPolicyRepository.findAllIdentifiers();
	}

	private List<AbstractDomain> getAuthorizedDomain(AbstractDomain domain, List<DomainAccessRule> rules) {
		Set<AbstractDomain> set = new HashSet<AbstractDomain>();
		set.add(domain);
		return getAuthorizedDomain(set,rules);
	}
	
	private List<AbstractDomain> getAuthorizedDomain(Set<AbstractDomain> domains, List<DomainAccessRule> rules) {
		
		logger.debug("Begin getAuthorizedDomain");
		
		List<AbstractDomain> includes = new ArrayList<AbstractDomain>();
		List<AbstractDomain> excludes = new ArrayList<AbstractDomain>();

		logger.debug("domainAccessRule list size : " + rules.size());
		
		String debug = "";
		for (AbstractDomain d: domains) {
			debug += d.getIdentifier() + ", "; 
			
		}
		logger.debug("input domain list size : " + domains.size() + " : " + debug);
		
		for (AbstractDomain domain : domains) {
			logger.debug("check:domain : " + domain.toString());
			for (DomainAccessRule domainAccessRule : rules) {
				logger.debug("check:domainAccessRule : " + domainAccessRule.toString());
				if(domainAccessRule.getDomainAccessRuleType().equals(DomainAccessRuleType.ALLOW_ALL)) {
//					logger.debug("check:domainAccessRule : ALLOW_ALL");
					// Allow domain without any check 
					if(!includes.contains(domain) && !excludes.contains(domain)) {
						includes.add(domain);
					}
					// This rule should me the last one
					break;
				} else if(domainAccessRule.getDomainAccessRuleType().equals(DomainAccessRuleType.DENY_ALL)) {
//					logger.debug("check:domainAccessRule : DENY_ALL");
					// Deny domain without any check
					if(!excludes.contains(domain)) {
						excludes.add(domain);
					}
					// This rule should me the last one
					break;
					
				} else if(domainAccessRule.getDomainAccessRuleType().equals(DomainAccessRuleType.ALLOW)) {
//					logger.debug("check:domainAccessRule : ALLOW");
					// Allow domain
					AllowDomain allowDomain = (AllowDomain)domainAccessRule;
					
					if(allowDomain.getDomain().equals(domain) && !includes.contains(domain)) {
						logger.debug(" ALLOW : " + domain.getIdentifier());
						includes.add(domain);
					}
					
				} else if(domainAccessRule.getDomainAccessRuleType().equals(DomainAccessRuleType.DENY)) {
					// Deny domain
//					logger.debug("check:domainAccessRule : DENY");
					DenyDomain denyDomain = (DenyDomain)domainAccessRule;
					
					if(denyDomain.getDomain().equals(domain) && !excludes.contains(domain) ) {
						logger.debug(" DENY : " + domain.getIdentifier());
						excludes.add(domain);
					}
				}
			}
		}
		
		logger.debug("End getAuthorizedDomain");
		return includes;
	}
	
	
	@Override
	public boolean isAuthorizedToCommunicateWithItSelf(AbstractDomain domain) {
		logger.debug("Begin isAuthorizedToCommunicateWithItSelf : " + domain);
		List<AbstractDomain> result = getAuthorizedDomain( domain, domain.getPolicy().getDomainAccessPolicy().getRules());
		if(result!=null && result.size()==1){
			logger.debug("Domain '" + domain.getIdentifier() + "' is authorized to communicate with itself.");
			return true;
		}
		logger.debug("Domain '" + domain.getIdentifier() + "' is not authorized to communicate with itself.");
		logger.debug("End isAuthorizedToCommunicateWithItSelf : " + domain);
		return false;
	}

	@Override
	public boolean isAuthorizedToCommunicateWithItsParent(AbstractDomain domain) {
		logger.debug("Begin isAuthorizedToCommunicateWithItsParent : " + domain);
		if(domain.getParentDomain() != null ) {
			List<AbstractDomain> result = getAuthorizedDomain( domain.getParentDomain(), domain.getPolicy().getDomainAccessPolicy().getRules());
			if(result!=null && result.size()==1){
				logger.debug("Domain '" + domain.getIdentifier() + "' is authorized to communicate with its parent.");
				return true;
			}
		}
		logger.debug("Domain '" + domain.getIdentifier() + "' is not authorized to communicate with its parent.");
		logger.debug("End isAuthorizedToCommunicateWithItsParent : " + domain);
		return false;
	}

	@Override
	public List<AbstractDomain> getAuthorizedSubDomain(AbstractDomain domain) {

		logger.debug("Begin public getAuthorizedSubDomain : " + domain);
		List<AbstractDomain> result = new ArrayList<AbstractDomain>();
		List<DomainAccessRule> rules = domain.getPolicy().getDomainAccessPolicy().getRules();
		
		// Check for communication with subdomains.		
		if(domain.getSubdomain() != null) {
			result.addAll(getAuthorizedDomain(domain.getSubdomain(), rules));
		}

		logger.debug("domain result list size : " + result.size());
		for (AbstractDomain abstractDomain : result) {
			logger.debug("result : " + abstractDomain.getIdentifier());
		}
		logger.debug("End public getAuthorizedSubDomain : " + domain);
		return result;
	}

	@Override
	public List<AbstractDomain> getAuthorizedSibblingDomain(AbstractDomain domain) {
		logger.debug("Begin getAuthorizedSibblingDomain : " + domain);
		List<AbstractDomain> result = new ArrayList<AbstractDomain>();
		List<DomainAccessRule> rules = domain.getPolicy().getDomainAccessPolicy().getRules();
		
		// Check for communication with siblings.		
		if(domain.getParentDomain() != null) {
			result.addAll(getAuthorizedDomain(domain.getParentDomain().getSubdomain(), rules));
		}

		logger.debug("domain result list size : " + result.size());
		for (AbstractDomain abstractDomain : result) {
			logger.debug("result : " + abstractDomain.getIdentifier());
		}
		logger.debug("End getAuthorizedSibblingDomain : " + domain);
		return result;
	}

	private List<AbstractDomain> getAllAuthorizedDomain(AbstractDomain domain, List<DomainAccessRule> rules) {
		List<AbstractDomain> result = new ArrayList<AbstractDomain>();
		
		// Step 1 : check for self communication
		result.addAll(getAuthorizedDomain( domain, rules));

		// Step 2 : check for communication with sub domains.
		if(domain.getSubdomain() != null) {
			
			for (AbstractDomain sub : domain.getSubdomain()) {
				for (AbstractDomain d : getAllAuthorizedDomain(sub, rules)) {
					if(!result.contains(d)) {
						result.add(d);
					}
				}
			}
			
			
		}
		return result;
	}
	
	private List<AbstractDomain> getAllAuthorizedDomainReverse(AbstractDomain domain, List<DomainAccessRule> rules) {
		List<AbstractDomain> result = new ArrayList<AbstractDomain>();
		
		if(domain.getParentDomain() != null) {
			result.addAll(getAllAuthorizedDomainReverse(domain.getParentDomain(), rules));
		}
		
		// Step 1 : check for self communication
		result.addAll(getAuthorizedDomain( domain, rules));

		// Step 2 : check for communication with sub domains.
		if(domain.getSubdomain() != null) {
			
			for (AbstractDomain sub : domain.getSubdomain()) {
				for (AbstractDomain d : getAllAuthorizedDomain(sub, rules)) {
					if(!result.contains(d)) {
						result.add(d);
					}
				}
			}
			
			
		}
		return result;
	}
	
	@Override
	public List<AbstractDomain> getAllAuthorizedDomain(AbstractDomain domain) {
		logger.debug("Begin getAllAuthorizedDomain : " + domain);
		List<AbstractDomain> result = new ArrayList<AbstractDomain>();
		List<DomainAccessRule> rules = domain.getPolicy().getDomainAccessPolicy().getRules();
		
		for (AbstractDomain d : getAllAuthorizedDomain(domain, rules)) {
			if(!result.contains(d)) {
				result.add(d);
			}	
		}
		
		for (AbstractDomain d : getAllAuthorizedDomainReverse(domain, rules)) {
			if(!result.contains(d)) {
				result.add(d);
			}
		}
		
		
		logger.debug("domain result list size : " + result.size());
		for (AbstractDomain abstractDomain : result) {
			logger.debug("result : " + abstractDomain.getIdentifier());
		}
		logger.debug("End getAllAuthorizedDomain : " + domain);
		return result;
	}
	
	
	
}