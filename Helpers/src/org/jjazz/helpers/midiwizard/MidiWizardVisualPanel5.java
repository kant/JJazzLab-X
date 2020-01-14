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
package org.jjazz.helpers.midiwizard;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.JPanel;
import org.jjazz.midi.synths.GM1Bank;
import org.jjazz.midi.synths.StdSynth;
import org.jjazz.midi.Instrument;
import org.jjazz.midi.JJazzMidiSystem;
import org.jjazz.midi.synths.Family;
import org.jjazz.musiccontrol.MusicController;
import org.jjazz.rhythmmusicgeneration.MusicGenerationException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

public final class MidiWizardVisualPanel5 extends JPanel
{

    MidiDevice midiDeviceOut;

    /**
     * Creates new form StartupWizardVisualPanel4
     */
    public MidiWizardVisualPanel5()
    {
        initComponents();
    }

    public void setMidiDeviceOut(MidiDevice md)
    {
        midiDeviceOut = md;
        lbl_outDevice.setText("Midi Out: " + JJazzMidiSystem.getInstance().getDeviceFriendlyName(md));
        lbl_outDevice.setToolTipText(md.getDeviceInfo().getDescription());
    }

    @Override
    public String getName()
    {
        return "GM compatibility";
    }

    public void setDrumsOnOtherChannelOK(boolean b)
    {
        if (b)
        {
            rbtn_yes.setSelected(true);
        } else
        {
            this.rbtn_no.setSelected(true);
        }
    }

    public boolean isDrumsOnOtherChannelOK()
    {
        return rbtn_yes.isSelected();
    }

