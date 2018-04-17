package org.sonarsource.branch.scanner;

import java.util.List;
import javax.annotation.Nullable;
import org.apache.commons.lang.StringUtils;
import org.sonar.scanner.bootstrap.GlobalConfiguration;
import org.sonar.scanner.scan.ProjectReactorValidator;
import org.sonar.scanner.scan.branch.BranchParamsValidator;

/**
 * This class is required as sonar won't accept new branch properties if it isn't provided.
 * @see ProjectReactorValidator
 */
public class BranchParamsValidatorImpl implements BranchParamsValidator {
    public static final String SONAR_BRANCH_NAME_PROP = "sonar.branch.name";
    public static final String SONAR_BRANCH_TARGET_PROP = "sonar.branch.target";
    private final GlobalConfiguration globalConfiguration;

    public BranchParamsValidatorImpl(GlobalConfiguration globalConfiguration) {
        this.globalConfiguration = globalConfiguration;
    }

    @Override
    public void validate(List<String> validationMessages, @Nullable String deprecatedBranchName) {
        String branchName = globalConfiguration.get(SONAR_BRANCH_NAME_PROP).orElse(null);
        String branchTarget = globalConfiguration.get(SONAR_BRANCH_TARGET_PROP).orElse(null);
        if (StringUtils.isNotEmpty(deprecatedBranchName) && (StringUtils.isNotEmpty(branchName) || StringUtils.isNotEmpty(branchTarget))) {
            validationMessages.add("Legacy property sonar.branch can't be used together with branch plugin properties");
        }
    }
}
