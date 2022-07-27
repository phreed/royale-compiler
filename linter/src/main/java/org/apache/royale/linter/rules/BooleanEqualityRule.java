////////////////////////////////////////////////////////////////////////////////
//
//  Licensed to the Apache Software Foundation (ASF) under one or more
//  contributor license agreements.  See the NOTICE file distributed with
//  this work for additional information regarding copyright ownership.
//  The ASF licenses this file to You under the Apache License, Version 2.0
//  (the "License"); you may not use this file except in compliance with
//  the License.  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////

package org.apache.royale.linter.rules;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.royale.compiler.problems.CompilerProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IBinaryOperatorNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.NodeVisitor;
import org.apache.royale.linter.TokenQuery;

/**
 * Checks for redundant equality comparisons with 'true' and 'false' boolean
 * literals using the '==' and '!=' operators.
 * 
 * Does not check for strict equality using the '===' and '!==' operators
 * because these operators do not type coerce the two sides to determine if they
 * are 'truthy' or 'falsy'.
 */
public class BooleanEqualityRule extends LinterRule {
	@Override
	public Map<ASTNodeID, NodeVisitor> getNodeVisitors() {
		Map<ASTNodeID, NodeVisitor> result = new HashMap<>();
		result.put(ASTNodeID.Op_EqualID, (node, tokenQuery, problems) -> {
			checkBinaryOperatorNode((IBinaryOperatorNode) node, tokenQuery, problems);
		});
		result.put(ASTNodeID.Op_NotEqualID, (node, tokenQuery, problems) -> {
			checkBinaryOperatorNode((IBinaryOperatorNode) node, tokenQuery, problems);
		});
		return result;
	}

	private void checkBinaryOperatorNode(IBinaryOperatorNode operatorNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		IExpressionNode leftOperandNode = operatorNode.getLeftOperandNode();
		if (ASTNodeID.LiteralBooleanID.equals(leftOperandNode.getNodeID())) {
			problems.add(new BooleanEqualityLinterProblem(leftOperandNode));
		}
		IExpressionNode rightOperandNode = operatorNode.getRightOperandNode();
		if (ASTNodeID.LiteralBooleanID.equals(rightOperandNode.getNodeID())) {
			problems.add(new BooleanEqualityLinterProblem(rightOperandNode));
		}
	}

	public static class BooleanEqualityLinterProblem extends CompilerProblem {
		public static final String DESCRIPTION = "Must simplify statement to remove redundant comparison with true or false";

		public BooleanEqualityLinterProblem(IExpressionNode node)
		{
			super(node);
		}
	}
}
