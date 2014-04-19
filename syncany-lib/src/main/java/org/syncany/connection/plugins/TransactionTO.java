/*
 * Syncany, www.syncany.org
 * Copyright (C) 2011-2014 Philipp C. Heckel <philipp.heckel@gmail.com> 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.syncany.connection.plugins;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;
import org.simpleframework.xml.core.Persist;
import org.syncany.connection.plugins.RemoteFile;
import org.syncany.connection.plugins.StorageException;
import org.syncany.connection.plugins.TempRemoteFile;
import org.syncany.operations.init.InitOperation;

/**
 * The Transaction transfer object exists to serialize a transaction,
 * which is saved on the remote location and deleted once the transaction is
 * completed.
 * 
 * <p>It uses the Simple framework for XML serialization, and its corresponding
 * annotation-based configuration.  
 *  
 * @see <a href="http://simple.sourceforge.net/">Simple framework</a> at simple.sourceforge.net
 * @author Pim Otte
 */
@Root(name="transaction")
@Namespace(reference="http://syncany.org/transaction/1")
public class TransactionTO {
	private static final Logger logger = Logger.getLogger(TransactionTO.class.getSimpleName()); 
	
	@ElementMap(entry="File", key="TempLocation", value="FinalLocation", attribute=false)
	Map<String, String> finalLocationNames;
	
	Map<RemoteFile, RemoteFile> finalLocations;
	
	public TransactionTO() {
		
	}
	
	public TransactionTO(Map<RemoteFile, RemoteFile> finalLocations) {
		this.finalLocations = finalLocations;
	}
	
	@Persist
	public void prepare() {
		finalLocationNames = new HashMap<String, String>();
		for (RemoteFile tempFile : finalLocations.keySet()) {
			finalLocationNames.put(tempFile.getName(), finalLocations.get(tempFile).getName());
		}
	}
	
	@Commit
	public void commit() {
		finalLocations = new HashMap<RemoteFile, RemoteFile>();
		for (String tempFile : finalLocationNames.keySet()) {
			try {
				finalLocations.put(new TempRemoteFile(tempFile), new TempRemoteFile(finalLocationNames.get(tempFile)));
			}
			catch (StorageException e) {
				logger.log(Level.INFO, "Invalid remote temporary filename: " + tempFile + " or " + finalLocationNames.get(tempFile));
			}
		}
	}
	
	public Map<RemoteFile, RemoteFile> getFinalLocations() {
		return finalLocations;
	}
}
