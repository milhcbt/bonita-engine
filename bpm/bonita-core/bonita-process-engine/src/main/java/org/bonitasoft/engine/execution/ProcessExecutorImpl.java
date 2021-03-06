/**
 * Copyright (C) 2011, 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.execution;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.SArchivingException;
import org.bonitasoft.engine.api.impl.transaction.event.CreateEventInstance;
import org.bonitasoft.engine.bpm.bar.DocumentsResourcesContribution;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinition;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinitionWithInputValues;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.InvalidEvaluationConnectorConditionException;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.ConnectorResult;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.connector.exception.SConnectorException;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceReadException;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.model.SDocument;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.document.model.builder.SDocumentBuilderFactory;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SDocumentDefinition;
import org.bonitasoft.engine.core.process.definition.model.SDocumentListDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SGatewayDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.TransitionState;
import org.bonitasoft.engine.core.process.definition.model.event.SEndEventDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.GatewayInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.TokenService;
import org.bonitasoft.engine.core.process.instance.api.TransitionService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SGatewayNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.STransitionCreationException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.SToken;
import org.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.event.SEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SThrowEventInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.HandlerRegistrationException;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.execution.flowmerger.FlowMerger;
import org.bonitasoft.engine.execution.flowmerger.FlowNodeCompletionTokenProvider;
import org.bonitasoft.engine.execution.flowmerger.FlowNodeTransitionsWrapper;
import org.bonitasoft.engine.execution.flowmerger.TokenInfo;
import org.bonitasoft.engine.execution.handler.SProcessInstanceHandler;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.work.WorkFactory;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.bonitasoft.engine.work.SWorkRegisterException;
import org.bonitasoft.engine.work.WorkService;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 * @author Hongwen Zang
 * @author Celine Souchet
 */
public class ProcessExecutorImpl implements ProcessExecutor {

    protected final ActivityInstanceService activityInstanceService;

    private final FlowNodeExecutor flowNodeExecutor;

    private final TechnicalLoggerService logger;

    protected final ProcessInstanceService processInstanceService;

    private final TokenService tokenService;

    private final WorkService workService;

    private final ProcessDefinitionService processDefinitionService;

    private final GatewayInstanceService gatewayInstanceService;

    private final TransitionService transitionService;

    private final EventInstanceService eventInstanceService;

    protected final ClassLoaderService classLoaderService;

    protected final ExpressionResolverService expressionResolverService;

    protected final ConnectorService connectorService;

    private final OperationService operationService;

    private final DocumentService documentService;

    private final ReadSessionAccessor sessionAccessor;

    protected final BPMInstancesCreator bpmInstancesCreator;

    protected final EventsHandler eventsHandler;

    private final ConnectorInstanceService connectorInstanceService;

    private final TransitionEvaluator transitionEvaluator;

