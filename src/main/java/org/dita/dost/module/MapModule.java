package org.dita.dost.module;

import static net.sf.saxon.s9api.streams.Predicates.empty;
import static net.sf.saxon.s9api.streams.Steps.attribute;
import static org.dita.dost.util.Constants.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.streams.Predicates;
import net.sf.saxon.s9api.streams.Step;
import net.sf.saxon.s9api.streams.Steps;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.filter.MapBranchFilterModule;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.Constants;
import org.dita.dost.util.DitaUtils;
import org.dita.dost.util.Job.FileInfo;

public class MapModule extends AbstractPipelineModuleImpl {

  private static final int MAX_ITERATION_COUNT = 10;

  private final Predicate<FileInfo> fileInfoFilter = fi ->
    DitaUtils.isDitaMap(fi) && (fi.isInput() || fi.isResourceOnly());

  private List<AbstractPipelineModuleImpl> modules;

  private void init() {
    modules = new ArrayList<AbstractPipelineModuleImpl>();

    //              unless:set="preprocess.mapref.skip">
    var maprefModule = new MaprefModule();
    maprefModule.setFileInfoFilter(fileInfoFilter);
    modules.add(maprefModule);

    //              unless:set="preprocess.map-profile.skip"
    //              if:true="${map.filter-on-parse}">
    var profileModule = new ProfileModule();
    profileModule.setFileInfoFilter(fileInfoFilter);
    modules.add(profileModule);

    //              unless:set="preprocess.branch-filter.skip"/>
    var mapBranchFilterModule = new MapBranchFilterModule();
    modules.add(mapBranchFilterModule);

    //              unless:set="preprocess.keyref.skip">
    var keyrefModule = new KeyrefModule();
    keyrefModule.setFileInfoFilter(fileInfoFilter);
    modules.add(keyrefModule);

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
    init();

    for (int i = 0; i < MAX_ITERATION_COUNT; i++) {
      if (hasUnresolvedMaprefs()) {
        logger.info("No mapref with @keyref, stop iteration");
        break;
      }
      logger.info("Run map module iteration {}", i + 1);
      for (AbstractPipelineModuleImpl module : modules) {
        logger.info("Running " + module.getClass().getSimpleName());
        // TODO: only run matching
        module.execute(input);
      }
      if (i + 1 == MAX_ITERATION_COUNT) {
        logger.info("Done with " + MAX_ITERATION_COUNT + " iterations, break");
      }
    }
    return null;
  }

  private static final Step<XdmNode> hasMaprefWithKeyref = Steps
    .descendant(MAPGROUP_D_MAPREF.matcher())
    .where(empty(attribute(ATTRIBUTE_NAME_HREF)).and(Predicates.exists(attribute(ATTRIBUTE_NAME_KEYREF))));

  private boolean hasUnresolvedMaprefs() {
    return job
      .getFileInfo(fileInfoFilter)
      .stream()
      .noneMatch(fi -> {
        try {
          var doc = job.getStore().getImmutableNode(job.tempDirURI.resolve(fi.uri()));
          return doc.select(hasMaprefWithKeyref).exists();
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
  }
}
