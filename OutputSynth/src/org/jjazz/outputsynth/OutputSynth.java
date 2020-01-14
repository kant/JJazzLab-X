/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright @2019 Jerome Lelasseux. All rights reserved.
 *
 * This file is part of the JJazzLab-X software.
 *
 * JJazzLab-X is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License (LGPLv3) 
 * as published by the Free Software Foundation, either version 3 of the License, 
 * or (at your option) any later version.
 *
 * JJazzLab-X is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JJazzLab-X.  If not, see <https://www.gnu.org/licenses/>
 *
 * Contributor(s): 
 *
 */
package org.jjazz.outputsynth;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.jjazz.midi.synths.GSSynth;
import org.jjazz.midi.Instrument;
import org.jjazz.midi.InstrumentBank;
import org.jjazz.midi.MidiSynth;
import org.jjazz.midi.synths.StdSynth;
import org.jjazz.midiconverters.api.ConvertersManager;
import org.jjazz.rhythm.api.RhythmVoice;

/**
 * The information about the MidiSynth connected to the Midi output of JJazzLab.
 */
public class OutputSynth implements Serializable
{

    /**
     * oldValue=false if removed, true if added. newValue=InstrumentBank<?>
     */
    public static final String PROP_STD_BANK = "PROP_STD_BANK";
    /**
     * oldValue=false if removed, true if added. newValue=MidiSynth
     */
    public static final String PROP_CUSTOM_SYNTH = "PROP_CUSTOM_SYNTH";
    /**
     * oldValue=old UserInstrument, newValue=new UserInstrument
     */
    public static final String PROP_USER_INSTRUMENT = "PROP_USER_INSTRUMENT";

    private final List<InstrumentBank<?>> compatibleStdBanks;
    private final List<MidiSynth> customSynths;
    private GMRemapTable remapTable;
    private Instrument userInstrument;
    private static final Logger LOGGER = Logger.getLogger(OutputSynth.class.getSimpleName());
    private final transient PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    /**
     * Construct a default OutputSynth compatible with the GM1 Bank and with no custom MidiSynth.
     */
    public OutputSynth()
    {
        compatibleStdBanks = new ArrayList<>();
        customSynths = new ArrayList<>();
        remapTable = new GMRemapTable();
        compatibleStdBanks.add(StdSynth.getGM1Bank());
        userInstrument = StdSynth.getGM1Bank().getInstrument(0);  // Piano
    }

    /**
     * Construct a new OutputSynth which copies the values from os.
     *
     * @param os
     */
    public OutputSynth(OutputSynth os)
    {
        compatibleStdBanks = new ArrayList<>();
        customSynths = new ArrayList<>();

        compatibleStdBanks.addAll(os.compatibleStdBanks);
        customSynths.addAll(customSynths);
        userInstrument = os.userInstrument;
        remapTable = new GMRemapTable(os.remapTable);
    }

    public GMRemapTable getGMRemapTable()
    {
        return remapTable;
    }

    /**
     * Get the list of standard InstrumentBanks (GM/GM2/XG/GS) which are compatible with this OutputSynth.
     *
     * @return Can be an empty list.
     */
    public List<InstrumentBank<?>> getCompatibleStdBanks()
    {
        return new ArrayList<>(compatibleStdBanks);
    }

    /**
     * Add a standard bank compatible with this OutputSynth.
     * <p>
     * NOTE: GS and XG/GM2 are incompatible, one will not be added if the other(s) is present.
     *
     * @param stdBank Must be GM/GM2/XG/GS
     */
    public void addCompatibleStdBank(InstrumentBank<?> stdBank)
    {
        if (stdBank == null || !getStdBanks().contains(stdBank))
        {
            throw new IllegalArgumentException("stdBank=" + stdBank);
        }

        if ((stdBank == StdSynth.getGM2Bank() || stdBank == StdSynth.getXGBank()) && compatibleStdBanks.contains(GSSynth.getGSBank()))
        {
            LOGGER.warning("addCompatibleStdBank() Can't add " + stdBank + " because the GS bank is used");
            return;
        } else if (stdBank == GSSynth.getGSBank() && (compatibleStdBanks.contains(StdSynth.getGM2Bank()) || compatibleStdBanks.contains(StdSynth.getXGBank())))
        {
            LOGGER.warning("addCompatibleStdBank() Can't add " + stdBank + " because the GM2 or XG bank is used");
            return;
        }

        if (!compatibleStdBanks.contains(this))
        {
            compatibleStdBanks.add(stdBank);
            pcs.firePropertyChange(PROP_STD_BANK, true, stdBank);
        }
    }

    /**
     * Remove a standard bank compatible with this OutputSynth.
     *
     * @param stdBank
     */
    public void removeCompatibleStdBank(InstrumentBank<?> stdBank)
    {
        if (stdBank == null)
        {
            throw new IllegalArgumentException("stdBank=" + stdBank);
        }

        if (compatibleStdBanks.remove(stdBank))
        {
            pcs.firePropertyChange(PROP_STD_BANK, false, stdBank);
        }
    }

