package com.sequenceiq.cloudbreak.shell.customization;

import java.io.IOException;
import org.springframework.shell.plugin.BannerProvider;
import org.springframework.stereotype.Component;

import com.github.lalyos.jfiglet.FigletFont;

/**
 * Prints the banner when the user starts the shell.
 */
@Component
public class CloudbreakBanner implements BannerProvider {

    @Override
    public String getProviderName() {
        return "CloudbreakShell";
    }

    @Override
    public String getBanner() {
        String text = "CloudbreakShell";
        try {
            return FigletFont.convertOneLine(text);
        } catch (IOException e) {
            return text;
        }
    }

    @Override
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    @Override
    public String getWelcomeMessage() {
        return "Welcome to Cloudbreak Shell. For command and param completion press TAB, for assistance type 'hint'.";
    }
}
