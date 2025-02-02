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
package org.jjazz.ui.rpviewer;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jjazz.rhythm.api.RhythmParameter;
import org.jjazz.ui.rpviewer.api.RpViewer;
import org.jjazz.ui.rpviewer.spi.StringRpRendererSettings;
import org.jjazz.ui.rpviewer.api.RpViewerRenderer;

/**
 * A simple editor: just display RP value as a string.
 */
public class StringRpRenderer implements RpViewerRenderer, PropertyChangeListener
{

    private static final int STRING_HBORDER_PREF_SIZE = 10;
    /**
     * Extra border to not appear vertically centered because of the rp name label managed by superclass
     */
    private static final int TOP_BORDER = 2;
    // UI variables
    private final StringRpRendererSettings settings;
    private final Function<Object, String> formatter;
    private RpViewer rpViewer;
    private Set<ChangeListener> listeners = new HashSet<>();
    private static final Logger LOGGER = Logger.getLogger(StringRpRenderer.class.getSimpleName());

    public StringRpRenderer(Function<Object, String> formatter, StringRpRendererSettings settings)
    {
        if (formatter == null || settings == null)
        {
            throw new IllegalArgumentException("formatter=" + formatter + " settings=" + settings);   //NOI18N
        }
        this.settings = settings;
        this.settings.addPropertyChangeListener(this);
        this.formatter = formatter;
    }

    @Override
    public void setRpViewer(RpViewer rpViewer)
    {
        this.rpViewer = rpViewer;
    }

    @Override
    public RpViewer getRpViewer()
    {
        return rpViewer;
    }

    /**
     * Preferred size depends on displayed string's size and zoomVFactor for height.
     * <p>
     * @return
     */
    @Override
    public Dimension getPreferredSize()
    {
        // Calculate preferred size from string bounds
        Insets ins = rpViewer.getInsets();
        String strValue = getStringValue();
        FontMetrics fontMetrics = rpViewer.getFontMetrics(settings.getFont());
        int strWidth = fontMetrics.stringWidth(strValue);
        int strHeight = fontMetrics.getMaxAscent() + fontMetrics.getMaxDescent();
        int stringVborderSize = strHeight;

        Dimension prefSize = new Dimension();
        prefSize.width = strWidth + ins.left + ins.right + 2 * STRING_HBORDER_PREF_SIZE;
        prefSize.height = strHeight + TOP_BORDER + ins.top + ins.bottom;
        prefSize.height += Math.round(1.5f * stringVborderSize * rpViewer.getZoomVFactor() / 100f) + 0.75f * stringVborderSize;

        prefSize.width = Math.max(25, prefSize.width);
        prefSize.height = Math.max(20, prefSize.height);

        return prefSize;
    }

    /**
     * Paint the RP value as a string centered.
     *
     * @param g
     */
    @Override
    public void paintComponent(Graphics g)
    {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(settings.getFont());
        g2.setColor(settings.getFontColor());

        Insets ins = rpViewer.getInsets();
        String strValue = getStringValue();
        FontMetrics fontMetrics = rpViewer.getFontMetrics(settings.getFont());
        int strWidth = fontMetrics.stringWidth(strValue);
        int strHeight = fontMetrics.getMaxAscent() + fontMetrics.getMaxDescent();
        float x = ins.left + (rpViewer.getWidth() - strWidth - ins.left - ins.right) / 2f;
        if (x < ins.left)
        {
            x = ins.left;
        }
        float y = ins.top + TOP_BORDER + (rpViewer.getHeight() - strHeight - ins.top - ins.bottom) / 2f;
        y += fontMetrics.getMaxAscent();
        g2.drawString(strValue, x, y);
    }

    @Override
    public void addChangeListener(ChangeListener l)
    {
        listeners.add(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l)
    {
        listeners.remove(l);
    }

    // ---------------------------------------------------------------
    // Implements the PropertyChangeListener interface
    // ---------------------------------------------------------------    
    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getSource() == settings)
        {
            fireChanged();
        }
    }

    // ---------------------------------------------------------------
    // Private methods
    // ---------------------------------------------------------------  
    private void fireChanged()
    {
        ChangeEvent evt = new ChangeEvent(this);
        listeners.stream().forEach(l -> l.stateChanged(evt));
    }

    private String getStringValue()
    {
        RhythmParameter rp = rpViewer.getRpModel();
        Object value = rpViewer.getSptModel().getRPValue(rp);
        String str = rp.getDisplayValue(value);
        return formatter.apply(str);
    }
}
