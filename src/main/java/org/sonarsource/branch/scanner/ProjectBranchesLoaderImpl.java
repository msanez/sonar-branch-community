package org.sonarsource.branch.scanner;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.scanner.bootstrap.ScannerWsClient;
import org.sonar.scanner.scan.branch.BranchInfo;
import org.sonar.scanner.scan.branch.BranchType;
import org.sonar.scanner.scan.branch.ProjectBranches;
import org.sonar.scanner.scan.branch.ProjectBranchesLoader;
import org.sonarqube.ws.client.GetRequest;
import org.sonarqube.ws.client.WsResponse;

/**
 * Retrieves list of project's branches using API call.
 */
public class ProjectBranchesLoaderImpl implements ProjectBranchesLoader {
    private static final Logger LOGGER = Loggers.get(ProjectBranchesLoaderImpl.class);
    private static final Gson GSON = new Gson();

    private final ScannerWsClient wsClient;

    public ProjectBranchesLoaderImpl(final ScannerWsClient wsClient) {
        this.wsClient = wsClient;
    }

    @Override
    public ProjectBranches load(final String projectKey) {
        return new ProjectBranches(loadListOfBranches(projectKey));
    }

    private List<BranchInfo> loadListOfBranches(final String projectKey) {
        GetRequest request = new GetRequest("/api/project_branches/list").setParam("project", projectKey);
        try (WsResponse response = wsClient.call(request)) {
            return readResponse(response);
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("Unable to load list of branches. Using empty list.", e);
            return Collections.emptyList();
        }
    }

    private List<BranchInfo> readResponse(final WsResponse wsResponse) throws IOException {
        try (Reader contentReader = wsResponse.contentReader()) {
            JsonObject map = GSON.fromJson(contentReader, JsonObject.class);
            return StreamSupport.stream(map.get("branches").getAsJsonArray().spliterator(), false)
                    .map(this::createBranchInfo)
                    .collect(Collectors.toList());
        }
    }

    private BranchInfo createBranchInfo(JsonElement branchElement) {
        JsonObject branchObj = (JsonObject) branchElement;

        String name = branchObj.get("name").getAsString();
        BranchType type = BranchType.valueOf(branchObj.get("type").getAsString());
        boolean main = branchObj.get("isMain").getAsBoolean();
        JsonElement mergeBranchElement = branchObj.get("mergeBranch");
        String mergeBranch = mergeBranchElement == null ? null : mergeBranchElement.getAsString();

        return new BranchInfo(name, type, main, mergeBranch);
    }
}
