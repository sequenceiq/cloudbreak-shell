package com.sequenceiq.cloudbreak.shell.converter;

import java.util.List;
import java.util.Map;

import org.springframework.shell.core.Completion;
import org.springframework.shell.core.MethodTarget;

import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.cloudbreak.shell.completion.NetworkName;

public class NetworkNameConverter extends AbstractConverter<NetworkName> {

    public NetworkNameConverter(CloudbreakClient client) {
        super(client);
    }

    @Override
    public boolean supports(Class<?> type, String optionContext) {
        return NetworkName.class.isAssignableFrom(type);
    }

    @Override
    public boolean getAllPossibleValues(List<Completion> completions, Class<?> targetType, String existingData, String optionContext, MethodTarget target) {
        try {
            Map<String, String> networksMap = getClient().getAccountNetworksMap();
            return getAllPossibleValues(completions, networksMap.values());
        } catch (Exception e) {
            return false;
        }
    }
}