    public ProcessExecutorImpl(final ActivityInstanceService activityInstanceService, final ProcessInstanceService processInstanceService,
            final TechnicalLoggerService logger, final FlowNodeExecutor flowNodeExecutor, final WorkService workService,
            final ProcessDefinitionService processDefinitionService, final GatewayInstanceService gatewayInstanceService,
            final TransitionService transitionService, final EventInstanceService eventInstanceService, final ConnectorService connectorService,
            final ConnectorInstanceService connectorInstanceService, final ClassLoaderService classLoaderService, final OperationService operationService,
            final ExpressionResolverService expressionResolverService, final EventService eventService,
            final Map<String, SProcessInstanceHandler<SEvent>> handlers, final DocumentService documentService,
            final ReadSessionAccessor sessionAccessor, final ContainerRegistry containerRegistry, final BPMInstancesCreator bpmInstancesCreator,
            final TokenService tokenService, final EventsHandler eventsHandler, final FlowNodeStateManager flowNodeStateManager) {
        super();
        this.activityInstanceService = activityInstanceService;
        this.processInstanceService = processInstanceService;
        this.connectorInstanceService = connectorInstanceService;
        this.tokenService = tokenService;
        this.logger = logger;
        this.flowNodeExecutor = flowNodeExecutor;
        this.workService = workService;
        this.processDefinitionService = processDefinitionService;
        this.gatewayInstanceService = gatewayInstanceService;
        this.transitionService = transitionService;
        this.eventInstanceService = eventInstanceService;
        this.connectorService = connectorService;
        this.classLoaderService = classLoaderService;
        this.operationService = operationService;
        this.expressionResolverService = expressionResolverService;
        this.documentService = documentService;
        this.sessionAccessor = sessionAccessor;
        this.bpmInstancesCreator = bpmInstancesCreator;
        this.eventsHandler = eventsHandler;
        transitionEvaluator = new TransitionEvaluator(expressionResolverService);
        // dependency injection because of circular references...
        flowNodeStateManager.setProcessExecutor(this);
        eventsHandler.setProcessExecutor(this);
        for (final Entry<String, SProcessInstanceHandler<SEvent>> handler : handlers.entrySet()) {
            try {
                eventService.addHandler(handler.getKey(), handler.getValue());
            } catch (final HandlerRegistrationException e) {
                if (logger.isLoggable(getClass(), TechnicalLogSeverity.WARNING)) {
                    logger.log(this.getClass(), TechnicalLogSeverity.WARNING, e.getMessage());
                }
                if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
                    logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, e);
                }
            }
        }
        containerRegistry.addContainerExecutor(this);
    }

    @Override
    public FlowNodeState executeFlowNode(final long flowNodeInstanceId, final SExpressionContext contextDependency, final List<SOperation> operations,
            final long processInstanceId, final Long executerId, final Long executerSubstituteId) throws SFlowNodeExecutionException {
        return flowNodeExecutor.stepForward(flowNodeInstanceId, contextDependency, operations, processInstanceId, executerId, executerSubstituteId);
    }

    private int getNumberOfTokenToMerge(final SFlowNodeInstance sFlownodeInstance) {
        if (SFlowNodeType.GATEWAY.equals(sFlownodeInstance.getType())) {
            final String hitBys = ((SGatewayInstance) sFlownodeInstance).getHitBys();
            final String nbOfTokenToMerge = hitBys.substring(GatewayInstanceService.FINISH.length());
            return Integer.parseInt(nbOfTokenToMerge);
        }
        return 1;
    }

    private SConnectorInstance getNextConnectorInstance(final SProcessInstance processInstance, final ConnectorEvent event)
            throws SConnectorInstanceReadException {
        final List<SConnectorInstance> connectorInstances = connectorInstanceService.getConnectorInstances(processInstance.getId(),
                SConnectorInstance.PROCESS_TYPE, event, 0, 1, ConnectorService.TO_BE_EXECUTED);
        return connectorInstances.size() == 1 ? connectorInstances.get(0) : null;
    }

    @Override
    public boolean executeConnectors(final SProcessDefinition processDefinition, final SProcessInstance sProcessInstance, final ConnectorEvent activationEvent,
            final FlowNodeSelector selectorForConnectorOnEnter) throws SBonitaException {
        final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
        final long processDefinitionId = processDefinition.getId();
        final List<SConnectorDefinition> connectors = processContainer.getConnectors(activationEvent);
        if (connectors.size() > 0) {
            SConnectorInstance nextConnectorInstance;
            nextConnectorInstance = getNextConnectorInstance(sProcessInstance, activationEvent);
            if (nextConnectorInstance != null) {
                for (final SConnectorDefinition sConnectorDefinition : connectors) {
                    if (sConnectorDefinition.getName().equals(nextConnectorInstance.getName())) {
                        workService.registerWork(WorkFactory.createExecuteConnectorOfProcess(processDefinitionId, sProcessInstance.getId(),
                                sProcessInstance.getRootProcessInstanceId(), nextConnectorInstance.getId(), sConnectorDefinition.getName(), activationEvent,
                                selectorForConnectorOnEnter));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private List<SFlowNodeInstance> initializeFirstExecutableElements(final SProcessInstance sProcessInstance, final FlowNodeSelector selector) {
        try {
            final List<SFlowNodeDefinition> flownNodeDefinitions = selector.getFilteredElements();
            long rootProcessInstanceId = sProcessInstance.getRootProcessInstanceId();
            if (rootProcessInstanceId <= 0) {
                rootProcessInstanceId = sProcessInstance.getId();
            }
            return bpmInstancesCreator.createFlowNodeInstances(selector.getProcessDefinition().getId(), rootProcessInstanceId, sProcessInstance.getId(),
                    flownNodeDefinitions, rootProcessInstanceId, sProcessInstance.getId(), SStateCategory.NORMAL, selector.getProcessDefinition().getId());
        } catch (final SBonitaException e) {
            setExceptionContext(selector.getProcessDefinition(), sProcessInstance, e);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.ERROR)) {
                logger.log(this.getClass(), TechnicalLogSeverity.ERROR, e);
            }
            throw new BonitaRuntimeException(e);
        }
    }

    private SProcessInstance createProcessInstance(final SProcessDefinition sDefinition, final long starterId, final long starterSubstituteId,
            final long callerId, final SFlowNodeType callerType, final long rootProcessInstanceId) throws SProcessInstanceCreationException {
        final SProcessInstanceBuilder processInstanceBuilder = BuilderFactory.get(SProcessInstanceBuilderFactory.class).createNewInstance(sDefinition)
                .setStartedBy(starterId).setStartedBySubstitute(starterSubstituteId).setCallerId(callerId, callerType)
                .setRootProcessInstanceId(rootProcessInstanceId);
        final SProcessInstance sProcessInstance = processInstanceBuilder.done();
        processInstanceService.createProcessInstance(sProcessInstance);
        return sProcessInstance;
    }

    protected SProcessInstance createProcessInstance(final SProcessDefinition processDefinition, final long starterId, final long starterSubstituteId,
            final long callerId) throws SProcessInstanceCreationException {
        SActivityInstance callerInstance;
        try {
            callerInstance = getCaller(callerId);
        } catch (final SBonitaException e) {
            throw new SProcessInstanceCreationException("Unable to get caller.", e);
        }

        if (callerInstance != null) {
            return createProcessInstance(processDefinition, starterId, starterSubstituteId, callerId, callerInstance.getType(),
                    callerInstance.getRootContainerId());
        }
        return createProcessInstance(processDefinition, starterId, starterSubstituteId, callerId, null, -1);
    }

    private SActivityInstance getCaller(final long callerId) throws SBonitaReadException, SActivityInstanceNotFoundException {
        if (callerId > 0) {
            return activityInstanceService.getActivityInstance(callerId);
        }
        return null;
    }

    private void executeGateway(final SProcessDefinition sProcessDefinition, final STransitionDefinition sTransitionDefinition,
            final SFlowNodeInstance flowNodeThatTriggeredTheTransition, final Long tokenRefId) throws SBonitaException {
        final long parentProcessInstanceId = flowNodeThatTriggeredTheTransition.getParentProcessInstanceId();
        final long rootProcessInstanceId = flowNodeThatTriggeredTheTransition.getRootProcessInstanceId();
        final SFlowNodeDefinition sFlowNodeDefinition = processDefinitionService.getNextFlowNode(sProcessDefinition, sTransitionDefinition.getName());
        final Long processDefinitionId = sProcessDefinition.getId();

        final boolean isGateway = SFlowNodeType.GATEWAY.equals(sFlowNodeDefinition.getType());
        boolean toExecute = false;
        final Long nextFlowNodeInstanceId;
        try {
            final SProcessInstance parentProcessInstance = processInstanceService.getProcessInstance(parentProcessInstanceId);
            final SStateCategory stateCategory = parentProcessInstance.getStateCategory();
            if (isGateway) {
                final SGatewayInstance gatewayInstance = createOrRetrieveGateway(sProcessDefinition, sFlowNodeDefinition, stateCategory, tokenRefId,
                        parentProcessInstanceId, rootProcessInstanceId, sTransitionDefinition);
                gatewayInstanceService.hitTransition(gatewayInstance, sFlowNodeDefinition.getTransitionIndex(sTransitionDefinition.getName()));
                nextFlowNodeInstanceId = gatewayInstance.getId();
                if (gatewayInstanceService.checkMergingCondition(sProcessDefinition, gatewayInstance)) {
                    gatewayInstanceService.setFinish(gatewayInstance);
                    toExecute = true;
                }
            } else {
                final SFlowNodeInstance sFlowNodeInstance = bpmInstancesCreator.toFlowNodeInstance(processDefinitionId,
                        flowNodeThatTriggeredTheTransition.getRootContainerId(), flowNodeThatTriggeredTheTransition.getParentContainerId(),
                        SFlowElementsContainerType.PROCESS, sFlowNodeDefinition, rootProcessInstanceId, parentProcessInstanceId, true, -1, stateCategory, -1,
                        tokenRefId);
                new CreateEventInstance((SEventInstance) sFlowNodeInstance, eventInstanceService).call();
                nextFlowNodeInstanceId = sFlowNodeInstance.getId();
                toExecute = true;
            }
            if (toExecute) {
                workService.registerWork(WorkFactory
                        .createExecuteFlowNodeWork(processDefinitionId, parentProcessInstanceId, nextFlowNodeInstanceId, null, null));
            }
        } catch (final SBonitaException e) {
            setExceptionContext(sProcessDefinition, flowNodeThatTriggeredTheTransition, e);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.ERROR)) {
                logger.log(this.getClass(), TechnicalLogSeverity.ERROR, e);
            }
            throw e;
        }
    }

    /*
     * try to gate active gateway.
     * if the gateway is already hit by this transition or by the same token, we create a new gateway
     */
    private SGatewayInstance createOrRetrieveGateway(final SProcessDefinition sProcessDefinition, final SFlowNodeDefinition flowNodeDefinition,
            final SStateCategory stateCategory, final Long tokenRefId, final long parentProcessInstanceId, final long rootProcessInstanceId,
            final STransitionDefinition transitionDefinition) throws SBonitaException {
        SGatewayInstance gatewayInstance = null;
        try {
            gatewayInstance = gatewayInstanceService.getActiveGatewayInstanceOfTheProcess(parentProcessInstanceId, flowNodeDefinition.getName());
        } catch (final SGatewayNotFoundException e) {
            // no gateway found we create one
        }

        // check if it's already hit
        if (gatewayInstance != null
                && gatewayInstance.getHitBys() != null
                && (gatewayInstance.getHitBys().contains(GatewayInstanceService.FINISH) || Arrays.asList(gatewayInstance.getHitBys().split(",")).contains(
                        String.valueOf(flowNodeDefinition.getTransitionIndex(transitionDefinition.getName()))))) {
            gatewayInstance = null;// already hit we create a new one
        }
        if (gatewayInstance != null && !tokenRefId.equals(gatewayInstance.getTokenRefId())) {
            final SFlowNodeExecutionException exception = new SFlowNodeExecutionException(
                    "Error while executing the join on the gateway. "
                            + "This can happen when you try to join branches that are not from the same split level (some branches were split more times than others). "
                            + "Check if your design is valid in the Bonita Studio. " + "Token is <" + tokenRefId + "> expected <"
                            + gatewayInstance.getTokenRefId() + ">");
            setExceptionContext(sProcessDefinition, gatewayInstance, exception);
            throw exception;
        }
        if (gatewayInstance != null) {
            return gatewayInstance;
        }
        return createGateway(sProcessDefinition.getId(), flowNodeDefinition, stateCategory, parentProcessInstanceId, tokenRefId, rootProcessInstanceId);
    }

    private SGatewayInstance createGateway(final Long processDefinitionId, final SFlowNodeDefinition flowNodeDefinition, final SStateCategory stateCategory,
            final long parentProcessInstanceId, final Long tokenRefId, final long rootProcessInstanceId) throws SBonitaException {
        return (SGatewayInstance) bpmInstancesCreator
                .createFlowNodeInstance(processDefinitionId, rootProcessInstanceId, parentProcessInstanceId, SFlowElementsContainerType.PROCESS,
                        flowNodeDefinition, rootProcessInstanceId, parentProcessInstanceId, false, 0, stateCategory, -1, tokenRefId);
    }

    protected void executeOperations(final List<SOperation> operations, final Map<String, Object> context, final SExpressionContext expressionContext,
            final SProcessInstance sProcessInstance) throws SBonitaException {
        if (operations != null && !operations.isEmpty()) {
            expressionContext.setInputValues(context);
            operationService.execute(operations, sProcessInstance.getId(), DataInstanceContainer.PROCESS_INSTANCE.name(), expressionContext);
        }
    }

    protected boolean initialize(final long userId, final SProcessDefinition sProcessDefinition, final SProcessInstance sProcessInstance,
            SExpressionContext expressionContext, final List<SOperation> operations, final Map<String, Object> context,
            final SFlowElementContainerDefinition processContainer, final List<ConnectorDefinitionWithInputValues> connectors,
            final FlowNodeSelector selectorForConnectorOnEnter) throws BonitaHomeNotSetException, IOException,
            InvalidEvaluationConnectorConditionException, SBonitaException {
        // Create SDataInstances
        bpmInstancesCreator.createDataInstances(sProcessInstance, processContainer, sProcessDefinition, expressionContext, operations, context);
        createDocuments(sProcessDefinition, sProcessInstance, userId);
        createDocumentLists(sProcessDefinition, sProcessInstance, userId, expressionContext, context);
        if (connectors != null) {
            executeConnectors(sProcessDefinition, sProcessInstance, connectors);
        }
        if (expressionContext == null) {
            expressionContext = new SExpressionContext();
        }
        executeOperations(operations, context, expressionContext, sProcessInstance);

        // Create connectors
        bpmInstancesCreator.createConnectorInstances(sProcessInstance, processContainer.getConnectors(), SConnectorInstance.PROCESS_TYPE);

        return executeConnectors(sProcessDefinition, sProcessInstance, ConnectorEvent.ON_ENTER, selectorForConnectorOnEnter);
    }

    protected void createDocuments(final SProcessDefinition sDefinition, final SProcessInstance sProcessInstance, final long authorId)
            throws SObjectCreationException, BonitaHomeNotSetException, STenantIdNotSetException, IOException, SObjectAlreadyExistsException {
        final SFlowElementContainerDefinition processContainer = sDefinition.getProcessContainer();
        final List<SDocumentDefinition> documentDefinitions = processContainer.getDocumentDefinitions();
        if (!documentDefinitions.isEmpty()) {
            final String processesFolder = BonitaHomeServer.getInstance().getProcessFolder(sessionAccessor.getTenantId(), sDefinition.getId());
            final File documentsFolder = new File(processesFolder, DocumentsResourcesContribution.DOCUMENTS_FOLDER);
            for (final SDocumentDefinition document : documentDefinitions) {
                if (document.getFile() != null) {
                    final String file = document.getFile();// should always exists...validation on businessarchive
                    final byte[] content = FileUtils.readFileToByteArray(new File(documentsFolder, file));
                    attachDocument(sProcessInstance.getId(), document.getName(), document.getFileName(), document.getContentMimeType(), content, authorId,
                            document.getDescription(), -1);
                } else if (document.getUrl() != null) {
                    attachDocument(sProcessInstance.getId(), document.getName(), document.getFileName(), document.getContentMimeType(), document.getUrl(),
                            authorId, document.getDescription(), -1);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void createDocumentLists(final SProcessDefinition sDefinition, final SProcessInstance processInstance, final long authorId,
            final SExpressionContext expressionContext, final Map<String, Object> context)
            throws SBonitaException {
        final SFlowElementContainerDefinition processContainer = sDefinition.getProcessContainer();
        final List<SDocumentListDefinition> documentListDefinitions = processContainer.getDocumentListDefinitions();
        if (!documentListDefinitions.isEmpty()) {
            final List<Object> initialValues = evaluateInitialExpressionsOfDocumentLists(processInstance, expressionContext, context, documentListDefinitions);
            for (int i = 0; i < documentListDefinitions.size(); i++) {
                attachDocumentForList(processInstance, authorId, (List<DocumentValue>) initialValues.get(i), documentListDefinitions.get(i));
            }
        }
    }

    private void attachDocumentForList(final SProcessInstance processInstance, final long authorId, final List<DocumentValue> initialValue,
            final SDocumentListDefinition documentListDefinition) throws SObjectCreationException, SObjectAlreadyExistsException {
        if (initialValue != null) {
            for (int index = 0; index < initialValue.size(); index++) {
                attacheDocument(processInstance, authorId, initialValue, documentListDefinition, index);
            }
        }
    }

    private void attacheDocument(final SProcessInstance processInstance, final long authorId, final List<DocumentValue> initialValue,
            final SDocumentListDefinition documentListDefinition, final int index) throws SObjectCreationException, SObjectAlreadyExistsException {
        final DocumentValue documentValue = initialValue.get(index);
        if (documentValue != null) {
            if (documentValue.hasContent()) {
                attachDocument(processInstance.getId(), documentListDefinition.getName(), documentValue.getFileName(), documentValue.getMimeType(),
                        documentValue.getContent(), authorId, documentListDefinition.getDescription(), index);
            } else {
                attachDocument(processInstance.getId(), documentListDefinition.getName(), documentValue.getFileName(), documentValue.getMimeType(),
                        documentValue.getUrl(), authorId, documentListDefinition.getDescription(), index);
            }
        }
    }

    private List<Object> evaluateInitialExpressionsOfDocumentLists(final SProcessInstance processInstance, final SExpressionContext expressionContext,
            final Map<String, Object> context, final List<SDocumentListDefinition> documentListDefinitions) throws SExpressionTypeUnknownException,
            SExpressionEvaluationException, SExpressionDependencyMissingException, SInvalidExpressionException {
        final ArrayList<SExpression> initialValuesExpressions = new ArrayList<SExpression>(documentListDefinitions.size());
        for (final SDocumentListDefinition documentList : documentListDefinitions) {
            initialValuesExpressions.add(documentList.getExpression());
        }
        final SExpressionContext currentExpressionContext = getsExpressionContext(processInstance, expressionContext, context);
        return expressionResolverService.evaluate(initialValuesExpressions, currentExpressionContext);
    }

    private SExpressionContext getsExpressionContext(final SProcessInstance processInstance, final SExpressionContext expressionContext,
            final Map<String, Object> context) {
        SExpressionContext currentExpressionContext;
        if (expressionContext != null) {
            expressionContext.setInputValues(context);
            currentExpressionContext = expressionContext;
        } else {
            currentExpressionContext = new SExpressionContext(processInstance.getId(), DataInstanceContainer.PROCESS_INSTANCE.name(),
                    processInstance.getProcessDefinitionId());
            currentExpressionContext.setInputValues(context);
        }
        return currentExpressionContext;
    }

    protected SMappedDocument attachDocument(final long processInstanceId, final String documentName, final String fileName, final String mimeType,
            final String url, final long authorId, final String description, final int index) throws SObjectCreationException, SObjectAlreadyExistsException {
        final SDocument attachment = BuilderFactory.get(SDocumentBuilderFactory.class)
                .createNewExternalProcessDocumentReference(fileName, mimeType, authorId, url).done();
        return documentService.attachDocumentToProcessInstance(attachment, processInstanceId, documentName, description, index);
    }

    protected SMappedDocument attachDocument(final long processInstanceId, final String documentName, final String fileName, final String mimeType,
            final byte[] documentContent, final long authorId, final String description, final int index) throws SObjectCreationException,
            SObjectAlreadyExistsException {
        final SDocument attachment = BuilderFactory.get(SDocumentBuilderFactory.class).createNewProcessDocument(fileName, mimeType, authorId, documentContent)
                .done();
        return documentService.attachDocumentToProcessInstance(attachment, processInstanceId, documentName, description, index);
    }

    @Override
    public void childFinished(final long processDefinitionId, final long flowNodeInstanceId, final long parentId) throws SBonitaException {
        final SFlowNodeInstance sFlowNodeInstanceChild = activityInstanceService.getFlowNodeInstance(flowNodeInstanceId);
        final SProcessDefinition sProcessDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
        final SUserTaskInstanceBuilderFactory flowNodeKeyProvider = BuilderFactory.get(SUserTaskInstanceBuilderFactory.class);
        final long processInstanceId = sFlowNodeInstanceChild.getLogicalGroup(flowNodeKeyProvider.getParentProcessInstanceIndex());

        SProcessInstance sProcessInstance = processInstanceService.getProcessInstance(processInstanceId);
        final int tokensOfProcess = executeValidOutgoingTransitionsAndUpdateTokens(sProcessDefinition, sFlowNodeInstanceChild, sProcessInstance);
        if (tokensOfProcess == 0) {
            boolean hasActionsToExecute = false;
            if (ProcessInstanceState.ABORTING.getId() != sProcessInstance.getStateId()) {
                hasActionsToExecute = executePostThrowEventHandlers(sProcessDefinition, sProcessInstance, sFlowNodeInstanceChild);
                // the process instance has maybe changed
                if (hasActionsToExecute) {
                    sProcessInstance = processInstanceService.getProcessInstance(processInstanceId);
                }
                eventsHandler.unregisterEventSubProcess(sProcessDefinition, sProcessInstance);
            }
            handleProcessCompletion(sProcessDefinition, sProcessInstance, hasActionsToExecute);
        }
    }

    @Override
    public void handleProcessCompletion(final SProcessDefinition sProcessDefinition, final SProcessInstance sProcessInstance, final boolean hasActionsToExecute)
            throws SBonitaException {
        ProcessInstanceState processInstanceState;
        switch (sProcessInstance.getStateCategory()) {
            case ABORTING:
                if (ProcessInstanceState.ABORTING.getId() == sProcessInstance.getStateId()) {
                    processInstanceState = ProcessInstanceState.ABORTED;
                } else {
                    if (hasActionsToExecute) {
                        processInstanceState = ProcessInstanceState.ABORTING;
                    } else {
                        processInstanceState = ProcessInstanceState.ABORTED;
                    }
                }
                break;
            case CANCELLING:
                processInstanceState = ProcessInstanceState.CANCELLED;
                break;
            default:
                if (ProcessInstanceState.COMPLETING.getId() == sProcessInstance.getStateId()) {
                    processInstanceState = ProcessInstanceState.COMPLETED;
                } else {
                    if (executeConnectors(sProcessDefinition, sProcessInstance, ConnectorEvent.ON_FINISH, null)) {
                        // some connectors were trigger
                        processInstanceState = ProcessInstanceState.COMPLETING;
                    } else {
                        processInstanceState = ProcessInstanceState.COMPLETED;
                    }
                }
                break;
        }
        processInstanceService.setState(sProcessInstance, processInstanceState);
        if (tokenService.getNumberOfToken(sProcessInstance.getId()) == 0) {
            flowNodeExecutor.childReachedState(sProcessInstance, processInstanceState, hasActionsToExecute);
        }

    }

    private boolean executePostThrowEventHandlers(final SProcessDefinition sProcessDefinition, final SProcessInstance sProcessInstance,
            final SFlowNodeInstance child) throws SBonitaException {
        boolean hasActionsToExecute = false;
        if (sProcessInstance.hasBeenInterruptedByEvent()) {
            final SFlowNodeInstance endEventInstance = activityInstanceService.getFlowNodeInstance(sProcessInstance.getInterruptingEventId());
            final SEndEventDefinition endEventDefinition = (SEndEventDefinition) sProcessDefinition.getProcessContainer().getFlowNode(
                    endEventInstance.getFlowNodeDefinitionId());
            hasActionsToExecute = eventsHandler.handlePostThrowEvent(sProcessDefinition, endEventDefinition, (SThrowEventInstance) endEventInstance, child);
            flowNodeExecutor.archiveFlowNodeInstance(endEventInstance, true, sProcessDefinition.getId());
        }
        return hasActionsToExecute;
    }

    /**
     * Evaluate the split of the element
     * The element contains the current token it received
     *
     * @return
     *         number of token of the process
     */
    private int executeValidOutgoingTransitionsAndUpdateTokens(final SProcessDefinition sProcessDefinition, final SFlowNodeInstance child,
            final SProcessInstance sProcessInstance) throws SBonitaException {
        // token we merged
        final int numberOfTokenToMerge = getNumberOfTokenToMerge(child);
        final SFlowNodeDefinition sFlowNodeDefinition = sProcessDefinition.getProcessContainer().getFlowNode(child.getFlowNodeDefinitionId());
        final FlowNodeTransitionsWrapper transitionsDescriptor = transitionEvaluator.buildTransitionsWrapper(sFlowNodeDefinition, sProcessDefinition, child);
        final TokenProvider tokenProvider = new FlowNodeCompletionTokenProvider(child, sProcessInstance, sFlowNodeDefinition, transitionsDescriptor,
                tokenService);
        final FlowMerger merger = new FlowMerger(sFlowNodeDefinition, transitionsDescriptor, tokenProvider);

        archiveInvalidTransitions(child, transitionsDescriptor);

        final List<STransitionDefinition> chosenGatewaysTransitions = new ArrayList<STransitionDefinition>(transitionsDescriptor
                .getValidOutgoingTransitionDefinitions().size());
        final List<SFlowNodeDefinition> chosenFlowNode = new ArrayList<SFlowNodeDefinition>(transitionsDescriptor.getValidOutgoingTransitionDefinitions()
                .size());
        for (final STransitionDefinition sTransitionDefinition : transitionsDescriptor.getValidOutgoingTransitionDefinitions()) {
            final SFlowNodeDefinition flowNodeDefinition = processDefinitionService.getNextFlowNode(sProcessDefinition, sTransitionDefinition.getName());
            // we archive a transition to keep a track of where the flow was
            transitionService.archive(sTransitionDefinition, child, TransitionState.TAKEN);
            if (flowNodeDefinition instanceof SGatewayDefinition) {
                chosenGatewaysTransitions.add(sTransitionDefinition);
            } else {
                // Shortcut: event or activity, we execute them directly
                chosenFlowNode.add(flowNodeDefinition);
            }
        }

        archiveFlowNodeInstance(sProcessDefinition, child, sProcessInstance);

        final TokenInfo outputTokenInfo = merger.getOutputTokenInfo();
        // execute transition/activities
        createAndExecuteActivities(sProcessDefinition.getId(), child, sProcessInstance.getId(), chosenFlowNode, child.getRootProcessInstanceId(),
                outputTokenInfo.outputTokenRefId);
        for (final STransitionDefinition sTransitionDefinition : chosenGatewaysTransitions) {
            executeGateway(sProcessDefinition, sTransitionDefinition, child, outputTokenInfo.outputTokenRefId);
        }

        return updateTokens(sProcessDefinition, child, sProcessInstance, numberOfTokenToMerge, transitionsDescriptor, merger);
    }

    private void archiveFlowNodeInstance(final SProcessDefinition sProcessDefinition, final SFlowNodeInstance child, final SProcessInstance sProcessInstance)
            throws SArchivingException {
        if (child.getId() != sProcessInstance.getInterruptingEventId() || SFlowNodeType.SUB_PROCESS.equals(sProcessInstance.getCallerType())) {
            // Let's archive the final state of the child:
            flowNodeExecutor.archiveFlowNodeInstance(child, true, sProcessDefinition.getId());
        }
    }

    private int updateTokens(final SProcessDefinition sProcessDefinition, final SFlowNodeInstance child, final SProcessInstance sProcessInstance,
            final int numberOfTokenToMerge, final FlowNodeTransitionsWrapper transitionsDescriptor, final FlowMerger merger)
            throws SBonitaException {
        // handle token creation/deletion
        if (merger.mustConsumeInputTokenOnTakingTransition()) {
            tokenService.deleteTokens(sProcessInstance.getId(), child.getTokenRefId(), numberOfTokenToMerge);
        }
        if (merger.mustCreateTokenOnFinish()) {
            final TokenInfo outputTokenInfo = merger.getOutputTokenInfo();
            tokenService.createTokens(sProcessInstance.getId(), outputTokenInfo.outputTokenRefId, outputTokenInfo.outputParentTokenRefId, transitionsDescriptor
                    .getValidOutgoingTransitionDefinitions().size());
        }
        if (merger.isImplicitEnd()) {
            final Long tokenRefId = child.getTokenRefId();
            if (tokenRefId == null) {
                throw new SObjectNotFoundException("No token ref id set for " + child);
            }
            implicitEnd(sProcessDefinition, sProcessInstance.getId(), numberOfTokenToMerge, tokenRefId);
        }
        return tokenService.getNumberOfToken(sProcessInstance.getId());
    }

    private void archiveInvalidTransitions(final SFlowNodeInstance child, final FlowNodeTransitionsWrapper transitionsDescriptor)
            throws STransitionCreationException {
        for (final STransitionDefinition sTransitionDefinition : transitionsDescriptor.getAllOutgoingTransitionDefinitions()) {
            if (!transitionsDescriptor.getValidOutgoingTransitionDefinitions().contains(sTransitionDefinition)) {
                // Archive invalid transitions
                transitionService.archive(sTransitionDefinition, child, TransitionState.ABORTED);
            }
        }
    }

    private void implicitEnd(final SProcessDefinition processDefinition, final long processInstanceId, final int numberOfTokenToMerge, final Long tokenRefId)
            throws SBonitaException {
        final SToken token = tokenService.getToken(processInstanceId, tokenRefId);
        tokenService.deleteTokens(processInstanceId, tokenRefId, numberOfTokenToMerge);
        /*
         * if there is inclusive gateway we check if there is one that refers to this token
         * In this case the gateway should be notified in order to recalculate if it is now merged
         */
        if (processDefinition.getProcessContainer().containsInclusiveGateway()) {
            final SGatewayInstance gatewayInstance = gatewayInstanceService.getGatewayMergingToken(processInstanceId, tokenRefId);
            if (gatewayInstance != null && gatewayInstanceService.checkMergingCondition(processDefinition, gatewayInstance)) {
                gatewayInstanceService.setFinish(gatewayInstance);
                workService.registerWork(WorkFactory.createExecuteFlowNodeWork(processDefinition.getId(), processInstanceId, gatewayInstance.getId(), null,
                        null));
            }
        }
        // consume on token parent if there is one and if there is no other token having the same refId
        if (token.getParentRefId() != null && tokenService.getNumberOfToken(processInstanceId, tokenRefId) == 0) {
            implicitEnd(processDefinition, processInstanceId, 1, token.getParentRefId());
        }

    }

    @Override
    public SProcessInstance start(final long starterId, final long starterSubstituteId, final List<SOperation> operations, final Map<String, Object> context,
            final List<ConnectorDefinitionWithInputValues> connectorsWithInput, final FlowNodeSelector selector) throws SProcessInstanceCreationException {
        return start(starterId, starterSubstituteId, null, operations, context, connectorsWithInput, -1, selector);
    }

    @Override
    public SProcessInstance start(final long processDefinitionId, final long targetSFlowNodeDefinitionId, final long starterId, final long starterSubstituteId,
            final SExpressionContext expressionContext, final List<SOperation> operations, final Map<String, Object> context,
            final List<ConnectorDefinitionWithInputValues> connectorsWithInput, final long callerId, final long subProcessDefinitionId)
            throws SProcessInstanceCreationException {
        try {
            final SProcessDefinition sProcessDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
            final FlowNodeSelector selector = new FlowNodeSelector(sProcessDefinition, getFilter(targetSFlowNodeDefinitionId), subProcessDefinitionId);
            return start(starterId, starterSubstituteId, expressionContext, operations, context, connectorsWithInput, callerId, selector);
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new SProcessInstanceCreationException(e);
        } catch (final SProcessDefinitionReadException e) {
            throw new SProcessInstanceCreationException(e);
        }
    }

    private Filter<SFlowNodeDefinition> getFilter(final long targetSFlowNodeDefinitionId) {
        if (targetSFlowNodeDefinitionId == -1) {
            return new StartFlowNodeFilter();
        }
        return new FlowNodeIdFilter(targetSFlowNodeDefinitionId);
    }

    @Override
    public SProcessInstance start(final long starterId, final long starterSubstituteId, final SExpressionContext expressionContext,
            final List<SOperation> operations, final Map<String, Object> context, final List<ConnectorDefinitionWithInputValues> connectors,
            final long callerId, final FlowNodeSelector selector) throws SProcessInstanceCreationException {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            final SProcessDefinition sProcessDefinition = selector.getProcessDefinition();
            final ClassLoader localClassLoader = classLoaderService.getLocalClassLoader(ScopeType.PROCESS.name(), sProcessDefinition.getId());
            Thread.currentThread().setContextClassLoader(localClassLoader);
            // initialize the process classloader by getting it one time
            try {
                localClassLoader.loadClass(this.getClass().getName());
            } catch (final ClassNotFoundException e) {
                //ignore, just to load
            }
            final SProcessInstance sProcessInstance = createProcessInstance(sProcessDefinition, starterId, starterSubstituteId, callerId);

            final boolean isInitializing = initialize(starterId, sProcessDefinition, sProcessInstance, expressionContext,
                    operations != null ? new ArrayList<SOperation>(operations) : null, context, selector.getContainer(), connectors,
                    selector);
            try {
                handleEventSubProcess(sProcessDefinition, sProcessInstance, selector.getSubProcessDefinitionId());
            } catch (final SProcessInstanceCreationException e) {
                throw e;
            } catch (final SBonitaException e) {
                setExceptionContext(sProcessDefinition, sProcessInstance, e);
                throw new SProcessInstanceCreationException("Unable to register events for event sub process in process.", e);
            }

            if (isInitializing) {
                // some connectors were trigger
                processInstanceService.setState(sProcessInstance, ProcessInstanceState.INITIALIZING);
                // we stop execution here
                return sProcessInstance;
            }
            return startElements(sProcessInstance, selector);
        } catch (final BonitaException e) {
            throw new SProcessInstanceCreationException(e);
        } catch (final IOException e) {
            throw new SProcessInstanceCreationException(e);
        } catch (final SProcessInstanceCreationException e) {
            throw e;
        } catch (final SBonitaException e) {
            throw new SProcessInstanceCreationException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    /*
     * Execute connectors then execute output operation and disconnect connectors
     */
    protected void executeConnectors(final SProcessDefinition processDefinition, final SProcessInstance sProcessInstance,
            final List<ConnectorDefinitionWithInputValues> connectorsList) throws InvalidEvaluationConnectorConditionException, SConnectorException {
        final SExpressionContext expcontext = new SExpressionContext();
        expcontext.setProcessDefinitionId(processDefinition.getId());
        expcontext.setProcessDefinition(processDefinition);
        expcontext.setContainerId(sProcessInstance.getId());
        expcontext.setContainerType(DataInstanceContainer.PROCESS_INSTANCE.name());
        for (final ConnectorDefinitionWithInputValues connectorWithInput : connectorsList) {
            final ConnectorDefinition connectorDefinition = connectorWithInput.getConnectorDefinition();
            final Map<String, Map<String, Serializable>> contextInputValues = connectorWithInput.getInputValues();
            final String connectorId = connectorDefinition.getConnectorId();
            final String version = connectorDefinition.getVersion();
            final Map<String, Expression> inputs = connectorDefinition.getInputs();
            if (contextInputValues.size() != inputs.size()) {
                throw new InvalidEvaluationConnectorConditionException(contextInputValues.size(), inputs.size());
            }
            final Map<String, SExpression> connectorsExps = ModelConvertor.constructExpressions(inputs);

            // we use the context classloader because the process classloader is already set
            final ConnectorResult result = connectorService.executeMutipleEvaluation(processDefinition.getId(), connectorId, version, connectorsExps,
                    contextInputValues, Thread.currentThread().getContextClassLoader(), expcontext);
            final List<Operation> outputs = connectorDefinition.getOutputs();
            connectorService.executeOutputOperation(ModelConvertor.convertOperations(outputs), expcontext, result);
        }
    }

    protected void handleEventSubProcess(final SProcessDefinition sProcessDefinition, final SProcessInstance sProcessInstance, final long subProcessDefinitionId)
            throws SBonitaException {
        if (subProcessDefinitionId == -1) {
            // modify that to support event sub-processes within sub-processes
            eventsHandler.handleEventSubProcess(sProcessDefinition, sProcessInstance);
        }
    }

    @Override
    public SProcessInstance startElements(final SProcessInstance sProcessInstance, final FlowNodeSelector selector) throws SProcessInstanceCreationException,
            SFlowNodeExecutionException {
        final List<SFlowNodeInstance> flowNodeInstances = initializeFirstExecutableElements(sProcessInstance, selector);
        // process is initialized and now the engine trigger jobs to execute other activities, give the hand back
        ProcessInstanceState state;
        final int size = flowNodeInstances.size();
        if (size == 0) {
            state = ProcessInstanceState.COMPLETED;
        } else {
            state = ProcessInstanceState.STARTED;
        }
        try {
            tokenService.createTokens(sProcessInstance.getId(), sProcessInstance.getProcessDefinitionId(), null, size);
            processInstanceService.setState(sProcessInstance, state);
        } catch (final SBonitaException e) {
            throw new SProcessInstanceCreationException("Unable to set the state on the process.", e);
        }
        for (final SFlowNodeInstance sFlowNodeInstance : flowNodeInstances) {
            try {
                workService.registerWork(WorkFactory.createExecuteFlowNodeWork(sProcessInstance.getProcessDefinitionId(), sProcessInstance.getId(),
                        sFlowNodeInstance.getId(), null, null));
            } catch (final SWorkRegisterException e) {
                setExceptionContext(sProcessInstance, sFlowNodeInstance, e);
                throw new SFlowNodeExecutionException("Unable to trigger execution of the flow node.", e);
            }
        }
        return sProcessInstance;
    }

    @Override
    public String getHandledType() {
        return SFlowElementsContainerType.PROCESS.name();
    }

    private void createAndExecuteActivities(final Long processDefinitionId, final SFlowNodeInstance flowNodeInstance, final long parentProcessInstanceId,
            final List<SFlowNodeDefinition> choosenActivityDefinitions, final long rootProcessInstanceId, final Long tokenRefId) throws SBonitaException {
        final SProcessInstance parentProcessInstance = processInstanceService.getProcessInstance(parentProcessInstanceId);
        final SStateCategory stateCategory = parentProcessInstance.getStateCategory();

        // Create Activities
        final List<SFlowNodeInstance> sFlowNodeInstances = bpmInstancesCreator.createFlowNodeInstances(processDefinitionId,
                flowNodeInstance.getRootContainerId(), flowNodeInstance.getParentContainerId(), choosenActivityDefinitions, rootProcessInstanceId,
                parentProcessInstanceId, stateCategory, tokenRefId);

        // Execute Activities
        for (final SFlowNodeInstance sFlowNodeInstance : sFlowNodeInstances) {
            workService
                    .registerWork(WorkFactory.createExecuteFlowNodeWork(processDefinitionId, parentProcessInstanceId, sFlowNodeInstance.getId(), null, null));
        }
    }

    private void setExceptionContext(final SProcessDefinition sProcessDefinition, final SFlowNodeInstance sFlowNodeInstance, final SBonitaException e) {
        setExceptionContext(sProcessDefinition, e);
        e.setProcessInstanceIdOnContext(sFlowNodeInstance.getParentProcessInstanceId());
        e.setRootProcessInstanceIdOnContext(sFlowNodeInstance.getRootProcessInstanceId());
        setExceptionContext(sFlowNodeInstance, e);
    }

    private void setExceptionContext(final SProcessDefinition sProcessDefinition, final SProcessInstance sProcessInstance, final SBonitaException e) {
        setExceptionContext(sProcessDefinition, e);
        setExceptionContext(sProcessInstance, e);
    }

    private void setExceptionContext(final SProcessInstance sProcessInstance, final SFlowNodeInstance sFlowNodeInstance, final SBonitaException e) {
        e.setProcessDefinitionIdOnContext(sProcessInstance.getProcessDefinitionId());
        e.setProcessDefinitionNameOnContext(sProcessInstance.getName());
        setExceptionContext(sProcessInstance, e);
        setExceptionContext(sFlowNodeInstance, e);
    }

    private void setExceptionContext(final SProcessDefinition sProcessDefinition, final SBonitaException e) {
        e.setProcessDefinitionIdOnContext(sProcessDefinition.getId());
        e.setProcessDefinitionNameOnContext(sProcessDefinition.getName());
        e.setProcessDefinitionVersionOnContext(sProcessDefinition.getVersion());
    }

    private void setExceptionContext(final SProcessInstance sProcessInstance, final SBonitaException e) {
        e.setProcessInstanceIdOnContext(sProcessInstance.getId());
        e.setRootProcessInstanceIdOnContext(sProcessInstance.getRootProcessInstanceId());
    }

    private void setExceptionContext(final SFlowNodeInstance sFlowNodeInstance, final SBonitaException e) {
        e.setFlowNodeDefinitionIdOnContext(sFlowNodeInstance.getFlowNodeDefinitionId());
        e.setFlowNodeInstanceIdOnContext(sFlowNodeInstance.getId());
        e.setFlowNodeNameOnContext(sFlowNodeInstance.getName());
    }
}
