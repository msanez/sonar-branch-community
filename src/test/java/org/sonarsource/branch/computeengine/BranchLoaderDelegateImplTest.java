package org.sonarsource.branch.computeengine;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sonar.db.DbClient;
import org.sonar.db.DbSession;
import org.sonar.db.component.BranchDao;
import org.sonar.db.component.BranchDto;
import org.sonar.db.component.BranchType;
import org.sonar.scanner.protocol.output.ScannerReport;
import org.sonar.server.computation.task.projectanalysis.analysis.Branch;
import org.sonar.server.computation.task.projectanalysis.analysis.MutableAnalysisMetadataHolder;
import org.sonar.server.project.Project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BranchLoaderDelegateImplTest {
    private final static String PROJECT_UUID = "6f453176-6766-47a6-8a1a-dba6c94b589d";
    private DbSession dbSession;
    private BranchDao branchDao;
    private MutableAnalysisMetadataHolder analysisMetadataHolder;

    private ArgumentCaptor<Branch> branchCaptor;

    private BranchLoaderDelegateImpl branchLoaderDelegate;

    @Before
    public void beforeTest() {
        analysisMetadataHolder = mock(MutableAnalysisMetadataHolder.class);
        DbClient dbClient = mock(DbClient.class);
        dbSession = mock(DbSession.class);
        branchDao = mock(BranchDao.class);

        Project project = new Project(PROJECT_UUID, "test", "Test Project");

        when(analysisMetadataHolder.getProject()).thenReturn(project);
        when(dbClient.openSession(false)).thenReturn(dbSession);
        when(dbClient.branchDao()).thenReturn(branchDao);

        branchCaptor = ArgumentCaptor.forClass(Branch.class);

        branchLoaderDelegate = new BranchLoaderDelegateImpl(dbClient, analysisMetadataHolder);
    }

    @Test
    public void testLoad_no_target() {
        ScannerReport.Metadata metadata = ScannerReport.Metadata.newBuilder()
                .setBranchName("test")
                .setBranchType(ScannerReport.Metadata.BranchType.LONG)
                .build();

        branchLoaderDelegate.load(metadata);

        verify(analysisMetadataHolder, times(1)).setBranch(branchCaptor.capture());
        Branch branch = branchCaptor.getValue();
        assertNotNull(branch);
        assertEquals("test", branch.getName());
        assertEquals(PROJECT_UUID, branch.getMergeBranchUuid().orElse(null));
        assertEquals(BranchType.LONG, branch.getType());
    }

    @Test
    public void testLoad_new() {
        ScannerReport.Metadata metadata = ScannerReport.Metadata.newBuilder()
                .build();
        BranchDto branchDto = new BranchDto();
        branchDto.setKey("mainBranchKey");
        when(branchDao.selectByUuid(dbSession, PROJECT_UUID)).thenReturn(Optional.of(branchDto));

        branchLoaderDelegate.load(metadata);

        verify(analysisMetadataHolder, times(1)).setBranch(branchCaptor.capture());
        Branch branch = branchCaptor.getValue();
        assertNotNull(branch);
        assertEquals("mainBranchKey", branch.getName());
        assertNull(branch.getMergeBranchUuid().orElse(null));
        assertTrue(branch.isMain());
        assertEquals(BranchType.LONG, branch.getType());
    }

    @Test(expected = IllegalStateException.class)
    public void testLoad_no_main_branch() {
        ScannerReport.Metadata metadata = ScannerReport.Metadata.newBuilder()
                .build();
        BranchDto branchDto = new BranchDto();
        branchDto.setKey("mainBranchKey");
        when(branchDao.selectByUuid(dbSession, PROJECT_UUID)).thenReturn(Optional.empty());

        branchLoaderDelegate.load(metadata);
    }

    @Test
    public void testLoad_with_target() {
        ScannerReport.Metadata metadata = ScannerReport.Metadata.newBuilder()
                .setBranchName("test")
                .setMergeBranchName("master")
                .setBranchType(ScannerReport.Metadata.BranchType.SHORT)
                .build();
        BranchDto branchDto = new BranchDto();
        branchDto.setKey("test");
        branchDto.setUuid("testUUID");
        branchDto.setProjectUuid(PROJECT_UUID);
        BranchDto mergeBranchDto = new BranchDto();
        mergeBranchDto.setKey("master");
        mergeBranchDto.setUuid(PROJECT_UUID);
        mergeBranchDto.setProjectUuid(PROJECT_UUID);
        when(branchDao.selectByBranchKey(dbSession, PROJECT_UUID, "test")).thenReturn(Optional.of(branchDto));
        when(branchDao.selectByBranchKey(dbSession, PROJECT_UUID, "master")).thenReturn(Optional.of(mergeBranchDto));

        branchLoaderDelegate.load(metadata);

        verify(analysisMetadataHolder, times(1)).setBranch(branchCaptor.capture());
        verify(dbSession, times(2)).close();
        Branch branch = branchCaptor.getValue();
        assertNotNull(branch);
        assertEquals("test", branch.getName());
        assertEquals(PROJECT_UUID, branch.getMergeBranchUuid().orElse(null));
        assertEquals(BranchType.SHORT, branch.getType());
        assertFalse(branch.isLegacyFeature());
        assertFalse(branch.supportsCrossProjectCpd());
    }
}