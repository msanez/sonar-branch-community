package org.sonarsource.branch.scanner;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.api.utils.log.Loggers;
import org.sonar.scanner.bootstrap.ScannerWsClient;
import org.sonar.scanner.scan.branch.BranchInfo;
import org.sonar.scanner.scan.branch.BranchType;
import org.sonar.scanner.scan.branch.ProjectBranches;
import org.sonarqube.ws.client.HttpException;
import org.sonarqube.ws.client.MockWsResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProjectBranchesLoaderImplTest {
    private final static String PROJECT_KEY = "testProject";
    private ScannerWsClient wsClient;

    @Before
    public void beforeTest() {
        wsClient = mock(ScannerWsClient.class);
    }

    @Test
    public void load_empty() {
        MockWsResponse response = new MockWsResponse();
        response.setContent("{\"branches\": []}");
        when(wsClient.call(any())).thenReturn(response);
        ProjectBranchesLoaderImpl branchesLoader = new ProjectBranchesLoaderImpl(wsClient);

        ProjectBranches projectBranches = branchesLoader.load(PROJECT_KEY);

        assertTrue(projectBranches.isEmpty());
    }

    @Test
    public void load_io_exception() {
        when(wsClient.call(any())).thenThrow(new RuntimeException());
        ProjectBranchesLoaderImpl branchesLoader = new ProjectBranchesLoaderImpl(wsClient);

        Loggers.get(ProjectBranchesLoaderImpl.class).setLevel(LoggerLevel.ERROR);
        ProjectBranches projectBranches = branchesLoader.load(PROJECT_KEY);
        Loggers.get(ProjectBranchesLoaderImpl.class).setLevel(LoggerLevel.INFO);

        assertTrue(projectBranches.isEmpty());
    }

    @Test
    public void load_404_exception() {
        when(wsClient.call(any())).thenThrow(new HttpException("", 404, ""));
        ProjectBranchesLoaderImpl branchesLoader = new ProjectBranchesLoaderImpl(wsClient);

        Loggers.get(ProjectBranchesLoaderImpl.class).setLevel(LoggerLevel.ERROR);
        ProjectBranches projectBranches = branchesLoader.load(PROJECT_KEY);
        Loggers.get(ProjectBranchesLoaderImpl.class).setLevel(LoggerLevel.INFO);

        assertTrue(projectBranches.isEmpty());
    }

    @Test
    public void load_branch() {
        MockWsResponse response = new MockWsResponse();
        response.setContent(
                "{\"branches\": [\n" +
                "  {\n" +
                "    \"name\": \"test\",\n" +
                "    \"type\": \"SHORT\",\n" +
                "    \"isMain\": false,\n" +
                "    \"mergeBranch\": \"master\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"master\",\n" +
                "    \"type\": \"LONG\",\n" +
                "    \"isMain\": true\n" +
                "  }\n" +
                "]}");
        when(wsClient.call(any())).thenReturn(response);
        ProjectBranchesLoaderImpl branchesLoader = new ProjectBranchesLoaderImpl(wsClient);

        ProjectBranches projectBranches = branchesLoader.load(PROJECT_KEY);

        BranchInfo branchInfo = projectBranches.get("test");
        assertNotNull(branchInfo);
        assertEquals("test", branchInfo.name());
        assertEquals(BranchType.SHORT, branchInfo.type());
        assertFalse(branchInfo.isMain());
        assertEquals("master", branchInfo.branchTargetName());
    }
}