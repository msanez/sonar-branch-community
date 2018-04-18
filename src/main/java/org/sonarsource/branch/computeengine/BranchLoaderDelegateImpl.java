package org.sonarsource.branch.computeengine;

import java.util.Optional;
import javax.annotation.Nullable;
import org.apache.commons.lang.StringUtils;
import org.sonar.db.DbClient;
import org.sonar.db.DbSession;
import org.sonar.db.component.BranchDto;
import org.sonar.db.component.BranchType;
import org.sonar.scanner.protocol.output.ScannerReport.Metadata;
import org.sonar.server.computation.task.projectanalysis.analysis.Branch;
import org.sonar.server.computation.task.projectanalysis.analysis.MutableAnalysisMetadataHolder;
import org.sonar.server.computation.task.projectanalysis.component.BranchLoader;
import org.sonar.server.computation.task.projectanalysis.component.BranchLoaderDelegate;

/**
 * Loads new branch type that can contain target (merge) branch.
 * It will use project's main branch if branchName is empty.
 *
 * @see BranchLoader
 */
public class BranchLoaderDelegateImpl implements BranchLoaderDelegate {
    private final DbClient dbClient;
    private final MutableAnalysisMetadataHolder analysisMetadataHolder;

    public BranchLoaderDelegateImpl(DbClient dbClient, MutableAnalysisMetadataHolder analysisMetadataHolder) {
        this.dbClient = dbClient;
        this.analysisMetadataHolder = analysisMetadataHolder;
    }

    @Override
    public void load(Metadata metadata) {
        String projectUuid = analysisMetadataHolder.getProject().getUuid();
        String branchName = StringUtils.trimToNull(metadata.getBranchName());
        Branch branch;

        if (branchName == null) {
            branch = getMainBranch(projectUuid);
        } else {
            BranchType branchType = BranchType.valueOf(metadata.getBranchType().name());
            String mergeBranchName = StringUtils.trimToNull(metadata.getMergeBranchName());
            branch = getMergeBranch(projectUuid, branchName, branchType, mergeBranchName);
        }
        analysisMetadataHolder.setBranch(branch);
    }

    private Branch getMainBranch(String projectUuid) {
        Optional<BranchDto> branchDto = loadMainBranchByProjectUuid(projectUuid);
        String branchKey = branchDto.map(BranchDto::getKey).orElseThrow(() -> new IllegalStateException("Main branch not found"));
        return new BranchImpl(BranchType.LONG, true, branchKey);
    }

    private Branch getMergeBranch(String projectUuid, String branchName, BranchType branchType, @Nullable String mergeBranchName) {
        String mergeBranchUuid = getMergeBranchUuid(projectUuid, mergeBranchName);
        Optional<BranchDto> branchOpt = loadBranchByKey(projectUuid, branchName);
        boolean main = branchOpt.isPresent() && branchOpt.get().isMain();
        return new BranchImpl(branchType, main, branchName, mergeBranchUuid);
    }

    private String getMergeBranchUuid(String projectUuid, @Nullable String mergeBranchName) {
        if (mergeBranchName == null) {
            return projectUuid; // default to project UUID as it is same as main branch UUID
        } else {
            Optional<BranchDto> branchDtoOpt = loadBranchByKey(projectUuid, mergeBranchName);
            return branchDtoOpt.map(BranchDto::getUuid).orElseThrow(() -> new IllegalStateException("Merge branch not found"));
        }
    }

    private Optional<BranchDto> loadMainBranchByProjectUuid(String projectUuid) {
        try (DbSession dbSession = dbClient.openSession(false)) {
            return dbClient.branchDao().selectByUuid(dbSession, projectUuid);
        }
    }

    private Optional<BranchDto> loadBranchByKey(String projectUuid, String key) {
        try (DbSession dbSession = dbClient.openSession(false)) {
            return dbClient.branchDao().selectByBranchKey(dbSession, projectUuid, key);
        }
    }
}
