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

/**
 * Holds information about the connected Cloudbreak server.
 */
@Component
public class CloudbreakContext {

    private Focus focus;
    private Hints hint;
    private boolean credentialAvailable = false;
    private boolean blueprintAvailable = false;
    private boolean templateAvailable = false;
    private boolean stackAvailable = false;

    public CloudbreakContext() {
        this.focus = getRootFocus();
        this.hint = Hints.NONE;
    }

    public boolean isStackAvailable() {
        return stackAvailable;
    }

    public void setStackAvailable(boolean stackAvailable) {
        this.stackAvailable = stackAvailable;
    }

    public boolean isTemplateAvailable() {
        return templateAvailable;
    }

    public void setTemplateAvailable(boolean templateAvailable) {
        this.templateAvailable = templateAvailable;
    }

    public boolean isBlueprintAvailable() {
        return blueprintAvailable;
    }

    public void setBlueprintAvailable(boolean blueprintAvailable) {
        this.blueprintAvailable = blueprintAvailable;
    }

    public boolean isCredentialAvailable() {
        return credentialAvailable;
    }

    public void setCredentialAvailable(boolean credentialAvailable) {
        this.credentialAvailable = credentialAvailable;
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
}
