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
package org.jjazz.rpcustomeditorfactoryimpl;

import static com.google.common.base.Preconditions.checkNotNull;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.TransferHandler;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import org.jjazz.harmony.api.TimeSignature;
import org.jjazz.midi.api.Instrument;
import org.jjazz.phrase.api.Phrase;
import org.jjazz.phrase.api.SizedPhrase;
import org.jjazz.phrasetransform.api.PhraseTransform;
import org.jjazz.phrasetransform.api.PhraseTransformChain;
import org.jjazz.phrasetransform.api.PhraseTransformManager;
import org.jjazz.phrasetransform.api.rps.RP_SYS_PhraseTransform;
import org.jjazz.phrasetransform.api.rps.RP_SYS_PhraseTransformValue;
import org.jjazz.phrasetransform.api.ui.PhraseTransformListCellRenderer;
import org.jjazz.rhythm.api.RhythmVoice;
import org.jjazz.rpcustomeditorfactoryimpl.spi.RealTimeRpEditorComponent;
import org.jjazz.songcontext.api.SongContext;
import org.jjazz.songstructure.api.SongPart;
import org.jjazz.util.api.FloatRange;

/**
 * A RP editor component for RP_SYS_PhraseTransformValue.
 */
public class RP_SYS_PhraseTransformComp extends RealTimeRpEditorComponent<RP_SYS_PhraseTransformValue> implements ListDataListener, ActionListener
{

    private static final DataFlavor PT_DATA_FLAVOR = new DataFlavor(PhraseTransform.class, "Phrase Transformer");
    private RP_SYS_PhraseTransform rp;
    private RP_SYS_PhraseTransformValue lastValue;
    private RP_SYS_PhraseTransformValue editedValue;
    private SongContext songContext;
    private SongPart songPart;
    private List<RhythmVoice> rhythmVoices;
    private final DefaultListModel<PhraseTransform> list_transformChainModel = new DefaultListModel<>();

    /**
     * Creates new form RP_SYS_DrumsTransformerComp
     */
    public RP_SYS_PhraseTransformComp(RP_SYS_PhraseTransform rp)
    {
        checkNotNull(rp);
        this.rp = rp;

        initComponents();

        // Update JLists
        list_transformChain.setModel(list_transformChainModel);
        list_transformChain.setTransferHandler(new TransformChainListTransferHandler());
        list_allTransforms.setTransferHandler(new TransformChainListTransferHandler());

        list_transformChainModel.addListDataListener(this);
        cmb_rhythmVoices.addActionListener(this);

    }

    @Override
    public RP_SYS_PhraseTransform getRhythmParameter()
    {
        return rp;
    }

    @Override
    public void setEnabled(boolean b)
    {
        super.setEnabled(b);
        // TODO IMPLEMENT!
    }


    @Override
    public void preset(RP_SYS_PhraseTransformValue rpValue, SongContext sgContext)
    {
        checkNotNull(sgContext);
        songContext = sgContext;
        songPart = songContext.getSongParts().get(0);
        rhythmVoices = songContext.getMidiMix().getRhythmVoices();
        assert !rhythmVoices.isEmpty();


        // Update the list of available transforms
        List<PhraseTransform> allTransforms = PhraseTransformManager.getDefault().getPhraseTransforms();
        allTransforms.sort(null);
        DefaultListModel dlm = new DefaultListModel();
        dlm.addAll(allTransforms);
        list_allTransforms.setModel(dlm);


        // Update the RhythmVoices combo box and select the first transformed rhythm voice
        cmb_rhythmVoices.removeActionListener(this);
        DefaultComboBoxModel cmb_model = new DefaultComboBoxModel(rhythmVoices.toArray(new RhythmVoice[0]));
        cmb_rhythmVoices.setModel(cmb_model);
        var transformedRvs = rpValue.getChainRhythmVoices();
        RhythmVoice trv = rhythmVoices.stream()
                .filter(rv -> transformedRvs.contains(rv))
                .findAny()
                .orElse(null);
        cmb_rhythmVoices.setSelectedItem(trv != null ? trv : rhythmVoices.get(0));
        cmb_rhythmVoices.addActionListener(this);


        setEditedRpValue(rpValue);


    }

    @Override
    public void setEditedRpValue(RP_SYS_PhraseTransformValue rpValue)
    {
        lastValue = editedValue;
        editedValue = new RP_SYS_PhraseTransformValue(rpValue);

        updateChainUI();
    }

    @Override
    public RP_SYS_PhraseTransformValue getEditedRpValue()
    {
        return editedValue;
    }

    @Override
    public boolean isResizable()
    {
        return true;
    }


    @Override
    public void cleanup()
    {
        // 
    }


