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
package org.bonitasoft.engine.command.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.LeftOperand;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

/**
 * @author Emmanuel Duchastenier
 */
public class GetUpdatedVariableValuesForProcessInstanceIT extends TestWithTechnicalUser {

    @SuppressWarnings("unchecked")
    @Cover(classes = CommandAPI.class, concept = BPMNConcept.PROCESS, keywords = { "Command", "Updated variable value", "Process instance" }, story = "Get updated variable values for process instance.", jira = "")
    @Test
    public void testGetUpdatedVariableValuesForProcessInstance() throws Exception {
        final User user = createUser("toto", "titi");
        // create process definition:
        final String dataName1 = "data1";
        final String dataName2 = "data2";
        final int dataValue = 1;
        final String actorName = "actor";
        // Process Def level default data values are: data1=1, data2=1
        final UserTaskDefinitionBuilder taskDefBuilder = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0").addActor(actorName)
                .addIntegerData(dataName1, new ExpressionBuilder().createConstantIntegerExpression(dataValue))
                .addIntegerData(dataName2, new ExpressionBuilder().createConstantIntegerExpression(dataValue)).addUserTask("step1", actorName);
        // a data in step1 with same name 'data1' is also defined with value 11:
        taskDefBuilder.addIntegerData(dataName1, new ExpressionBuilder().createConstantIntegerExpression(11));
        final DesignProcessDefinition processDef = taskDefBuilder.addUserTask("step2", actorName).addTransition("step1", "step2").getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, actorName, user);

        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance pi = getProcessAPI().startProcess(processDefinitionId);
        final long activityInstanceId = waitForUserTask("step1", pi).getId();

        // Let's update the value of data1 to 22. It should not be taken into account at process def level:
        getProcessAPI().updateActivityDataInstance(dataName1, activityInstanceId, 22);

        // create current variable Name&Value map: var1 = 12, var2 = ArrayList{"a", "b"}
        final String varName1 = "var1";
        final String varName2 = "var2";
        final ArrayList<String> var2Value = new ArrayList<String>();
        var2Value.add("a");
        var2Value.add("b");
        final Map<String, Serializable> currentVariables = new HashMap<String, Serializable>();
        currentVariables.put(varName1, 12);
        currentVariables.put(varName2, var2Value);

        // Create Operation keyed map:
        final List<Operation> operations = new ArrayList<Operation>();
        // First one is 'var1 = data1 + 33'
        final Expression dependencyData1 = new ExpressionBuilder().createDataExpression(dataName1, Integer.class.getName());
        final Expression expression1 = new ExpressionBuilder().createNewInstance("data1 + 33").setContent("data1 + 33")
                .setDependencies(Arrays.asList(dependencyData1)).setExpressionType(ExpressionType.TYPE_READ_ONLY_SCRIPT).setInterpreter("GROOVY")
                .setReturnType(Integer.class.getName()).done();
        final Operation integerOperation1 = new OperationBuilder().createNewInstance()
                .setLeftOperand(new LeftOperandBuilder().createNewInstance().setName(varName1).setType(LeftOperand.TYPE_EXTERNAL_DATA).done())
                .setType(OperatorType.ASSIGNMENT)
                .setOperator("=").setRightOperand(expression1).done();
        final Map<String, Serializable> contexts = new HashMap<String, Serializable>();
        operations.add(integerOperation1);

        // Second one is 'var2.add("toto" + data1)'
        final Expression expression2 = new ExpressionBuilder().createNewInstance("concat 'toto' to data1 value").setContent("\"toto\" + data1")
                .setDependencies(Arrays.asList(dependencyData1)).setExpressionType(ExpressionType.TYPE_READ_ONLY_SCRIPT).setInterpreter("GROOVY")
                .setReturnType(String.class.getName()).done();
        final Operation integerOperation3 = new OperationBuilder().createNewInstance().setLeftOperand(varName2, true).setType(OperatorType.JAVA_METHOD)
                .setOperator("add").setOperatorInputType(Object.class.getName()).setRightOperand(expression2).done();
        operations.add(integerOperation3);

        // execute command:
        final String commandName = "getUpdatedVariableValuesForProcessInstance";
        final HashMap<String, Serializable> commandParameters = new HashMap<String, Serializable>();
        commandParameters.put("OPERATIONS_LIST_KEY", (Serializable) operations);
        commandParameters.put("OPERATIONS_INPUT_KEY", (Serializable) contexts);
        commandParameters.put("CURRENT_VARIABLE_VALUES_MAP_KEY", (Serializable) currentVariables);
        commandParameters.put("PROCESS_INSTANCE_ID_KEY", pi.getId());
        final Map<String, Serializable> updatedVariable = (Map<String, Serializable>) getCommandAPI().execute(commandName, commandParameters);
        // check and do assert:
        assertTrue(updatedVariable.size() == 2);
        assertEquals(34, updatedVariable.get(varName1));
        assertEquals(3, ((ArrayList<String>) updatedVariable.get(varName2)).size());
        assertEquals("toto1", ((ArrayList<String>) updatedVariable.get(varName2)).get(2));

        disableAndDeleteProcess(processDefinition);
        deleteUser(user);
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.PROCESS, keywords = { "Command", "Updated variable value", "Process instance", "Wrong parameter" }, story = "Execute get updated variable values for process instance command with wrong parameter", jira = "ENGINE-586")
    @Test(expected = CommandParameterizationException.class)
    public void testGetUpdatedVariableValuesForProcessInstanceCommandWithWrongParameter() throws Exception {

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("BAD_KEY", "bad_value");

        getCommandAPI().execute("getUpdatedVariableValuesForProcessInstance", parameters);
    }

}
