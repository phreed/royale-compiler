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
package org.apache.flex.compiler.internal.projects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.driver.as.ASBackend;
import org.apache.flex.compiler.internal.workspaces.Workspace;

/**
 * @author aharui
 *
 */
public class FlexJSASDocProject extends FlexJSProject
{

    /**
     * Constructor
     *
     * @param workspace The {@code Workspace} containing this project.
     */
    public FlexJSASDocProject(Workspace workspace, IBackend backend)
    {
        super(workspace, backend);
    }
    
    public class ASDocRecord
    {
    	public IDefinition definition;
    	public String description;
    }
    
    public Map<String, List<ASDocRecord>> index = new HashMap<String, List<ASDocRecord>>();
    public Map<String, ASDocRecord> classes = new HashMap<String, ASDocRecord>();
    public List<String> tags = new ArrayList<String>();
}
