package com.sequenceiq.cloudbreak.shell.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.sequenceiq.cloudbreak.client.CloudbreakClient;

/**
 * Holds information about the connected Cloudbreak server.
 */
@Component
public class CloudbreakContext {

    private static final String ACCESSIBLE = "accessible";
    private Map<String, Collection<String>> platformToVariants;
    private Map<String, Collection<String>> regions;
    private Map<String, Map<String, Collection<String>>> availabilityZones;
    private Map<String, Collection<String>> volumeTypes;
    private Map<String, Collection<Map<String, String>>> instanceTypes;
    private Focus focus;
    private Hints hint;
    private Map<PropertyKey, String> properties = new HashMap<>();
    private Map<String, Object> instanceGroups = new HashMap<>();
    private Map<String, Map<String, Object>> hostGroups = new HashMap<>();
    private Set<String> activeHostGroups = new HashSet<>();
    private Set<String> activeInstanceGroups = new HashSet<>();
    private Set<String> activeTemplates = new HashSet<>();
    private Set<String> activeTemplateNames = new HashSet<>();
    private String activeCloudPlatform;
    private Map<String, String> networksByProvider = new HashMap<>();
    private Map<String, String> securityGroups = new HashMap<>();
    private String activeNetworkId;
    private String activeSecurityGroupId;
    private String fileSystemType;

    private Map<String, Object> fileSystemParameters = new HashMap<>();
    private Boolean defaultFileSystem;
    @Autowired
    private CloudbreakClient client;

    public CloudbreakContext() {
        this.focus = getRootFocus();
        this.hint = Hints.NONE;
        this.instanceGroups = new HashMap<>();
        this.hostGroups = new HashMap<>();
        this.activeHostGroups = new HashSet<>();
        this.activeInstanceGroups = new HashSet<>();
    }

    public boolean isStackAvailable() {
        return isPropertyAvailable(PropertyKey.STACK_ID);
    }

    public void addStack(String id, String name) {
        addProperty(PropertyKey.STACK_ID, id);
        addProperty(PropertyKey.STACK_NAME, name);
        setStackAccessible();
    }

    public void resetFileSystemConfiguration() {
        this.fileSystemParameters = new HashMap<>();
        this.defaultFileSystem = null;
        this.defaultFileSystem = null;
    }

    public String getActiveCloudPlatform() {
        return this.activeCloudPlatform == null ? "" : this.activeCloudPlatform.split("_")[0];
    }

    public void removeStack(String id) {
        removeProperty(PropertyKey.STACK_ID, id);
    }

    public void setInstanceGroups(Map<String, Object> instanceGroups) {
        this.instanceGroups = instanceGroups;
    }

    public Map<String, Object> getInstanceGroups() {
        return this.instanceGroups;
    }

    public void setHostGroups(Map<String, Map<String, Object>> hostGroups) {
        this.hostGroups = hostGroups;
    }

    public Map<String, Map<String, Object>> getHostGroups() {
        return hostGroups;
    }

    public Map<String, Object> putInstanceGroup(String name, InstanceGroupEntry value) {
        this.instanceGroups.put(name, value);
        return this.instanceGroups;
    }

    public Map<String, Map<String, Object>> putHostGroup(Map.Entry<String, Object> hostGroup) {
        this.hostGroups.put(hostGroup.getKey(), ImmutableMap.of(hostGroup.getKey(), hostGroup.getValue()));
        return this.hostGroups;
    }

    public Set<String> getActiveTemplates() {
        return activeTemplates;
    }

    public Set<String> getActiveTemplateNames() {
        return activeTemplateNames;
    }

    public boolean isBlueprintAvailable() {
        return isPropertyAvailable(PropertyKey.BLUEPRINT_ID);
    }

    public void addBlueprint(String id) throws Exception {
        Map<String, Object> blueprintMap = client.getBlueprintMap(id);
        this.instanceGroups = new HashMap<>();
        this.hostGroups = new HashMap<>();
        Object ambariBlueprint = blueprintMap.get("ambariBlueprint");
        this.activeHostGroups = new HashSet<>(((Map) ambariBlueprint).keySet());
        this.activeInstanceGroups = new HashSet<>(((Map) ambariBlueprint).keySet());
        this.activeInstanceGroups.add("cbgateway");
        addProperty(PropertyKey.BLUEPRINT_ID, id);
        setBlueprintAccessible();
    }

    public void prepareInstanceGroups(Object stack) {
        Map<String, Object> stackMap = (Map<String, Object>) stack;
        List<Object> instanceGroupsList = (List<Object>) stackMap.get("instanceGroups");
        this.instanceGroups = new HashMap<>();
        this.activeInstanceGroups = new HashSet<>();
        for (Object instanceGroup : instanceGroupsList) {
            Map<String, Object> instanceGroupMap = (Map<String, Object>) instanceGroup;
            this.activeInstanceGroups.add(instanceGroupMap.get("group").toString());
            instanceGroups.put(
                    instanceGroupMap.get("group").toString(),
                    new InstanceGroupEntry(
                            Long.valueOf(instanceGroupMap.get("templateId").toString()),
                            Integer.valueOf(instanceGroupMap.get("nodeCount").toString()),
                            instanceGroupMap.get("type").toString()
                    )
            );
        }
    }

    public boolean isCredentialAvailable() {
        return isPropertyAvailable(PropertyKey.CREDENTIAL_ID);
    }

    public void setCredential(String id) throws Exception {
        Map<String, String> credential = (Map<String, String>) client.getCredential(id);
        this.activeCloudPlatform = credential.get("cloudPlatform");
        Map<String, Map<String, String>> templateList = client.getAccountTemplatesWithCloudPlatformMap(this.activeCloudPlatform);
        fillTemplates(templateList);
        addProperty(PropertyKey.CREDENTIAL_ID, id);
        setCredentialAccessible();
    }

