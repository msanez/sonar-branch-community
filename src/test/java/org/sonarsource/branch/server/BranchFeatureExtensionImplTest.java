package org.sonarsource.branch.server;

import org.junit.Test;

import static org.junit.Assert.*;

public class BranchFeatureExtensionImplTest {

    @Test
    public void isEnabled() {
        BranchFeatureExtensionImpl branchFeatureExtension = new BranchFeatureExtensionImpl();

        boolean enabled = branchFeatureExtension.isEnabled();

        assertTrue(enabled);
    }
}