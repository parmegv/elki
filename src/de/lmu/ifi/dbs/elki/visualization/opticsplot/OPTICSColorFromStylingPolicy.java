package de.lmu.ifi.dbs.elki.visualization.opticsplot;

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

import de.lmu.ifi.dbs.elki.result.optics.ClusterOrderEntry;
import de.lmu.ifi.dbs.elki.visualization.colors.ColorLibrary;
import de.lmu.ifi.dbs.elki.visualization.style.StylingPolicy;

/**
 * Adapter that uses a styling policy to colorize the OPTICS plot.
 * 
 * @author Erich Schubert
 * 
 * @apiviz.uses ColorLibrary
 */
public class OPTICSColorFromStylingPolicy implements OPTICSColorAdapter {
  /**
   * The styling policy
   */
  private StylingPolicy policy;

  /**
   * Constructor.
   * 
   * @param colors Color library to use
   * @param refc Clustering to use
   */
  public OPTICSColorFromStylingPolicy(ColorLibrary colors, StylingPolicy policy) {
    super();
    this.policy = policy;
  }

  @Override
  public int getColorForEntry(ClusterOrderEntry<?> coe) {
    return policy.getColorForDBID(coe.getID());
  }
}