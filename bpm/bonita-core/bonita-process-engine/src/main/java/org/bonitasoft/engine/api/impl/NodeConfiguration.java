/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.api.impl;

import java.util.List;

import org.bonitasoft.engine.commons.RestartHandler;
import org.bonitasoft.engine.execution.work.TenantRestartHandler;
import org.bonitasoft.engine.scheduler.JobRegister;

/**
 * This class allow to provide a configuration for the platform
 * 
 * @author Baptiste Mesta
 */
public interface NodeConfiguration {

    /**
     * specify if the scheduler should start on this node
     * 
     * @return
     *         true if the scheduler
     */
    boolean shouldStartScheduler();

    /**
     * specify if we should resume unfinished elements when the node is started
     * 
     * @return
     */
    boolean shouldResumeElements();

    /**
     * Handlers called on restart of the platform
     */
    List<RestartHandler> getRestartHandlers();

    /**
     * @return
     */
    String getEventHandlingJobCron();

    /**
     * @return
     */
    boolean shouldStartEventHandlingJob();

    /**
     * @return
     */
    List<TenantRestartHandler> getTenantRestartHandlers();

    /**
     * @return
     */
    List<JobRegister> getJobsToRegister();

    /**
     * Specify how often invalid sessions will be cleaned
     * 
     * @return a String representing a Unix Cron
     */
    String getCleanInvalidSessionsJobCron();

    /**
     * 
     * @return
     *         true if the sessions should be cleaned when the node is stopped
     */
    boolean shouldClearSessions();

}
