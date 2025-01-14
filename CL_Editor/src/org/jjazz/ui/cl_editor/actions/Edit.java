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
package org.jjazz.ui.cl_editor.actions;

import org.jjazz.ui.cl_editor.api.CL_ContextActionListener;
import org.jjazz.ui.cl_editor.api.CL_ContextActionSupport;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.jjazz.leadsheet.chordleadsheet.api.Section;
import org.jjazz.leadsheet.chordleadsheet.api.ChordLeadSheet;
import org.jjazz.leadsheet.chordleadsheet.api.UnsupportedEditException;
import org.jjazz.leadsheet.chordleadsheet.api.item.CLI_Section;
import org.jjazz.leadsheet.chordleadsheet.api.item.CLI_ChordSymbol;
import org.jjazz.leadsheet.chordleadsheet.api.item.ChordLeadSheetItem;
import org.jjazz.leadsheet.chordleadsheet.api.item.ExtChordSymbol;
import org.jjazz.leadsheet.chordleadsheet.api.item.Position;
import org.jjazz.quantizer.api.Quantization;
import org.jjazz.ui.cl_editor.spi.CL_BarEditorDialog;
import org.jjazz.ui.cl_editor.spi.Preset;
import org.jjazz.ui.cl_editor.api.CL_EditorTopComponent;
import org.jjazz.ui.cl_editor.api.CL_Editor;
import org.jjazz.ui.cl_editor.api.CL_SelectionUtilities;
import org.jjazz.ui.cl_editor.spi.SectionEditorDialog;
import org.jjazz.ui.cl_editor.spi.ChordSymbolEditorDialog;
import org.jjazz.undomanager.api.JJazzUndoManager;
import org.jjazz.undomanager.api.JJazzUndoManagerFinder;
import org.jjazz.util.api.ResUtil;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

/**
 * Edit the selected item or bar.
 * <p>
 * Use the following services if available in the global lookup: <br>
 * - ChordSymbolEditorDialog <br>
 * - SectionEditorDialog <br>
 * otherwise use default bar editor for edit operations.
 */
@ActionID(category = "JJazz", id = "org.jjazz.ui.cl_editor.actions.edit")
@ActionRegistration(displayName = "not_used", lazy = false)
@ActionReferences(
        {
            @ActionReference(path = "Actions/Section", position = 100),
            @ActionReference(path = "Actions/ChordSymbol", position = 100),
            @ActionReference(path = "Actions/Bar", position = 100),
        })
public class Edit extends AbstractAction implements ContextAwareAction, CL_ContextActionListener
{

    private Lookup context;
    private CL_ContextActionSupport cap;
    private final String undoText = ResUtil.getString(getClass(), "CTL_Edit");
    private static final Logger LOGGER = Logger.getLogger(Edit.class.getSimpleName());

    public Edit()
    {
        this(Utilities.actionsGlobalContext());
    }

    private Edit(Lookup context)
    {
        this.context = context;
        cap = CL_ContextActionSupport.getInstance(this.context);
        cap.addListener(this);
        putValue(NAME, undoText);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ENTER"));
        selectionChange(cap.getSelection());
    }

    @Override
    public Action createContextAwareInstance(Lookup context)
    {
        return new Edit(context);
    }

