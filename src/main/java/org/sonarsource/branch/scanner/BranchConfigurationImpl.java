package org.sonarsource.branch.scanner;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.sonar.scanner.scan.branch.BranchConfiguration;
import org.sonar.scanner.scan.branch.BranchType;

@Immutable
public class BranchConfigurationImpl implements BranchConfiguration {
    private final BranchType branchType;
    private final String branchName;
    private final String branchTarget;
    private final String branchBase;

    BranchConfigurationImpl(BranchType branchType, String branchName, @Nullable String branchTarget, @Nullable String branchBase) {
        this.branchType = branchType;
        this.branchName = branchName;
        this.branchTarget = branchTarget;
        this.branchBase = branchBase;
    }

    @Override
    public BranchType branchType() {
        return branchType;
    }

    @Override
    public String branchName() {
        return branchName;
    }

    @Override
    public String branchTarget() {
        return branchTarget;
    }

    @Override
    public String branchBase() {
        return branchBase;
    }
}
