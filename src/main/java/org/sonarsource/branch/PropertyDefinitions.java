package org.sonarsource.branch;

import java.util.Arrays;
import java.util.List;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;

public final class PropertyDefinitions {
    public static final String DEFAULT_LONG_BRANCH_REGEXP = "(branch|release)-.*";
    private static final String BRANCH_PLUGIN_CATEGORY = "general";
    private static final String BRANCH_PLUGIN_SUBCATEGORY = "Branches";

    private PropertyDefinitions() {
    }

    public static List getPropertyDefinitions() {
        return Arrays.asList(
                PropertyDefinition
                        .builder("sonar.branch.longLivedBranches.regex")
                        .name("Detection of long living branches")
                        .description("Regular expression used to detect whether a branch is a long living branch (as opposed to short living branch), based on its name. "
                                + "This applies only during first analysis, the type of a branch cannot be changed later.")
                        .defaultValue(DEFAULT_LONG_BRANCH_REGEXP)
                        .category(BRANCH_PLUGIN_CATEGORY)
                        .subCategory(BRANCH_PLUGIN_SUBCATEGORY)
                        .onQualifiers("TRK")
                        .build(),
                PropertyDefinition
                        .builder("sonar.dbcleaner.daysBeforeDeletingInactiveShortLivingBranches")
                        .name("Number of days before purging inactive short living branches")
                        .description("Short living branches are permanently deleted when there are no analysis for the configured number of days.")
                        .defaultValue("30")
                        .type(PropertyType.INTEGER)
                        .category(BRANCH_PLUGIN_CATEGORY)
                        .subCategory(BRANCH_PLUGIN_SUBCATEGORY)
                        .build());
    }
}
