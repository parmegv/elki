package de.lmu.ifi.dbs.elki.visualization.visualizers.parallel;

/*
 This file is part of ELKI:
 Environment for Developing KDD-Applications Supported by Index-Structures

 Copyright (C) 2012
 Ludwig-Maximilians-Universität München
 Lehr- und Forschungseinheit für Datenbanksysteme
 ELKI Development Team

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.Collection;

import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.result.HierarchicalResult;
import de.lmu.ifi.dbs.elki.result.Result;
import de.lmu.ifi.dbs.elki.result.ResultUtil;
import de.lmu.ifi.dbs.elki.visualization.VisualizationTask;
import de.lmu.ifi.dbs.elki.visualization.css.CSSClass;
import de.lmu.ifi.dbs.elki.visualization.projector.ParallelPlotProjector;
import de.lmu.ifi.dbs.elki.visualization.style.StyleLibrary;
import de.lmu.ifi.dbs.elki.visualization.svg.SVGArrow;
import de.lmu.ifi.dbs.elki.visualization.svg.SVGPlot;
import de.lmu.ifi.dbs.elki.visualization.svg.SVGUtil;
import de.lmu.ifi.dbs.elki.visualization.visualizers.AbstractVisFactory;
import de.lmu.ifi.dbs.elki.visualization.visualizers.Visualization;

/**
 * Interactive SVG-Elements for reordering the axes.
 * 
 * @author Robert Rödler
 * @author Erich Schubert
 * 
 * @apiviz.stereotype factory
 * @apiviz.uses Instance oneway - - «create»
 */
public class AxisReorderVisualization extends AbstractVisFactory {
  /**
   * A short name characterizing this Visualizer.
   */
  private static final String NAME = "Dimension Ordering Tool";

  /**
   * Constructor, adhering to
   */
  public AxisReorderVisualization() {
    super();
  }

  @Override
  public Visualization makeVisualization(VisualizationTask task) {
    return new Instance(task);
  }

  @Override
  public void processNewResult(HierarchicalResult baseResult, Result result) {
    Collection<ParallelPlotProjector<?>> ps = ResultUtil.filterResults(result, ParallelPlotProjector.class);
    for(ParallelPlotProjector<?> p : ps) {
      final VisualizationTask task = new VisualizationTask(NAME, p, p.getRelation(), this);
      task.level = VisualizationTask.LEVEL_INTERACTIVE;
      task.noexport = true;
      task.thumbnail = false;
      baseResult.getHierarchy().add(p, task);
    }
  }

  /**
   * Instance for a particular plot.
   * 
   * @author Robert Rödler
   * @author Erich Schubert
   */
  public class Instance extends AbstractParallelVisualization<NumberVector> {
    /**
     * Generic tags to indicate the type of element. Used in IDs, CSS-Classes
     * etc.
     */
    public static final String SELECTDIMENSIONORDER = "SelectDimensionOrder";

    /**
     * CSS class for a tool button
     */
    public static final String SDO_BUTTON = "DObutton";

    /**
     * CSS class for a button border
     */
    public static final String SDO_BORDER = "DOborder";

    /**
     * CSS class for a button cross
     */
    public static final String SDO_ARROW = "DOarrow";

    /**
     * Currently selected dimension. Use -1 to not have a dimension selected.
     */
    private int selecteddim = -1;

    /**
     * Constructor.
     * 
     * @param task VisualizationTask
     */
    public Instance(VisualizationTask task) {
      super(task);
      incrementalRedraw();
      context.addResultListener(this);
    }

    @Override
    protected void redraw() {
      addCSSClasses(svgp);
      final int dim = proj.getVisibleDimensions();

      final double controlsize = 0.025 * getSizeY();
      final double buttonsize = 0.75 * controlsize;
      final double padding = 0.125 * controlsize;
      final double arrowsize = .75 * buttonsize;
      final double ypos = getSizeY() + getMarginTop() * .5 + controlsize;
      final double spacing = 0.9 * controlsize;

      Element back = svgp.svgRect(-controlsize * .5, ypos, getSizeX() + controlsize, controlsize);
      SVGUtil.addCSSClass(back, SELECTDIMENSIONORDER);
      layer.appendChild(back);

      if(selecteddim < 0) {
        // Nothing selected
        for(int i = 0; i < dim; i++) {
          final double xpos = getVisibleAxisX(i);
          if(i > 0) {
            Element arrow = SVGArrow.makeArrow(svgp, SVGArrow.LEFT, xpos - spacing, ypos + controlsize * .5, arrowsize);
            SVGUtil.addCSSClass(arrow, SDO_ARROW);
            layer.appendChild(arrow);
            Element button = svgp.svgRect(xpos - spacing - buttonsize * .5, ypos + padding, buttonsize, buttonsize);
            SVGUtil.addCSSClass(button, SDO_BUTTON);
            addEventListener(button, i, SVGArrow.LEFT);
            layer.appendChild(button);
          }
          {
            Element arrow = SVGArrow.makeArrow(svgp, SVGArrow.DOWN, xpos, ypos + controlsize * .5, arrowsize);
            SVGUtil.addCSSClass(arrow, SDO_ARROW);
            layer.appendChild(arrow);
            Element button = svgp.svgRect(xpos - buttonsize * .5, ypos + padding, buttonsize, buttonsize);
            SVGUtil.addCSSClass(button, SDO_BUTTON);
            addEventListener(button, i, SVGArrow.DOWN);
            layer.appendChild(button);
          }
          if(i < dim - 1) {
            Element arrow = SVGArrow.makeArrow(svgp, SVGArrow.RIGHT, xpos + spacing, ypos + controlsize * .5, arrowsize);
            SVGUtil.addCSSClass(arrow, SDO_ARROW);
            layer.appendChild(arrow);
            Element button = svgp.svgRect(xpos + spacing - buttonsize * .5, ypos + padding, buttonsize, buttonsize);
            SVGUtil.addCSSClass(button, SDO_BUTTON);
            addEventListener(button, i, SVGArrow.RIGHT);
            layer.appendChild(button);
          }
        }
      }
      else {
        for(int i = 0; i < dim; i++) {
          {
            Element arrow = SVGArrow.makeArrow(svgp, SVGArrow.DOWN, getVisibleAxisX(i), ypos + controlsize * .5, arrowsize);
            SVGUtil.addCSSClass(arrow, SDO_ARROW);
            layer.appendChild(arrow);
            Element button = svgp.svgRect(getVisibleAxisX(i) - buttonsize * .5, ypos + padding, buttonsize, buttonsize);
            SVGUtil.addCSSClass(button, SDO_BUTTON);
            addEventListener(button, i, SVGArrow.DOWN);
            layer.appendChild(button);
          }
          if(i > 0.) {
            Element arrow = SVGArrow.makeArrow(svgp, SVGArrow.UP, getVisibleAxisX(i - .5), ypos + controlsize * .5, arrowsize);
            SVGUtil.addCSSClass(arrow, SDO_ARROW);
            layer.appendChild(arrow);
            Element button = svgp.svgRect(getVisibleAxisX(i - .5) - buttonsize * .5, ypos + padding, buttonsize, buttonsize);
            SVGUtil.addCSSClass(button, SDO_BUTTON);
            addEventListener(button, i, SVGArrow.UP);
            layer.appendChild(button);
          }
        }
      }
    }