    // ===============================================================================
    // ActionListener interface
    // ===============================================================================  
    @Override
    public void actionPerformed(ActionEvent ae)
    {
        if (ae.getSource() == cmb_rhythmVoices)
        {
            updateChainUI();
        }
    }


    // ===============================================================================
    // ListDataListener interface
    // ===============================================================================  
    @Override
    public void intervalAdded(ListDataEvent e)
    {
        list_transformChanged();
    }

    @Override
    public void intervalRemoved(ListDataEvent e)
    {
        list_transformChanged();
    }

    @Override
    public void contentsChanged(ListDataEvent e)
    {
        list_transformChanged();
    }


    // ===============================================================================
    // Private methods
    // ===============================================================================    
    private void list_transformChanged()
    {
        RhythmVoice rv = getCurrentRhythmVoice();
        editedValue.setTransformChain(rv, new PhraseTransformChain(getChainListModelAsList()));
        updateChainUI();
    }

    private List<PhraseTransform> getChainListModelAsList()
    {
        List<PhraseTransform> res = new ArrayList<>();
        for (int i = 0; i < list_transformChainModel.size(); i++)
        {
            res.add(list_transformChainModel.get(i));
        }
        return res;
    }

    private SizedPhrase getInPhrase(RhythmVoice rv)
    {
        // For TEST
        int channel = songContext.getMidiMix().getChannel(rv);
        SizedPhrase res = new SizedPhrase(channel, new FloatRange(0, 8), TimeSignature.FOUR_FOUR);
        res.add(Phrase.getRandomPhrase(channel, 2, 4));
        return res;
    }

    /**
     * Chain has changed (because added/removed PhraseTransform, or because RhythmVoice has changed), we need to update
     * chain-related UI
     */
    private void updateChainUI()
    {
        RhythmVoice rv = getCurrentRhythmVoice();
        Instrument ins = songContext.getMidiMix().getInstrumentMixFromKey(rv).getInstrument();
        var transformChain = editedValue.getTransformChain(rv);


        // Update the list transform chain, don't generate events because it's not user-triggered
        list_transformChainModel.removeListDataListener(this);
        list_transformChainModel.clear();
        if (transformChain != null)
        {
            list_transformChainModel.addAll(transformChain);
        }
        list_transformChainModel.addListDataListener(this);


        // Update the birdviews
        SizedPhrase inPhrase = getInPhrase(rv);
        birdview_inPhrase.setModel(inPhrase, inPhrase.getTimeSignature(), inPhrase.getBeatRange());

        SizedPhrase outPhrase = transformChain == null ? inPhrase : transformChain.transform(inPhrase, ins);
        birdview_outPhrase.setModel(outPhrase, outPhrase.getTimeSignature(), outPhrase.getBeatRange());

    }

    private RhythmVoice getCurrentRhythmVoice()
    {
        RhythmVoice rv = (RhythmVoice) cmb_rhythmVoices.getSelectedItem();
        return rv;
    }


    // ===============================================================================
    // Inner classes
    // ===============================================================================    
    private class RhythmVoiceRenderer extends BasicComboBoxRenderer
    {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            RhythmVoice rv = (RhythmVoice) value;

            if (value != null)
            {
                String text = rv.getName();
                if (songContext != null)
                {
                    int channel = songContext.getMidiMix().getChannel(rv);
                    var ins = songContext.getMidiMix().getInstrumentMixFromKey(rv).getInstrument();
                    text = "[" + (channel + 1) + "] " + ins.getPatchName() + " / " + rv.getName();
                }

                label.setText(text);
            }

