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
package org.jjazz.uisettings.api;

import java.awt.Color;
import javax.swing.UIDefaults;
import org.openide.util.lookup.ServiceProvider;

/**
 * A JJazzLab theme using light colors.
 */
// @ServiceProvider(service = Theme.class)
public class LightTheme implements Theme
{
    
    public static String NAME = "Light Theme";
//    @StaticResource(relative = true)
//    private static final String SPEAKER_ICON_DISABLED_PATH = "resources/SpeakerDisabled-20x20.png";
    
    private UIDefaults uiDefaults;
    
    private String name;
    
    public LightTheme()
    {
        this.name = NAME;
        uiDefaults = new UIDefaults();
                
//        UIDefaults.LazyValue value;        
//        value = tbl -> new ImageIcon(getClass().getResource(SPEAKER_ICON_DISABLED_PATH));
//        uiDefaults.put("speaker.icon.disabled", value);   // Better to return null: let the L&F create the disabled icon
        uiDefaults.put("background.white", new Color(255, 255, 240));        
        uiDefaults.put("mixconsole.background", Color.LIGHT_GRAY);        
        uiDefaults.put("mixchannel.background", new Color(237, 237, 237));        
        uiDefaults.put("bar.selected.background", new Color(209, 238, 246));
        uiDefaults.put("item.selected.background", new Color(128, 212, 235));        
        uiDefaults.put("default.focused.border.color", new Color(16, 65, 242));        
        uiDefaults.put("songpart.focused.border.color", uiDefaults.getColor("default.focused.border.color"));        
        uiDefaults.put("songpart.selected.background", new Color(94, 203, 231));        
    }
    
    @Override
    public UIDefaults getUIDefaults()
    {
        return uiDefaults;
    }
    
    @Override
    public String getName()
    {
        return name;
    }
    
    @Override
    public GeneralUISettings.LookAndFeelId getLookAndFeel()
    {
        return GeneralUISettings.LookAndFeelId.LOOK_AND_FEEL_SYSTEM_DEFAULT;
    }
}