    /**
     * Get the list of custom MidiSynths which are compatible with this OutputSynth.
     *
     * @return Can be an empty list.
     */
    public List<MidiSynth> getCustomSynths()
    {
        return new ArrayList<>(customSynths);
    }

    /**
     * Add a custom MidiSynth compatible with this OutputSynth.
     * <p>
     *
     * @param synth
     */
    public void addCustomSynth(MidiSynth synth)
    {
        if (synth == null)
        {
            throw new IllegalArgumentException("stdBank=" + synth);
        }

        if (!customSynths.contains(synth))
        {
            customSynths.add(synth);
            for (InstrumentBank<?> bank : getCompatibleStdBanks(synth))
            {
                if (!compatibleStdBanks.contains(bank))
                {
                    addCompatibleStdBank(bank);
                }
            }       
            pcs.firePropertyChange(PROP_CUSTOM_SYNTH, true, synth);
        }
    }

    /**
     * Remove a custom MidiSynth compatible with this OutputSynth.
     * <p>
     *
     * @param synth
     */
    public void removeCustomSynth(MidiSynth synth)
    {
        if (synth == null || synth == StdSynth.getInstance())
        {
            throw new IllegalArgumentException("stdBank=" + synth);
        }
        if (customSynths.remove(synth))
        {
            pcs.firePropertyChange(PROP_CUSTOM_SYNTH, false, synth);
        }
    }

    /**
     * Get an instrument compatible with this OutputSynth for the specified rhythm voice.
     * <p>
     * Search a matching instrument in the following order :<br>
     * - in the custom synths <br>
     * - in the compatible banks<br>
     * - in the GMRemapTable<br>
     * - Use the substitute or, if , create a custom instrument for the rv.GetPreferredInstrument().
     *
     * @param rv
     * @return Can't be null. It may be the VoidInstrument for drums/percussion.
     */
    public Instrument findInstrument(RhythmVoice rv)
    {
        Instrument rvIns = rv.getPreferredInstrument();
        assert rvIns != null : "rv=" + rv;
        ConvertersManager cm = ConvertersManager.getInstance();
        Instrument ins = null;
        // Try with custom synths first
        for (MidiSynth synth : customSynths)
        {
            ins = cm.convertInstrument(rvIns, synth);
            if (ins != null)
            {
                break;
            }
        }
        if (ins == null)
        {
            // Try with the standard banks
            ins = cm.convertInstrument(rvIns, StdSynth.getInstance());
        }
        if (ins == null)
        {
            // Use the GM1Substitute or its remaps
            switch (rv.getType())
            {
                case DRUMS:
                    ins = remapTable.getInstrument(GMRemapTable.DRUMS_INSTRUMENT);
                    if (ins == null)
                    {
                        ins = StdSynth.getVoidInstrument();
                    }
                    break;
                case PERCUSSION:
                    ins = remapTable.getInstrument(GMRemapTable.PERCUSSION_INSTRUMENT);
                    if (ins == null)
                    {
                        ins = StdSynth.getVoidInstrument();
                    }
                    break;
                default:
                    // Use the default GM1 instrument
                    ins = remapTable.getInstrument(rvIns.getSubstitute());
                    if (ins == null)
                    {
                        // No remap: use the substitute
                        ins = rvIns.getSubstitute();
                    }
                    break;
            }
        }
        assert ins != null : "rv=" + rv;
        return ins;
    }

    /**
     * The instrument for the user channel.
     *
     * @return Can't be null.
     */
    public Instrument getUserInstrument()
    {
        return userInstrument;
    }

    /**
     * Set the instrument for the user channel.
     *
     * @param ins Can't be null
     */
    public void setUserInstrument(Instrument ins)
    {
        if (ins == null)
        {
            throw new IllegalArgumentException("ins=" + ins);
        }
        Instrument oldIns = userInstrument;
        if (oldIns != userInstrument)
        {
            userInstrument = ins;
            pcs.firePropertyChange(PROP_USER_INSTRUMENT, oldIns, ins);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l)
    {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l)
    {
        pcs.removePropertyChangeListener(l);
    }

    // ========================================================================================
    // Private methods
    // ========================================================================================    
    /**
     * Check if the specified synth is compatible with standard banks.
     *
     * @param synth
     * @return The list of standard banks compatible with synth.
     */
    private List<InstrumentBank<?>> getCompatibleStdBanks(MidiSynth synth)
    {
        ArrayList<InstrumentBank<?>> res = new ArrayList<>();
        for (InstrumentBank<?> stdBank : getStdBanks())
        {
            float coverage = synth.getMidiAddressMatchingCoverage(stdBank);
            if (coverage > 0.8f)
            {
                res.add(stdBank);
            }
        }
        return res;
    }

    private List<InstrumentBank<?>> getStdBanks()
    {
        ArrayList<InstrumentBank<?>> res = new ArrayList<>();
        res.addAll(StdSynth.getInstance().getBanks());
        res.add(GSSynth.getGSBank());
        return res;
    }
}