            return label;
        }
    }

    /**
     * We don't directly use the PhraseTransform as data, because there might be several identical PhraseTransforms in a chain, so
     * using index and list provides a unique instance to be transferred.
     */
    private class PtData
    {

        private final JList<PhraseTransform> srcList;
        private final int srcPtIndex;

        public PtData(JList<PhraseTransform> srcList, int srcPtIndex)
        {
            this.srcPtIndex = srcPtIndex;
            this.srcList = srcList;
        }
    }

    /**
     * Transferable for a single PhraseTransform instance.
     */
    private class PtTransferable implements Transferable
    {

        private final PtData data;

        public PtTransferable(PtData data)
        {
            this.data = data;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors()
        {
            return new DataFlavor[]
            {
                PT_DATA_FLAVOR
            };
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor fl)
        {
            return fl.equals(PT_DATA_FLAVOR);
        }

        @Override
        public Object getTransferData(DataFlavor fl) throws UnsupportedFlavorException
        {
            if (fl.equals(PT_DATA_FLAVOR))
            {
                return data;
            } else
            {
                throw new UnsupportedFlavorException(fl);
            }
        }
    }

    /**
     * Supported use cases:<br>
     * - drag and drop from list_allTransforms to list_transformChain : add<br>
     * - drag and drop from list_transformChain to list_allTransforms : remove<br>
     * - drag and drop from list_transformChain to list_transformChain : reorder<br>
     */
    private class TransformChainListTransferHandler extends TransferHandler
    {

        @Override
        public boolean canImport(TransferHandler.TransferSupport info)
        {
            return info.isDataFlavorSupported(PT_DATA_FLAVOR);
        }

        /**
         * Create the transferable from the selected PhraseTransform
         */
        @Override
        protected Transferable createTransferable(JComponent jc)
        {
            JList list = (JList) jc;
            return new PtTransferable(new PtData(list, list.getSelectedIndex()));
        }

        /**
         * Only move actions.
         */
        @Override
        public int getSourceActions(JComponent c)
        {
            return TransferHandler.MOVE;
        }

        /**
         * Different operations depending of source and target list.
         */
        @Override
        public boolean importData(TransferHandler.TransferSupport info)
        {
            if (!info.isDrop())
            {
                return false;
            }


            // Retrieve the data
            Transferable t = info.getTransferable();
            PtData ptData;
            try
            {
                ptData = (PtData) t.getTransferData(PT_DATA_FLAVOR);
            } catch (UnsupportedFlavorException | IOException ex)
            {
                return false;
            }
            JList srcList = ptData.srcList;
            int srcIndex = ptData.srcPtIndex;
            PhraseTransform srcPt = (PhraseTransform) srcList.getModel().getElementAt(srcIndex);


            JList destList = (JList) info.getComponent();
            if (srcList == list_allTransforms && destList == list_allTransforms)
            {
                // No import, do nothing
                return false;

            } else if (srcList == list_transformChain && destList == list_allTransforms)
            {
                // Import but do nothing, exportDone will remove the element from list_transformChain
                return true;

            } else  //  srcList == list_transformChain && destList == list_transformChain
            {
                // Update transform chain
                DefaultListModel listModel = (DefaultListModel) list_transformChain.getModel();
                JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
                if (dl.isInsert())
                {
                    // Add
                    listModel.add(dl.getIndex(), srcPt);
                } else
                {
                    // Replace existing
                    listModel.set(dl.getIndex(), srcPt);
                }
                return true;

            }
        }

        /**
         * Remove the items moved from list_tranformChain.
         */
        @Override
        protected void exportDone(JComponent jc, Transferable data, int action)
        {
            JList srcList = (JList) jc;
            if (srcList == list_allTransforms)
            {
                return;
            }
            assert srcList == list_transformChain;


            if (action == TransferHandler.MOVE)
            {
                PtData ptData;
                try
                {
                    ptData = (PtData) data.getTransferData(PT_DATA_FLAVOR);
                } catch (UnsupportedFlavorException | IOException ex)
                {
                    return;
                }
                DefaultListModel listModel = (DefaultListModel) srcList.getModel();
                listModel.remove(ptData.srcPtIndex);
            }

        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of
     * this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        list_allTransforms = new javax.swing.JList<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        list_transformChain = new javax.swing.JList<>();
        jLabel2 = new javax.swing.JLabel();
        btn_clearChain = new javax.swing.JButton();
        btn_ptSettings = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        lbl_rightArrow = new javax.swing.JLabel();
        birdview_outPhrase = new org.jjazz.phrase.api.ui.PhraseBirdView();
        birdview_inPhrase = new org.jjazz.phrase.api.ui.PhraseBirdView();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        helpTextArea1 = new org.jjazz.ui.utilities.api.HelpTextArea();
        cmb_rhythmVoices = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(RP_SYS_PhraseTransformComp.class, "RP_SYS_PhraseTransformComp.jLabel1.text")); // NOI18N

        list_allTransforms.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        list_allTransforms.setCellRenderer(new PhraseTransformListCellRenderer());
        list_allTransforms.setDragEnabled(true);
        list_allTransforms.setDropMode(javax.swing.DropMode.ON);
        jScrollPane1.setViewportView(list_allTransforms);

        list_transformChain.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        list_transformChain.setToolTipText(org.openide.util.NbBundle.getMessage(RP_SYS_PhraseTransformComp.class, "RP_SYS_PhraseTransformComp.list_transformChain.toolTipText")); // NOI18N
        list_transformChain.setCellRenderer(new PhraseTransformListCellRenderer());
        list_transformChain.setDragEnabled(true);
        list_transformChain.setDropMode(javax.swing.DropMode.ON_OR_INSERT);
        jScrollPane2.setViewportView(list_transformChain);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(RP_SYS_PhraseTransformComp.class, "RP_SYS_PhraseTransformComp.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btn_clearChain, org.openide.util.NbBundle.getMessage(RP_SYS_PhraseTransformComp.class, "RP_SYS_PhraseTransformComp.btn_clearChain.text")); // NOI18N
        btn_clearChain.setToolTipText(org.openide.util.NbBundle.getMessage(RP_SYS_PhraseTransformComp.class, "RP_SYS_PhraseTransformComp.btn_clearChain.toolTipText")); // NOI18N
        btn_clearChain.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btn_clearChainActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btn_ptSettings, org.openide.util.NbBundle.getMessage(RP_SYS_PhraseTransformComp.class, "RP_SYS_PhraseTransformComp.btn_ptSettings.text")); // NOI18N
        btn_ptSettings.setToolTipText(org.openide.util.NbBundle.getMessage(RP_SYS_PhraseTransformComp.class, "RP_SYS_PhraseTransformComp.btn_ptSettings.toolTipText")); // NOI18N
        btn_ptSettings.setEnabled(false);

        lbl_rightArrow.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jjazz/rpcustomeditorfactoryimpl/resources/RightArrow.png"))); // NOI18N
        lbl_rightArrow.setToolTipText(org.openide.util.NbBundle.getMessage(RP_SYS_PhraseTransformComp.class, "RP_SYS_PhraseTransformComp.lbl_rightArrow.toolTipText")); // NOI18N
        jPanel1.add(lbl_rightArrow);

        birdview_outPhrase.setBorder(birdview_inPhrase.getBorder());
        birdview_outPhrase.setForeground(new java.awt.Color(255, 0, 51));

        javax.swing.GroupLayout birdview_outPhraseLayout = new javax.swing.GroupLayout(birdview_outPhrase);
        birdview_outPhrase.setLayout(birdview_outPhraseLayout);
        birdview_outPhraseLayout.setHorizontalGroup(
            birdview_outPhraseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        birdview_outPhraseLayout.setVerticalGroup(
            birdview_outPhraseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 70, Short.MAX_VALUE)
        );

        birdview_inPhrase.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        birdview_inPhrase.setForeground(new java.awt.Color(102, 204, 255));

        javax.swing.GroupLayout birdview_inPhraseLayout = new javax.swing.GroupLayout(birdview_inPhrase);
        birdview_inPhrase.setLayout(birdview_inPhraseLayout);
        birdview_inPhraseLayout.setHorizontalGroup(
            birdview_inPhraseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        birdview_inPhraseLayout.setVerticalGroup(
            birdview_inPhraseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 70, Short.MAX_VALUE)
        );

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jjazz/rpcustomeditorfactoryimpl/resources/arrow-down-16x16.gif"))); // NOI18N
        jPanel2.add(jLabel3);

        jScrollPane3.setBorder(null);

        helpTextArea1.setColumns(20);
        helpTextArea1.setRows(5);
        helpTextArea1.setText(org.openide.util.NbBundle.getMessage(RP_SYS_PhraseTransformComp.class, "RP_SYS_PhraseTransformComp.helpTextArea1.text")); // NOI18N
        jScrollPane3.setViewportView(helpTextArea1);

        cmb_rhythmVoices.setRenderer(new RhythmVoiceRenderer());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(RP_SYS_PhraseTransformComp.class, "RP_SYS_PhraseTransformComp.jLabel4.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(birdview_inPhrase, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(birdview_outPhrase, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(btn_ptSettings)
                                            .addComponent(btn_clearChain))
                                        .addGap(0, 0, Short.MAX_VALUE))))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmb_rhythmVoices, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(319, 319, 319)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btn_clearChain, btn_ptSettings});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmb_rhythmVoices, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btn_clearChain))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btn_ptSettings)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)))
                .addGap(18, 18, 18)
                .addComponent(birdview_inPhrase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(birdview_outPhrase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {birdview_inPhrase, birdview_outPhrase});

    }// </editor-fold>//GEN-END:initComponents

    private void btn_clearChainActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btn_clearChainActionPerformed
    {//GEN-HEADEREND:event_btn_clearChainActionPerformed
        editedValue.setTransformChain((RhythmVoice) cmb_rhythmVoices.getSelectedItem(), null);
        updateChainUI();
    }//GEN-LAST:event_btn_clearChainActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.jjazz.phrase.api.ui.PhraseBirdView birdview_inPhrase;
    private org.jjazz.phrase.api.ui.PhraseBirdView birdview_outPhrase;
    private javax.swing.JButton btn_clearChain;
    private javax.swing.JButton btn_ptSettings;
    private javax.swing.JComboBox<RhythmVoice> cmb_rhythmVoices;
    private org.jjazz.ui.utilities.api.HelpTextArea helpTextArea1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lbl_rightArrow;
    private javax.swing.JList<PhraseTransform> list_allTransforms;
    private javax.swing.JList<PhraseTransform> list_transformChain;
    // End of variables declaration//GEN-END:variables


}
