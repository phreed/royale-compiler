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

package org.apache.flex.compiler.internal.codegen.js.jx;

import java.util.ArrayList;
import java.util.Stack;

import org.apache.flex.compiler.codegen.ISubEmitter;
import org.apache.flex.compiler.codegen.js.IJSEmitter;
import org.apache.flex.compiler.common.IMetaInfo;
import org.apache.flex.compiler.common.Multiname;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitterTokens;
import org.apache.flex.compiler.internal.tree.as.LiteralNode;
import org.apache.flex.compiler.internal.tree.as.RegExpLiteralNode;
import org.apache.flex.compiler.internal.tree.as.XMLLiteralNode;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IImportNode;
import org.apache.flex.compiler.tree.as.ILiteralNode;
import org.apache.flex.compiler.tree.as.ILiteralNode.LiteralType;
import org.apache.flex.compiler.tree.as.IScopedNode;

public class LiteralEmitter extends JSSubEmitter implements
        ISubEmitter<ILiteralNode>
{

    public LiteralEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(ILiteralNode node)
    {
        boolean isWritten = false;

        String newlineReplacement = "\\\\n";
        String s = node.getValue(true);
        if (!(node instanceof RegExpLiteralNode))
        {
            if (node.getLiteralType() == LiteralType.XML)
            {
                boolean jsx = false;
                IFunctionNode functionNode = (IFunctionNode) node
                        .getAncestorOfType(IFunctionNode.class);
                if (functionNode != null)
                {
                    IMetaInfo[] metaInfos = functionNode.getMetaInfos();
                    for (IMetaInfo metaInfo : metaInfos)
                    {
                        if (metaInfo.getTagName().equals(JSFlexJSEmitterTokens.JSX.getToken()))
                        {
                            jsx = true;
                            break;
                        }
                    }
                }
                XMLLiteralNode xmlNode = (XMLLiteralNode) node;
                if (jsx)
                {
                    emitJSX(xmlNode);
                    return;
                }
                else
                {
                    newlineReplacement = "\\\\\n";
                    if (xmlNode.getContentsNode().getChildCount() == 1)
                    {
                        if (s.contains("'"))
                            s = "\"" + s + "\"";
                        else
                            s = "'" + s + "'";
                    }
                    else
                    {
                        StringBuilder sb = new StringBuilder();
                        // probably contains {initializers}
                        boolean inAttribute = false;
                        int n = xmlNode.getContentsNode().getChildCount();
                        for (int i = 0; i < n; i++)
                        {
                            if (i > 0)
                                sb.append(" + ");
                            IASNode child = xmlNode.getContentsNode().getChild(i);
                            if (child instanceof LiteralNode)
                            {
                                s = ((LiteralNode)child).getValue(true);
                                if (s.contains("'"))
                                    sb.append("\"" + s + "\"");
                                else
                                    sb.append("'" + s + "'");
                            }
                            else
                            {
                                s = getEmitter().stringifyNode(child);
                                if (inAttribute)
                                {
                                    sb.append("'\"' + ");
    
                                    sb.append(s);
                                    
                                    sb.append(" + '\"'");
                                }
                                else
                                    sb.append(s);
                            }
                            inAttribute = s.endsWith("=");
                        }
                        s = sb.toString();
                    }
                    char c = s.charAt(0);
                    if (c == '"')
                    {
                        s = s.substring(1, s.length() - 1);
                        s = s.replace("\"", "\\\"");
                        s = "\"" + s + "\"";
                    }
                    s = "new XML( " + s + ")";
                }
            }
            s = s.replaceAll("\n", "__NEWLINE_PLACEHOLDER__");
            s = s.replaceAll("\r", "__CR_PLACEHOLDER__");
            s = s.replaceAll("\t", "__TAB_PLACEHOLDER__");
            s = s.replaceAll("\f", "__FORMFEED_PLACEHOLDER__");
            s = s.replaceAll("\b", "__BACKSPACE_PLACEHOLDER__");
            s = s.replaceAll("\\\\", "__ESCAPE_PLACEHOLDER__");
            s = s.replaceAll("\\\\\"", "__QUOTE_PLACEHOLDER__");
            //s = "\'" + s.replaceAll("\'", "\\\\\'") + "\'";
            s = s.replaceAll("__QUOTE_PLACEHOLDER__", "\\\\\"");
            s = s.replaceAll("__ESCAPE_PLACEHOLDER__", "\\\\\\\\");
            s = s.replaceAll("__BACKSPACE_PLACEHOLDER__", "\\\\b");
            s = s.replaceAll("__FORMFEED_PLACEHOLDER__", "\\\\f");
            s = s.replaceAll("__TAB_PLACEHOLDER__", "\\\\t");
            s = s.replaceAll("__CR_PLACEHOLDER__", "\\\\r");
            s = s.replaceAll("__NEWLINE_PLACEHOLDER__", newlineReplacement);
            if (node.getLiteralType() == LiteralType.STRING)
            {
                char c = s.charAt(0);
                if (c == '"')
                {
                    s = s.substring(1, s.length() - 1);
                    s = s.replace("\"", "\\\"");
                    s = "\"" + s + "\"";
                }
                else if (c == '\'')
                {
                    s = s.substring(1, s.length() - 1);
                    s = s.replace("'", "\\'");
                    s = "'" + s + "'";
                }
                s = s.replace("\u2028", "\\u2028");
                s = s.replace("\u2029", "\\u2029");
            }

        }

        if (!isWritten)
        {
            startMapping(node);
            write(s);
            endMapping(node);
        }
    }

    private void emitJSX(XMLLiteralNode node)
    {
        int childCount = node.getContentsNode().getChildCount();
        Stack<String> elementStack = new Stack<String>();
        String elementName = null;
        boolean endsWithAttribute = false;
        for (int i = 0; i < childCount; i++)
        {
            IASNode child = node.getContentsNode().getChild(i);
            if (child instanceof ILiteralNode)
            {
                ILiteralNode literalChild = (ILiteralNode) child;
                if (literalChild.getLiteralType() != LiteralType.XML)
                {
                    //inside {} syntax. emit normally.
                    getEmitter().getWalker().walk(literalChild);
                    continue;
                }
                String value = literalChild.getValue(true);
                value = value.replaceAll(ASEmitterTokens.NEW_LINE.getToken(), "");
                value = value.trim();
                while (value.length() > 0)
                {
                    int nextTagStartIndex = value.indexOf("<");
                    int nextTagEndIndex = value.indexOf(">");
                    boolean selfClosing = false;
                    boolean startsWithAttribute = false;
                    if (nextTagEndIndex > 0
                            && value.charAt(nextTagEndIndex - 1) == '/')
                    {
                        selfClosing = true;
                    }
                    if (endsWithAttribute)
                    {
                        //we'll fall back into attribute parsing below
                        endsWithAttribute = false;
                        startsWithAttribute = true;
                        elementName = elementStack.peek();
                    }
                    else if (nextTagStartIndex == 0)
                    {
                        //assume that the name ends at the end of the open tag
                        int endNameIndex = nextTagEndIndex;
                        if (endNameIndex == -1)
                        {
                            //literal ends with an attribute that uses {} syntax
                            endNameIndex = value.length() - 1;
                        }
                        int attributeIndex = value.indexOf(" ");
                        if (attributeIndex > 0 && attributeIndex < endNameIndex)
                        {
                            //if there are attributes, the name does not end at
                            //the end of the open tag
                            endNameIndex = attributeIndex;
                        }
                        elementName = value.substring(1, endNameIndex);
                        if (elementName.endsWith("/"))
                        {
                            elementName = elementName.substring(0, elementName.length() - 1);
                        }
                        if (elementName.startsWith("/"))
                        {
                            //the close tag of the current element
                            elementName = elementName.substring(1);
                            elementName = getQualifiedElementName(elementName, node);
                            if (elementStack.size() > 0)
                            {
                                indentPop();
                            }
                            write(ASEmitterTokens.PAREN_CLOSE);
                            String topOfStack = elementStack.pop();
                            assert topOfStack.equals(elementName);
                            value = value.substring(nextTagEndIndex + 1);
                            value = value.trim();
                            continue;
                        }
                        else
                        {
                            //the open tag of a new element
                            if (elementStack.size() > 0)
                            {
                                indentPush();
                                writeNewline(ASEmitterTokens.COMMA);
                            }
                            elementName = getQualifiedElementName(elementName, node);
                            elementStack.push(elementName);
                            write("React.createElement");
                            write(ASEmitterTokens.PAREN_OPEN);
                            write(elementName);
                            value = value.substring(endNameIndex);
                            value = value.trim();
                            //we changed the string, so find it again
                            nextTagEndIndex = value.indexOf(">");
                        }
                    }
                    else
                    {
                        //we're inside an element's open and closing tags
                        String elementText = value.substring(0, nextTagStartIndex);
                        writeToken(ASEmitterTokens.COMMA);
                        emitJSXText(elementText);
                        value = value.substring(nextTagStartIndex);
                        continue;
                    }
                    //parse the tag's attributes
                    if (nextTagEndIndex == -1)
                    {
                        //literal ends with an attribute that uses {} syntax
                        endsWithAttribute = true;
                        nextTagEndIndex = value.length() - 1;
                    }
                    int attributesEndIndex = (selfClosing && nextTagEndIndex > 0) ? nextTagEndIndex - 1 : nextTagEndIndex;
                    String attributes = value.substring(0, attributesEndIndex);
                    emitJSXAttributes(attributes, startsWithAttribute, endsWithAttribute);
                    if (selfClosing)
                    {
                        //end of open tag, including attributes
                        write(ASEmitterTokens.PAREN_CLOSE);
                        if (elementStack.size() > 0)
                        {
                            indentPop();
                        }
                        String topOfStack = elementStack.pop();
                        assert topOfStack.equals(elementName);
                    }
                    value = value.substring(nextTagEndIndex + 1);
                    value = value.trim();
                }
            }
            else
            {
                if (!endsWithAttribute)
                {
                    writeToken(ASEmitterTokens.COMMA);
                }
                //not a literal, and inside {} syntax. emit normally.
                getEmitter().getWalker().walk(child);
            }
        }
    }

    private void emitJSXAttributes(String value, boolean startsWithAttribute, boolean endsWithAttribute)
    {
        int attributeCount = 0;
        while (true)
        {
            int charCount = value.length();
            if (charCount == 0)
            {
                break;
            }
            int endAttributeNameIndex = value.indexOf("=");
            if (endAttributeNameIndex == -1)
            {
                endAttributeNameIndex = value.length();
            }
            String attributeName = value.substring(0, endAttributeNameIndex);
            writeToken(ASEmitterTokens.COMMA);
            if (!startsWithAttribute && attributeCount == 0)
            {
                writeToken(ASEmitterTokens.BLOCK_OPEN);
            }
            if (attributeName.indexOf('-') >= 0)
            {
                emitJSXText(attributeName);
            }
            else
            {
                write(attributeName);
            }
            writeToken(ASEmitterTokens.COLON);
            attributeCount++;
            if ((endAttributeNameIndex + 1) >= charCount)
            {
                //literal ends with an attribute that uses {} syntax
                break;
            }
            int quoteChar = value.charAt(endAttributeNameIndex + 1);
            int startAttributeValueIndex = endAttributeNameIndex + 2;
            if (startAttributeValueIndex > charCount)
            {
                startAttributeValueIndex = charCount;
            }
            int endAttributeValueIndex = value.indexOf(quoteChar, startAttributeValueIndex);
            if (endAttributeValueIndex == -1 || endAttributeValueIndex > charCount)
            {
                endAttributeValueIndex = charCount;
            }
            if (endAttributeValueIndex > 0)
            {
                String attributeValue = value.substring(startAttributeValueIndex, endAttributeValueIndex);
                emitJSXText(attributeValue);
                value = value.substring(endAttributeValueIndex + 1);
                value = value.trim();
            }
        }
        if (!endsWithAttribute)
        {
            if (!startsWithAttribute && attributeCount == 0)
            {
                writeToken(ASEmitterTokens.COMMA);
                write(ASEmitterTokens.NULL);
            }
            else
            {
                write(ASEmitterTokens.SPACE);
                write(ASEmitterTokens.BLOCK_CLOSE);
            }
        }
    }

    private void emitJSXText(String elementText)
    {
        write(ASEmitterTokens.SINGLE_QUOTE);
        elementText = elementText.replaceAll("'", "\\\\\'");
        write(elementText);
        write(ASEmitterTokens.SINGLE_QUOTE);
    }

    private String getQualifiedElementName(String elementName, IASNode node)
    {
        String firstChar = elementName.substring(0, 1);
        boolean isHTMLTag = firstChar.toLowerCase().equals(firstChar);
        if (isHTMLTag)
        {
            return ASEmitterTokens.SINGLE_QUOTE.getToken() + elementName + ASEmitterTokens.SINGLE_QUOTE.getToken();
        }
        ArrayList<IImportNode> importNodes = new ArrayList<IImportNode>();
        IScopedNode scopedNode = node.getContainingScope();
        scopedNode.getAllImportNodes(importNodes);
        for (IImportNode importNode : importNodes)
        {
            if (importNode.isWildcardImport())
            {
                continue;
            }
            String importName = importNode.getImportName();
            String importAlias = importNode.getImportAlias();
            if (importAlias != null && importAlias.equals(elementName))
            {
                return importName;
            }
            String baseName = Multiname.getBaseNameForQName(importName);
            if (baseName.equals(elementName))
            {
                return importName;
            }
        }
        return elementName;
    }
}