    /**
     * Add an event listener to the Element
     * 
     * @param tag Element to add the listener
     * @param i represented axis
     */
    private void addEventListener(final Element tag, final int i, final SVGArrow.Direction j) {
      EventTarget targ = (EventTarget) tag;
      targ.addEventListener(SVGConstants.SVG_EVENT_CLICK, new EventListener() {
        @Override
        public void handleEvent(Event evt) {
          if(selecteddim < 0) {
            switch(j){
            case DOWN:
              selecteddim = i;
              break;
            case LEFT:
              int prev = i - 1;
              while(prev >= 0 && !proj.isAxisVisible(prev)) {
                prev -= 1;
              }
              proj.swapAxes(i, prev);
              break;
            case RIGHT:
              int next = i + 1;
              while(next < proj.getInputDimensionality() - 1 && !proj.isAxisVisible(next)) {
                next += 1;
              }
              proj.swapAxes(i, next);
              break;
            default:
              break;
            }
          }
          else {
            switch(j){
            case DOWN:
              proj.swapAxes(selecteddim, i);
              selecteddim = -1;
              break;
            case UP:
              if(selecteddim != i) {
                proj.moveAxis(selecteddim, i);
              }
              selecteddim = -1;
              break;
            default:
              break;
            }
          }
          // Notify
          context.getHierarchy().resultChanged(proj);
        }
      }, false);
    }

    /**
     * Adds the required CSS-Classes
     * 
     * @param svgp SVG-Plot
     */
    private void addCSSClasses(SVGPlot svgp) {
      final StyleLibrary style = context.getStyleResult().getStyleLibrary();
      if(!svgp.getCSSClassManager().contains(SELECTDIMENSIONORDER)) {
        CSSClass cls = new CSSClass(this, SELECTDIMENSIONORDER);
        cls.setStatement(SVGConstants.CSS_OPACITY_PROPERTY, 0.1);
        cls.setStatement(SVGConstants.CSS_FILL_PROPERTY, SVGConstants.CSS_BLUE_VALUE);
        svgp.addCSSClassOrLogError(cls);
      }
      if(!svgp.getCSSClassManager().contains(SDO_BORDER)) {
        CSSClass cls = new CSSClass(this, SDO_BORDER);
        cls.setStatement(SVGConstants.CSS_STROKE_PROPERTY, SVGConstants.CSS_GREY_VALUE);
        cls.setStatement(SVGConstants.CSS_STROKE_WIDTH_PROPERTY, style.getLineWidth(StyleLibrary.PLOT) / 3.0);
        cls.setStatement(SVGConstants.CSS_FILL_PROPERTY, SVGConstants.CSS_NONE_VALUE);
        svgp.addCSSClassOrLogError(cls);
      }
      if(!svgp.getCSSClassManager().contains(SDO_BUTTON)) {
        CSSClass cls = new CSSClass(this, SDO_BUTTON);
        cls.setStatement(SVGConstants.CSS_OPACITY_PROPERTY, 0.01);
        cls.setStatement(SVGConstants.CSS_FILL_PROPERTY, SVGConstants.CSS_GREY_VALUE);
        cls.setStatement(SVGConstants.CSS_CURSOR_PROPERTY, SVGConstants.CSS_POINTER_VALUE);
        svgp.addCSSClassOrLogError(cls);
      }
      if(!svgp.getCSSClassManager().contains(SDO_ARROW)) {
        CSSClass cls = new CSSClass(this, SDO_ARROW);
        cls.setStatement(SVGConstants.CSS_STROKE_PROPERTY, SVGConstants.CSS_DARKGREY_VALUE);
        cls.setStatement(SVGConstants.CSS_STROKE_WIDTH_PROPERTY, style.getLineWidth(StyleLibrary.PLOT) / 3);
        cls.setStatement(SVGConstants.CSS_FILL_PROPERTY, SVGConstants.CSS_BLACK_VALUE);
        svgp.addCSSClassOrLogError(cls);
      }
    }
  }
}