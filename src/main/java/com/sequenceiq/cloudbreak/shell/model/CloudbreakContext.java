/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sequenceiq.cloudbreak.shell.model;

import org.springframework.stereotype.Component;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Holds information about the connected Cloudbreak server.
 */
@Component
public class CloudbreakContext {

    private Focus focus;
    private Hints hint;
    private ListMultimap<PropertyKey, String> properties = LinkedListMultimap.create();

    public CloudbreakContext() {
        this.focus = getRootFocus();
        this.hint = Hints.NONE;
    }

    public boolean isStackAvailable() {
        return isPropertyAvailable(PropertyKey.STACK_ID);
    }

    public void addStack(String id) {
        addProperty(PropertyKey.STACK_ID, id);
    }

    public boolean isTemplateAvailable() {
        return isPropertyAvailable(PropertyKey.TEMPLATE_ID);
    }

    public void addTemplate(String id) {
        addProperty(PropertyKey.TEMPLATE_ID, id);
    }

    public boolean isBlueprintAvailable() {
        return isPropertyAvailable(PropertyKey.BLUEPRINT_ID);
    }

    public void addBlueprint(String id) {
        addProperty(PropertyKey.BLUEPRINT_ID, id);
    }

    public boolean isCredentialAvailable() {
        return isPropertyAvailable(PropertyKey.CREDENTIAL_ID);
    }

    public void setCredential(String id) {
        addProperty(PropertyKey.CREDENTIAL_ID, id);
    }

    public String getStackId() {
        return getFirstPropertyValue(PropertyKey.STACK_ID);
    }

    public String getTemplateId() {
        return getFirstPropertyValue(PropertyKey.TEMPLATE_ID);
    }

    public String getBlueprintId() {
        return getFirstPropertyValue(PropertyKey.BLUEPRINT_ID);
    }

    public String getCredentialId() {
        return getFirstPropertyValue(PropertyKey.CREDENTIAL_ID);
    }

    /**
     * Sets the focus to the root.
     */
    public void resetFocus() {
        this.focus = getRootFocus();
    }

    /**
     * Sets the focus.
     *
     * @param id   target of the focus
     * @param type type of the focus
     */
    public void setFocus(String id, FocusType type) {
        this.focus = new Focus(id, type);
    }

    /**
     * Returns the target of the focus.
     *
     * @return target
     */
    public String getFocusValue() {
        return focus.getValue();
    }

    /**
     * Sets what should be the next hint message.
     *
     * @param hint the new message
     */
    public void setHint(Hints hint) {
        this.hint = hint;
    }

    /**
     * Returns the context sensitive prompt.
     *
     * @return text of the prompt
     */
    public String getPrompt() {
        return focus.isType(FocusType.ROOT) ? "cloudbreak-shell>" : formatPrompt(focus.getPrefix(), focus.getValue());
    }

    /**
     * Returns some context sensitive hint.
     *
     * @return hint
     */
    public String getHint() {
        return "Hint: " + hint.message();
    }

    private boolean isFocusOn(FocusType type) {
        return focus.isType(type);
    }

    private Focus getRootFocus() {
        return new Focus("root", FocusType.ROOT);
    }

    private String formatPrompt(String prefix, String postfix) {
        return String.format("%s:%s>", prefix, postfix);
    }

    private boolean isPropertyAvailable(PropertyKey key) {
        return !properties.get(key).isEmpty();
    }

    private void addProperty(PropertyKey key, String value) {
        properties.put(key, value);
    }

    private String getFirstPropertyValue(PropertyKey key) {
        return properties.get(key).get(0);
    }

    private enum PropertyKey {
        CREDENTIAL_ID, BLUEPRINT_ID, TEMPLATE_ID, STACK_ID
    }
}
