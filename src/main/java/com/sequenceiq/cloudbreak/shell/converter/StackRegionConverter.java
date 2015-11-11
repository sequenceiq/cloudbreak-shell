package com.sequenceiq.cloudbreak.shell.converter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.Completion;
import org.springframework.shell.core.MethodTarget;

import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.cloudbreak.shell.completion.StackRegion;
import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;

public class StackRegionConverter extends AbstractConverter<StackRegion> {

    @Autowired
    private CloudbreakContext context;

    public StackRegionConverter(CloudbreakClient client) {
        super(client);
    }

    @Override
    public boolean supports(Class<?> type, String s) {
        return StackRegion.class.isAssignableFrom(type);
    }

    @Override
    public boolean getAllPossibleValues(List<Completion> completions, Class<?> targetType, String existingData, String optionContext, MethodTarget target) {
        try {
            return getAllPossibleValues(completions, context.getRegionsByPlatform(context.getActiveCloudPlatform()));
        } catch (Exception e) {
            return false;
        }
    }
}
