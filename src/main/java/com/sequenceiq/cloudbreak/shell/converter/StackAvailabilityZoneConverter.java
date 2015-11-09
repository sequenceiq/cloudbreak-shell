package com.sequenceiq.cloudbreak.shell.converter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.Completion;
import org.springframework.shell.core.MethodTarget;

import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.cloudbreak.shell.completion.StackAvailabilityZone;
import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;

public class StackAvailabilityZoneConverter extends AbstractConverter<StackAvailabilityZone> {

    @Autowired
    private CloudbreakContext context;

    public StackAvailabilityZoneConverter(CloudbreakClient client) {
        super(client);
    }

    @Override
    public boolean supports(Class<?> type, String s) {
        return StackAvailabilityZone.class.isAssignableFrom(type);
    }

    @Override
    public boolean getAllPossibleValues(List<Completion> completions, Class<?> targetType, String existingData, String optionContext, MethodTarget target) {
        try {
            return getAllPossibleValues(completions, context.getAvailabilityZonesByPlatform(context.getActiveCloudPlatform()));
        } catch (Exception e) {
            return false;
        }
    }
}
