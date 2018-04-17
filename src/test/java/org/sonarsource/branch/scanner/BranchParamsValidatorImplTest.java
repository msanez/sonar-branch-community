package org.sonarsource.branch.scanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sonar.scanner.bootstrap.GlobalConfiguration;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.sonarsource.branch.scanner.BranchParamsValidatorImpl.SONAR_BRANCH_NAME_PROP;
import static org.sonarsource.branch.scanner.BranchParamsValidatorImpl.SONAR_BRANCH_TARGET_PROP;

@RunWith(MockitoJUnitRunner.class)
public class BranchParamsValidatorImplTest {

    @Mock
    GlobalConfiguration globalConfiguration;

    @Test
    public void validate_new_properties() {
        when(globalConfiguration.get(SONAR_BRANCH_NAME_PROP)).thenReturn(Optional.of("feature/test"));
        when(globalConfiguration.get(SONAR_BRANCH_TARGET_PROP)).thenReturn(Optional.of("master"));
        BranchParamsValidatorImpl paramsValidator = new BranchParamsValidatorImpl(globalConfiguration);

        List<String> validationMessages = new ArrayList<>();
        paramsValidator.validate(validationMessages, null);

        assertTrue(validationMessages.isEmpty());
    }

    @Test
    public void validate_legacy_only() {
        BranchParamsValidatorImpl paramsValidator = new BranchParamsValidatorImpl(globalConfiguration);

        List<String> validationMessages = new ArrayList<>();
        paramsValidator.validate(validationMessages, null);

        assertTrue(validationMessages.isEmpty());
    }

    @Test
    public void validate_lagacy_and_branch_name() {
        when(globalConfiguration.get(SONAR_BRANCH_NAME_PROP)).thenReturn(Optional.of("feature/test"));
        BranchParamsValidatorImpl paramsValidator = new BranchParamsValidatorImpl(globalConfiguration);

        List<String> validationMessages = new ArrayList<>();
        paramsValidator.validate(validationMessages, "legacy");

        assertThat(validationMessages).hasSize(1);
    }

    @Test
    public void validate_lagacy_and_target_name() {
        when(globalConfiguration.get(SONAR_BRANCH_TARGET_PROP)).thenReturn(Optional.of("master"));
        BranchParamsValidatorImpl paramsValidator = new BranchParamsValidatorImpl(globalConfiguration);

        List<String> validationMessages = new ArrayList<>();
        paramsValidator.validate(validationMessages, "legacy");

        assertThat(validationMessages).hasSize(1);
    }

    @Test
    public void validate_lagacy_and_new_properties() {
        when(globalConfiguration.get(SONAR_BRANCH_TARGET_PROP)).thenReturn(Optional.of("master"));
        when(globalConfiguration.get(SONAR_BRANCH_NAME_PROP)).thenReturn(Optional.of("feature/test"));
        BranchParamsValidatorImpl paramsValidator = new BranchParamsValidatorImpl(globalConfiguration);

        List<String> validationMessages = new ArrayList<>();
        paramsValidator.validate(validationMessages, "legacy");

        assertThat(validationMessages).hasSize(1);
    }
}