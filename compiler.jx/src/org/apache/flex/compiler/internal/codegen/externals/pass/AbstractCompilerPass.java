/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.flex.compiler.internal.codegen.externals.pass;

import org.apache.flex.compiler.internal.codegen.externals.reference.ReferenceModel;

import com.google.javascript.jscomp.AbstractCompiler;
import com.google.javascript.jscomp.CompilerPass;
import com.google.javascript.jscomp.NodeTraversal;
import com.google.javascript.jscomp.NodeTraversal.Callback;
import com.google.javascript.rhino.Node;

public abstract class AbstractCompilerPass implements CompilerPass, Callback
{
    protected ReferenceModel model;
    protected AbstractCompiler compiler;

    public AbstractCompilerPass(ReferenceModel model, AbstractCompiler compiler)
    {
        this.model = model;
        this.compiler = compiler;
    }

    @Override
    public void process(Node externs, Node root)
    {
        //NodeTraversal.traverse(compiler, root, this);
        NodeTraversal.traverseRoots(compiler, this, externs, root);
    }

    protected void log(String message)
    {
        System.out.println(message);
    }

    protected void err(String message)
    {
        System.err.println(message);
    }
}
