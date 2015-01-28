package com.sequenceiq.cloudbreak.shell.commands;

import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;

import java.io.File;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;

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
    @Ignore
    public void testCreateAzureCredentialsWithSshKeyPath() throws Exception {
        // GIVEN
        String sshKeyPath = getAbsolutePath("keys/azure");
        // WHEN
        underTest.createAzureCredential(DUMMY_NAME, DUMMY_SUBSCRIPTION_ID, new File("test"), null, false, DUMMY_DESCRIPTION);
        // THEN
        verify(cloudbreak, times(1)).postAzureCredential(anyString(), anyString(), anyString(), anyString(), anyBoolean());
    }

    @Test
    @Ignore
    public void testCreateAzureCredentialsWithSshKeyPathFileNotFound() throws Exception {
        // GIVEN
        String sshKeyPath = DUMMY_SSH_KEY_PATH;
        // WHEN
        underTest.createAzureCredential(DUMMY_NAME, DUMMY_SUBSCRIPTION_ID, null, sshKeyPath, false, DUMMY_DESCRIPTION);
        // THEN
        verify(cloudbreak, times(0)).postAzureCredential(anyString(), anyString(), anyString(), anyString(), anyBoolean());
    }

    private String getAbsolutePath(String path) {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(path).getFile());
        return file.getAbsolutePath();
    }


}
