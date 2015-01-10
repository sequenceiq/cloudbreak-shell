package com.sequenceiq.cloudbreak.shell.model;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Holds information about the connected Cloudbreak server.
 */
@Component
public class CloudbreakContext {

    private static final String ACCESSIBLE = "accessible";
    private Focus focus;
    private Hints hint;
    private Map<PropertyKey, String> properties = new HashMap<>();

    public CloudbreakContext() {
        this.focus = getRootFocus();
        this.hint = Hints.NONE;
    }

    public boolean isStackAvailable() {
        return isPropertyAvailable(PropertyKey.STACK_ID);
    }

    public void addStack(String id, String name) {
        addProperty(PropertyKey.STACK_ID, id);
        addProperty(PropertyKey.STACK_NAME, name);
        setStackAccessible();
    }

    public void removeStack(String id) {
        removeProperty(PropertyKey.STACK_ID, id);
    }

    public boolean isTemplateAvailable() {
        return isPropertyAvailable(PropertyKey.TEMPLATE_ID);
    }

    public void addTemplate(String id) {
        addProperty(PropertyKey.TEMPLATE_ID, id);
        setTemplateAccessible();
    }

    public boolean isBlueprintAvailable() {
        return isPropertyAvailable(PropertyKey.BLUEPRINT_ID);
    }

    public void addBlueprint(String id) {
        addProperty(PropertyKey.BLUEPRINT_ID, id);
        setBlueprintAccessible();
    }

    public boolean isCredentialAvailable() {
        return isPropertyAvailable(PropertyKey.CREDENTIAL_ID);
    }

    public void setCredential(String id) {
        addProperty(PropertyKey.CREDENTIAL_ID, id);
        setCredentialAccessible();
    }

    public void setBlueprintAccessible() {
        addProperty(PropertyKey.BLUEPRINT_ACCESSIBLE, ACCESSIBLE);
    }

    public boolean isBlueprintAccessible() {
        return isPropertyAvailable(PropertyKey.BLUEPRINT_ACCESSIBLE);
    }

    public void setCredentialAccessible() {
        addProperty(PropertyKey.CREDENTIAL_ACCESSIBLE, ACCESSIBLE);
    }

    public boolean isCredentialAccessible() {
        return isPropertyAvailable(PropertyKey.CREDENTIAL_ACCESSIBLE);
    }

    public void setTemplateAccessible() {
        addProperty(PropertyKey.TEMPLATE_ACCESSIBLE, ACCESSIBLE);
    }

    public boolean isTemplateAccessible() {
        return isPropertyAvailable(PropertyKey.TEMPLATE_ACCESSIBLE);
    }

    public void setStackAccessible() {
        addProperty(PropertyKey.STACK_ACCESSIBLE, ACCESSIBLE);
    }

    public boolean isStackAccessible() {
        return isPropertyAvailable(PropertyKey.STACK_ACCESSIBLE);
    }

    public String getStackId() {
        return getLastPropertyValue(PropertyKey.STACK_ID);
    }

    public String getStackName() {
        return getLastPropertyValue(PropertyKey.STACK_NAME);
    }

    public String getTemplateId() {
        return getLastPropertyValue(PropertyKey.TEMPLATE_ID);
    }

    public String getBlueprintId() {
        return getLastPropertyValue(PropertyKey.BLUEPRINT_ID);
    }

    public String getCredentialId() {
        return getLastPropertyValue(PropertyKey.CREDENTIAL_ID);
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
        properties.remove(key);
        properties.put(key, value);
    }

    private void removeProperty(PropertyKey key, String value) {
        properties.remove(key);
    }

    private String getLastPropertyValue(PropertyKey key) {
        try {
            return properties.get(key);
        } catch (Exception ex) {
            return "";
        }
    }

    private enum PropertyKey {
        CREDENTIAL_ID, BLUEPRINT_ID, TEMPLATE_ID, STACK_ID, STACK_NAME,
        CREDENTIAL_ACCESSIBLE, BLUEPRINT_ACCESSIBLE, TEMPLATE_ACCESSIBLE, STACK_ACCESSIBLE
    }
}
