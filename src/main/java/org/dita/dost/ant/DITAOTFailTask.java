/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.ant;

import static org.dita.dost.ant.ExtensibleAntInvoker.isValid;
import static org.dita.dost.log.MessageBean.*;

import java.util.ArrayList;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Exit;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.apache.tools.ant.taskdefs.condition.ConditionBase;
import org.dita.dost.ant.ExtensibleAntInvoker.ParamElem;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTAntLogger;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageBean;
import org.dita.dost.log.MessageUtils;

/**
 * Ant echo task for custom error message.
 */
public final class DITAOTFailTask extends Exit {

  private String id = null;

  /** Nested params. */
  private final ArrayList<ParamElem> params = new ArrayList<>();

  /**
   * Default Construtor.
   *
   */
  public DITAOTFailTask() {}

  /**
   * Set the id.
   * @param identifier The id to set.
   *
   */
  public void setId(final String identifier) {
    id = identifier;
  }

  /**
   * Handle nested parameters. Add the key/value to the pipeline hash, unless
   * the "if" attribute is set and refers to a unset property.
   * @return parameter
   */
  public ParamElem createParam() {
    final ParamElem p = new ParamElem();
    params.add(p);
    return p;
  }

  /**
   * Task execute point.
   * @throws BuildException exception
   * @see org.apache.tools.ant.taskdefs.Exit#execute()
   */
  @Override
  public void execute() throws BuildException {
    if (ifCondition != null) {
      final String msg = MessageUtils.getMessage("DOTA014W", "if", "if:set").setLocation(this.getLocation()).toString();
      getProject().log(msg, Project.MSG_WARN);
    }
    if (unlessCondition != null) {
      final String msg = MessageUtils
        .getMessage("DOTA014W", "unless", "unless:set")
        .setLocation(this.getLocation())
        .toString();
      getProject().log(msg, Project.MSG_WARN);
    }
    final boolean fail = nestedConditionPresent()
      ? testNestedCondition()
      : (testIfCondition() && testUnlessCondition());
    if (!fail) {
      return;
    }

    if (id == null) {
      throw new BuildException("id attribute must be specified");
    }

    final MessageBean msgBean = MessageUtils.getMessage(id, readParamValues());
    final DITAOTLogger logger = new DITAOTAntLogger(getProject());
    if (msgBean != null) {
      final String type = msgBean.getType();
      if (FATAL.equals(type)) {
        setMessage(msgBean.toString());
        try {
          super.execute();
        } catch (final BuildException ex) {
          throw new BuildException(msgBean.toString(), new DITAOTException(msgBean, ex, msgBean.toString()));
        }
      } else if (ERROR.equals(type)) {
        logger.error(msgBean.toString());
      } else if (WARN.equals(type)) {
        logger.warn(msgBean.toString());
      } else if (INFO.equals(type)) {
        logger.info(msgBean.toString());
      } else if (DEBUG.equals(type)) {
        logger.debug(msgBean.toString());
      }
    }
  }

  /**
   * Read parameter values to an array.
   *
   * @return parameter values where array index corresponds to parameter name
   */
  private String[] readParamValues() throws BuildException {
    final ArrayList<String> prop = new ArrayList<>();
    for (final ParamElem p : params) {
      if (!p.isValid()) {
        throw new BuildException("Incomplete parameter");
      }
      if (isValid(getProject(), getLocation(), p.getIf(), p.getUnless())) {
        final int idx = Integer.parseInt(p.getName()) - 1;
        if (idx >= prop.size()) {
          prop.ensureCapacity(idx + 1);
          while (prop.size() < idx + 1) {
            prop.add(null);
          }
        }
        prop.set(idx, p.getValue());
      }
    }
    return prop.toArray(new String[0]);
  }

  // Ant Exit class methods --------------------------------------------------

  private static class NestedCondition extends ConditionBase implements Condition {

    @Override
    public boolean eval() {
      if (countConditions() != 1) {
        throw new BuildException("A single nested condition is required.");
      }
      return getConditions().nextElement().eval();
    }
  }

  private String message;
  private String ifCondition, unlessCondition;
  private NestedCondition nestedCondition;
  private Integer status;

  /**
   * A message giving further information on why the build exited.
   *
   * @param value message to output
   */
  @Override
  public void setMessage(final String value) {
    message = value;
  }

  /**
   * Only fail if a property of the given name exists in the current project.
   * @param c property name
   */
  @Override
  public void setIf(final String c) {
    ifCondition = c;
  }

  /**
   * Only fail if a property of the given name does not
   * exist in the current project.
   * @param c property name
   */
  @Override
  public void setUnless(final String c) {
    unlessCondition = c;
  }

  /**
   * Set the status code to associate with the thrown Exception.
   * @param i   the <code>int</code> status
   */
  @Override
  public void setStatus(final int i) {
    status = i;
  }

  /**
   * Add a condition element.
   * @return <code>ConditionBase</code>.
   * @since Ant 1.6.2
   */
  @Override
  public ConditionBase createCondition() {
    if (nestedCondition != null) {
      throw new BuildException("Only one nested condition is allowed.");
    }
    nestedCondition = new NestedCondition();
    return nestedCondition;
  }

  /**
   * test the if condition
   * @return true if there is no if condition, or the named property exists
   */
  private boolean testIfCondition() {
    if (ifCondition == null || "".equals(ifCondition)) {
      return true;
    }
    return getProject().getProperty(ifCondition) != null;
  }

  /**
   * test the unless condition
   * @return true if there is no unless condition,
   *  or there is a named property but it doesn't exist
   */
  private boolean testUnlessCondition() {
    if (unlessCondition == null || "".equals(unlessCondition)) {
      return true;
    }
    return getProject().getProperty(unlessCondition) == null;
  }

  /**
   * test the nested condition
   * @return true if there is none, or it evaluates to true
   */
  private boolean testNestedCondition() {
    final boolean result = nestedConditionPresent();

    if (result && ifCondition != null || unlessCondition != null) {
      throw new BuildException("Nested conditions " + "not permitted in conjunction with if/unless attributes");
    }

    return result && nestedCondition.eval();
  }

  /**
   * test whether there is a nested condition.
   * @return <code>boolean</code>.
   */
  private boolean nestedConditionPresent() {
    return (nestedCondition != null);
  }
}
