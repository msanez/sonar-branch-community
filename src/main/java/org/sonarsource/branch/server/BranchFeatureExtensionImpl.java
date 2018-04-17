package org.sonarsource.branch.server;

import org.sonar.server.branch.BranchFeatureExtension;

public class BranchFeatureExtensionImpl implements BranchFeatureExtension {
    public boolean isEnabled() {
        return true;
    }
}
