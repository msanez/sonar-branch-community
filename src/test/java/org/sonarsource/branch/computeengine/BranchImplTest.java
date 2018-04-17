package org.sonarsource.branch.computeengine;

import org.junit.Test;
import org.sonar.db.component.BranchType;
import org.sonar.scanner.protocol.output.ScannerReport;

import static org.junit.Assert.assertEquals;

public class BranchImplTest {

    @Test
    public void generateKey() {
        BranchImpl branch = new BranchImpl(BranchType.SHORT, false, "test", "target");
        ScannerReport.Component module = ScannerReport.Component.newBuilder().setKey("moduleKey").build();

        String key = branch.generateKey(module, null);

        assertEquals("moduleKey:BRANCH:test", key);
    }

    @Test
    public void generateKey_path() {
        BranchImpl branch = new BranchImpl(BranchType.SHORT, false, "test", "target");
        ScannerReport.Component module = ScannerReport.Component.newBuilder().setKey("moduleKey").build();
        ScannerReport.Component component = ScannerReport.Component.newBuilder().setPath("path").build();

        String key = branch.generateKey(module, component);

        assertEquals("moduleKey:path:BRANCH:test", key);
    }

    @Test
    public void generateKey_main() {
        BranchImpl branch = new BranchImpl(BranchType.SHORT, true, "test", "target");
        ScannerReport.Component module = ScannerReport.Component.newBuilder().setKey("moduleKey").build();
        ScannerReport.Component component = ScannerReport.Component.newBuilder().setPath("path").build();

        String key = branch.generateKey(module, component);

        assertEquals("moduleKey:path", key);
    }
}