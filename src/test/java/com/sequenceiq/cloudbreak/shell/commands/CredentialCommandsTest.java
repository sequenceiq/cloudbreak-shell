package com.sequenceiq.cloudbreak.shell.commands;

import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.times;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;

import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;

public class CredentialCommandsTest {

    private static final String DUMMY_DESCRIPTION = "dummyDescription";
    private static final String DUMMY_NAME = "dummyName";
    private static final String DUMMY_SUBSCRIPTION_ID = "dummySubscriptionId";
    private static final String DUMMY_SSH_KEY_PATH = "dummy";

    @InjectMocks
    private CredentialCommands underTest;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private CloudbreakContext context;
    @Mock
    private CloudbreakClient cloudbreak;

    @Before
    public void setUp() {
        underTest = new CredentialCommands();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateAzureCredentialsWithSshKeyPath() throws Exception {
        // GIVEN
        String sshKeyPath = getAbsolutePath("keys/azure");
        // WHEN
        underTest.createAzureCredential(DUMMY_DESCRIPTION, DUMMY_NAME, DUMMY_SUBSCRIPTION_ID, sshKeyPath, null, false);
        // THEN
        verify(cloudbreak, times(1)).postAzureCredential(anyString(), anyString(), anyString(), anyString(), anyBoolean());
    }

    @Test
    public void testCreateAzureCredentialsWithSshKeyPathFileNotFound() throws Exception {
        // GIVEN
        String sshKeyPath = DUMMY_SSH_KEY_PATH;
        // WHEN
        underTest.createAzureCredential(DUMMY_DESCRIPTION, DUMMY_NAME, DUMMY_SUBSCRIPTION_ID, sshKeyPath, null, false);
        // THEN
        verify(cloudbreak, times(0)).postAzureCredential(anyString(), anyString(), anyString(), anyString(), anyBoolean());
    }

    private String getAbsolutePath(String path) {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(path).getFile());
        return file.getAbsolutePath();
    }


}
