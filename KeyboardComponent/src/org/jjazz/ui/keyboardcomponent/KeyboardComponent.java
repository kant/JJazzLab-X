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
package org.jjazz.ui.keyboardcomponent;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.JPanel;

/**
 * A JPanel representing a Piano keyboard with selectable keys.
 * <p>
 * Pressed notes can be shown taking account the velocity value.
 * <p>
 */
public class KeyboardComponent extends JPanel
{
    
    private KeyboardRange keyboardRange;
    
    private final List<PianoKey> pianoKeys = new ArrayList<>();
    private static final Logger LOGGER = Logger.getLogger(KeyboardComponent.class.getSimpleName());

    /**
     * Create a component with 88 notes.
     * <p>
     */
    public KeyboardComponent()
    {
        this(KeyboardRange._88_KEYS);
    }

    /**
     *
     * @param kbdSize KbdSize
     */
    public KeyboardComponent(KeyboardRange kbdSize)
    {
        setKeyboardRange(kbdSize);
    }
    
    @Override
    public String toString()
    {
        return "Keyboard[" + keyboardRange + "]";
    }
    
    public KeyboardRange getRange()
    {
        return keyboardRange;
    }

    /**
     * Set the keyboard size.
     * <p>
     * New PianoKeys are created. Pressed/marked notes are maintained. This updates also the preferred and minimum size. Caller
     * must synchronize this method if other threads update this keyboard in parallel.
     *
     * @param kbdRange
     */
    public final void setKeyboardRange(KeyboardRange kbdRange)
    {
        if (kbdRange == null)
        {
            throw new NullPointerException("kbdRange");
        }
        
        if (kbdRange.equals(keyboardRange))
        {
            return;
        }

        // Save state
        Map<Integer, Color> pressedColors = new HashMap<>();
        Map<Integer, Integer> pressedVelocities = new HashMap<>();
        Map<Integer, Color> markedColors = new HashMap<>();
        Map<Integer, Map<String, Color>> colorProps = new HashMap<>();
        pianoKeys.forEach(pk ->
        {
            int pitch = pk.getPitch();
            if (pk.isPressed())
            {
                pressedColors.put(pitch, pk.getPressedWhiteKeyColor());
                pressedVelocities.put(pitch, pk.getVelocity());
            }
            Color mColor = pk.getMarkedColor();
            if (mColor != null)
            {
                markedColors.put(pitch, mColor);
            }            
            colorProps.put(pitch, pk.getColorProperties());
        });


        // Update keys and restore state
        keyboardRange = kbdRange;
        
        
        removeAll();
        pianoKeys.clear();
        
        for (int i = keyboardRange.getLowestPitch(); i <= keyboardRange.getHighestPitch(); i++)
        {
            boolean leftmost = (i == keyboardRange.getLowestPitch());
            boolean rightmost = (i == keyboardRange.getHighestPitch());
            PianoKey key = new PianoKey(i, leftmost, rightmost);
            pianoKeys.add(key);
            add(key);

            // Restore state
            Color c = pressedColors.get(i);
            if (c != null)
            {
                key.setPressed(pressedVelocities.get(i), c);
            }
            key.setMarked(markedColors.get(i));
            var mapPropColors = colorProps.get(i);
            if (mapPropColors != null)
            {
                key.setColorProperties(mapPropColors);
            }
        }

        // Set preferred size
        Insets in = getInsets();
        int w = (PianoKey.WH * keyboardRange.getNbWhiteKeys()) + in.left + in.right + 1;
        setPreferredSize(new Dimension(w, PianoKey.WH));

        // Set minimum size
        setMinimumSize(new Dimension(getRange().getNbWhiteKeys() * PianoKey.WW_MIN, PianoKey.WH_MIN));
        
        
        revalidate();
        repaint();
    }

    /**
     * Get all the PianoKeys.
     *
     * @return
     */
    public List<PianoKey> getPianoKeys()
    {
        return new ArrayList<>(pianoKeys);
    }
    
    public List<PianoKey> getBlackKeys()
    {
        return pianoKeys.stream().filter(pk -> !pk.isWhiteKey()).collect(Collectors.toList());
    }
    
    public List<PianoKey> getWhiteKeys()
    {
        return pianoKeys.stream().filter(pk -> pk.isWhiteKey()).collect(Collectors.toList());
    }

