package org.sonarsource.branch.scanner;

import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.utils.MessageException;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.scanner.scan.branch.BranchConfiguration;
import org.sonar.scanner.scan.branch.BranchConfigurationLoader;
import org.sonar.scanner.scan.branch.BranchInfo;
import org.sonar.scanner.scan.branch.BranchType;
import org.sonar.scanner.scan.branch.DefaultBranchConfiguration;
import org.sonar.scanner.scan.branch.ProjectBranches;
import org.sonar.scanner.scan.branch.ProjectPullRequests;
import org.sonarsource.branch.PropertyDefinitions;

/**
 * Loads configuration for existing branches. Configuration for the new branch is computed from regexp.
 */
public class BranchConfigurationLoaderImpl implements BranchConfigurationLoader {
    private static final Logger LOGGER = Loggers.get(BranchConfigurationLoaderImpl.class);

    @Override
    public BranchConfiguration load(Map<String, String> localSettings, Supplier<Map<String, String>> remoteSettingsSupplier, ProjectBranches branches, ProjectPullRequests pullRequests) {
        String branchName = StringUtils.trimToNull(localSettings.get("sonar.branch.name"));
        if (branchName == null) {
            return new DefaultBranchConfiguration();
        } else {
            String targetBranch = StringUtils.trimToNull(localSettings.get("sonar.branch.target"));
            return loadConfiguration(branchName, targetBranch, remoteSettingsSupplier, branches);
        }
    }

    private BranchConfiguration loadConfiguration(String branchName, @Nullable String targetBranch, Supplier<Map<String, String>> remoteSettingsSupplier, ProjectBranches branches) {
        if (branches.isEmpty()) {
            if ("master".equals(branchName)) {
                return new DefaultBranchConfiguration();
            } else {
                throw MessageException.of("Project not found. Run analysis for master branch before analysing other branches.");
            }
        } else {
            String base = targetBranch;
            BranchInfo branchInfo = branches.get(branchName);
            BranchType branchType;
            if (branchInfo == null) { // first scan for a new branch, determine type using regexp
                branchType = resolveTypeFromRegex(remoteSettingsSupplier, branchName);
            } else {
                branchType = branchInfo.type();
                if (branchType == BranchType.LONG) {
                    base = branchName;
                }
            }

            return new BranchConfigurationImpl(branchType, branchName, findLingLivedTargetBranch(branches, targetBranch), base);
        }
    }

    private BranchType resolveTypeFromRegex(Supplier<Map<String, String>> remoteSettingsSupplier, String branchName) {
        String longBrRegex = remoteSettingsSupplier.get().getOrDefault("sonar.branch.longLivedBranches.regex", PropertyDefinitions.DEFAULT_LONG_BRANCH_REGEXP);
        return branchName.matches(longBrRegex) ? BranchType.LONG : BranchType.SHORT;
    }

    private String findLingLivedTargetBranch(ProjectBranches projectBranches, @Nullable String targetBranch) {
        if (targetBranch != null) {
            BranchInfo branchInfo = projectBranches.get(targetBranch);
            Preconditions.checkNotNull(branchInfo, "Branch %s not found", targetBranch);
            if (branchInfo.type() == BranchType.SHORT) {
                String branchTargetName = branchInfo.branchTargetName();
                LOGGER.info("{} is short lived branch, checking parent {}", targetBranch, branchTargetName);
                return findLingLivedTargetBranch(projectBranches, branchTargetName);
            } else {
                return targetBranch;
            }
        } else {
            return null;
        }
    }
}
