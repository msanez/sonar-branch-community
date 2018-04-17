package org.sonarsource.branch;

import java.util.List;
import junit.framework.TestCase;
import org.sonar.api.Plugin;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;
import org.sonarsource.branch.computeengine.ReportAnalysisComponentProviderImpl;
import org.sonarsource.branch.scanner.BranchConfigurationLoaderImpl;
import org.sonarsource.branch.scanner.BranchParamsValidatorImpl;
import org.sonarsource.branch.scanner.ProjectBranchesLoaderImpl;
import org.sonarsource.branch.server.BranchFeatureExtensionImpl;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
public class BranchPluginTest extends TestCase {

    public void testScannerExtensions() {
        Plugin.Context context = getContext(SonarQubeSide.SCANNER);

        List extensions = context.getExtensions();

        assertThat(extensions).hasSize(5)
                .contains(BranchConfigurationLoaderImpl.class)
                .contains(BranchParamsValidatorImpl.class)
                .contains(ProjectBranchesLoaderImpl.class);
    }

    public void testComputeEngineExtensions() {
        Plugin.Context context = getContext(SonarQubeSide.COMPUTE_ENGINE);

        List extensions = context.getExtensions();

        assertThat(extensions).hasSize(3)
                .contains(ReportAnalysisComponentProviderImpl.class);
    }

    public void testServerExtensions() {
        Plugin.Context context = getContext(SonarQubeSide.SERVER);

        List extensions = context.getExtensions();

        assertThat(extensions).hasSize(3)
                .contains(BranchFeatureExtensionImpl.class);
    }

    private Plugin.Context getContext(SonarQubeSide side) {
        SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(Version.create(6, 7), side);
        Plugin.Context context = new Plugin.Context(runtime);
        new BranchPlugin().define(context);
        return context;
    }
}