    /**
     * Get the PianoKey for specified pitch.
     * <p>
     *
     * @param pitch
     * @return Can be null if pitch is out of range.
     */
    public PianoKey getPianoKey(int pitch)
    {
        return keyboardRange.isValid(pitch) ? pianoKeys.get(pitch - keyboardRange.lowPitch) : null;
    }
    
    @Override
    public void setEnabled(boolean b)
    {
        super.setEnabled(b);
        for (PianoKey c : pianoKeys)
        {
            c.setEnabled(b);
        }
    }

    /**
     * Set all keys unpressed, remove all marks.
     */
    public void reset()
    {
        pianoKeys.forEach(pk ->
        {
            pk.setReleased();
            pk.setMarked(null);
        });
    }

    /**
     * Release the specified key.
     *
     * @param pitch
     */
    public void setReleased(int pitch)
    {
        if (keyboardRange.isValid(pitch))
        {
            getPianoKey(pitch).setReleased();
        }
    }

    /**
     * Set the pressed status of specified key.
     * <p>
     * Method just delegates to setVelocity() of the relevant PianoKey. Do nothing if pitch is not valid for this KeyboardRange.
     *
     * @param pitch
     * @param velocity If 0 equivalent to calling setReleased()
     * @param pressedKeyColor The pressed key color to be used. If null use default color.
     */
    public void setPressed(int pitch, int velocity, Color pressedKeyColor)
    {
        if (keyboardRange.isValid(pitch))
        {
            getPianoKey(pitch).setPressed(velocity, pressedKeyColor);
        }
    }

    /**
     * Get the pressed velocity of a specific key.
     * <p>
     * Method delegates to getVelocity() of the relevant PianoKey.
     *
     * @param pitch The pitch of the key. Must be a valid pitch for the KeyboardRange.
     *
     * @return If 0 it means the key is released.
     */
    public int getPressedVelocity(int pitch)
    {
        if (!keyboardRange.isValid(pitch))
        {
            throw new IllegalArgumentException("pitch=" + pitch + " keyboardRange=" + keyboardRange);
        }
        return getPianoKey(pitch).getVelocity();
    }

    /**
     * Get the PianoKey that correspond to a specific point.
     *
     * @param p A Point object relative to this component.
     *
     * @return Can be null.
     */
    public PianoKey getPianokey(Point p)
    {
        Component c = this.getComponentAt(p.x, p.y);
        if (c instanceof PianoKey)
        {
            return (PianoKey) c;
        }
        return null;
    }

    /**
     * Layout the keys to fit the size.
     * <p>
     * Because of integer rounding errors, it may not fit exactly the required dimensions. The keyboard is centered inside the
     * box.
     */
    @Override
    public void doLayout()
    {
        Insets in = getInsets();
        Rectangle r = new Rectangle(in.left, in.top, getWidth() - in.left - in.right, getHeight() - in.top - in.bottom);


        // Keyboard takes all the horizontal space
        int wKeyHeight = computeKeyboardHeightFromWidth(r.width);
        wKeyHeight = Math.max(wKeyHeight, getMinimumSize().height);  // Can't be smaller than minimal height
        wKeyHeight = Math.min(wKeyHeight, r.height);      // Can't be taller than available height


        // Size of a white key
        int wKeyWidth = (r.width - 1) / keyboardRange.getNbWhiteKeys();


        // Calculate X so the keyboard will be centered (because of integer rounding we may have differences
        // between the object size and the real keyboard size)
        int realSize = wKeyWidth * keyboardRange.getNbWhiteKeys();
        int x_pos = ((r.width - realSize) / 2) + r.x;

        // Y centered
        // int y_pos = r.y + (r.height - wKeyHeight) / 2;
        int y_pos = r.y;
        
        
        for (PianoKey key : pianoKeys)
        {
            // adjust size
            key.setRelativeSize(wKeyWidth, wKeyHeight);

            // translate it to put it after the last key
            key.setLocation(x_pos, y_pos);

            // adjust with top x size of the key
            x_pos += key.getNextKeyPos();
        }
    }

    //--------------------------------------------------------------------
    // Private methods
    //--------------------------------------------------------------------
    /**
     * Calculate the keyboard height from specified keyboard width in order to maintain the optimal aspect ratio.
     *
     * @param w The target keyboard width.
     * @return The keyboard height.
     */
    private int computeKeyboardHeightFromWidth(int w)
    {
        // Adapt to minimize integer rounding errors
        int wKeyWidth = (w - 1) / keyboardRange.getNbWhiteKeys();
        int h = (int) (wKeyWidth * ((float) PianoKey.WH / PianoKey.WW));
        return h;
    }
    
}
