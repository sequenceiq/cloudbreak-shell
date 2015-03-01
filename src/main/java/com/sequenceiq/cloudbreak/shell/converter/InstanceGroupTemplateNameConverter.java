package com.sequenceiq.cloudbreak.shell.converter;

import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.cloudbreak.shell.completion.InstanceGroupTemplateName;
import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.Completion;
import org.springframework.shell.core.MethodTarget;

import java.util.List;

public class InstanceGroupTemplateNameConverter extends AbstractConverter<InstanceGroupTemplateName> {

    @Autowired
    private CloudbreakContext context;

    public InstanceGroupTemplateNameConverter(CloudbreakClient client) {
        super(client);
    }


    @Override
    public boolean supports(Class<?> type, String s) {
        return InstanceGroupTemplateName.class.isAssignableFrom(type);
    }

    @Override
    public boolean getAllPossibleValues(List<Completion> completions, Class<?> targetType, String existingData, String optionContext, MethodTarget target) {
        try {
            return getAllPossibleValues(completions, context.getActiveTemplateNames());
        } catch (Exception e) {
            return false;
        }
    }
}
