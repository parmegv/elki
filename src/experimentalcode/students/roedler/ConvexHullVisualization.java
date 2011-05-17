package experimentalcode.students.roedler;

import java.util.Collection;
import java.util.Iterator;

import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Element;

import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.model.Model;
import de.lmu.ifi.dbs.elki.database.ids.DBID;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.math.DoubleMinMax;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import de.lmu.ifi.dbs.elki.result.Result;
import de.lmu.ifi.dbs.elki.result.ResultUtil;
import de.lmu.ifi.dbs.elki.utilities.iterator.IterableUtil;
import de.lmu.ifi.dbs.elki.utilities.pairs.Pair;
import de.lmu.ifi.dbs.elki.visualization.colors.ColorLibrary;
import de.lmu.ifi.dbs.elki.visualization.css.CSSClass;
import de.lmu.ifi.dbs.elki.visualization.projections.Projection;
import de.lmu.ifi.dbs.elki.visualization.projections.Projection2D;
import de.lmu.ifi.dbs.elki.visualization.style.StyleLibrary;
import de.lmu.ifi.dbs.elki.visualization.svg.SVGPath;
import de.lmu.ifi.dbs.elki.visualization.svg.SVGPlot;
import de.lmu.ifi.dbs.elki.visualization.svg.SVGUtil;
import de.lmu.ifi.dbs.elki.visualization.visualizers.AbstractVisFactory;
import de.lmu.ifi.dbs.elki.visualization.visualizers.Visualization;
import de.lmu.ifi.dbs.elki.visualization.visualizers.VisualizationTask;
import de.lmu.ifi.dbs.elki.visualization.visualizers.VisualizerContext;
import de.lmu.ifi.dbs.elki.visualization.visualizers.VisualizerUtil;
import de.lmu.ifi.dbs.elki.visualization.visualizers.vis2d.P2DVisualization;
import experimentalcode.students.roedler.utils.convexhull.ConvexHull2D;

/**
 * Visualizer for generating an SVG-Element containing the convex hull of each
 * cluster.
 * 
 * @author Robert Rödler
 * 
 * @apiviz.has MeanModel oneway - - visualizes
 * 
 * @param <NV> Type of the NumberVector being visualized.
 */
public class ConvexHullVisualization<NV extends NumberVector<NV, ?>> extends P2DVisualization<NV> {
  /**
   * A short name characterizing this Visualizer.
   */
  private static final String NAME = "Convex Hull Visualization";

  /**
   * Generic tags to indicate the type of element. Used in IDs, CSS-Classes etc.
   */
  public static final String CONVEXHULL = "convexHull";

  /**
   * The result we work on
   */
  Clustering<Model> clustering;

  /**
   * The hulls
   */
  Element hulls;

  /**
   * Constructor
   * 
   * @param task VisualizationTask
   */
  public ConvexHullVisualization(VisualizationTask task) {
    super(task);
    this.clustering = task.getResult();
    context.addContextChangeListener(this);
    incrementalRedraw();
  }

  @Override
  protected void redraw() {
    // Viewport size, for "relative size" computations
    final Pair<DoubleMinMax, DoubleMinMax> viewp = proj.estimateViewport();
    double projarea = (viewp.getFirst().getDiff()) * (viewp.getSecond().getDiff());

    // addCSSClasses(svgp);
    // SVGPath path = new SVGPath();
    ConvexHull2D ch;

    double opacity = 0.25;
    Vector[] chres;

    Iterator<Cluster<Model>> ci = clustering.getAllClusters().iterator();
    int clusterID = 0;

    for(int cnum = 0; cnum < clustering.getAllClusters().size(); cnum++) {
      SVGPath path = new SVGPath();
      Cluster<?> clus = ci.next();

      Vector[] clsPoints = new Vector[clus.getIDs().size()];
      Iterator<DBID> clp = clus.getIDs().iterator();

      for(int i = 0; i < clsPoints.length; i++) {
        DBID clpnum = clp.next();
        double[] projP = proj.fastProjectDataToRenderSpace(rep.get(clpnum).getColumnVector());
        clsPoints[i] = new Vector(projP);
      }
      ch = new ConvexHull2D(clsPoints);
      chres = ch.start();

      if(chres != null) {
        DoubleMinMax mmX = new DoubleMinMax();
        DoubleMinMax mmY = new DoubleMinMax();

        for(int i = 0; i < chres.length; i++) {
          mmX.put(chres[i].get(0));
          mmY.put(chres[i].get(1));
          path.drawTo(chres[i].get(0), chres[i].get(1));
        }
        final double hullarea = Math.abs(mmX.getDiff()) * Math.abs(mmY.getDiff());
        opacity = Math.sqrt(((double) clsPoints.length / rep.size()) * ((projarea - hullarea) / projarea));

        path.close();
      }

      hulls = path.makeElement(svgp);
      addCSSClasses(svgp, cnum, opacity);

      SVGUtil.addCSSClass(hulls, CONVEXHULL + cnum);
      layer.appendChild(hulls);
      clusterID++;
    }
  }

  /**
   * Adds the required CSS-Classes
   * 
   * @param svgp SVG-Plot
   */
  private void addCSSClasses(SVGPlot svgp, int clusterID, double opac) {
    ColorLibrary colors = context.getStyleLibrary().getColorSet(StyleLibrary.PLOT);

    CSSClass cls = new CSSClass(this, CONVEXHULL + clusterID);
    cls.setStatement(SVGConstants.CSS_STROKE_WIDTH_PROPERTY, context.getStyleLibrary().getLineWidth(StyleLibrary.PLOT));

    final String color;
    if(clustering.getAllClusters().size() == 1) {
      color = "black";
    }
    else {
      color = colors.getColor(clusterID);
    }
    cls.setStatement(SVGConstants.CSS_STROKE_PROPERTY, color);
    cls.setStatement(SVGConstants.CSS_FILL_PROPERTY, color);
    cls.setStatement(SVGConstants.CSS_FILL_OPACITY_PROPERTY, opac);

    svgp.addCSSClassOrLogError(cls);
  }

  /**
   * Factory for visualizers to generate an SVG-Element containing the convex
   * hull of a cluster.
   * 
   * @author Robert Rödler
   * 
   * @apiviz.stereotype factory
   * @apiviz.uses ConvexHullVisualization oneway - - «create»
   * 
   * @param <NV> Type of the NumberVector being visualized.
   */

  public static class Factory<NV extends NumberVector<NV, ?>> extends AbstractVisFactory {
    /**
     * Constructor
     */
    public Factory() {
      super();
    }

    @Override
    public Visualization makeVisualization(VisualizationTask task) {
      return new ConvexHullVisualization<NV>(task);
    }

    @Override
    public void addVisualizers(VisualizerContext context, Result result) {
      Iterator<Relation<? extends NumberVector<?, ?>>> reps = VisualizerUtil.iterateVectorFieldRepresentations(context.getDatabase());
      for(Relation<? extends NumberVector<?, ?>> rep : IterableUtil.fromIterator(reps)) {
        // Find clusterings we can visualize:
        Collection<Clustering<?>> clusterings = ResultUtil.filterResults(result, Clustering.class);
        for(Clustering<?> c : clusterings) {
          final VisualizationTask task = new VisualizationTask(NAME, context, c, rep, this, P2DVisualization.class);
          task.put(VisualizationTask.META_LEVEL, VisualizationTask.LEVEL_DATA - 1);
          context.addVisualizer(c, task);
        }
      }
    }

    @Override
    public Class<? extends Projection> getProjectionType() {
      return Projection2D.class;
    }
  }
}
