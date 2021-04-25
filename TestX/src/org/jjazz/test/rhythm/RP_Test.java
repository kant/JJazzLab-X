package org.jjazz.test.rhythm;

import java.util.logging.Logger;
import org.jjazz.rhythm.api.RhythmParameter;
import org.jjazz.rhythm.api.RhythmVoice;
import org.jjazz.rpcustomeditor.spi.RpCustomEditor;
import org.jjazz.rpcustomeditor.spi.RpCustomEditorProvider;
import org.jjazz.ui.rpviewer.api.RpRenderer;
import org.jjazz.ui.rpviewer.spi.DefaultRpRendererFactory;
import org.jjazz.ui.rpviewer.spi.RpRendererFactory;
import org.jjazz.ui.rpviewer.spi.RpViewerSettings;

/**
 *
 * @author Administrateur
 */
public class RP_Test implements RhythmParameter<RP_TestValue>, RpCustomEditorProvider<RP_TestValue>, RpRendererFactory
{

    private RhythmVoice rhythmVoice;
    private static final Logger LOGGER = Logger.getLogger(RP_Test.class.getSimpleName());

    public RP_Test(RhythmVoice rv)
    {
        if (rv == null)
        {
            throw new NullPointerException("rv");
        }
        rhythmVoice = rv;
    }

    /**
     * @return The RhythmVoices impacted by this
     */
    public RhythmVoice getRhythmVoice()
    {
        return rhythmVoice;
    }

    @Override
    public String getId()
    {
        return "RP_TestId";
    }

    @Override
    public String getDisplayName()
    {
        return "RP_Test";
    }

    @Override
    public String getDescription()
    {
        return "RP_Test-desc";
    }

    @Override
    public String getValueDescription(RP_TestValue value)
    {
        return rhythmVoice.getName() + ": " + value.toDescriptionString();
    }

    @Override
    public RP_TestValue getDefaultValue()
    {
        return new RP_TestValue();
    }

    @Override
    public String valueToString(RP_TestValue v)
    {
        return RP_TestValue.saveAsString(v);
    }

    @Override
    public RP_TestValue stringToValue(String s)
    {
        return RP_TestValue.loadFromString(s);
    }

    @Override
    public boolean isValidValue(RP_TestValue value)
    {
        return value instanceof RP_TestValue;
    }

    // ======================================================================================
    // RpCustomEditorProvider
    // ======================================================================================    
    @Override
    public RpCustomEditor<RP_TestValue> getCustomEditor()
    {
        return new RP_TestCustomEditor(this);
    }

    // ======================================================================================
    // RpRendererFactory interface
    // ======================================================================================  
    @Override
    public RpRenderer getRpRenderer(RhythmParameter<?> rp, RpViewerSettings settings)
    {
        if (rp != this || settings == null)
        {
            throw new IllegalArgumentException("rp=" + rp + " is different than this=" + this + " settings=" + settings);
        }
        return DefaultRpRendererFactory.getDefault().getRpRenderer(DefaultRpRendererFactory.Type.STRING, RpViewerSettings.getDefault());
    }

}