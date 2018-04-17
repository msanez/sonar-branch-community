package org.sonarsource.branch.computeengine;

import java.util.List;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ReportAnalysisComponentProviderImplTest {

    @Test
    public void getComponents() {
        ReportAnalysisComponentProviderImpl reportAnalysisComponentProvider = new ReportAnalysisComponentProviderImpl();

        List<Object> components = reportAnalysisComponentProvider.getComponents();

        assertThat(components).hasSize(1).containsExactly(BranchLoaderDelegateImpl.class);
    }
}