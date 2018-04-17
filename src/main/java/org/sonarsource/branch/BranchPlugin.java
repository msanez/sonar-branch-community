package org.sonarsource.branch;

import org.sonar.api.Plugin;
import org.sonar.api.SonarQubeSide;
import org.sonarsource.branch.computeengine.ReportAnalysisComponentProviderImpl;
import org.sonarsource.branch.scanner.BranchConfigurationLoaderImpl;
import org.sonarsource.branch.scanner.BranchParamsValidatorImpl;
import org.sonarsource.branch.scanner.ProjectBranchesLoaderImpl;
import org.sonarsource.branch.server.BranchFeatureExtensionImpl;

public final class BranchPlugin implements Plugin {
    public void define(final Context context) {
        SonarQubeSide side = context.getRuntime().getSonarQubeSide();
        switch (side) {
            case COMPUTE_ENGINE:
                context.addExtension(ReportAnalysisComponentProviderImpl.class);
                break;
            case SCANNER:
                context.addExtension(BranchConfigurationLoaderImpl.class);
                context.addExtension(BranchParamsValidatorImpl.class);
                context.addExtension(ProjectBranchesLoaderImpl.class);
                break;
            case SERVER:
                context.addExtension(BranchFeatureExtensionImpl.class);
                break;
            default:
                break;
        }
        context.addExtensions(PropertyDefinitions.getPropertyDefinitions());
    }
}
