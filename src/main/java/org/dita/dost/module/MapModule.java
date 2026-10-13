package org.dita.dost.module;

import static net.sf.saxon.s9api.streams.Predicates.empty;
import static net.sf.saxon.s9api.streams.Steps.attribute;
import static org.dita.dost.util.Configuration.configuration;
import static org.dita.dost.util.Constants.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.streams.Predicates;
import net.sf.saxon.s9api.streams.Step;
import net.sf.saxon.s9api.streams.Steps;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.filter.MapBranchFilterModule;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.DitaUtils;
import org.dita.dost.util.Job.FileInfo;

public class MapModule extends AbstractPipelineModuleImpl {

  private final Predicate<FileInfo> fileInfoFilter = fi ->
    DitaUtils.isDitaMap(fi) && (fi.isInput() || fi.isResourceOnly());

  private int maxIterationCount;
  private List<AbstractPipelineModuleImpl> modules;

  private void init(AbstractPipelineInput input) {
    maxIterationCount =
      Optional.ofNullable(configuration.get("preprocess.max-iteration-count")).map(Integer::parseInt).orElse(10);

    var skipMapProfile = input.getAttribute("preprocess.map-profile.skip") != null;
    var skipBranchFilter = input.getAttribute("preprocess.branch-filter.skip") != null;
    var filterOnParse = input.getAttribute("map.filter-on-parse") != null;
    var skipKeyref = input.getAttribute("preprocess.keyref.skip") != null;

    modules = new ArrayList<>();

    //              unless:set="preprocess.map-profile.skip"
    //              if:true="${map.filter-on-parse}">
    if (!skipMapProfile && filterOnParse) {
      var profileModule = new ProfileModule();
      profileModule.setFileInfoFilter(fileInfoFilter);
      modules.add(profileModule);
    }

    //              unless:set="preprocess.branch-filter.skip"/>
    if (!skipBranchFilter) {
      var mapBranchFilterModule = new MapBranchFilterModule();
      // TODO
      //    mapBranchFilterModule.setIgnoreKeySpaceRewrite(true);
      modules.add(mapBranchFilterModule);
    }

    //              unless:set="preprocess.keyref.skip">
    if (!skipKeyref) {
      var keyrefModule = new KeyrefModule();
      // TODO
      //    keyrefModule.setKeyScope(keyScope);
      keyrefModule.setFileInfoFilter(fileInfoFilter);
      modules.add(keyrefModule);
    }

    //              unless:set="preprocess.mapref.skip"> Handled by Ant
    var maprefModule = new MaprefModule();
    maprefModule.setFileInfoFilter(fileInfoFilter);
    modules.add(maprefModule);

    for (AbstractPipelineModuleImpl module : modules) {
      module.setJob(job);
      module.setXmlUtils(xmlUtils);
      module.setLogger(logger);
      module.setParallel(parallel);
      module.setProcessingMode(processingMode);
    }
  }

  @Override
  public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
    init(input);

    for (int i = 0; i < maxIterationCount; i++) {
      if (hasUnresolvedKeyrefs()) {
        logger.info("No topicref with @keyref, stop iteration");
        break;
      }
      logger.info("Run map module iteration {}", i + 1);
      for (AbstractPipelineModuleImpl module : modules) {
        logger.info("Running " + module.getClass().getSimpleName());
        // TODO: keep track which files still have something to process and only process them
        module.execute(input);
      }
      if (i + 1 == maxIterationCount) {
        logger.warn("Done with " + maxIterationCount + " iterations, break to prevent infinite loop");
      }
    }
    return null;
  }

  private static final Step<XdmNode> hasTopicrefWithKeyref = Steps
    .descendant(MAP_TOPICREF.matcher())
    .where(empty(attribute(ATTRIBUTE_NAME_HREF)).and(Predicates.exists(attribute(ATTRIBUTE_NAME_KEYREF))));

  private boolean hasUnresolvedKeyrefs() {
    return job
      .getFileInfo(fileInfoFilter)
      .stream()
      .noneMatch(fi -> {
        try {
          var doc = job.getStore().getImmutableNode(job.tempDirURI.resolve(fi.uri()));
          return doc.select(hasTopicrefWithKeyref).exists();
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
  }
}
