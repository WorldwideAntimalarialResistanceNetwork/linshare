/*
 *    This file is part of Linshare.
 *
 *   Linshare is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as
 *   published by the Free Software Foundation, either version 3 of
 *   the License, or (at your option) any later version.
 *
 *   Linshare is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public
 *   License along with Foobar.  If not, see
 *                                    <http://www.gnu.org/licenses/>.
 *
 *   (c) 2008 Groupe Linagora - http://linagora.org
 *
*/
package org.linagora.linshare.core.repository;

import java.util.List;

import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.DocumentEntry;
import org.linagora.linshare.core.domain.vo.SearchDocumentCriterion;

public interface DocumentEntryRepository extends AbstractRepository<DocumentEntry>{
	
	final static int BEGIN=0;
	
	final static int END=1;
	
	final static int ANYWHERE=2;
	
	
	 /** Find a document using its uuid.
     * @param  uuid
     * @return found document (null if no document found).
     */
	public DocumentEntry findById(String uuid);
	
	public List<DocumentEntry> findAllMyDocumentEntries(Account owner);
	
	public long getRelatedEntriesCount(DocumentEntry documentEntry);
	
	public List<DocumentEntry> findAllExpiredEntries();
	
	public List<DocumentEntry> retrieveUserDocumentEntriesWithMatchCriterion(final SearchDocumentCriterion searchDocumentCriterion);
}