    /**
     *
     * @param channel
     * @param instrument Can be null
     */
    private void sendTestNotes(int channel, Instrument instrument, int transpose)
    {
        assert midiDeviceOut != null;

        final JJazzMidiSystem jms = JJazzMidiSystem.getInstance();
        final MidiDevice saveDeviceOut = jms.getDefaultOutDevice();
        try
        {
            jms.setDefaultOutDevice(midiDeviceOut);
        } catch (MidiUnavailableException ex)
        {
            NotifyDescriptor d = new NotifyDescriptor.Message(ex.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return;
        }

        // Send the instrument patch messages
        if (instrument != null)
        {
            jms.sendMidiMessagesOnJJazzMidiOut(instrument.getMidiMessages(channel));
        }

        btn_test1.setEnabled(false);
        btn_test2.setEnabled(false);
        btn_testDrums.setEnabled(false);
        btn_testDrumsOtherChannel.setEnabled(false);

        Runnable endAction = new Runnable()
        {
            @Override
            public void run()
            {
                // Called when sequence is stopped
                btn_test1.setEnabled(true);
                btn_test2.setEnabled(true);
                btn_testDrums.setEnabled(true);
                btn_testDrumsOtherChannel.setEnabled(true);
                try
                {
                    jms.setDefaultOutDevice(saveDeviceOut);
                } catch (MidiUnavailableException ex)
                {
                    NotifyDescriptor d = new NotifyDescriptor.Message(ex.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(d);
                }
            }
        };

        MusicController mc = MusicController.getInstance();
        try
        {
            mc.playTestNotes(channel, -1, transpose, endAction);
        } catch (MusicGenerationException ex)
        {
            NotifyDescriptor d = new NotifyDescriptor.Message(ex.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of
     * this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        wizardTextArea1 = new org.jjazz.helpers.midiwizard.WizardTextArea();
        btn_test1 = new javax.swing.JButton();
        btn_test2 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        helpTextArea1 = new org.jjazz.ui.utilities.HelpTextArea();
        lbl_outDevice = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        rbtn_yes = new javax.swing.JRadioButton();
        btn_testDrumsOtherChannel = new javax.swing.JButton();
        rbtn_no = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        btn_testDrums = new javax.swing.JButton();

        jScrollPane1.setBorder(null);

        wizardTextArea1.setEditable(false);
        wizardTextArea1.setColumns(20);
        wizardTextArea1.setRows(5);
        wizardTextArea1.setText(org.openide.util.NbBundle.getMessage(MidiWizardVisualPanel5.class, "MidiWizardVisualPanel5.wizardTextArea1.text")); // NOI18N
        jScrollPane1.setViewportView(wizardTextArea1);

        org.openide.awt.Mnemonics.setLocalizedText(btn_test1, org.openide.util.NbBundle.getMessage(MidiWizardVisualPanel5.class, "MidiWizardVisualPanel5.btn_test1.text")); // NOI18N
        btn_test1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_test1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btn_test2, org.openide.util.NbBundle.getMessage(MidiWizardVisualPanel5.class, "MidiWizardVisualPanel5.btn_test2.text")); // NOI18N
        btn_test2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_test2ActionPerformed(evt);
            }
        });

        jScrollPane2.setBorder(null);

        helpTextArea1.setColumns(20);
        helpTextArea1.setRows(5);
        helpTextArea1.setText(org.openide.util.NbBundle.getMessage(MidiWizardVisualPanel5.class, "MidiWizardVisualPanel5.helpTextArea1.text")); // NOI18N
        jScrollPane2.setViewportView(helpTextArea1);

        org.openide.awt.Mnemonics.setLocalizedText(lbl_outDevice, org.openide.util.NbBundle.getMessage(MidiWizardVisualPanel5.class, "MidiWizardVisualPanel5.lbl_outDevice.text")); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(MidiWizardVisualPanel5.class, "MidiWizardVisualPanel5.jPanel1.border.title"))); // NOI18N

        buttonGroup1.add(rbtn_yes);
        org.openide.awt.Mnemonics.setLocalizedText(rbtn_yes, org.openide.util.NbBundle.getMessage(MidiWizardVisualPanel5.class, "MidiWizardVisualPanel5.rbtn_yes.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btn_testDrumsOtherChannel, org.openide.util.NbBundle.getMessage(MidiWizardVisualPanel5.class, "MidiWizardVisualPanel5.btn_testDrumsOtherChannel.text")); // NOI18N
        btn_testDrumsOtherChannel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_testDrumsOtherChannelActionPerformed(evt);
            }
        });

        buttonGroup1.add(rbtn_no);
        org.openide.awt.Mnemonics.setLocalizedText(rbtn_no, org.openide.util.NbBundle.getMessage(MidiWizardVisualPanel5.class, "MidiWizardVisualPanel5.rbtn_no.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(MidiWizardVisualPanel5.class, "MidiWizardVisualPanel5.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btn_testDrumsOtherChannel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rbtn_yes)
                .addGap(18, 18, 18)
                .addComponent(rbtn_no)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_testDrumsOtherChannel)
                    .addComponent(rbtn_yes)
                    .addComponent(rbtn_no)
                    .addComponent(jLabel1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(btn_testDrums, org.openide.util.NbBundle.getMessage(MidiWizardVisualPanel5.class, "MidiWizardVisualPanel5.btn_testDrums.text")); // NOI18N
        btn_testDrums.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_testDrumsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addComponent(jScrollPane1)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lbl_outDevice)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btn_test1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btn_test2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btn_testDrums)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbl_outDevice)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_test1)
                    .addComponent(btn_test2)
                    .addComponent(btn_testDrums))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btn_test1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btn_test1ActionPerformed
    {//GEN-HEADEREND:event_btn_test1ActionPerformed
        sendTestNotes(2, StdSynth.getGM1Bank().getDefaultInstrument(Family.Organ), 0);
    }//GEN-LAST:event_btn_test1ActionPerformed

    private void btn_testDrumsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btn_testDrumsActionPerformed
    {//GEN-HEADEREND:event_btn_testDrumsActionPerformed
        sendTestNotes(9, null, -12);
    }//GEN-LAST:event_btn_testDrumsActionPerformed

    private void btn_test2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btn_test2ActionPerformed
    {//GEN-HEADEREND:event_btn_test2ActionPerformed
        sendTestNotes(11, StdSynth.getGM1Bank().getDefaultInstrument(Family.Reed), -12);
    }//GEN-LAST:event_btn_test2ActionPerformed

    private void btn_testDrumsOtherChannelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btn_testDrumsOtherChannelActionPerformed
    {//GEN-HEADEREND:event_btn_testDrumsOtherChannelActionPerformed
        sendTestNotes(2, StdSynth.getGM2Bank().getDefaultDrumsInstrument(), -12);
    }//GEN-LAST:event_btn_testDrumsOtherChannelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_test1;
    private javax.swing.JButton btn_test2;
    private javax.swing.JButton btn_testDrums;
    private javax.swing.JButton btn_testDrumsOtherChannel;
    private javax.swing.ButtonGroup buttonGroup1;
    private org.jjazz.ui.utilities.HelpTextArea helpTextArea1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lbl_outDevice;
    private javax.swing.JRadioButton rbtn_no;
    private javax.swing.JRadioButton rbtn_yes;
    private org.jjazz.helpers.midiwizard.WizardTextArea wizardTextArea1;
    // End of variables declaration//GEN-END:variables
}