    private void fillTemplates(Map<String, Map<String, String>> templateList) {
        this.activeTemplates = templateList.keySet();
        for (Map.Entry templateEntry : templateList.entrySet()) {
            Map<String, String> templateMap = (Map<String, String>) templateEntry.getValue();
            this.activeTemplateNames.addAll(templateMap.keySet());
        }
    }

    public String getFileSystemType() {
        return fileSystemType;
    }

    public void setFileSystemType(String fileSystemType) {
        this.fileSystemType = fileSystemType;
    }

    public Map<String, Object> getFileSystemParameters() {
        return fileSystemParameters;
    }

    public void setFileSystemParameters(Map<String, Object> fileSystemParameters) {
        this.fileSystemParameters = fileSystemParameters;
    }

    public Boolean getDefaultFileSystem() {
        return defaultFileSystem;
    }

    public void setDefaultFileSystem(Boolean defaultFileSystem) {
        this.defaultFileSystem = defaultFileSystem;
    }

    public void setPlatformToVariantsMap(Map<String, Collection<String>> platformToVariants) {
        this.platformToVariants = platformToVariants;
    }


    public Collection<String> getVariantsByPlatform(String platform) {
        return platformToVariants.get(platform);
    }

    public void setRegions(Map<String, Collection<String>> regions) {
        this.regions = regions;
    }

    public Collection<String> getRegionsByPlatform(String platform) {
        return regions.get(platform);
    }

    public void setAvailabilityZones(Map<String, Map<String, Collection<String>>> availabilityZones) {
        this.availabilityZones = availabilityZones;
    }

    public Collection<String> getAvailabilityZonesByPlatform(String platform) {
        Collection<String> result = Lists.newArrayList();
        Map<String, Collection<String>> regionZones = availabilityZones.get(platform);
        for (Collection<String> zones : regionZones.values()) {
            result.addAll(zones);
        }
        return result;
    }

    public Collection<String> getAvailabilityZonesByRegion(String platform, String region) {
        return availabilityZones.get(platform).get(region);
    }

    public Collection<String> getInstanceTypeNamesByPlatform(String platform) {
        Collection<String> result = Lists.newArrayList();
        Collection<Map<String, String>> platformInstances = instanceTypes.get(platform);
        for (Map<String, String> instance : platformInstances) {
            result.add(instance.get("value"));
        }
        return result;
    }

    public Set<String> getActiveHostGroups() {
        return activeHostGroups;
    }

    public Set<String> getActiveInstanceGroups() {
        return activeInstanceGroups;
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

    public void setStackAccessible() {
        addProperty(PropertyKey.STACK_ACCESSIBLE, ACCESSIBLE);
    }

    public boolean isStackAccessible() {
        return isPropertyAvailable(PropertyKey.STACK_ACCESSIBLE);
    }

    public void setRecipeAccessible() {
        addProperty(PropertyKey.RECIPE_ACCESSIBLE, ACCESSIBLE);
    }

    public boolean isRecipeAccessible() {
        return isPropertyAvailable(PropertyKey.RECIPE_ACCESSIBLE);
    }

    public String getStackId() {
        return getLastPropertyValue(PropertyKey.STACK_ID);
    }

    public String getStackName() {
        return getLastPropertyValue(PropertyKey.STACK_NAME);
    }

    public String getBlueprintId() {
        return getLastPropertyValue(PropertyKey.BLUEPRINT_ID);
    }

    public String getRecipeId() {
        return getLastPropertyValue(PropertyKey.RECIPE_ID);
    }

    public String getCredentialId() {
        return getLastPropertyValue(PropertyKey.CREDENTIAL_ID);
    }

    public Map<String, String> getNetworksByProvider() {
        return networksByProvider;
    }

    public void putNetwork(String id, String provider) {
        networksByProvider.put(id, provider);
    }

    public void putNetworks(Map<String, String> networksByProvider) {
        this.networksByProvider.putAll(networksByProvider);
    }

    public void putSecurityGroup(String id, String name) {
        this.securityGroups.put(id, name);
    }

    public Map<String, String> getSecurityGroups() {
        return securityGroups;
    }

    public void setSecurityGroups(Map<String, String> securityGroups) {
        this.securityGroups = securityGroups;
    }

    public String getActiveNetworkId() {
        return activeNetworkId;
    }

    public void setActiveNetworkId(String activeNetworkId) {
        this.activeNetworkId = activeNetworkId;
    }

    public String getActiveSecurityGroupId() {
        return activeSecurityGroupId;
    }

    public void setActiveSecurityGroupId(String id) {
        this.activeSecurityGroupId = id;
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
        return properties.get(key) != null && !properties.get(key).isEmpty();
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

    public void setVolumeTypes(Map<String, Collection<String>> volumeTypes) {
        this.volumeTypes = volumeTypes;
    }

    public Collection<String> getVolumeTypesByPlatform(String platform) {
        return volumeTypes.get(platform);
    }

    public void setInstanceTypes(Map<String, Collection<Map<String, String>>> instanceTypes) {
        this.instanceTypes = instanceTypes;
    }

    private enum PropertyKey {
        CREDENTIAL_ID, BLUEPRINT_ID, RECIPE_ID, TEMPLATE_ID, STACK_ID, STACK_NAME,
        CREDENTIAL_ACCESSIBLE, BLUEPRINT_ACCESSIBLE, TEMPLATE_ACCESSIBLE, STACK_ACCESSIBLE, RECIPE_ACCESSIBLE
    }
}