    /**
     * @param e If action triggered by a key press, e.getActionCommand() provide the key pressed.
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        CL_SelectionUtilities selection = cap.getSelection();
        final ChordLeadSheet cls = selection.getChordLeadSheet();
        final CL_Editor editor = CL_EditorTopComponent.getActive().getCL_Editor();
        char key = (char) 0;
        LOGGER.fine("e=" + e);   //NOI18N

        // Is it a chord note ?        
        if (e != null && e.getActionCommand().length() == 1)
        {
            char c = e.getActionCommand().toUpperCase().charAt(0);
            if (c >= 'A' && c <= 'G')
            {
                key = c;
            }
        }


        if (selection.isItemSelected())
        {
            ChordLeadSheetItem<?> item = selection.getSelectedItems().get(0);
            int barIndex = item.getPosition().getBar();
            if (item instanceof CLI_ChordSymbol)
            {
                CLI_ChordSymbol csItem = (CLI_ChordSymbol) item;
                ChordSymbolEditorDialog dialog = ChordSymbolEditorDialog.getDefault();
                if (dialog != null)
                {
                    // Use specific editor if service is provided
                    editCSWithDialog(dialog, csItem, key, cls, undoText);
                } else
                {
                    // Otherwise use the standard Bar dialog
                    editBarWithDialog(editor, barIndex, new Preset(Preset.Type.ChordSymbolEdit, item, key), cls, undoText);
                }
            } else if (item instanceof CLI_Section)
            {
                CLI_Section sectionItem = (CLI_Section) item;
                SectionEditorDialog dialog = SectionEditorDialog.getDefault();
                if (dialog != null)
                {
                    // Use specific editor if service is provided               
                    editSectionWithDialog(dialog, sectionItem, key, cls, undoText);
                } else
                {
                    // Otherwise use the standard Bar dialog
                    editBarWithDialog(editor, barIndex, new Preset(Preset.Type.SectionNameEdit, item, (char) 0), cls, undoText);
                }
            }
        } else
        {
            assert selection.isBarSelectedWithinCls() == true : "selection=" + selection;   //NOI18N
            int modelBarIndex = selection.getMinBarIndexWithinCls();
            editBarWithDialog(editor, modelBarIndex, new Preset(Preset.Type.BarEdit, null, key), cls, undoText);
        }
    }

    @Override
    public void selectionChange(CL_SelectionUtilities selection)
    {
        boolean b;
        if (selection.isItemSelected())
        {
            b = selection.getSelectedItems().size() == 1;
        } else
        {
            b = selection.getSelectedBarIndexesWithinCls().size() == 1;
        }
        LOGGER.log(Level.FINE, "selectionChange() b=" + b);   //NOI18N
        setEnabled(b);
    }

    @Override
    public void sizeChanged(int oldSize, int newSize)
    {
        selectionChange(cap.getSelection());
    }

    static protected void editSectionWithDialog(final SectionEditorDialog dialog, final CLI_Section sectionItem, final char key, final ChordLeadSheet cls, String undoText)
    {
        // Use specific editor if service is provided
        Runnable run = new Runnable()
        {
            @Override
            public void run()
            {
                dialog.preset(sectionItem, key);
                dialog.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
                dialog.setVisible(true);
                if (dialog.exitedOk())
                {
                    Section newSection = dialog.getNewData();
                    assert newSection != null;   //NOI18N
                    JJazzUndoManager um = JJazzUndoManagerFinder.getDefault().get(cls);
                    try
                    {
                        cls.setSectionName(sectionItem, newSection.getName());
                        cls.setSectionTimeSignature(sectionItem, newSection.getTimeSignature());
                    } catch (UnsupportedEditException ex)
                    {

                        String msg = ResUtil.getString(getClass(), "ERR_ChangeSection", sectionItem.getData());
                        msg += "\n" + ex.getLocalizedMessage();
                        um.handleUnsupportedEditException(undoText, msg);
                    }
                }
            }
        };
        // IMPORTANT: Dialog must be shown using invokeLater(), otherwise we have the problem of random double chars 
        // when action is triggered by a key (InputMap/ActionMap) and key is used in the dialog.      
        // See complete explanation in my question on stackoverflow:
        // https://stackoverflow.com/questions/53073707/my-jdialog-sometimes-receives-a-redundant-keystroke-from-the-calling-app-code      
        SwingUtilities.invokeLater(run);
    }

    static protected void editCSWithDialog(final ChordSymbolEditorDialog dialog, final CLI_ChordSymbol csItem, final char key, final ChordLeadSheet cls, String undoText)
    {
        Runnable run = new Runnable()
        {
            @Override
            public void run()
            {
                // Use specific editor if service is provided              
                Position pos = csItem.getPosition();
                dialog.preset("Edit Chord Symbol - " + csItem.getData() + " - bar:" + (pos.getBar() + 1) + " beat:" + pos.getBeatAsUserString(), csItem, key, true);
                dialog.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
                dialog.setVisible(true);
                if (dialog.exitedOk())
                {
                    ExtChordSymbol newCs = dialog.getData();
                    assert newCs != null;   //NOI18N
                    JJazzUndoManager um = JJazzUndoManagerFinder.getDefault().get(cls);
                    um.startCEdit(undoText);
                    cls.changeItem(csItem, newCs);
                    um.endCEdit(undoText);
                }
                dialog.cleanup();
            }
        };
        // IMPORTANT: Dialog must be shown using invokeLater(), otherwise we have the problem of random double chars
        // when action is triggered by a key (InputMap/ActionMap) and key is used in the dialog.      
        // See complete explanation in my question on stackoverflow:
        // https://stackoverflow.com/questions/53073707/my-jdialog-sometimes-receives-a-redundant-keystroke-from-the-calling-app-code      
        SwingUtilities.invokeLater(run);
    }

    static protected void editBarWithDialog(final CL_Editor editor, final int barIndex, final Preset preset, final ChordLeadSheet cls, String undoText)
    {
        Runnable run = new Runnable()
        {
            @Override
            public void run()
            {
                // Prepare dialog
                final CL_BarEditorDialog dialog = CL_BarEditorDialog.getDefault();
                dialog.preset(preset, editor.getModel(), barIndex, editor.getDisplayQuantizationValue(cls.getSection(barIndex)).equals(Quantization.ONE_THIRD_BEAT));
                adjustDialogPosition(dialog, barIndex);
                dialog.setVisible(true);
                LOGGER.fine("editBarWithDialog() right after setVisible(true)");   //NOI18N
                if (!dialog.isExitOk())
                {
                    dialog.cleanup();
                    return;
                }

                JJazzUndoManager um = JJazzUndoManagerFinder.getDefault().get(cls);
                um.startCEdit(undoText);

                // Manage section change
                CLI_Section resultSection = dialog.getSection();
                if (resultSection != null)
                {
                    CLI_Section currentSection = cls.getSection(barIndex);
                    if (currentSection.getPosition().getBar() == barIndex)
                    {
                        // Update existing section
                        cls.setSectionName(currentSection, resultSection.getData().getName());
                        try
                        {
                            // Manage the case where we change initial section, user prompt to apply to whole song
                            SetTimeSignature.changeTimeSignaturePossiblyForWholeSong(cls, resultSection.getData().getTimeSignature(), Arrays.asList(currentSection));
                        } catch (UnsupportedEditException ex)
                        {
                            String msg = ResUtil.getString(getClass(), "ERR_ChangeSection", resultSection.getData());
                            msg += "\n" + ex.getLocalizedMessage();
                            um.handleUnsupportedEditException(undoText, msg);
                            // There are other things to do, restart edit
                            um.startCEdit(undoText);
                        }
                    } else
                    {
                        // Add new section
                        try
                        {
                            cls.addSection(resultSection);
                        } catch (UnsupportedEditException ex)
                        {
                            String msg = ResUtil.getString(getClass(), "ERR_ChangeSection", resultSection.getData());
                            msg += "\n" + ex.getLocalizedMessage();
                            um.handleUnsupportedEditException(undoText, msg);
                            // There are other things to do, restart edit
                            um.startCEdit(undoText);
                        }
                    }
                }

                // Manage added/removed/changed items
                List<ChordLeadSheetItem<?>> resultAddedItems = dialog.getAddedItems();
                resultAddedItems.forEach(item -> cls.addItem(item));

                List<ChordLeadSheetItem<?>> resultRemovedItems = dialog.getRemovedItems();
                resultRemovedItems.forEach(item -> cls.removeItem(item));

                Map<CLI_ChordSymbol, ExtChordSymbol> map = dialog.getUpdatedChordSymbols();
                map.keySet().forEach(cliCs -> cls.changeItem(cliCs, map.get(cliCs)));

                um.endCEdit(undoText);

                // Go to next bar if chords have changed
                boolean chordSymbolChange = !resultAddedItems.isEmpty() || !resultRemovedItems.isEmpty() || !map.isEmpty();
                if (barIndex < cls.getSizeInBars() - 1 && chordSymbolChange)
                {
                    CL_SelectionUtilities selection = new CL_SelectionUtilities(editor.getLookup());
                    selection.unselectAll(editor);
                    editor.setFocusOnBar(barIndex + 1);
                    editor.selectBars(barIndex + 1, barIndex + 1, true);
                }

                dialog.cleanup();
            }
        };

        // IMPORTANT: Dialog must be shown using invokeLater(), otherwise we have the problem of random double chars
        // when action is triggered by a key (InputMap/ActionMap) and key is used in the dialog.      
        // See complete explanation in my question on stackoverflow:
        // https://stackoverflow.com/questions/53073707/my-jdialog-sometimes-receives-a-redundant-keystroke-from-the-calling-app-code
        SwingUtilities.invokeLater(run);
    }

    static private void adjustDialogPosition(JDialog dialog, int barIndex)
    {
        CL_Editor editor = CL_EditorTopComponent.getActive().getCL_Editor();
        Rectangle r = editor.getBarRectangle(barIndex);
        Point p = r.getLocation();
        SwingUtilities.convertPointToScreen(p, editor);
        int x = p.x - ((dialog.getWidth() - r.width) / 2);
        int y = p.y - dialog.getHeight();
        dialog.setLocation(Math.max(x, 0), Math.max(y, 0));
    }
}
