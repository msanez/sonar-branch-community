package org.sonarsource.branch.computeengine;

import java.util.Collections;
import java.util.List;
import org.sonar.plugin.ce.ReportAnalysisComponentProvider;

public class ReportAnalysisComponentProviderImpl implements ReportAnalysisComponentProvider {
    public List<Object> getComponents() {
        return Collections.singletonList(BranchLoaderDelegateImpl.class);
    }
}
