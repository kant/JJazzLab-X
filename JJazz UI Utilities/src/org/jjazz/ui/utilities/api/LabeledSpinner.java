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

import java.awt.Color;
import java.awt.Font;
import java.util.List;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;

/**
 * A WheelSpinner with a label.
 */
public class LabeledSpinner extends javax.swing.JPanel
{

    private String label;

    /**
     * Creates a spinner with an integer spinner.
     */
    public LabeledSpinner()
    {
        initComponents();
    }

    /**
     * An integer spinner between min and max.
     *
     * @param value
     * @param min
     * @param max
     * @param step
     */
    public LabeledSpinner(int value, int min, int max, int step)
    {
        initComponents();
        wheelSpinner1.setModel(new SpinnerNumberModel(value, min, max, step));
    }

    /**
     * A list spinner with the specified values.
     *
     * @param values
     */
    public LabeledSpinner(List<Object> values)
    {
        initComponents();
        wheelSpinner1.setModel(new SpinnerListModel(values));
    }

    public WheelSpinner getSpinner()
    {
        return this.wheelSpinner1;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String text)
    {
        label = text;
        this.lbl_label.setText(label);
    }

    @Override
    public void setFont(Font font)
    {
        super.setFont(font);
        lbl_label.setFont(font);
    }

    @Override
    public void setForeground(Color c)
    {
        super.setForeground(c);
        lbl_label.setForeground(c);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of
     * this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        lbl_label = new javax.swing.JLabel();
        wheelSpinner1 = new org.jjazz.ui.utilities.api.WheelSpinner();

        lbl_label.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        org.openide.awt.Mnemonics.setLocalizedText(lbl_label, "label"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbl_label, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(wheelSpinner1, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbl_label)
                    .addComponent(wheelSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lbl_label;
    private org.jjazz.ui.utilities.api.WheelSpinner wheelSpinner1;
    // End of variables declaration//GEN-END:variables
}
