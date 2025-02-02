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
package org.jjazz.ui.utilities.api;

import java.awt.Dimension;
import javax.swing.JPanel;

/**
 * A JPanel whose preferred size keeps its aspect ratio.
 */
public class ProportionalPanel extends JPanel
{

    private double widthOverHeightRatio;

    /**
     * Uses an A4 portrait ratio.
     */
    public ProportionalPanel()
    {
        this(210, 297);
    }

    public ProportionalPanel(int w, int h)
    {
        this((double) w / h);
    }

    public ProportionalPanel(double widthOverHeightRatio)
    {
        this.widthOverHeightRatio = widthOverHeightRatio;
    }

    /**
     * @return the widthOverHeightRatio
     */
    public double getWidthOverHeightRatio()
    {
        return widthOverHeightRatio;
    }

    /**
     * @param widthOverHeightRatio the widthOverHeightRatio to set
     */
    public void setWidthOverHeightRatio(double widthOverHeightRatio)
    {
        if (widthOverHeightRatio < 0.0001f)
        {
            throw new IllegalArgumentException("widthOverHeightRatio=" + widthOverHeightRatio);   //NOI18N
        }
        this.widthOverHeightRatio = widthOverHeightRatio;
        revalidate();

    }

    @Override
    public Dimension getPreferredSize()
    {
        return new ProportionalDimension(super.getPreferredSize(), widthOverHeightRatio);
    }

}
