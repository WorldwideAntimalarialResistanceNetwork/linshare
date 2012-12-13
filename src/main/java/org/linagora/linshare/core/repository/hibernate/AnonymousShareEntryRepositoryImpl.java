package org.linagora.linshare.core.repository.hibernate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.linagora.linshare.core.domain.entities.AnonymousShareEntry;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.repository.AnonymousShareEntryRepository;
import org.springframework.orm.hibernate3.HibernateTemplate;

public class AnonymousShareEntryRepositoryImpl extends AbstractRepositoryImpl<AnonymousShareEntry>implements AnonymousShareEntryRepository {

	public AnonymousShareEntryRepositoryImpl(HibernateTemplate hibernateTemplate) {
		super(hibernateTemplate);
	}

	@Override
	protected DetachedCriteria getNaturalKeyCriteria(AnonymousShareEntry entity) {
		DetachedCriteria det = DetachedCriteria.forClass(AnonymousShareEntry.class).add(Restrictions.eq( "uuid", entity.getUuid()) );
		return det;
	}
	
	@Override
	public AnonymousShareEntry findById(String uuid) {
		List<AnonymousShareEntry> entries = findByCriteria(Restrictions.eq("uuid", uuid));
        if (entries == null || entries.isEmpty()) {
            return null;
        } else if (entries.size() == 1) {
            return entries.get(0);
        } else {
            throw new IllegalStateException("Id must be unique");
        }
    }
	
	@Override
	public AnonymousShareEntry create(AnonymousShareEntry entity) throws BusinessException {
		entity.setCreationDate(new GregorianCalendar());
		entity.setModificationDate(new GregorianCalendar());
		entity.setUuid(UUID.randomUUID().toString());
		return super.create(entity);
	}
	

	@Override
	public AnonymousShareEntry update(AnonymousShareEntry entity) throws BusinessException {
		entity.setModificationDate(new GregorianCalendar());
		return super.update(entity);
	}

	
	@Override
	public List<AnonymousShareEntry> findAllExpiredEntries() {
		List<AnonymousShareEntry> entries = findByCriteria(Restrictions.lt("expirationDate", Calendar.getInstance()));
        if (entries == null) {
        	logger.error("the result is null ! this should not happen.");
            return new ArrayList<AnonymousShareEntry>();
        } 
        return entries;
	}
	

	@Override
	public List<AnonymousShareEntry> findUpcomingExpiredEntries(Integer date) {
		Calendar calMin = Calendar.getInstance();
    	calMin.add(Calendar.DAY_OF_MONTH, date);
    	
    	Calendar calMax = Calendar.getInstance();
    	calMax.add(Calendar.DAY_OF_MONTH, date+1);
        
    	return findByCriteria(Restrictions.lt("expirationDate", calMax), Restrictions.gt("expirationDate", calMin));
	}
}