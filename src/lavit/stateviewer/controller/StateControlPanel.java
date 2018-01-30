/*
 *   Copyright (c) 2008, Ueda Laboratory LMNtal Group <lmntal@ueda.info.waseda.ac.jp>
 *   All rights reserved.
 *
 *   Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are
 *   met:
 *
 *    1. Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *
 *    3. Neither the name of the Ueda Laboratory LMNtal Group nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *   A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *   OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *   OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package lavit.stateviewer.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lavit.*;
import lavit.stateviewer.StateGraphPanel;
import lavit.stateviewer.StatePanel;
import lavit.util.CommonFontUser;
import lavit.util.FixFlowLayout;

public class StateControlPanel extends JPanel implements ChangeListener,MouseListener {

	private StatePanel statePanel;

    public StateControlTab stateControllerTab;
    private JSlider zoomSlider = new JSlider(1,399);
    private StateUnderInfoPanel infoPanel;

	public StateControlPanel(StatePanel statePanel){

		this.statePanel = statePanel;
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

		stateControllerTab = new StateControlTab(statePanel);
		stateControllerTab.setVisible(Env.is("SV_CONTROLLER"));
		//stateControllerTab.addMouseListener(this);
		add(stateControllerTab);

		zoomSlider.addChangeListener(this);
		zoomSlider.setVisible(Env.is("SV_ZOOMSLIDER"));
		add(zoomSlider);

		infoPanel = new StateUnderInfoPanel(statePanel);
		infoPanel.addMouseListener(this);
		infoPanel.setVisible(Env.is("SV_INFO"));
		add(infoPanel);

		setEnabled(false);

	}

	public void setEnabled(boolean enabled){
		super.setEnabled(enabled);
		stateControllerTab.setEnabled(enabled);
		zoomSlider.setEnabled(enabled);
	}

	public void setSliderPos(double z){
		int pos = (int)(Math.sqrt(z*10000)*2-1);
		if(pos<1){ pos=1; }else if(pos>399){ pos=399; }
		zoomSlider.removeChangeListener(this);
		zoomSlider.setValue(pos);
		zoomSlider.addChangeListener(this);
	}

	public void stateChanged(ChangeEvent e) {
		double z = (zoomSlider.getValue()+1)/2.0;
		statePanel.stateGraphPanel.setInnerZoom(z*z/10000.0);
		statePanel.stateGraphPanel.update();
	}

	public void updateInfo(){
		infoPanel.updateInfo();
	}

	public void toggleControllerVisible(){
		stateControllerTab.setVisible(!stateControllerTab.isVisible());
		Env.set("SV_CONTROLLER", stateControllerTab.isVisible());
	}

	public void toggleZoomSliderVisible(){
		zoomSlider.setVisible(!zoomSlider.isVisible());
		Env.set("SV_ZOOMSLIDER",zoomSlider.isVisible());
	}

	public void toggleInfoVisible(){
		infoPanel.setVisible(!infoPanel.isVisible());
		Env.set("SV_INFO",infoPanel.isVisible());
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if(e.getClickCount()>0&&e.getClickCount()%2==0){
			toggleControllerVisible();
			javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run() {
				statePanel.stateGraphPanel.fitCentering();
			}});
		}
	}

	public void mouseReleased(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}

}
