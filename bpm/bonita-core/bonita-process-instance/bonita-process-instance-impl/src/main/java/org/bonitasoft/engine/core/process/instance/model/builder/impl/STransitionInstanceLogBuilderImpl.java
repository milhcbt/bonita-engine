/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.instance.model.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.builder.STransitionInstanceLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.MissingMandatoryFieldsException;

/**
 * @author Zhao Na
 */
public class STransitionInstanceLogBuilderImpl extends CRUDELogBuilder implements STransitionInstanceLogBuilder {

    private static final String TRANSITION_INSTANCE = "TRANSITION_INSTANCE";

    @Override
    public SPersistenceLogBuilder objectId(final long objectId) {
        this.queriableLogBuilder.numericIndex(SProcessInstanceLogIndexesMapper.TRANSITION_INSTANCE_INDEX, objectId);
        return this;
    }

    @Override
    public STransitionInstanceLogBuilder processInstanceId(final long processInstanceId) {
        this.queriableLogBuilder.numericIndex(SProcessInstanceLogIndexesMapper.PROCESS_INSTANCE_INDEX, processInstanceId);
        return this;
    }

    @Override
    protected String getActionTypePrefix() {
        return TRANSITION_INSTANCE;
    }

    @Override
    protected void checkExtraRules(final SQueriableLog log) {
        if (log.getActionStatus() != SQueriableLog.STATUS_FAIL) {
            if (log.getNumericIndex(SProcessInstanceLogIndexesMapper.TRANSITION_INSTANCE_INDEX) == 0L) {
                throw new MissingMandatoryFieldsException("Some mandatory fields are missing: " + "TransitionInstance Id");
            }
        }
        if (log.getNumericIndex(SProcessInstanceLogIndexesMapper.PROCESS_INSTANCE_INDEX) == 0L) {
            throw new MissingMandatoryFieldsException("Some mandatory fields are missing: " + "ProcessInstance Id");
        }
    }

}
