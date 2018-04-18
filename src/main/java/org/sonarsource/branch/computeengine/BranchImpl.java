package org.sonarsource.branch.computeengine;

import java.util.Optional;
import javax.annotation.Nullable;
import org.apache.commons.lang.StringUtils;
import org.sonar.core.component.ComponentKeys;
import org.sonar.db.component.BranchType;
import org.sonar.db.component.ComponentDto;
import org.sonar.scanner.protocol.output.ScannerReport.Component;
import org.sonar.server.computation.task.projectanalysis.analysis.Branch;

public class BranchImpl implements Branch {
    private final BranchType branchType;
    private final boolean main;
    private final String mergeBranchUuid;
    private final String branchName;

    public BranchImpl(BranchType branchType, boolean main, String branchName) {
        this(branchType, main, branchName, null);
    }

    public BranchImpl(BranchType branchType, boolean main, String branchName, @Nullable String mergeBranchUuid) {
        this.branchType = branchType;
        this.main = main;
        this.branchName = branchName;
        this.mergeBranchUuid = mergeBranchUuid;
    }

    @Override
    public BranchType getType() {
        return branchType;
    }

    @Override
    public Optional<String> getMergeBranchUuid() {
        return Optional.ofNullable(mergeBranchUuid);
    }

    @Override
    public boolean isMain() {
        return main;
    }

    @Override
    public boolean isLegacyFeature() {
        return false;
    }

    @Override
    public String getName() {
        return branchName;
    }

    @Override
    public boolean supportsCrossProjectCpd() {
        return main;
    }

    @Override
    public String generateKey(Component module, @Nullable Component component) {
        String mainBranchKey = component == null ? module.getKey()
                : ComponentKeys.createEffectiveKey(module.getKey(), StringUtils.trimToNull(component.getPath()));
        return main ? mainBranchKey : mainBranchKey + ComponentDto.BRANCH_KEY_SEPARATOR + branchName;
    }

    // Not supported by the plugin
    @Override
    public String getPullRequestId() {
        return null;
    }
}
