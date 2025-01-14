/*
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 *  Copyright @2019 Jerome Lelasseux. All rights reserved.
 *
 *  This file is part of the JJazzLabX software.
 *   
 *  JJazzLabX is free software: you can redistribute it and/or modify
 *  it under the terms of the Lesser GNU General Public License (LGPLv3) 
 *  as published by the Free Software Foundation, either version 3 of the License, 
 *  or (at your option) any later version.
 *
 *  JJazzLabX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with JJazzLabX.  If not, see <https://www.gnu.org/licenses/>
 * 
 *  Contributor(s): 
 */
package org.jjazz.ui.rpviewer.api;

import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.event.ChangeListener;

/**
 * A renderer for a RpViewer.
 * 
 * <p>
 */
public interface RpViewerRenderer
{       
    /**
     * Set the target RpViewer for which we perform the rendering.
     *
     * @param rpv
     */
    void setRpViewer(RpViewer rpv);

    /**
     * The target RpViewer for which we perform the rendering.
     *
     * @return
     */
    RpViewer getRpViewer();

    /**
     * The preferred size of this Renderer.
     *
     * @return
     */
    Dimension getPreferredSize();

    /**
     * Render the RpViewer.
     *
     * @param g
     */
    void paintComponent(Graphics g);

    /**
     * A change event is fired when this RpRenderer configuration has changed for some reason (e.g. user has changed some
     * settings).
     * <p>
     * A change event means the preferredSize() and/or the paintComponent() behavior might have changed.
     *
     * @param l
     */
    void addChangeListener(ChangeListener l);

    void removeChangeListener(ChangeListener l);
}
